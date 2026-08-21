package com.tom.storagemod.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.fml.loading.LoadingModList;

import com.tom.storagemod.block.entity.IInventoryConnector;

class InventoryCompactorTest {
	@BeforeAll
	static void bootstrapMinecraft() {
		LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void mergesMatchingStacksAndReleasesSlot() {
		ItemStackHandler destination = handler(stack(20));
		ItemStackHandler source = handler(stack(30));

		runToCompletion(destination, source);

		assertEquals(50, destination.getStackInSlot(0).getCount());
		assertTrue(source.getStackInSlot(0).isEmpty());
		assertEquals(50, total(destination, source));
	}

	@Test
	void compactsVanillaContainersThatMutateLiveStacksDuringExtraction() {
		SimpleContainer destinationContainer = new SimpleContainer(stack(20));
		SimpleContainer sourceContainer = new SimpleContainer(stack(30));
		InvWrapper destination = new InvWrapper(destinationContainer);
		InvWrapper source = new InvWrapper(sourceContainer);

		runToCompletion(destination, source);

		assertEquals(50, destination.getStackInSlot(0).getCount());
		assertTrue(source.getStackInSlot(0).isEmpty());
		assertEquals(50, total(destination, source));
	}

	@Test
	void compactsNonNetworkInventoryAccess() {
		ItemStackHandler inventory = handler(stack(20), stack(30));
		InventoryCompactor.Session session = InventoryCompactor.start(new MutableAccess(inventory), stack -> {});

		runToCompletion(session);

		assertEquals(50, inventory.getStackInSlot(0).getCount());
		assertTrue(inventory.getStackInSlot(1).isEmpty());
	}

	@Test
	void compactsSeveralPartialStacksIntoFullStackAndRemainder() {
		ItemStackHandler first = handler(stack(20));
		ItemStackHandler second = handler(stack(30));
		ItemStackHandler third = handler(stack(40));

		runToCompletion(first, second, third);

		assertEquals(64, first.getStackInSlot(0).getCount());
		assertEquals(26, second.getStackInSlot(0).getCount());
		assertTrue(third.getStackInSlot(0).isEmpty());
		assertEquals(90, total(first, second, third));
	}

	@Test
	void doesNotMergeDifferentComponents() {
		ItemStack namedA = stack(20);
		namedA.set(DataComponents.CUSTOM_NAME, Component.literal("A"));
		ItemStack namedB = stack(30);
		namedB.set(DataComponents.CUSTOM_NAME, Component.literal("B"));
		ItemStackHandler first = handler(namedA);
		ItemStackHandler second = handler(namedB);

		runToCompletion(first, second);

		assertEquals(20, first.getStackInSlot(0).getCount());
		assertEquals(30, second.getStackInSlot(0).getCount());
		assertEquals(50, total(first, second));
	}

	@Test
	void skipsSourceThatRefusesAutomationExtraction() {
		ItemStackHandler destination = handler(stack(20));
		ItemStackHandler source = new NoExtractHandler(stack(30));

		runToCompletion(destination, source);

		assertEquals(20, destination.getStackInSlot(0).getCount());
		assertEquals(30, source.getStackInSlot(0).getCount());
		assertEquals(50, total(destination, source));
	}

	@Test
	void skipsDestinationThatRefusesAutomationInsertion() {
		ItemStackHandler destination = new NoInsertHandler(stack(20));
		ItemStackHandler source = handler(stack(30));

		runToCompletion(destination, source);

		assertEquals(20, destination.getStackInSlot(0).getCount());
		assertEquals(30, source.getStackInSlot(0).getCount());
		assertEquals(50, total(destination, source));
	}

	@Test
	void fillsExistingStackWithoutUsingEarlierEmptySlot() {
		ItemStackHandler empty = handler(ItemStack.EMPTY);
		ItemStackHandler destination = handler(stack(30));
		ItemStackHandler source = handler(stack(30));

		runToCompletion(empty, destination, source);

		assertTrue(empty.getStackInSlot(0).isEmpty());
		assertEquals(60, destination.getStackInSlot(0).getCount());
		assertTrue(source.getStackInSlot(0).isEmpty());
		assertEquals(60, total(empty, destination, source));
	}

	@Test
	void restoresExtractedItemsWhenActualInsertionIsRejected() {
		ItemStackHandler destination = new SimulateOnlyInsertHandler(stack(20));
		ItemStackHandler source = handler(stack(30));
		List<ItemStack> recovery = new ArrayList<>();
		InventoryCompactor.Session session = InventoryCompactor.startForTesting(
				List.of(destination, source), recovery::add);

		runToCompletion(session);

		assertEquals(20, destination.getStackInSlot(0).getCount());
		assertEquals(30, source.getStackInSlot(0).getCount());
		assertTrue(recovery.isEmpty());
		assertEquals(50, total(destination, source));
	}

	@Test
	void abortsSessionWhenNetworkStructureRevisionChanges() {
		ItemStackHandler destination = handler(stack(20));
		ItemStackHandler source = handler(stack(30));
		AtomicLong revision = new AtomicLong();
		InventoryCompactor.Session session = InventoryCompactor.startForTesting(
				List.of(destination, source), stack -> {}, revision::get);

		session.tick(2, 1);
		revision.incrementAndGet();
		InventoryCompactor.TickResult result = session.tick(2, 1);

		assertTrue(result.finished());
		assertEquals(20, destination.getStackInSlot(0).getCount());
		assertEquals(30, source.getStackInSlot(0).getCount());
	}

	@Test
	void abortsSessionWhenCapturedAccessChanges() {
		MutableAccess destination = new MutableAccess(handler(stack(20)));
		MutableAccess source = new MutableAccess(handler(stack(30)));
		InventoryCompactor.Session session = InventoryCompactor.startForTestingAccesses(
				List.of(destination, source), stack -> {});

		session.tick(2, 1);
		source.invalidateStructure();
		InventoryCompactor.TickResult result = session.tick(2, 1);

		assertTrue(result.finished());
		assertEquals(20, destination.handler.getStackInSlot(0).getCount());
		assertEquals(30, source.handler.getStackInSlot(0).getCount());
	}

	@Test
	void networkRevisionIgnoresEquivalentRebuildAndDetectsMembershipChange() {
		PlatformMultiInventoryAccess network = new PlatformMultiInventoryAccess();
		MutableAccess first = new MutableAccess(handler(stack(20)));
		MutableAccess second = new MutableAccess(handler(stack(30)));
		TestConnector connector = new TestConnector(List.of(first, second), network);

		network.build(connector, List.of());
		long initialRevision = network.getStructureRevision();
		network.beginRebuild();
		network.build(connector, List.of());
		network.finishRebuild();
		assertEquals(initialRevision, network.getStructureRevision());

		connector.inventories = List.of(first);
		network.beginRebuild();
		network.build(connector, List.of());
		network.finishRebuild();
		assertTrue(network.getStructureRevision() > initialRevision);
	}

	@Test
	void compactionSurvivesEquivalentPeriodicRebuild() {
		PlatformMultiInventoryAccess network = new PlatformMultiInventoryAccess();
		MutableAccess destination = new MutableAccess(handler(stack(20)));
		MutableAccess source = new MutableAccess(handler(stack(30)));
		TestConnector connector = new TestConnector(List.of(destination, source), network);
		network.build(connector, List.of());
		InventoryCompactor.Session session = InventoryCompactor.start(network, stack -> {});

		session.tick(2, 1);
		network.beginRebuild();
		network.build(connector, List.of());
		network.finishRebuild();
		runToCompletion(session);

		assertEquals(50, destination.handler.getStackInSlot(0).getCount());
		assertTrue(source.handler.getStackInSlot(0).isEmpty());
	}

	@Test
	void compactsFiftyFourSingleItemsAcrossConnectorInventories() {
		PlatformMultiInventoryAccess network = new PlatformMultiInventoryAccess();
		MutableAccess first = new MutableAccess(filledHandler(27, new ItemStack(Items.CRAFTING_TABLE)));
		MutableAccess second = new MutableAccess(filledHandler(27, new ItemStack(Items.CRAFTING_TABLE)));
		TestConnector connector = new TestConnector(List.of(first, second), network);
		network.build(connector, List.of());
		InventoryCompactor.Session session = InventoryCompactor.start(network, stack -> {});

		runToCompletion(session);

		assertEquals(54, total(first.handler, second.handler));
		assertEquals(1, occupiedSlots(first.handler, second.handler));
	}

	@Test
	void filterRuleChangeInvalidatesItsStructureKey() {
		BlockFilter filter = new BlockFilter(BlockPos.ZERO);
		Object initialKey = filter.getStructureKey();

		filter.setKeepLast(true);

		assertNotEquals(initialKey, filter.getStructureKey());
	}

	private static void runToCompletion(IItemHandler... handlers) {
		InventoryCompactor.Session session = InventoryCompactor.startForTesting(List.of(handlers), stack -> {
			throw new AssertionError("Unexpected recovery remainder: " + stack);
		});
		runToCompletion(session);
	}

	private static void runToCompletion(InventoryCompactor.Session session) {
		for (int tick = 0; tick < 100 && !session.isFinished(); tick++) {
			InventoryCompactor.TickResult result = session.tick(2, 1);
			assertTrue(result.slotChecks() <= 2);
			assertTrue(result.moves() <= 1);
		}
		assertTrue(session.isFinished());
	}

	private static ItemStack stack(int count) {
		return new ItemStack(Items.COBBLESTONE, count);
	}

	private static ItemStackHandler handler(ItemStack... stacks) {
		ItemStackHandler handler = new ItemStackHandler(stacks.length);
		for (int i = 0; i < stacks.length; i++)handler.setStackInSlot(i, stacks[i]);
		return handler;
	}

	private static ItemStackHandler filledHandler(int slots, ItemStack stack) {
		ItemStackHandler handler = new ItemStackHandler(slots);
		for (int i = 0; i < slots; i++)handler.setStackInSlot(i, stack.copy());
		return handler;
	}

	private static int occupiedSlots(ItemStackHandler... handlers) {
		int occupied = 0;
		for (ItemStackHandler handler : handlers) {
			for (int slot = 0; slot < handler.getSlots(); slot++) {
				if (!handler.getStackInSlot(slot).isEmpty())occupied++;
			}
		}
		return occupied;
	}

	private static int total(IItemHandler... handlers) {
		int total = 0;
		for (IItemHandler handler : handlers) {
			for (int slot = 0; slot < handler.getSlots(); slot++) {
				total += handler.getStackInSlot(slot).getCount();
			}
		}
		return total;
	}

	private static class NoExtractHandler extends ItemStackHandler {
		private NoExtractHandler(ItemStack stack) {
			super(1);
			setStackInSlot(0, stack);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}
	}

	private static class SimulateOnlyInsertHandler extends ItemStackHandler {
		private SimulateOnlyInsertHandler(ItemStack stack) {
			super(1);
			setStackInSlot(0, stack);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return simulate ? super.insertItem(slot, stack, true) : stack;
		}
	}

	private static class NoInsertHandler extends ItemStackHandler {
		private NoInsertHandler(ItemStack stack) {
			super(1);
			setStackInSlot(0, stack);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return stack;
		}
	}

	private static class MutableAccess implements IInventoryAccess {
		private final ItemStackHandler handler;
		private long structureRevision;

		private MutableAccess(ItemStackHandler handler) {
			this.handler = handler;
		}

		private void invalidateStructure() {
			structureRevision++;
		}

		@Override
		public IInventoryChangeTracker tracker() {
			return InventoryChangeTracker.NULL;
		}

		@Override
		public int getFreeSlotCount() {
			return handler.getStackInSlot(0).isEmpty() ? 1 : 0;
		}

		@Override
		public int getSlotCount() {
			return handler.getSlots();
		}

		@Override
		public Object get() {
			return handler;
		}

		@Override
		public IInventoryAccess getRootHandler(Set<IProxy> dejaVu) {
			return this;
		}

		@Override
		public Object getStructureKey() {
			return new MutableStructureKey(new IInventoryAccess.IdentityKey(this), structureRevision);
		}
	}

	private record MutableStructureKey(IInventoryAccess.IdentityKey access, long revision) {
	}

	private static class TestConnector implements IInventoryConnector {
		private Collection<IInventoryAccess> inventories;
		private final IInventoryAccess merged;

		private TestConnector(Collection<IInventoryAccess> inventories, IInventoryAccess merged) {
			this.inventories = inventories;
			this.merged = merged;
		}

		@Override
		public IInventoryAccess getMergedHandler() {
			return merged;
		}

		@Override
		public Collection<IInventoryAccess> getConnectedInventories() {
			return inventories;
		}

		@Override
		public Collection<IInventoryConnector> getConnectedConnectors() {
			return List.of();
		}

		@Override
		public boolean hasConnectedInventories() {
			return true;
		}
	}
}
