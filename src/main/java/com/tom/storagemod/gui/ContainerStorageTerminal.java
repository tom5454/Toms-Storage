package com.tom.storagemod.gui;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Blocks;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import com.google.common.collect.Lists;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.network.IDataReceiver;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.tile.TileEntityStorageTerminal;

public class ContainerStorageTerminal extends RecipeBookContainer<CraftingInventory> implements IDataReceiver {
	private static final int DIVISION_BASE = 1000;
	private static final char[] ENCODED_POSTFIXES = "KMGTPE".toCharArray();
	public static final Format format;
	private IInventory syncInv = new Inventory(1);
	private boolean skipUpdateTick;

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
	private int syncSlotID;
	protected PlayerInventory pinv;
	public Runnable onPacket;
	public int terminalData;
	public String search;

	public ContainerStorageTerminal(int id, PlayerInventory inv, TileEntityStorageTerminal te) {
		this(StorageMod.storageTerminal, id, inv, te);
		addSyncSlot();
		this.addPlayerSlots(inv, 8, 120);
	}

	public ContainerStorageTerminal(ContainerType<?> type, int id, PlayerInventory inv, TileEntityStorageTerminal te) {
		super(type, id);
		this.te = te;
		this.pinv = inv;
		addStorageSlots();
	}

	protected void addSyncSlot() {
		syncSlotID = addSlot(new Slot(syncInv, 0, -16, -16) {
			@Override
			public boolean canTakeStack(PlayerEntity playerIn) {
				return false;
			}
			@Override
			public boolean isItemValid(ItemStack stack) {
				return false;
			}
			@OnlyIn(Dist.CLIENT)
			@Override
			public boolean isEnabled() {
				return false;
			}
		}).slotNumber;
		syncInv.setInventorySlotContents(0, new ItemStack(Items.BARRIER));
	}

	@Override
	public boolean enchantItem(PlayerEntity playerIn, int id) {
		if(id == 0)return false;
		int newC = id >> 1;
		te.setSorting(newC);
		return false;
	}

	public ContainerStorageTerminal(ContainerType<?> type, int id, PlayerInventory inv) {
		this(type, id, inv, null);
	}

	protected void addStorageSlots() {
		addStorageSlots(5, 8, 18);
	}

	public ContainerStorageTerminal(int id, PlayerInventory inv) {
		this(StorageMod.storageTerminal, id, inv);
		addSyncSlot();
		this.addPlayerSlots(inv, 8, 120);
	}

