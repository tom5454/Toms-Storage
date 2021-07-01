package com.tom.storagemod.gui;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.ServerRecipePlacer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SSetSlotPacket;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.common.collect.Lists;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.jei.IJEIAutoFillTerminal;
import com.tom.storagemod.network.IDataReceiver;
import com.tom.storagemod.tile.TileEntityCraftingTerminal;

public class ContainerCraftingTerminal extends ContainerStorageTerminal implements IJEIAutoFillTerminal, IDataReceiver {
	public static class SlotCrafting extends Slot {
		public SlotCrafting(IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}
	}
	private static Field recipeItemHelperField;
	static {
		try {
			for (Field f : ServerRecipePlacer.class.getDeclaredFields()) {
				if(f.getType() == RecipeItemHelper.class) {
					f.setAccessible(true);
					recipeItemHelperField = f;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final CraftingInventory craftMatrix;
	private final CraftResultInventory craftResult;
	private Slot craftingResultSlot;
	private final List<IContainerListener> listeners = Lists.newArrayList();

	@Override
	public void addSlotListener(IContainerListener listener) {
		super.addSlotListener(listener);
		listeners.add(listener);
	}

	@Override
	public void removeSlotListener(IContainerListener listener) {
		super.removeSlotListener(listener);
		listeners.remove(listener);
	}

	public ContainerCraftingTerminal(int id, PlayerInventory inv, TileEntityCraftingTerminal te) {
		super(StorageMod.craftingTerminalCont, id, inv, te);
		craftMatrix = te.getCraftingInv();
		craftResult = te.getCraftResult();
		init();
		this.addPlayerSlots(inv, 8, 174);
		te.registerCrafting(this);
	}

	public ContainerCraftingTerminal(int id, PlayerInventory inv) {
		super(StorageMod.craftingTerminalCont, id, inv);
		craftMatrix = new CraftingInventory(this, 3, 3);
		craftResult = new CraftResultInventory();
		init();
		this.addPlayerSlots(inv, 8, 174);
	}

	@Override
	public void removed(PlayerEntity playerIn) {
		super.removed(playerIn);
		if(te != null)
			((TileEntityCraftingTerminal) te).unregisterCrafting(this);
	}

	private void init() {
		int x = -4;
		int y = 94;
		this.addSlot(craftingResultSlot = new CraftingResultSlot(pinv.player, craftMatrix, craftResult, 0, x + 124, y + 35) {
			@Override
			public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
				if (thePlayer.level.isClientSide)
					return ItemStack.EMPTY;
				this.checkTakeAchievements(stack);
				if (!pinv.player.getCommandSenderWorld().isClientSide) {
					((TileEntityCraftingTerminal) te).craft(thePlayer);
				}
				return ItemStack.EMPTY;
			}
		});

		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				this.addSlot(new SlotCrafting(craftMatrix, j + i * 3, x + 30 + j * 18, y + 17 + i * 18));
			}
		}
	}

	@Override
	protected void addStorageSlots() {
		addStorageSlots(5, 8, 18);
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn) {
		return slotIn.container != craftResult && super.canTakeItemForPickAll(stack, slotIn);
	}

	@Override
	public ItemStack shiftClickItems(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (index == 0) {
				if(te == null)return ItemStack.EMPTY;
				((TileEntityCraftingTerminal) te).craftShift(playerIn);
				if (!playerIn.level.isClientSide)
					broadcastChanges();
				return ItemStack.EMPTY;
			} else if (index > 0 && index < 10) {
				if(te == null)return ItemStack.EMPTY;
				ItemStack stack = ((TileEntityCraftingTerminal) te).pushStack(itemstack);
				slot.set(stack);
				if (!playerIn.level.isClientSide)
					broadcastChanges();
			}
			slot.onTake(playerIn, itemstack1);
		}
		return ItemStack.EMPTY;
	}

	public void onCraftMatrixChanged() {
		for (int i = 0; i < slots.size(); ++i) {
			Slot slot = slots.get(i);

			if (slot instanceof SlotCrafting || slot == craftingResultSlot) {
				for (IContainerListener listener : listeners) {
					if (listener instanceof ServerPlayerEntity) {
						((ServerPlayerEntity) listener).connection.send(new SSetSlotPacket(containerId, i, slot.getItem()));
					}
				}
			}
		}
	}

	@Override
	public boolean clickMenuButton(PlayerEntity playerIn, int id) {
		if(te != null && id == 0)
			((TileEntityCraftingTerminal) te).clear();
		else super.clickMenuButton(playerIn, id);
		return false;
	}

