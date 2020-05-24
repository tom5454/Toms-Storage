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
import net.minecraft.network.play.server.SSetSlotPacket;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.common.collect.Lists;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.tile.TileEntityCraftingTerminal;

public class ContainerCraftingTerminal extends ContainerStorageTerminal {
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

	private final PlayerEntity player;
	private final CraftingInventory craftMatrix;
	private final CraftResultInventory craftResult;
	private Slot craftingResultSlot;
	private final List<IContainerListener> listeners = Lists.newArrayList();

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		listeners.add(listener);
	}

	@Override
	public void removeListener(IContainerListener listener) {
		super.removeListener(listener);
		listeners.remove(listener);
	}

	public ContainerCraftingTerminal(int id, PlayerInventory inv, TileEntityCraftingTerminal te) {
		super(StorageMod.craftingTerminalCont, id, inv, te);
		this.player = inv.player;
		craftMatrix = te.getCraftingInv();
		craftResult = te.getCraftResult();
		init();
		this.addPlayerSlots(inv, 8, 174);
		te.registerCrafting(this);
	}

	public ContainerCraftingTerminal(int id, PlayerInventory inv) {
		super(StorageMod.craftingTerminalCont, id, inv);
		this.player = inv.player;
		craftMatrix = new CraftingInventory(this, 3, 3);
		craftResult = new CraftResultInventory();
		init();
		this.addPlayerSlots(inv, 8, 174);
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
		if(te != null)
			((TileEntityCraftingTerminal) te).unregisterCrafting(this);
	}

	private void init() {
		int x = -4;
		int y = 94;
		this.addSlot(craftingResultSlot = new CraftingResultSlot(player, craftMatrix, craftResult, 0, x + 124, y + 35) {
			@Override
			public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
				if (thePlayer.world.isRemote)
					return ItemStack.EMPTY;
				this.onCrafting(stack);
				if (!player.getEntityWorld().isRemote) {
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

		addSyncSlot();
	}

	@Override
	protected void addStorageSlots() {
		addStorageSlots(5, 8, 18);
	}

	@Override
	public boolean canMergeSlot(ItemStack stack, Slot slotIn) {
		return slotIn.inventory != craftResult && super.canMergeSlot(stack, slotIn);
	}

	@Override
	public ItemStack shiftClickItems(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index == 0) {
				if(te == null)return ItemStack.EMPTY;
				((TileEntityCraftingTerminal) te).craftShift(playerIn);
				if (!playerIn.world.isRemote)
					detectAndSendChanges();
				return ItemStack.EMPTY;
			} else if (index > 0 && index < 10) {
				if(te == null)return ItemStack.EMPTY;
				ItemStack stack = ((TileEntityCraftingTerminal) te).pushStack(itemstack);
				slot.putStack(stack);
				if (!playerIn.world.isRemote)
					detectAndSendChanges();
			}
			slot.onTake(playerIn, itemstack1);
		}
		return ItemStack.EMPTY;
	}

	public void onCraftMatrixChanged() {
		for (int i = 0; i < inventorySlots.size(); ++i) {
			Slot slot = inventorySlots.get(i);

			if (slot instanceof SlotCrafting || slot == craftingResultSlot) {
				for (IContainerListener listener : listeners) {
					if (listener instanceof ServerPlayerEntity) {
						((ServerPlayerEntity) listener).connection.sendPacket(new SSetSlotPacket(windowId, i, slot.getStack()));
					}
				}
			}
		}
	}

	@Override
	public boolean enchantItem(PlayerEntity playerIn, int id) {
		if(te != null)
			((TileEntityCraftingTerminal) te).clear();
		return false;
	}

	@Override
	public void fillStackedContents(RecipeItemHelper itemHelperIn) {
		this.craftMatrix.fillStackedContents(itemHelperIn);
	}

	@Override
	public void clear() {
		this.craftMatrix.clear();
		this.craftResult.clear();
	}

	@Override
	public boolean matches(IRecipe<? super CraftingInventory> recipeIn) {
		return recipeIn.matches(this.craftMatrix, this.player.world);
	}

	@Override
	public int getOutputSlot() {
		return 0;
	}

	@Override
	public int getWidth() {
		return this.craftMatrix.getWidth();
	}

	@Override
	public int getHeight() {
		return this.craftMatrix.getHeight();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getSize() {
		return 10;
	}

	@Override
	public java.util.List<net.minecraft.client.util.RecipeBookCategories> getRecipeBookCategories() {
		return Lists.newArrayList(RecipeBookCategories.SEARCH, RecipeBookCategories.EQUIPMENT, RecipeBookCategories.BUILDING_BLOCKS, RecipeBookCategories.MISC, RecipeBookCategories.REDSTONE);
	}

	public class TerminalRecipeItemHelper extends RecipeItemHelper {
		@Override
		public void clear() {
			super.clear();
			itemList.forEach(e -> {
				accountPlainStack(e.getActualStack());
			});
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void func_217056_a(boolean p_217056_1_, IRecipe<?> p_217056_2_, ServerPlayerEntity p_217056_3_) {
		(new ServerRecipePlacer(this) {
			{
				try {
					recipeItemHelperField.set(this, new TerminalRecipeItemHelper());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			protected void consumeIngredient(Slot slotToFill, ItemStack ingredientIn) {
				int i = this.playerInventory.findSlotMatchingUnusedItem(ingredientIn);
				if (i != -1) {
					ItemStack itemstack = this.playerInventory.getStackInSlot(i).copy();
					if (!itemstack.isEmpty()) {
						if (itemstack.getCount() > 1) {
							this.playerInventory.decrStackSize(i, 1);
						} else {
							this.playerInventory.removeStackFromSlot(i);
						}

						itemstack.setCount(1);
						if (slotToFill.getStack().isEmpty()) {
							slotToFill.putStack(itemstack);
						} else {
							slotToFill.getStack().grow(1);
						}

					}
				} else if(te != null) {
					StoredItemStack st = te.pullStack(new StoredItemStack(ingredientIn), 1);
					if(st != null) {
						if (slotToFill.getStack().isEmpty()) {
							slotToFill.putStack(st.getActualStack());
						} else {
							slotToFill.getStack().grow(1);
						}
					}
				}
			}

			@Override
			protected void giveToPlayer(int slotIn) {
				ItemStack itemstack = this.recipeBookContainer.getSlot(slotIn).getStack();
				if (!itemstack.isEmpty()) {
					PlayerEntity player = playerInventory.player;
					InventoryHelper.spawnItemStack(player.world, player.getPosX(), player.getPosY()-5, player.getPosZ(), itemstack);
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
			protected void clear() {
				((TileEntityCraftingTerminal) te).clear();
				this.recipeBookContainer.clear();
			}
		}).place(p_217056_3_, p_217056_2_, p_217056_1_);
	}
}