	protected void addPlayerSlots(PlayerInventory playerInventory, int x, int y) {
		this.playerSlotsStart = inventorySlots.size() - 1;
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

		@OnlyIn(Dist.CLIENT)
		public void drawSlot(MatrixStack st, GuiStorageTerminalBase gui, int mouseX, int mouseY) {
			if (mouseX >= gui.getGuiLeft() + xDisplayPosition - 1 && mouseY >= gui.getGuiTop() + yDisplayPosition - 1 && mouseX < gui.getGuiLeft() + xDisplayPosition + 17 && mouseY < gui.getGuiTop() + yDisplayPosition + 17) {
				//RenderUtil.setColourWithAlphaPercent(0xFFFFFF, 60);
				int l = gui.getGuiLeft() + xDisplayPosition;
				int t = gui.getGuiTop() + yDisplayPosition;
				GuiStorageTerminal.fill(st, l, t, l+16, t+16, 0x80FFFFFF);

			}
			if (stack != null) {
				st.push();
				gui.renderItemInGui(st, stack.getStack().copy().split(1), gui.getGuiLeft() + xDisplayPosition, gui.getGuiTop() + yDisplayPosition, 0, 0, false, 0xFFFFFF, false);
				FontRenderer r = stack.getStack().getItem().getFontRenderer(stack.getStack());
				if(r == null)r = gui.getFont();
				this.drawStackSize(st, r, stack.getQuantity(), gui.getGuiLeft() + xDisplayPosition, gui.getGuiTop() + yDisplayPosition);
				st.pop();
			}
		}

		@OnlyIn(Dist.CLIENT)
		public boolean drawTooltip(MatrixStack st, GuiStorageTerminalBase gui, int mouseX, int mouseY) {
			if (stack != null) {
				if (stack.getQuantity() > 9999) {
					gui.renderItemInGui(st, stack.getStack(), gui.getGuiLeft() + xDisplayPosition, gui.getGuiTop() + yDisplayPosition, mouseX, mouseY, false, 0, true, I18n.format("tooltip.toms_storage.amount", stack.getQuantity()));
				} else {
					gui.renderItemInGui(st, stack.getStack(), gui.getGuiLeft() + xDisplayPosition, gui.getGuiTop() + yDisplayPosition, mouseX, mouseY, false, 0, true);
				}
			}
			return mouseX >= (gui.getGuiLeft() + xDisplayPosition) - 1 && mouseY >= (gui.getGuiTop() + yDisplayPosition) - 1 && mouseX < (gui.getGuiLeft() + xDisplayPosition) + 17 && mouseY < (gui.getGuiTop() + yDisplayPosition) + 17;
		}

		@OnlyIn(Dist.CLIENT)
		private void drawStackSize(MatrixStack st, FontRenderer fr, long size, int x, int y) {
			float scaleFactor = 0.6f;
			//boolean unicodeFlag = fr.getUnicodeFlag();
			//fr.setUnicodeFlag(false);
			RenderSystem.disableLighting();
			RenderSystem.disableDepthTest();
			RenderSystem.disableBlend();
			String stackSize = formatNumber(size);
			st.push();
			st.scale(scaleFactor, scaleFactor, scaleFactor);
			st.translate(0, 0, 450);
			float inverseScaleFactor = 1.0f / scaleFactor;
			int X = (int) (((float) x + 0 + 16.0f - fr.getStringWidth(stackSize) * scaleFactor) * inverseScaleFactor);
			int Y = (int) (((float) y + 0 + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
			fr.drawStringWithShadow(st, stackSize, X, Y, 16777215);
			st.pop();
			RenderSystem.enableLighting();
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
	public boolean canInteractWith(PlayerEntity playerIn) {
		return te.canInteractWith(playerIn);
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

	@OnlyIn(Dist.CLIENT)
	public int drawSlots(MatrixStack st, GuiStorageTerminalBase gui, int mouseX, int mouseY) {
		for (int i = 0;i < storageSlotList.size();i++) {
			storageSlotList.get(i).drawSlot(st, gui, mouseX, mouseY);
		}
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();
		RenderSystem.disableBlend();
		st.push();
		st.translate(0, 0, 100);
		for (int i = 0;i < storageSlotList.size();i++) {
			if (storageSlotList.get(i).drawTooltip(st, gui, mouseX, mouseY)) { st.pop(); return i; }
		}
		st.pop();
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
	public void detectAndSendChanges() {
		if(te == null)return;
		//List<StoredItemStack> itemListOld = itemList;
		itemList = te.getStacks();
		ListNBT list = new ListNBT();
		for (int i = 0;i < itemList.size();i++) {
			StoredItemStack storedS = itemList.get(i);
			//StoredItemStack storedSOld = itemListOld.size() > i ? itemListOld.get(i) : null;
			if (storedS != null) {
				CompoundNBT tag = new CompoundNBT();
				//tag.putInt("slot", i);
				if (storedS != null)
					storedS.writeToNBT(tag);
				list.add(tag);
			}
		}
		CompoundNBT mainTag = new CompoundNBT();
		mainTag.put("l", list);
		mainTag.putInt("p", te.getSorting());
		mainTag.putString("s", te.getLastSearch());
		ItemStack is = syncInv.getStackInSlot(0);
		boolean t = is.getItem() == Items.BARRIER;
		if(!mainTag.equals(is.getTag()) && !skipUpdateTick) {
			//System.out.println(itemList);
			is = new ItemStack(t ? Blocks.STRUCTURE_VOID : Items.BARRIER);
			syncInv.setInventorySlotContents(0, is);
			//System.out.println("Set Slot: " + mainTag);
			is.setTag(mainTag);
			((ServerPlayerEntity)pinv.player).isChangingQuantityOnly = false;
		}
		super.detectAndSendChanges();
		if(skipUpdateTick)is.setTag(null);
		skipUpdateTick = false;
	}

	@Override
	public void putStackInSlot(int slotID, ItemStack stack) {
		if(slotID == syncSlotID && stack.hasTag()) {
			receiveClientNBTPacket(stack.getTag());
		}
		super.putStackInSlot(slotID, stack);
	}

	public final void receiveClientNBTPacket(CompoundNBT message) {
		//System.out.println(message);
		ListNBT list = message.getList("l", 10);
		itemList.clear();
		for (int i = 0;i < list.size();i++) {
			CompoundNBT tag = list.getCompound(i);
			itemList.add(StoredItemStack.readFromNBT(tag));
			//System.out.println(tag);
		}
		//System.out.println(itemList);
		itemListClient = new ArrayList<>(itemList);
		pinv.markDirty();
		terminalData = message.getInt("p");
		search = message.getString("s");
		if(onPacket != null)onPacket.run();
	}

	/*@Override
	public final ItemStack slotClick(int slotId, int clickedButton, ClickType clickTypeIn, PlayerEntity playerIn) {
		skipUpdateTick = true;
		SlotAction mode = SlotAction.VALUES[clickTypeIn.ordinal()];
		if (slotId < -1 && slotId != -999 && mode != SlotAction.SPACE_CLICK) {
			if(te == null)return new ItemStack(Blocks.BARRIER, 69);
			if (mode == SlotAction.PULL_OR_PUSH_STACK) {
				SlotStorage slot = storageSlotList.get(0);
				ItemStack stack = playerIn.inventory.getItemStack();
				if (!stack.isEmpty()) {
					ItemStack itemstack = slot.pushStack(stack);
					playerIn.inventory.setItemStack(itemstack);
					return itemstack;
				} else {
					if (itemList.size() > clickedButton) {
						slot.stack = itemList.get(clickedButton);
						if (slot.stack.getQuantity() == 0) {
							// craft(playerIn, slot);
							return ItemStack.EMPTY;
						} else {
							ItemStack itemstack = slot.pullFromSlot(64);
							playerIn.inventory.setItemStack(itemstack);
							return itemstack;
						}
					} else
						return ItemStack.EMPTY;
				}
			} else if (mode == SlotAction.PULL_ONE) {
				SlotStorage slot = storageSlotList.get(0);
				ItemStack stack = playerIn.inventory.getItemStack();
				if (slotId == -3) {
					if (itemList.size() > clickedButton) {
						slot.stack = itemList.get(clickedButton);
						ItemStack itemstack = slot.pullFromSlot(1);
						if (!itemstack.isEmpty()) {
							this.mergeItemStack(itemstack, playerSlotsStart + 1, this.inventorySlots.size(), true);
							if (itemstack.getCount() > 0)
								slot.pushStack(itemstack);
						}
						playerIn.inventory.markDirty();
						return stack;
					} else
						return stack;
				} else {
					if (!stack.isEmpty()) {
						slot.stack = itemList.get(clickedButton);
						if (areItemStacksEqual(stack, slot.stack.getStack(), true) && stack.getCount() + 1 <= stack.getMaxStackSize()) {
							ItemStack itemstack = slot.pullFromSlot(1);
							if (!itemstack.isEmpty()) {
								stack.grow(1);
								return stack;
							}
						}
					} else {
						if (itemList.size() > clickedButton) {
							slot.stack = itemList.get(clickedButton);
							ItemStack itemstack = slot.pullFromSlot(1);
							playerIn.inventory.setItemStack(itemstack);
							return itemstack;
						} else
							return ItemStack.EMPTY;
					}
				}
				return ItemStack.EMPTY;
			} else if (mode == SlotAction.GET_HALF) {
				SlotStorage slot = storageSlotList.get(0);
				ItemStack stack = playerIn.inventory.getItemStack();
				if (!stack.isEmpty()) {
					ItemStack stack1 = stack.split(Math.min(stack.getCount(), stack.getMaxStackSize()) / 2);
					ItemStack itemstack = slot.pushStack(stack1);
					stack.grow(!itemstack.isEmpty() ? itemstack.getCount() : 0);
					playerIn.inventory.setItemStack(stack);
					return stack;
				} else {
					if (itemList.size() > clickedButton) {
						slot.stack = itemList.get(clickedButton);
						ItemStack itemstack = slot.pullFromSlot(Math.min(slot.stack.getQuantity(), slot.stack.getStack().getMaxStackSize()) / 2);
						playerIn.inventory.setItemStack(itemstack);
						return itemstack;
					} else
						return ItemStack.EMPTY;
				}
			} else if (mode == SlotAction.GET_QUARTER) {
				SlotStorage slot = storageSlotList.get(0);
				ItemStack stack = playerIn.inventory.getItemStack();
				if (!stack.isEmpty()) {
					ItemStack stack1 = stack.split(Math.min(stack.getCount(), stack.getMaxStackSize()) / 4);
					ItemStack itemstack = slot.pushStack(stack1);
					stack.grow(!itemstack.isEmpty() ? itemstack.getCount() : 0);
					playerIn.inventory.setItemStack(stack);
					return stack;
				} else {
					if (itemList.size() > clickedButton) {
						slot.stack = itemList.get(clickedButton);
						ItemStack itemstack = slot.pullFromSlot(Math.min(slot.stack.getQuantity(), slot.stack.getStack().getMaxStackSize()) / 4);
						playerIn.inventory.setItemStack(itemstack);
						return itemstack;
					} else
						return ItemStack.EMPTY;
				}
			} else if (mode == SlotAction.PULL_ONE) {
				SlotStorage slot = storageSlotList.get(0);
				ItemStack stack = playerIn.inventory.getItemStack();
				if (!stack.isEmpty()) {
					slot.stack = itemList.get(clickedButton);
					// if(TomsModUtils.areItemStacksEqual(stack,
					// slot.stack.stack, true, true, false)){
					ItemStack s = stack.split(1);
					ItemStack s2 = slot.pushStack(s);
					if (!s2.isEmpty()) {
						stack.grow(s2.getCount());
					}
					if (stack.isEmpty()) {
						stack = ItemStack.EMPTY;
					}
					playerIn.inventory.setItemStack(stack);
					return stack;
					// }
				}
				return ItemStack.EMPTY;
			} else {
				SlotStorage slot = storageSlotList.get(0);
				if (itemList.size() > clickedButton && !playerIn.world.isRemote) {
					slot.stack = itemList.get(clickedButton);
					ItemStack itemstack = slot.pullFromSlot(64);
					if (!itemstack.isEmpty()) {
						this.mergeItemStack(itemstack, playerSlotsStart + 1, this.inventorySlots.size(), true);
						if (itemstack.getCount() > 0)
							slot.pushStack(itemstack);
					}
					playerIn.inventory.markDirty();
				}
				return ItemStack.EMPTY;
			}
		} else if (slotId == -1 && mode == SlotAction.SPACE_CLICK) {
			for (int i = playerSlotsStart + 1;i < playerSlotsStart + 28;i++) {
				transferStackInSlot(playerIn, i);
			}
			return ItemStack.EMPTY;
		} else
			return super.slotClick(slotId, clickedButton, clickTypeIn, playerIn);
	}*/

	@Override
	public final ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		if (inventorySlots.size() > index) {
			if (index > playerSlotsStart && te != null) {
				if (inventorySlots.get(index) != null && inventorySlots.get(index).getHasStack()) {
					Slot slot = inventorySlots.get(index);
					ItemStack slotStack = slot.getStack();
					StoredItemStack c = te.pushStack(new StoredItemStack(slotStack, slotStack.getCount()));
					ItemStack itemstack = c != null ? c.getActualStack() : ItemStack.EMPTY;
					slot.putStack(itemstack);
					if (!playerIn.world.isRemote)
						detectAndSendChanges();
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

	public static boolean areItemStacksEqual(ItemStack stack, ItemStack matchTo, boolean checkNBT) {
		if (stack.isEmpty() && matchTo.isEmpty())
			return false;
		if (!stack.isEmpty() && !matchTo.isEmpty()) {
			if (stack.getItem() == matchTo.getItem()) {
				boolean equals = true;
				if (checkNBT) {
					equals = equals && ItemStack.areItemStackTagsEqual(stack, matchTo);
				}
				return equals;
			}
		}
		return false;
	}


	@Override
	public void fillStackedContents(RecipeItemHelper itemHelperIn) {
	}

	@Override
	public void clear() {
	}

	@Override
	public boolean matches(IRecipe<? super CraftingInventory> recipeIn) {
		return false;
	}

	@Override
	public int getOutputSlot() {
		return 0;
	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public int getSize() {
		return 0;
	}

	public void sendMessage(CompoundNBT compound) {
		NetworkHandler.sendDataToServer(compound);
	}

	@Override
	public void receive(CompoundNBT message) {
		if(message.contains("s")) {
			te.setLastSearch(message.getString("s"));
		}
		if(message.contains("a")) {
			ServerPlayerEntity player = (ServerPlayerEntity) pinv.player;
			player.markPlayerActive();
			CompoundNBT d = message.getCompound("a");
			ItemStack clicked = ItemStack.read(d.getCompound("s"));
			SlotAction act = SlotAction.VALUES[Math.abs(d.getInt("a")) % SlotAction.VALUES.length];
			if(act == SlotAction.SPACE_CLICK) {
				for (int i = playerSlotsStart + 1;i < playerSlotsStart + 28;i++) {
					transferStackInSlot(player, i);
				}
			} else {
				if (act == SlotAction.PULL_OR_PUSH_STACK) {
					ItemStack stack = player.inventory.getItemStack();
					if (!stack.isEmpty()) {
						StoredItemStack rem = te.pushStack(new StoredItemStack(stack));
						ItemStack itemstack = rem == null ? ItemStack.EMPTY : rem.getActualStack();
						player.inventory.setItemStack(itemstack);
					} else {
						if (clicked.isEmpty())return;
						StoredItemStack pulled = te.pullStack(new StoredItemStack(clicked), clicked.getMaxStackSize());
						if(pulled != null) {
							player.inventory.setItemStack(pulled.getActualStack());
						}
					}
				} else if (act == SlotAction.PULL_ONE) {
					ItemStack stack = player.inventory.getItemStack();
					if (clicked.isEmpty())return;
					if (d.getBoolean("m")) {
						StoredItemStack pulled = te.pullStack(new StoredItemStack(clicked), 1);
						if(pulled != null) {
							ItemStack itemstack = pulled.getActualStack();
							this.mergeItemStack(itemstack, playerSlotsStart + 1, this.inventorySlots.size(), true);
							if (itemstack.getCount() > 0)
								te.pushOrDrop(itemstack);
							player.inventory.markDirty();
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
								player.inventory.setItemStack(pulled.getActualStack());
							}
						}
					}
				} else if (act == SlotAction.GET_HALF) {
					ItemStack stack = player.inventory.getItemStack();
					if (!stack.isEmpty()) {
						ItemStack stack1 = stack.split(Math.max(Math.min(stack.getCount(), stack.getMaxStackSize()) / 2, 1));
						ItemStack itemstack = te.pushStack(stack1);
						stack.grow(!itemstack.isEmpty() ? itemstack.getCount() : 0);
						player.inventory.setItemStack(stack);
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
							player.inventory.setItemStack(pulled.getActualStack());
						}
					}
				} else if (act == SlotAction.GET_QUARTER) {
					ItemStack stack = player.inventory.getItemStack();
					if (!stack.isEmpty()) {
						ItemStack stack1 = stack.split(Math.max(Math.min(stack.getCount(), stack.getMaxStackSize()) / 4, 1));
						ItemStack itemstack = te.pushStack(stack1);
						stack.grow(!itemstack.isEmpty() ? itemstack.getCount() : 0);
						player.inventory.setItemStack(stack);
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
							player.inventory.setItemStack(pulled.getActualStack());
						}
					}
				} else {
					if (clicked.isEmpty())return;
					StoredItemStack pulled = te.pullStack(new StoredItemStack(clicked), clicked.getMaxStackSize());
					if(pulled != null) {
						ItemStack itemstack = pulled.getActualStack();
						this.mergeItemStack(itemstack, playerSlotsStart + 1, this.inventorySlots.size(), true);
						if (itemstack.getCount() > 0)
							te.pushOrDrop(itemstack);
						player.inventory.markDirty();
					}
				}
			}
			player.updateHeldItem();
		}
	}
}
