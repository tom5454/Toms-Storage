package com.tom.storagemod.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraftforge.fml.hooks.BasicEventHooks;
import net.minecraftforge.items.ItemHandlerHelper;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.gui.ContainerCraftingTerminal;

public class TileEntityCraftingTerminal extends TileEntityStorageTerminal {
	private Container craftingContainer = new Container(ContainerType.CRAFTING, 0) {
		@Override
		public boolean canInteractWith(PlayerEntity player) {
			return false;
		}

		@Override
		public void onCraftMatrixChanged(IInventory inventory) {
			if (world != null && !world.isRemote) {
				onCraftingMatrixChanged();
			}
		}
	};
	private ICraftingRecipe currentRecipe;
	private final CraftingInventory craftMatrix = new CraftingInventory(craftingContainer, 3, 3);
	private CraftResultInventory craftResult = new CraftResultInventory();
	private HashSet<ContainerCraftingTerminal> craftingListeners = new HashSet<>();
	public TileEntityCraftingTerminal() {
		super(StorageMod.craftingTerminalTile);
	}

	@Override
	public Container createMenu(int id, PlayerInventory plInv, PlayerEntity arg2) {
		return new ContainerCraftingTerminal(id, plInv, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("ts.crafting_terminal");
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		ListNBT listnbt = new ListNBT();

		for(int i = 0; i < craftMatrix.getSizeInventory(); ++i) {
			ItemStack itemstack = craftMatrix.getStackInSlot(i);
			if (!itemstack.isEmpty()) {
				CompoundNBT compoundnbt = new CompoundNBT();
				compoundnbt.putByte("Slot", (byte)i);
				itemstack.write(compoundnbt);
				listnbt.add(compoundnbt);
			}
		}

		compound.put("CraftingTable", listnbt);
		return super.write(compound);
	}
	private boolean reading;
	@Override
	public void read(BlockState st, CompoundNBT compound) {
		super.read(st, compound);
		reading = true;
		ListNBT listnbt = compound.getList("CraftingTable", 10);

		for(int i = 0; i < listnbt.size(); ++i) {
			CompoundNBT compoundnbt = listnbt.getCompound(i);
			int j = compoundnbt.getByte("Slot") & 255;
			if (j >= 0 && j < craftMatrix.getSizeInventory()) {
				craftMatrix.setInventorySlotContents(j, ItemStack.read(compoundnbt));
			}
		}
		reading = false;
	}

	public CraftingInventory getCraftingInv() {
		return craftMatrix;
	}

	public CraftResultInventory getCraftResult() {
		return craftResult;
	}

	public void craftShift(PlayerEntity player) {
		List<ItemStack> craftedItemsList = new ArrayList<>();
		int amountCrafted = 0;
		ItemStack crafted = craftResult.getStackInSlot(0);
		do {
			craft(player);
			craftedItemsList.add(crafted.copy());
			amountCrafted += crafted.getCount();
		} while(ItemStack.areItemsEqual(crafted, craftResult.getStackInSlot(0)) && (amountCrafted+crafted.getCount()) < crafted.getMaxStackSize());

		for (ItemStack craftedItem : craftedItemsList) {
			if (!player.inventory.addItemStackToInventory(craftedItem.copy())) {
				ItemStack is = pushStack(craftedItem);
				if(!is.isEmpty()) {
					InventoryHelper.spawnItemStack(world, player.getPosX(), player.getPosY(), player.getPosZ(), is);
				}
			}
		}

		crafted.onCrafting(player.world, player, amountCrafted);
		BasicEventHooks.firePlayerCraftingEvent(player, ItemHandlerHelper.copyStackWithSize(crafted, amountCrafted), craftMatrix);
	}

	public void craft(PlayerEntity thePlayer) {
		if(currentRecipe != null) {
			NonNullList<ItemStack> remainder = currentRecipe.getRemainingItems(craftMatrix);
			boolean playerInvUpdate = false;
			for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
				ItemStack slot = craftMatrix.getStackInSlot(i);
				if (i < remainder.size() && !remainder.get(i).isEmpty()) {
					if (!slot.isEmpty() && slot.getCount() > 1) {
						ItemStack is = pushStack(remainder.get(i).copy());
						if(!is.isEmpty()) {
							if(!thePlayer.inventory.addItemStackToInventory(is)) {
								InventoryHelper.spawnItemStack(world, thePlayer.getPosX(), thePlayer.getPosY(), thePlayer.getPosZ(), is);
							}
						}
						craftMatrix.decrStackSize(i, 1);
					} else {
						craftMatrix.setInventorySlotContents(i, remainder.get(i).copy());
					}
				} else if (!slot.isEmpty()) {
					if (slot.getCount() == 1) {
						StoredItemStack is = pullStack(new StoredItemStack(slot), 1);
						if(is == null && (getSorting() & (1 << 8)) != 0) {
							for(int j = 0;j<thePlayer.inventory.getSizeInventory();j++) {
								ItemStack st = thePlayer.inventory.getStackInSlot(j);
								if(ItemStack.areItemsEqual(slot, st) && ItemStack.areItemStackTagsEqual(slot, st)) {
									st = thePlayer.inventory.decrStackSize(j, 1);
									if(!st.isEmpty()) {
										is = new StoredItemStack(st, 1);
										playerInvUpdate = true;
										break;
									}
								}
							}
						}
						if(is == null)craftMatrix.setInventorySlotContents(i, ItemStack.EMPTY);
						else craftMatrix.setInventorySlotContents(i, is.getActualStack());
					} else {
						craftMatrix.decrStackSize(i, 1);
					}
				}
			}
			if(playerInvUpdate)thePlayer.openContainer.detectAndSendChanges();
			onCraftingMatrixChanged();
		}
	}

