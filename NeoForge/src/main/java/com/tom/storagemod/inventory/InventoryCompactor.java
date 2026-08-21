package com.tom.storagemod.inventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Incrementally compacts matching stacks in a network without ever using an
 * empty destination slot. All handler access happens on the server thread.
 */
public final class InventoryCompactor {
	private InventoryCompactor() {
	}

	public static Session start(IInventoryAccess access, Consumer<ItemStack> recoverRemainder) {
		List<InventoryHandle> handlers = new ArrayList<>();
		LongSupplier structureRevision = () -> 0L;
		long initialStructureRevision = 0L;
		if (access instanceof MultiInventoryAccess multi) {
			structureRevision = multi::getStructureRevision;
			initialStructureRevision = multi.getStructureRevision();
			for (IInventoryAccess connected : multi.getConnected()) {
				InventoryHandle handler = new AccessHandle(connected);
				int slots = safeSlotCount(handler);
				if (slots > 0)handlers.add(handler);
			}
		} else {
			InventoryHandle handler = new AccessHandle(access);
			if (safeSlotCount(handler) > 0)handlers.add(handler);
		}
		return handlers.isEmpty() ? null : new Session(handlers, recoverRemainder,
				structureRevision, initialStructureRevision);
	}

	static Session startForTesting(List<IItemHandler> handlers, Consumer<ItemStack> recoverRemainder) {
		return startForTesting(handlers, recoverRemainder, () -> 0L);
	}

	static Session startForTesting(List<IItemHandler> handlers, Consumer<ItemStack> recoverRemainder,
			LongSupplier structureRevision) {
		List<InventoryHandle> handles = handlers.stream().map(FixedHandle::new).map(InventoryHandle.class::cast).toList();
		return new Session(handles, recoverRemainder, structureRevision, structureRevision.getAsLong());
	}

	static Session startForTestingAccesses(List<IInventoryAccess> accesses, Consumer<ItemStack> recoverRemainder) {
		List<InventoryHandle> handles = accesses.stream().map(AccessHandle::new).map(InventoryHandle.class::cast).toList();
		return new Session(handles, recoverRemainder, () -> 0L, 0L);
	}

	private static int safeSlotCount(InventoryHandle handle) {
		try {
			return Math.max(0, handle.resolve().getSlots());
		} catch (RuntimeException ignored) {
			return 0;
		}
	}

	public static final class Session {
		private final List<HandlerSlots> handlers;
		private final Consumer<ItemStack> recoverRemainder;
		private final LongSupplier structureRevision;
		private final long initialStructureRevision;
		private final Map<StoredItemStack, List<SlotRef>> groups = new LinkedHashMap<>();
		private int handlerIndex;
		private int slotIndex;
		private int destinationIndex;
		private int sourceIndex = -1;
		private Iterator<List<SlotRef>> groupIterator;
		private List<SlotRef> currentGroup;
		private boolean scanning = true;
		private boolean finished;
		private long movedItems;

		private Session(List<InventoryHandle> handlers, Consumer<ItemStack> recoverRemainder,
				LongSupplier structureRevision, long initialStructureRevision) {
			this.handlers = handlers.stream()
					.map(handler -> new HandlerSlots(handler, safeSlotCount(handler)))
					.filter(handler -> handler.slots() > 0)
					.toList();
			this.recoverRemainder = recoverRemainder;
			this.structureRevision = structureRevision;
			this.initialStructureRevision = initialStructureRevision;
			finished = this.handlers.isEmpty();
		}