	@Override
	public void fillCraftSlotsStackedContents(RecipeItemHelper itemHelperIn) {
		this.craftMatrix.fillStackedContents(itemHelperIn);
	}

	@Override
	public void clearCraftingContent() {
		this.craftMatrix.clearContent();
		this.craftResult.clearContent();
	}

	@Override
	public boolean recipeMatches(IRecipe<? super CraftingInventory> recipeIn) {
		return recipeIn.matches(this.craftMatrix, this.pinv.player.level);
	}

	@Override
	public int getResultSlotIndex() {
		return 0;
	}

	@Override
	public int getGridWidth() {
		return this.craftMatrix.getWidth();
	}

	@Override
	public int getGridHeight() {
		return this.craftMatrix.getHeight();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getSize() {
		return 10;
	}

	@Override
	public java.util.List<net.minecraft.client.util.RecipeBookCategories> getRecipeBookCategories() {
		return Lists.newArrayList(RecipeBookCategories.CRAFTING_SEARCH, RecipeBookCategories.CRAFTING_EQUIPMENT, RecipeBookCategories.CRAFTING_BUILDING_BLOCKS, RecipeBookCategories.CRAFTING_MISC, RecipeBookCategories.CRAFTING_REDSTONE);
	}

	public class TerminalRecipeItemHelper extends RecipeItemHelper {
		@Override
		public void clear() {
			super.clear();
			itemList.forEach(e -> {
				accountSimpleStack(e.getActualStack());
			});
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void handlePlacement(boolean p_217056_1_, IRecipe<?> p_217056_2_, ServerPlayerEntity p_217056_3_) {
		(new ServerRecipePlacer(this) {
			{
				try {
					recipeItemHelperField.set(this, new TerminalRecipeItemHelper());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			protected void moveItemToGrid(Slot slotToFill, ItemStack ingredientIn) {
				int i = this.inventory.findSlotMatchingUnusedItem(ingredientIn);
				if (i != -1) {
					ItemStack itemstack = this.inventory.getItem(i).copy();
					if (!itemstack.isEmpty()) {
						if (itemstack.getCount() > 1) {
							this.inventory.removeItem(i, 1);
						} else {
							this.inventory.removeItemNoUpdate(i);
						}

						itemstack.setCount(1);
						if (slotToFill.getItem().isEmpty()) {
							slotToFill.set(itemstack);
						} else {
							slotToFill.getItem().grow(1);
						}

					}
				} else if(te != null) {
					StoredItemStack st = te.pullStack(new StoredItemStack(ingredientIn), 1);
					if(st != null) {
						if (slotToFill.getItem().isEmpty()) {
							slotToFill.set(st.getActualStack());
						} else {
							slotToFill.getItem().grow(1);
						}
					}
				}
			}

			@Override
			protected void moveItemToInventory(int slotIn) {
				ItemStack itemstack = this.menu.getSlot(slotIn).getItem();
				if (!itemstack.isEmpty()) {
					PlayerEntity player = inventory.player;
					InventoryHelper.dropItemStack(player.level, player.getX(), player.getY()-5, player.getZ(), itemstack);
					/*
					for(; itemstack.getCount() > 0; this.recipeBookContainer.getSlot(slotIn).decrStackSize(1)) {
						int i = this.playerInventory.storeItemStack(itemstack);
						if (i == -1) {
							i = this.playerInventory.getFirstEmptyStack();
						}

						ItemStack itemstack1 = itemstack.copy();
						itemstack1.setCount(1);
						if (!this.playerInventory.add(i, itemstack1)) {
							LOGGER.error("Can't find any space for item in the inventory");
						}
					}*/
				}
			}

			@Override
			protected void clearGrid() {
				((TileEntityCraftingTerminal) te).clear();
				this.menu.clearCraftingContent();
			}
		}).recipeClicked(p_217056_3_, p_217056_2_, p_217056_1_);
	}

	@Override
	public void receive(CompoundNBT message) {
		super.receive(message);
		if(message.contains("i")) {
			ItemStack[][] stacks = new ItemStack[9][];
			ListNBT list = message.getList("i", 10);
			for (int i = 0;i < list.size();i++) {
				CompoundNBT nbttagcompound = list.getCompound(i);
				byte slot = nbttagcompound.getByte("s");
				byte l = nbttagcompound.getByte("l");
				stacks[slot] = new ItemStack[l];
				for (int j = 0;j < l;j++) {
					CompoundNBT tag = nbttagcompound.getCompound("i" + j);
					stacks[slot][j] = ItemStack.of(tag);
				}
			}
			((TileEntityCraftingTerminal) te).handlerItemTransfer(pinv.player, stacks);
		}
	}
}