	public void unregisterCrafting(ContainerCraftingTerminal containerCraftingTerminal) {
		craftingListeners.remove(containerCraftingTerminal);
	}

	public void registerCrafting(ContainerCraftingTerminal containerCraftingTerminal) {
		craftingListeners.add(containerCraftingTerminal);
	}

	protected void onCraftingMatrixChanged() {
		if (currentRecipe == null || !currentRecipe.matches(craftMatrix, world)) {
			currentRecipe = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftMatrix, world).orElse(null);
		}

		if (currentRecipe == null) {
			craftResult.setInventorySlotContents(0, ItemStack.EMPTY);
		} else {
			craftResult.setInventorySlotContents(0, currentRecipe.getCraftingResult(craftMatrix));
		}

		craftingListeners.forEach(ContainerCraftingTerminal::onCraftMatrixChanged);

		if (!reading) {
			markDirty();
		}
	}

	public void clear() {
		for (int i = 0; i < craftMatrix.getSizeInventory(); i++) {
			ItemStack st = craftMatrix.removeStackFromSlot(i);
			if(!st.isEmpty()) {
				pushOrDrop(st);
			}
		}
		onCraftingMatrixChanged();
	}

	public void handlerItemTransfer(PlayerEntity player, ItemStack[][] items) {
		clear();
		for (int i = 0;i < 9;i++) {
			if (items[i] != null) {
				ItemStack stack = ItemStack.EMPTY;
				for (int j = 0;j < items[i].length;j++) {
					ItemStack pulled = pullStack(items[i][j]);
					if (!pulled.isEmpty()) {
						stack = pulled;
						break;
					}
				}
				if (stack.isEmpty()) {
					for (int j = 0;j < items[i].length;j++) {
						boolean br = false;
						for (int k = 0;k < player.inventory.getSizeInventory();k++) {
							if(ItemStack.areItemsEqual(player.inventory.getStackInSlot(k), items[i][j])) {
								stack = player.inventory.decrStackSize(k, 1);
								br = true;
								break;
							}
						}
						if (br)
							break;
					}
				}
				if (!stack.isEmpty()) {
					craftMatrix.setInventorySlotContents(i, stack);
				}
			}
		}
		onCraftingMatrixChanged();
	}

	private ItemStack pullStack(ItemStack itemStack) {
		StoredItemStack is = pullStack(new StoredItemStack(itemStack), 1);
		if(is == null)return ItemStack.EMPTY;
		else return is.getActualStack();
	}
}
