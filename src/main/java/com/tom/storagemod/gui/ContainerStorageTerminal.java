package com.tom.storagemod.gui;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

import com.google.common.collect.Lists;

import com.tom.storagemod.NetworkHandler;
import com.tom.storagemod.NetworkHandler.IDataReceiver;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.tile.TileEntityStorageTerminal;
import com.tom.storagemod.util.DataSlots;
import com.tom.storagemod.util.TerminalSyncManager;
import com.tom.storagemod.util.TerminalSyncManager.InteractHandler;

public class ContainerStorageTerminal extends AbstractRecipeScreenHandler<CraftingInventory> implements IDataReceiver, InteractHandler {
	private static final int DIVISION_BASE = 1000;
	private static final char[] ENCODED_POSTFIXES = "KMGTPE".toCharArray();
	public static final Format format;

	static {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		DecimalFormat format_ = new DecimalFormat(".#;0.#");
		format_.setDecimalFormatSymbols(symbols);
		format_.setRoundingMode(RoundingMode.DOWN);
		format = format_;
	}

	protected TileEntityStorageTerminal te;
	protected int playerSlotsStart;
	protected List<SlotStorage> storageSlotList = new ArrayList<>();
	public List<StoredItemStack> itemList = Lists.<StoredItemStack>newArrayList();
	public List<StoredItemStack> itemListClient = Lists.<StoredItemStack>newArrayList();
	public List<StoredItemStack> itemListClientSorted = Lists.<StoredItemStack>newArrayList();
	private int lines;
	protected PlayerInventory pinv;
	public Runnable onPacket;
	public int terminalData, beaconLvl;
	public String search;
	public boolean noSort;
	public TerminalSyncManager sync = new TerminalSyncManager();
	public List<SlotData> slotData = new ArrayList<>();

	public ContainerStorageTerminal(int id, PlayerInventory inv, TileEntityStorageTerminal te) {
		this(StorageMod.storageTerminal, id, inv, te);
		this.addPlayerSlots(inv, 8, 120);
	}

	public ContainerStorageTerminal(ScreenHandlerType<?> type, int id, PlayerInventory inv, TileEntityStorageTerminal te) {
		super(type, id);
		this.te = te;
		this.pinv = inv;
		addStorageSlots();
		addProperty(DataSlots.create(v -> {
			terminalData = v;
			if(onPacket != null)
				onPacket.run();
		}, () -> te != null ? te.getSorting() : 0));
		addProperty(DataSlots.create(v -> beaconLvl = v, () -> te != null ? te.getBeaconLevel() : 0));
	}

	@Override
	public boolean onButtonClick(PlayerEntity playerIn, int id) {
		if(id == 0)return false;
		int newC = id >> 1;
		te.setSorting(newC);
		return false;
	}

	public ContainerStorageTerminal(ScreenHandlerType<?> type, int id, PlayerInventory inv) {
		this(type, id, inv, null);
	}

	protected void addStorageSlots() {
		addStorageSlots(5, 8, 18);
	}

	public ContainerStorageTerminal(int id, PlayerInventory inv) {
		this(StorageMod.storageTerminal, id, inv);
		this.addPlayerSlots(inv, 8, 120);
	}

	protected void addPlayerSlots(PlayerInventory playerInventory, int x, int y) {
		this.playerSlotsStart = slots.size() - 1;
		for (int i = 0;i < 3;++i) {
			for (int j = 0;j < 9;++j) {
				addSlot(new Slot(playerInventory, j + i * 9 + 9, x + j * 18, y + i * 18));
			}
		}

		for (int i = 0;i < 9;++i) {
			addSlot(new Slot(playerInventory, i, x + i * 18, y + 58));
		}
	}

	@Override
	protected Slot addSlot(Slot slotIn) {
		slotData.add(new SlotData(slotIn));
		return super.addSlot(slotIn);
	}

