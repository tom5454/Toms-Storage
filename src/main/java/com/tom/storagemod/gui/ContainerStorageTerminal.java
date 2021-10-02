package com.tom.storagemod.gui;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import com.google.common.collect.Lists;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.network.IDataReceiver;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.tile.TileEntityStorageTerminal;

public class ContainerStorageTerminal extends RecipeBookMenu<CraftingContainer> implements IDataReceiver {
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
	private Map<StoredItemStack, Long> itemsCount = new HashMap<>();
	private int lines;
	protected Inventory pinv;
	public Runnable onPacket;
	public int terminalData;
	public String search;

	public ContainerStorageTerminal(int id, Inventory inv, TileEntityStorageTerminal te) {
		this(StorageMod.storageTerminal, id, inv, te);
		this.addPlayerSlots(inv, 8, 120);
	}

	public ContainerStorageTerminal(MenuType<?> type, int id, Inventory inv, TileEntityStorageTerminal te) {
		super(type, id);
		this.te = te;
		this.pinv = inv;
		addStorageSlots();
	}

	public ContainerStorageTerminal(MenuType<?> type, int id, Inventory inv) {
		this(type, id, inv, null);
	}

	protected void addStorageSlots() {
		addStorageSlots(5, 8, 18);
	}

	public ContainerStorageTerminal(int id, Inventory inv) {
		this(StorageMod.storageTerminal, id, inv);
		this.addPlayerSlots(inv, 8, 120);
	}