		public TickResult tick(int maxSlotChecks, int maxMoves) {
			if (finished)return new TickResult(true, 0, 0);
			long revision = structureRevision.getAsLong();
			if (revision != initialStructureRevision) {
				invalidate();
				return new TickResult(true, 0, 0);
			}
			int checksLimit = Math.max(2, maxSlotChecks);
			int movesLimit = Math.max(1, maxMoves);
			int checks = 0;
			int moves = 0;

			while (!finished && checks < checksLimit && moves < movesLimit) {
				if (scanning) {
					checks += scanNextSlot();
					continue;
				}

				if (currentGroup == null) {
					if (!selectNextGroup()) {
						groups.clear();
						finished = true;
						break;
					}
					checks++;
					continue;
				}
				if (destinationIndex >= sourceIndex) {
					advanceGroup();
					continue;
				}
				if (checks + 2 > checksLimit)break;

				SlotRef destination = currentGroup.get(destinationIndex);
				SlotRef source = currentGroup.get(sourceIndex);
				if (!destination.handle().isValid() || !source.handle().isValid()) {
					invalidate();
					break;
				}
				TransferAttempt attempt = transfer(destination, source);
				checks += 2;
				if (attempt.moved() > 0) {
					moves++;
					movedItems += attempt.moved();
				}
				if (attempt.destinationDone())destinationIndex++;
				if (attempt.sourceDone())sourceIndex--;
				if (!attempt.destinationDone() && !attempt.sourceDone() && attempt.moved() == 0) {
					// A handler refused an otherwise compatible transfer. Other sources
					// cannot make this destination accept the same stack.
					destinationIndex++;
				}
			}
			return new TickResult(finished, checks, moves);
		}

		private int scanNextSlot() {
			while (handlerIndex < handlers.size()) {
				HandlerSlots handler = handlers.get(handlerIndex);
				if (!handler.handle().isValid()) {
					invalidate();
					return 0;
				}
				if (slotIndex >= handler.slots()) {
					handlerIndex++;
					slotIndex = 0;
					continue;
				}

				int currentSlot = slotIndex++;
				ItemStack stack = safeGetStack(handler.handle().resolve(), currentSlot);
				if (!stack.isEmpty() && stack.getMaxStackSize() > 1) {
					StoredItemStack key = new StoredItemStack(stack);
					groups.computeIfAbsent(key, ignored -> new ArrayList<>())
							.add(new SlotRef(handler.handle(), currentSlot, key));
				}
				return 1;
			}

			groupIterator = groups.values().iterator();
			scanning = false;
			return 0;
		}

		private boolean selectNextGroup() {
			if (!groupIterator.hasNext())return false;
			List<SlotRef> group = groupIterator.next();
			if (group.size() > 1) {
				currentGroup = group;
				destinationIndex = 0;
				sourceIndex = group.size() - 1;
			}
			return true;
		}

		private void advanceGroup() {
			currentGroup = null;
			destinationIndex = 0;
			sourceIndex = -1;
		}

		private void invalidate() {
			groups.clear();
			currentGroup = null;
			finished = true;
		}