	public void setOffset(int x, int y) {
		slotData.forEach(d -> d.setOffset(x, y));
	}

	public final void addStorageSlots(int lines, int x, int y) {
		storageSlotList.clear();
		this.lines = lines;
		for (int i = 0;i < lines;++i) {
			for (int j = 0;j < 9;++j) {
				this.addSlotToContainer(new SlotStorage(this.te, i * 9 + j, x + j * 18, y + i * 18));
			}
		}
		scrollTo(0.0F);
	}

	protected final void addSlotToContainer(SlotStorage slotStorage) {
		storageSlotList.add(slotStorage);
	}

	public static class SlotStorage {
		/** display position of the inventory slot on the screen x axis */
		public int xDisplayPosition;
		/** display position of the inventory slot on the screen y axis */
		public int yDisplayPosition;
		/** The index of the slot in the inventory. */
		private final int slotIndex;
		/** The inventory we want to extract a slot from. */
		public final TileEntityStorageTerminal inventory;
		public StoredItemStack stack;

		public SlotStorage(TileEntityStorageTerminal inventory, int slotIndex, int xPosition, int yPosition) {
			this.xDisplayPosition = xPosition;
			this.yDisplayPosition = yPosition;
			this.slotIndex = slotIndex;
			this.inventory = inventory;
		}

		public ItemStack pullFromSlot(long max) {
			if (stack == null || max < 1 || inventory == null)
				return ItemStack.EMPTY;
			StoredItemStack r = inventory.pullStack(stack, max);
			if (r != null) {
				return r.getActualStack();
			} else
				return ItemStack.EMPTY;
		}

		public ItemStack pushStack(ItemStack pushStack) {
			if(inventory == null)return pushStack;
			StoredItemStack r = inventory.pushStack(new StoredItemStack(pushStack, pushStack.getCount()));
			if (r != null) {
				return r.getActualStack();
			} else
				return ItemStack.EMPTY;
		}

		public int getSlotIndex() {
			return slotIndex;
		}
	}

	public static String formatNumber(long number) {
		int width = 4;
		assert number >= 0;
		String numberString = Long.toString(number);
		int numberSize = numberString.length();
		if (numberSize <= width) { return numberString; }

		long base = number;
		double last = base * 1000;
		int exponent = -1;
		String postFix = "";

		while (numberSize > width) {
			last = base;
			base /= DIVISION_BASE;

			exponent++;

			numberSize = Long.toString(base).length() + 1;
			postFix = String.valueOf(ENCODED_POSTFIXES[exponent]);
		}

		String withPrecision = format.format(last / DIVISION_BASE) + postFix;
		String withoutPrecision = Long.toString(base) + postFix;

		String slimResult = (withPrecision.length() <= width) ? withPrecision : withoutPrecision;
		assert slimResult.length() <= width;
		return slimResult;
	}

	public final void scrollTo(float p_148329_1_) {
		int i = (this.itemListClientSorted.size() + 9 - 1) / 9 - lines;
		int j = (int) (p_148329_1_ * i + 0.5D);

		if (j < 0) {
			j = 0;
		}

		for (int k = 0;k < lines;++k) {
			for (int l = 0;l < 9;++l) {
				int i1 = l + (k + j) * 9;

				if (i1 >= 0 && i1 < this.itemListClientSorted.size()) {
					setSlotContents(l + k * 9, this.itemListClientSorted.get(i1));
				} else {
					setSlotContents(l + k * 9, null);
				}
			}
		}
	}

	public final void setSlotContents(int id, StoredItemStack stack) {
		storageSlotList.get(id).stack = stack;
	}

	public final SlotStorage getSlotByID(int id) {
		return storageSlotList.get(id);
	}

	public static enum SlotAction {
		PULL_OR_PUSH_STACK, PULL_ONE, SPACE_CLICK, SHIFT_PULL, GET_HALF, GET_QUARTER;//CRAFT
		public static final SlotAction[] VALUES = values();
	}