	protected void addPlayerSlots(Inventory playerInventory, int x, int y) {
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

		public void drawSlot(PoseStack st, GuiStorageTerminalBase gui, int mouseX, int mouseY) {
			if (mouseX >= gui.getGuiLeft() + xDisplayPosition - 1 && mouseY >= gui.getGuiTop() + yDisplayPosition - 1 && mouseX < gui.getGuiLeft() + xDisplayPosition + 17 && mouseY < gui.getGuiTop() + yDisplayPosition + 17) {
				//RenderUtil.setColourWithAlphaPercent(0xFFFFFF, 60);
				int l = gui.getGuiLeft() + xDisplayPosition;
				int t = gui.getGuiTop() + yDisplayPosition;
				GuiStorageTerminal.fill(st, l, t, l+16, t+16, 0x80FFFFFF);

			}
			if (stack != null) {
				st.pushPose();
				gui.renderItemInGui(st, stack.getStack().copy().split(1), gui.getGuiLeft() + xDisplayPosition, gui.getGuiTop() + yDisplayPosition, 0, 0, false, 0xFFFFFF, false);
				Font r = gui.getFont();
				this.drawStackSize(st, r, stack.getQuantity(), gui.getGuiLeft() + xDisplayPosition, gui.getGuiTop() + yDisplayPosition);
				st.popPose();
			}
		}

		public boolean drawTooltip(PoseStack st, GuiStorageTerminalBase gui, int mouseX, int mouseY) {
			if (stack != null) {
				if (stack.getQuantity() > 9999) {
					gui.renderItemInGui(st, stack.getStack(), gui.getGuiLeft() + xDisplayPosition, gui.getGuiTop() + yDisplayPosition, mouseX, mouseY, false, 0, true, I18n.get("tooltip.toms_storage.amount", stack.getQuantity()));
				} else {
					gui.renderItemInGui(st, stack.getStack(), gui.getGuiLeft() + xDisplayPosition, gui.getGuiTop() + yDisplayPosition, mouseX, mouseY, false, 0, true);
				}
			}
			return mouseX >= (gui.getGuiLeft() + xDisplayPosition) - 1 && mouseY >= (gui.getGuiTop() + yDisplayPosition) - 1 && mouseX < (gui.getGuiLeft() + xDisplayPosition) + 17 && mouseY < (gui.getGuiTop() + yDisplayPosition) + 17;
		}

		private void drawStackSize(PoseStack st, Font fr, long size, int x, int y) {
			float scaleFactor = 0.6f;
			//boolean unicodeFlag = fr.getUnicodeFlag();
			//fr.setUnicodeFlag(false);
			//RenderSystem.disableLighting();
			RenderSystem.disableDepthTest();
			RenderSystem.disableBlend();
			String stackSize = formatNumber(size);
			st.pushPose();
			st.scale(scaleFactor, scaleFactor, scaleFactor);
			st.translate(0, 0, 450);
			float inverseScaleFactor = 1.0f / scaleFactor;
			int X = (int) (((float) x + 0 + 16.0f - fr.width(stackSize) * scaleFactor) * inverseScaleFactor);
			int Y = (int) (((float) y + 0 + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
			fr.drawShadow(st, stackSize, X, Y, 16777215);
			st.popPose();
			//RenderSystem.enableLighting();
			RenderSystem.enableDepthTest();
			//fr.setUnicodeFlag(unicodeFlag);
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

	@Override
	public boolean stillValid(Player playerIn) {
		return te == null || te.canInteractWith(playerIn);
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

	public int drawSlots(PoseStack st, GuiStorageTerminalBase gui, int mouseX, int mouseY) {
		for (int i = 0;i < storageSlotList.size();i++) {
			storageSlotList.get(i).drawSlot(st, gui, mouseX, mouseY);
		}
		RenderSystem.disableDepthTest();
		RenderSystem.disableBlend();
		st.pushPose();
		st.translate(0, 0, 100);
		for (int i = 0;i < storageSlotList.size();i++) {
			if (storageSlotList.get(i).drawTooltip(st, gui, mouseX, mouseY)) { st.popPose(); return i; }
		}
		st.popPose();
		return -1;
	}

	public final SlotStorage getSlotByID(int id) {
		return storageSlotList.get(id);
	}

	public static enum SlotAction {
		PULL_OR_PUSH_STACK, PULL_ONE, SPACE_CLICK, SHIFT_PULL, GET_HALF, GET_QUARTER;//CRAFT
		public static final SlotAction[] VALUES = values();
	}

	@Override
	public void broadcastChanges() {
		if(te == null)return;
		Map<StoredItemStack, Long> itemsCount = te.getStacks();
		if(!this.itemsCount.equals(itemsCount)) {
			ListTag list = new ListTag();
			CompoundTag mainTag = new CompoundTag();
			this.itemList.clear();
			for(Entry<StoredItemStack, Long> e : itemsCount.entrySet()) {
				StoredItemStack storedS = e.getKey();
				CompoundTag tag = new CompoundTag();
				storedS.writeToNBT(tag, e.getValue());
				list.add(tag);
				this.itemList.add(new StoredItemStack(e.getKey().getStack(), e.getValue()));
			}
			mainTag.put("l", list);
			mainTag.putInt("p", te.getSorting());
			mainTag.putString("s", te.getLastSearch());
			NetworkHandler.sendTo((ServerPlayer) pinv.player, mainTag);
			this.itemsCount = new HashMap<>(itemsCount);
		}
		super.broadcastChanges();
	}

	public final void receiveClientNBTPacket(CompoundTag message) {
		ListTag list = message.getList("l", 10);
		itemList.clear();
		for (int i = 0;i < list.size();i++) {
			CompoundTag tag = list.getCompound(i);
			itemList.add(StoredItemStack.readFromNBT(tag));
		}
		itemListClient = new ArrayList<>(itemList);
		pinv.setChanged();
		terminalData = message.getInt("p");
		search = message.getString("s");
		if(onPacket != null)onPacket.run();
	}

	@Override
	public final ItemStack quickMoveStack(Player playerIn, int index) {
		if (slots.size() > index) {
			if (index > playerSlotsStart && te != null) {
				if (slots.get(index) != null && slots.get(index).hasItem()) {
					Slot slot = slots.get(index);
					ItemStack slotStack = slot.getItem();
					StoredItemStack c = te.pushStack(new StoredItemStack(slotStack, slotStack.getCount()));
					ItemStack itemstack = c != null ? c.getActualStack() : ItemStack.EMPTY;
					slot.set(itemstack);
					if (!playerIn.level.isClientSide)
						broadcastChanges();
				}
			} else {
				return shiftClickItems(playerIn, index);
			}
		}
		return ItemStack.EMPTY;
	}

	protected ItemStack shiftClickItems(Player playerIn, int index) {
		return ItemStack.EMPTY;
	}

	public static boolean areItemStacksEqual(ItemStack stack, ItemStack matchTo, boolean checkNBT) {
		if (stack.isEmpty() && matchTo.isEmpty())
			return false;
		if (!stack.isEmpty() && !matchTo.isEmpty()) {
			if (stack.getItem() == matchTo.getItem()) {
				boolean equals = true;
				if (checkNBT) {
					equals = equals && ItemStack.tagMatches(stack, matchTo);
				}
				return equals;
			}
		}
		return false;
	}


	@Override
	public void fillCraftSlotsStackedContents(StackedContents itemHelperIn) {
	}

	@Override
	public void clearCraftingContent() {
	}

	@Override
	public boolean recipeMatches(Recipe<? super CraftingContainer> recipeIn) {
		return false;
	}

	@Override
	public int getResultSlotIndex() {
		return 0;
	}

	@Override
	public int getGridWidth() {
		return 0;
	}

	@Override
	public int getGridHeight() {
		return 0;
	}

	@Override
	public int getSize() {
		return 0;
	}

	public void sendMessage(CompoundTag compound) {
		NetworkHandler.sendDataToServer(compound);
	}

	@Override
	public void receive(CompoundTag message) {
		if(pinv.player.isSpectator())return;
		if(message.contains("s")) {
			te.setLastSearch(message.getString("s"));
		}
		if(message.contains("a")) {
			ServerPlayer player = (ServerPlayer) pinv.player;
			player.resetLastActionTime();
			CompoundTag d = message.getCompound("a");
			ItemStack clicked = ItemStack.of(d.getCompound("s"));
			SlotAction act = SlotAction.VALUES[Math.abs(d.getInt("a")) % SlotAction.VALUES.length];
			if(act == SlotAction.SPACE_CLICK) {
				for (int i = playerSlotsStart + 1;i < playerSlotsStart + 28;i++) {
					quickMoveStack(player, i);
				}
			} else {
				if (act == SlotAction.PULL_OR_PUSH_STACK) {
					ItemStack stack = getCarried();
					if (!stack.isEmpty()) {
						StoredItemStack rem = te.pushStack(new StoredItemStack(stack));
						ItemStack itemstack = rem == null ? ItemStack.EMPTY : rem.getActualStack();
						setCarried(itemstack);
					} else {
						if (clicked.isEmpty())return;
						StoredItemStack pulled = te.pullStack(new StoredItemStack(clicked), clicked.getMaxStackSize());
						if(pulled != null) {
							setCarried(pulled.getActualStack());
						}
					}
				} else if (act == SlotAction.PULL_ONE) {
					ItemStack stack = getCarried();
					if (clicked.isEmpty())return;
					if (d.getBoolean("m")) {
						StoredItemStack pulled = te.pullStack(new StoredItemStack(clicked), 1);
						if(pulled != null) {
							ItemStack itemstack = pulled.getActualStack();
							this.moveItemStackTo(itemstack, playerSlotsStart + 1, this.slots.size(), true);
							if (itemstack.getCount() > 0)
								te.pushOrDrop(itemstack);
							player.getInventory().setChanged();
						}
					} else {
						if (!stack.isEmpty()) {
							if (areItemStacksEqual(stack, clicked, true) && stack.getCount() + 1 <= stack.getMaxStackSize()) {
								StoredItemStack pulled = te.pullStack(new StoredItemStack(clicked), 1);
								if (pulled != null) {
									stack.grow(1);
								}
							}
						} else {
							StoredItemStack pulled = te.pullStack(new StoredItemStack(clicked), 1);
							if (pulled != null) {
								setCarried(pulled.getActualStack());
							}
						}
					}
				} else if (act == SlotAction.GET_HALF) {
					ItemStack stack = getCarried();
					if (!stack.isEmpty()) {
						ItemStack stack1 = stack.split(Math.max(Math.min(stack.getCount(), stack.getMaxStackSize()) / 2, 1));
						ItemStack itemstack = te.pushStack(stack1);
						stack.grow(!itemstack.isEmpty() ? itemstack.getCount() : 0);
						setCarried(stack);
					} else {
						if (clicked.isEmpty())return;
						long maxCount = 64;
						StoredItemStack clickedSt = new StoredItemStack(clicked);
						for (int i = 0; i < itemList.size(); i++) {
							StoredItemStack e = itemList.get(i);
							if(e.equals((Object)clickedSt))
								maxCount = e.getQuantity();
						}
						StoredItemStack pulled = te.pullStack(new StoredItemStack(clicked), Math.max(Math.min(maxCount, clicked.getMaxStackSize()) / 2, 1));
						if(pulled != null) {
							setCarried(pulled.getActualStack());
						}
					}
				} else if (act == SlotAction.GET_QUARTER) {
					ItemStack stack = getCarried();
					if (!stack.isEmpty()) {
						ItemStack stack1 = stack.split(Math.max(Math.min(stack.getCount(), stack.getMaxStackSize()) / 4, 1));
						ItemStack itemstack = te.pushStack(stack1);
						stack.grow(!itemstack.isEmpty() ? itemstack.getCount() : 0);
						setCarried(stack);
					} else {
						if (clicked.isEmpty())return;
						long maxCount = 64;
						StoredItemStack clickedSt = new StoredItemStack(clicked);
						for (int i = 0; i < itemList.size(); i++) {
							StoredItemStack e = itemList.get(i);
							if(e.equals((Object)clickedSt))maxCount = e.getQuantity();
						}
						StoredItemStack pulled = te.pullStack(new StoredItemStack(clicked), Math.max(Math.min(maxCount, clicked.getMaxStackSize()) / 4, 1));
						if(pulled != null) {
							setCarried(pulled.getActualStack());
						}
					}
				} else {
					if (clicked.isEmpty())return;
					StoredItemStack pulled = te.pullStack(new StoredItemStack(clicked), clicked.getMaxStackSize());
					if(pulled != null) {
						ItemStack itemstack = pulled.getActualStack();
						this.moveItemStackTo(itemstack, playerSlotsStart + 1, this.slots.size(), true);
						if (itemstack.getCount() > 0)
							te.pushOrDrop(itemstack);
						player.getInventory().setChanged();
					}
				}
			}
		}
		if(message.contains("c")) {
			CompoundTag d = message.getCompound("c");
			te.setSorting(d.getInt("d"));
		}
	}

	@Override
	public RecipeBookType getRecipeBookType() {
		return RecipeBookType.CRAFTING;
	}

	@Override
	public boolean shouldMoveToInventory(int p_150635_) {
		return false;
	}
}