		private TransferAttempt transfer(SlotRef destination, SlotRef source) {
			IItemHandler destinationHandler = destination.handle().resolve();
			IItemHandler sourceHandler = source.handle().resolve();
			ItemStack destinationStack = safeGetStack(destinationHandler, destination.slot()).copy();
			if (destinationStack.isEmpty()
					|| !ItemStack.isSameItemSameComponents(destination.key().getStack(), destinationStack)) {
				return TransferAttempt.DESTINATION_DONE;
			}

			ItemStack sourceStack = safeGetStack(sourceHandler, source.slot()).copy();
			if (sourceStack.isEmpty()
					|| !ItemStack.isSameItemSameComponents(source.key().getStack(), sourceStack)) {
				return TransferAttempt.SOURCE_DONE;
			}

			int vanillaSpace = destinationStack.getMaxStackSize() - destinationStack.getCount();
			if (vanillaSpace <= 0)return TransferAttempt.DESTINATION_DONE;

			ItemStack simulatedExtract;
			try {
				simulatedExtract = sourceHandler.extractItem(source.slot(),
						Math.min(vanillaSpace, sourceStack.getCount()), true);
			} catch (RuntimeException ignored) {
				return TransferAttempt.SOURCE_DONE;
			}
			if (simulatedExtract.isEmpty()
					|| !ItemStack.isSameItemSameComponents(sourceStack, simulatedExtract)) {
				return TransferAttempt.SOURCE_DONE;
			}

			ItemStack simulatedRemainder;
			try {
				simulatedRemainder = destinationHandler.insertItem(destination.slot(), simulatedExtract.copy(), true);
			} catch (RuntimeException ignored) {
				return TransferAttempt.DESTINATION_DONE;
			}
			int insertable = simulatedExtract.getCount() - simulatedRemainder.getCount();
			if (insertable <= 0)return TransferAttempt.DESTINATION_DONE;

			ItemStack extracted;
			try {
				extracted = sourceHandler.extractItem(source.slot(), insertable, false);
			} catch (RuntimeException ignored) {
				return TransferAttempt.SOURCE_DONE;
			}
			if (extracted.isEmpty())return TransferAttempt.SOURCE_DONE;
			if (!ItemStack.isSameItemSameComponents(sourceStack, extracted)) {
				recover(sourceHandler, source.slot(), extracted);
				return TransferAttempt.SOURCE_DONE;
			}

			int extractedCount = extracted.getCount();
			ItemStack remainder;
			try {
				remainder = destinationHandler.insertItem(destination.slot(), extracted, false);
			} catch (RuntimeException ignored) {
				remainder = extracted;
			}
			int moved = Math.max(0, extractedCount - remainder.getCount());
			if (!remainder.isEmpty())recover(sourceHandler, source.slot(), remainder);

			ItemStack destinationAfter = safeGetStack(destinationHandler, destination.slot());
			ItemStack sourceAfter = safeGetStack(sourceHandler, source.slot());
			boolean destinationDone = destinationAfter.isEmpty()
					|| !ItemStack.isSameItemSameComponents(destinationStack, destinationAfter)
					|| destinationAfter.getCount() >= destinationAfter.getMaxStackSize();
			boolean sourceDone = sourceAfter.isEmpty()
					|| !ItemStack.isSameItemSameComponents(sourceStack, sourceAfter);
			return new TransferAttempt(moved, destinationDone, sourceDone);
		}

		private void recover(IItemHandler sourceHandler, int sourceSlot, ItemStack stack) {
			ItemStack remainder = stack;
			try {
				remainder = sourceHandler.insertItem(sourceSlot, stack, false);
			} catch (RuntimeException ignored) {
				// The network-level recovery callback is the next safe location.
			}
			if (!remainder.isEmpty())recoverRemainder.accept(remainder);
		}

		public boolean isFinished() {
			return finished;
		}

		public long getMovedItems() {
			return movedItems;
		}
	}

	private interface InventoryHandle {
		IItemHandler resolve();
		boolean isValid();
	}

	private record AccessHandle(IInventoryAccess access, Object structureKey) implements InventoryHandle {
		private AccessHandle(IInventoryAccess access) {
			this(access, access.getStructureKey());
		}

		@Override
		public IItemHandler resolve() {
			return access.getPlatformHandler();
		}

		@Override
		public boolean isValid() {
			return Objects.equals(structureKey, access.getStructureKey());
		}
	}

	private record FixedHandle(IItemHandler handler) implements InventoryHandle {
		@Override
		public IItemHandler resolve() {
			return handler;
		}

		@Override
		public boolean isValid() {
			return true;
		}
	}

	private static ItemStack safeGetStack(IItemHandler handler, int slot) {
		try {
			return handler.getStackInSlot(slot);
		} catch (RuntimeException ignored) {
			return ItemStack.EMPTY;
		}
	}

	private record HandlerSlots(InventoryHandle handle, int slots) {
	}

	private record SlotRef(InventoryHandle handle, int slot, StoredItemStack key) {
	}

	private record TransferAttempt(int moved, boolean destinationDone, boolean sourceDone) {
		private static final TransferAttempt DESTINATION_DONE = new TransferAttempt(0, true, false);
		private static final TransferAttempt SOURCE_DONE = new TransferAttempt(0, false, true);
	}

	public record TickResult(boolean finished, int slotChecks, int moves) {
	}
}