	@Override
	public void sendContentUpdates() {
		if(te == null)return;
		Map<StoredItemStack, Long> itemsCount = te.getStacks();
		sync.update(itemsCount, (ServerPlayerEntity) pinv.player, !te.getLastSearch().equals(search) ? tag -> {
			search = te.getLastSearch();
			tag.putString("s", search);
		} : null);
		super.sendContentUpdates();
	}

	public final void receiveClientTagPacket(NbtCompound message) {
		if(sync.receiveUpdate(message)) {
			itemList = sync.getAsList();
			if(noSort) {
				itemListClient.forEach(s -> s.setCount(sync.getAmount(s)));
			} else {
				itemListClient = new ArrayList<>(itemList);
			}
			pinv.markDirty();
		}
		if(message.contains("s"))
			search = message.getString("s");
		if(onPacket != null)onPacket.run();
	}

	@Override
	public final ItemStack transferSlot(PlayerEntity playerIn, int index) {
		if (slots.size() > index) {
			if (index > playerSlotsStart && te != null) {
				if (slots.get(index) != null && slots.get(index).hasStack()) {
					Slot slot = slots.get(index);
					ItemStack slotStack = slot.getStack();
					StoredItemStack c = te.pushStack(new StoredItemStack(slotStack, slotStack.getCount()));
					ItemStack itemstack = c != null ? c.getActualStack() : ItemStack.EMPTY;
					slot.setStack(itemstack);
					if (!playerIn.world.isClient)
						sendContentUpdates();
				}
			} else {
				return shiftClickItems(playerIn, index);
			}
		}
		return ItemStack.EMPTY;
	}

	protected ItemStack shiftClickItems(PlayerEntity playerIn, int index) {
		return ItemStack.EMPTY;
	}

	public void sendMessage(NbtCompound compound) {
		NetworkHandler.sendToServer(compound);
	}

	@Override
	public void receive(NbtCompound message) {
		if(pinv.player.isSpectator())return;
		if(message.contains("s")) {
			te.setLastSearch(message.getString("s"));
		}
		sync.receiveInteract(message, this);
		if(message.contains("c")) {
			NbtCompound d = message.getCompound("c");
			te.setSorting(d.getInt("d"));
		}
	}

	@Override
	public void populateRecipeFinder(RecipeMatcher paramRecipeFinder) {
	}

	@Override
	public void clearCraftingSlots() {
	}

	@Override
	public boolean matches(Recipe<? super CraftingInventory> paramRecipe) {
		return false;
	}

	@Override
	public int getCraftingResultSlotIndex() {
		return 0;
	}

	@Override
	public int getCraftingWidth() {
		return 0;
	}

