package com.tom.storagemod.gui;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import com.google.common.collect.Lists;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.jei.IJEIAutoFillTerminal;
import com.tom.storagemod.network.IDataReceiver;
import com.tom.storagemod.tile.TileEntityCraftingTerminal;

public class ContainerCraftingTerminal extends ContainerStorageTerminal implements IJEIAutoFillTerminal, IDataReceiver {
	public static class SlotCrafting extends Slot {
		public SlotCrafting(Container inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}
	}
	private static Field recipeItemHelperField;
	static {
		try {
			for (Field f : ServerPlaceRecipe.class.getDeclaredFields()) {
				if(f.getType() == StackedContents.class) {
					f.setAccessible(true);
					recipeItemHelperField = f;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final CraftingContainer craftMatrix;
	private final ResultContainer craftResult;
	private Slot craftingResultSlot;
	private final List<ContainerListener> listeners = Lists.newArrayList();

	@Override
	public void addSlotListener(ContainerListener listener) {
		super.addSlotListener(listener);
		listeners.add(listener);
	}

	@Override
	public void removeSlotListener(ContainerListener listener) {
		super.removeSlotListener(listener);
		listeners.remove(listener);
	}

	public ContainerCraftingTerminal(int id, Inventory inv, TileEntityCraftingTerminal te) {
		super(StorageMod.craftingTerminalCont.get(), id, inv, te);
		craftMatrix = te.getCraftingInv();
		craftResult = te.getCraftResult();
		init();
		this.addPlayerSlots(inv, 8, 174);
		te.registerCrafting(this);
	}

	public ContainerCraftingTerminal(int id, Inventory inv) {
		super(StorageMod.craftingTerminalCont.get(), id, inv);
		craftMatrix = new CraftingContainer(this, 3, 3);
		craftResult = new ResultContainer();
		init();
		this.addPlayerSlots(inv, 8, 174);
	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
		if(te != null)
			((TileEntityCraftingTerminal) te).unregisterCrafting(this);
	}

	private void init() {
		int x = -4;
		int y = 94;
		this.addSlot(craftingResultSlot = new ResultSlot(pinv.player, craftMatrix, craftResult, 0, x + 124, y + 35) {
			@Override
			public void onTake(Player thePlayer, ItemStack stack) {
				if (thePlayer.level.isClientSide)
					return;
				this.checkTakeAchievements(stack);
				if (!pinv.player.getCommandSenderWorld().isClientSide) {
					((TileEntityCraftingTerminal) te).craft(thePlayer);
				}
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
	public ItemStack shiftClickItems(Player playerIn, int index) {
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
				for (ContainerListener listener : listeners) {
					if (listener instanceof ServerPlayer) {
						((ServerPlayer) listener).connection.send(new ClientboundContainerSetSlotPacket(containerId, incrementStateId(), i, slot.getItem()));
					}
				}
			}
		}
	}

	@Override
	public boolean clickMenuButton(Player playerIn, int id) {
		if(te != null && id == 0)
			((TileEntityCraftingTerminal) te).clear();
		else super.clickMenuButton(playerIn, id);
		return false;
	}

	@Override
	public void fillCraftSlotsStackedContents(StackedContents itemHelperIn) {
		this.craftMatrix.fillStackedContents(itemHelperIn);
	}

	@Override
	public void clearCraftingContent() {
		this.craftMatrix.clearContent();
		this.craftResult.clearContent();
	}

	@Override
	public boolean recipeMatches(Recipe<? super CraftingContainer> recipeIn) {
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
	public int getSize() {
		return 10;
	}

	@Override
	public java.util.List<RecipeBookCategories> getRecipeBookCategories() {
		return Lists.newArrayList(RecipeBookCategories.CRAFTING_SEARCH, RecipeBookCategories.CRAFTING_EQUIPMENT, RecipeBookCategories.CRAFTING_BUILDING_BLOCKS, RecipeBookCategories.CRAFTING_MISC, RecipeBookCategories.CRAFTING_REDSTONE);
	}

	public class TerminalRecipeItemHelper extends StackedContents {
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
	public void handlePlacement(boolean p_217056_1_, Recipe<?> p_217056_2_, ServerPlayer p_217056_3_) {
		(new ServerPlaceRecipe(this) {
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
			protected void clearGrid(boolean bool) {
				((TileEntityCraftingTerminal) te).clear();
				this.menu.clearCraftingContent();
			}
		}).recipeClicked(p_217056_3_, p_217056_2_, p_217056_1_);
	}

	@Override
	public void receive(CompoundTag message) {
		super.receive(message);
		if(message.contains("i")) {
			ItemStack[][] stacks = new ItemStack[9][];
			ListTag list = message.getList("i", 10);
			for (int i = 0;i < list.size();i++) {
				CompoundTag nbttagcompound = list.getCompound(i);
				byte slot = nbttagcompound.getByte("s");
				byte l = nbttagcompound.getByte("l");
				stacks[slot] = new ItemStack[l];
				for (int j = 0;j < l;j++) {
					CompoundTag tag = nbttagcompound.getCompound("i" + j);
					stacks[slot][j] = ItemStack.of(tag);
				}
			}
			((TileEntityCraftingTerminal) te).handlerItemTransfer(pinv.player, stacks);
		}
	}

	@Override
	public List<StoredItemStack> getStoredItems() {
		return itemList;
	}
}
