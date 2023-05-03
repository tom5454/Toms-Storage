package com.tom.storagemod.gui;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.InputSlotFiller;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeMatcher;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;

import com.google.common.collect.Lists;

import com.tom.storagemod.NetworkHandler.IDataReceiver;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.rei.IREIAutoFillTerminal;
import com.tom.storagemod.tile.TileEntityCraftingTerminal;

public class ContainerCraftingTerminal extends ContainerStorageTerminal implements IDataReceiver, IREIAutoFillTerminal {
	public static class SlotCrafting extends Slot {
		public SlotCrafting(Inventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
		}
	}

	private final CraftingInventory craftMatrix;
	private final CraftingResultInventory craftResult;
	private Slot craftingResultSlot;
	private final List<ScreenHandlerListener> listeners = Lists.newArrayList();

	@Override
	public void addListener(ScreenHandlerListener listener) {
		super.addListener(listener);
		listeners.add(listener);
	}

	@Override
	public void removeListener(ScreenHandlerListener listener) {
		super.removeListener(listener);
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
		craftResult = new CraftingResultInventory();
		init();
		this.addPlayerSlots(inv, 8, 174);
	}

	@Override
	public void close(PlayerEntity playerIn) {
		super.close(playerIn);
		if(te != null)
			((TileEntityCraftingTerminal) te).unregisterCrafting(this);
	}

	private void init() {
		int x = -4;
		int y = 94;
		this.addSlot(craftingResultSlot = new Result(x + 124, y + 35));

		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				this.addSlot(new SlotCrafting(craftMatrix, j + i * 3, x + 30 + j * 18, y + 17 + i * 18));
			}
		}
	}

	private class Result extends CraftingResultSlot {

		public Result(int x, int y) {
			super(pinv.player, craftMatrix, craftResult, 0, x, y);
		}

		@Override
		public void onTakeItem(PlayerEntity thePlayer, ItemStack stack) {
			this.onCrafted(stack);
			if (!pinv.player.getEntityWorld().isClient) {
				((TileEntityCraftingTerminal) te).craft(thePlayer);
			}
		}
	}

	@Override
	protected void addStorageSlots() {
		addStorageSlots(5, 8, 18);
	}

	@Override
	public boolean canInsertIntoSlot(ItemStack stack, Slot slotIn) {
		return slotIn.inventory != craftResult && super.canInsertIntoSlot(stack, slotIn);
	}

	@Override
	public ItemStack shiftClickItems(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index == 0) {
				if(te == null)return ItemStack.EMPTY;
				if (!((TileEntityCraftingTerminal)te).canCraft() || !this.insertItem(itemstack1, 10, 46, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickTransfer(itemstack1, itemstack);

				if (itemstack1.isEmpty()) {
					slot.setStack(ItemStack.EMPTY);
				} else {
					slot.markDirty();
				}

				if (itemstack1.getCount() == itemstack.getCount()) {
					return ItemStack.EMPTY;
				}

				slot.onTakeItem(playerIn, itemstack1);
				if (index == 0) {
					playerIn.dropItem(itemstack1, false);
				}

				return itemstack;
			} else if (index > 0 && index < 10) {
				if(te == null)return ItemStack.EMPTY;
				ItemStack stack = ((TileEntityCraftingTerminal) te).pushStack(itemstack);
				slot.setStack(stack);
				if (!playerIn.world.isClient)
					sendContentUpdates();
			}
			slot.onTakeItem(playerIn, itemstack1);
		}
		return ItemStack.EMPTY;
	}

	public void onCraftMatrixChanged() {
		for (int i = 0; i < slots.size(); ++i) {
			Slot slot = slots.get(i);

			if (slot instanceof SlotCrafting || slot == craftingResultSlot) {
				for (ScreenHandlerListener listener : listeners) {
					if (listener instanceof ServerPlayerEntity) {
						((ServerPlayerEntity) listener).networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, nextRevision(), i, slot.getStack()));
					}
				}
			}
		}
	}

	@Override
	public boolean onButtonClick(PlayerEntity playerIn, int id) {
		if(te != null && id == 0)
			((TileEntityCraftingTerminal) te).clear();
		else super.onButtonClick(playerIn, id);
		return false;
	}

	@Override
	public void populateRecipeFinder(RecipeMatcher itemHelperIn) {
		this.craftMatrix.provideRecipeInputs(itemHelperIn);
		if(te != null)sync.fillStackedContents(itemHelperIn);
		else itemList.forEach(e -> {
			itemHelperIn.addUnenchantedInput(e.getActualStack());
		});
	}

	@Override
	public void clearCraftingSlots() {
		this.craftMatrix.clear();
		this.craftResult.clear();
	}

	@Override
	public boolean matches(Recipe<? super CraftingInventory> recipeIn) {
		return recipeIn.matches(this.craftMatrix, this.pinv.player.world);
	}

	@Override
	public int getCraftingResultSlotIndex() {
		return 0;
	}

	@Override
	public int getCraftingWidth() {
		return this.craftMatrix.getWidth();
	}

	@Override
	public int getCraftingHeight() {
		return this.craftMatrix.getHeight();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public int getCraftingSlotCount() {
		return 10;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void fillInputSlots(boolean p_217056_1_, Recipe<?> p_217056_2_, ServerPlayerEntity p_217056_3_) {
		(new InputSlotFiller(this) {

			@Override
			protected void fillInputSlot(Slot slotToFill, ItemStack ingredientIn) {
				int i = this.inventory.indexOf(ingredientIn);
				if (i != -1) {
					ItemStack itemstack = this.inventory.getStack(i).copy();
					if (!itemstack.isEmpty()) {
						if (itemstack.getCount() > 1) {
							this.inventory.removeStack(i, 1);
						} else {
							this.inventory.removeStack(i);
						}

						itemstack.setCount(1);
						if (slotToFill.getStack().isEmpty()) {
							slotToFill.setStack(itemstack);
						} else {
							slotToFill.getStack().increment(1);
						}

					}
				} else if(te != null) {
					StoredItemStack st = te.pullStack(new StoredItemStack(ingredientIn), 1);
					if(st != null) {
						if (slotToFill.getStack().isEmpty()) {
							slotToFill.setStack(st.getActualStack());
						} else {
							slotToFill.getStack().increment(1);
						}
					}
				}
			}

			@Override
			protected void returnInputs(boolean bool) {
				((TileEntityCraftingTerminal) te).clear();
				clearCraftingSlots();
			}
		}).fillInputSlots(p_217056_3_, p_217056_2_, p_217056_1_);
	}

	@Override
	public void receive(NbtCompound message) {
		super.receive(message);
		if(message.contains("i")) {
			ItemStack[][] stacks = new ItemStack[9][];
			NbtList list = message.getList("i", 10);
			for (int i = 0;i < list.size();i++) {
				NbtCompound nbttagcompound = list.getCompound(i);
				byte slot = nbttagcompound.getByte("s");
				byte l = nbttagcompound.getByte("l");
				stacks[slot] = new ItemStack[l];
				for (int j = 0;j < l;j++) {
					NbtCompound tag = nbttagcompound.getCompound("i" + j);
					stacks[slot][j] = ItemStack.fromNbt(tag);
				}
			}
			((TileEntityCraftingTerminal) te).handlerItemTransfer(pinv.player, stacks);
		}
	}

	@Override
	public boolean canInsertIntoSlot(int id) {
		return id > 0 && id < 10;
	}

	@Override
	public List<StoredItemStack> getStoredItems() {
		return itemList;
	}
}