	@Override
	public int getCraftingHeight() {
		return 0;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public int getCraftingSlotCount() {
		return 0;
	}

	@Override
	public boolean canUse(PlayerEntity paramPlayerEntity) {
		return te == null || te.canInteractWith(paramPlayerEntity);
	}

	@Override
	public RecipeBookCategory getCategory() {
		return RecipeBookCategory.CRAFTING;
	}

	@Override
	public boolean canInsertIntoSlot(int index) {
		return false;
	}

	@Override
	public void onInteract(StoredItemStack clicked, SlotAction act, boolean mod) {
		ServerPlayerEntity player = (ServerPlayerEntity) pinv.player;
		player.updateLastActionTime();
		if(act == SlotAction.SPACE_CLICK) {
			for (int i = playerSlotsStart + 1;i < playerSlotsStart + 28;i++) {
				transferSlot(player, i);
			}
		} else {
			if (act == SlotAction.PULL_OR_PUSH_STACK) {
				ItemStack stack = getCursorStack();
				if (!stack.isEmpty()) {
					StoredItemStack rem = te.pushStack(new StoredItemStack(stack));
					ItemStack itemstack = rem == null ? ItemStack.EMPTY : rem.getActualStack();
					setCursorStack(itemstack);
				} else {
					if (clicked == null)return;
					StoredItemStack pulled = te.pullStack(clicked, clicked.getMaxStackSize());
					if(pulled != null) {
						setCursorStack(pulled.getActualStack());
					}
				}
			} else if (act == SlotAction.PULL_ONE) {
				ItemStack stack = getCursorStack();
				if (clicked == null)return;
				if (mod) {
					StoredItemStack pulled = te.pullStack(clicked, 1);
					if(pulled != null) {
						ItemStack itemstack = pulled.getActualStack();
						this.insertItem(itemstack, playerSlotsStart + 1, this.slots.size(), true);
						if (itemstack.getCount() > 0)
							te.pushOrDrop(itemstack);
						player.getInventory().markDirty();
					}
				} else {
					if (!stack.isEmpty()) {
						if (ItemStack.areEqual(stack, clicked.getStack()) && stack.getCount() + 1 <= stack.getMaxCount()) {
							StoredItemStack pulled = te.pullStack(clicked, 1);
							if (pulled != null) {
								stack.increment(1);
							}
						}
					} else {
						StoredItemStack pulled = te.pullStack(clicked, 1);
						if (pulled != null) {
							setCursorStack(pulled.getActualStack());
						}
					}
				}
			} else if (act == SlotAction.GET_HALF) {
				ItemStack stack = getCursorStack();
				if (!stack.isEmpty()) {
					ItemStack stack1 = stack.split(Math.max(Math.min(stack.getCount(), stack.getMaxCount()) / 2, 1));
					ItemStack itemstack = te.pushStack(stack1);
					stack.increment(!itemstack.isEmpty() ? itemstack.getCount() : 0);
					setCursorStack(stack);
				} else {
					if (clicked == null)return;
					long maxCount = 64;
					for (int i = 0; i < itemList.size(); i++) {
						StoredItemStack e = itemList.get(i);
						if(e.equals((Object)clicked))
							maxCount = e.getQuantity();
					}
					StoredItemStack pulled = te.pullStack(clicked, Math.max(Math.min(maxCount, clicked.getMaxStackSize()) / 2, 1));
					if(pulled != null) {
						setCursorStack(pulled.getActualStack());
					}
				}
			} else if (act == SlotAction.GET_QUARTER) {
				ItemStack stack = getCursorStack();
				if (!stack.isEmpty()) {
					ItemStack stack1 = stack.split(Math.max(Math.min(stack.getCount(), stack.getMaxCount()) / 4, 1));
					ItemStack itemstack = te.pushStack(stack1);
					stack.increment(!itemstack.isEmpty() ? itemstack.getCount() : 0);
					setCursorStack(stack);
				} else {
					if (clicked == null)return;
					long maxCount = 64;
					for (int i = 0; i < itemList.size(); i++) {
						StoredItemStack e = itemList.get(i);
						if(e.equals((Object)clicked))maxCount = e.getQuantity();
					}
					StoredItemStack pulled = te.pullStack(clicked, Math.max(Math.min(maxCount, clicked.getMaxStackSize()) / 4, 1));
					if(pulled != null) {
						setCursorStack(pulled.getActualStack());
					}
				}
			} else {
				if (clicked == null)return;
				StoredItemStack pulled = te.pullStack(clicked, clicked.getMaxStackSize());
				if(pulled != null) {
					ItemStack itemstack = pulled.getActualStack();
					this.insertItem(itemstack, playerSlotsStart + 1, this.slots.size(), true);
					if (itemstack.getCount() > 0)
						te.pushOrDrop(itemstack);
					player.getInventory().markDirty();
				}
			}
		}
		//player.updateCursorStack();
	}

	public static record SlotData(Slot slot, int x, int y) {

		public SlotData(Slot s) {
			this(s, s.x, s.y);
		}

		public void setOffset(int x, int y) {
			slot.x = this.x + x;
			slot.y = this.y + y;
		}
	}
}
