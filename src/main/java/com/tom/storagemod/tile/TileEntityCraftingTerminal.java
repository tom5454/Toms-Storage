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
		public boolean stillValid(PlayerEntity player) {
			return false;
		}

		@Override
		public void slotsChanged(IInventory inventory) {
			if (level != null && !level.isClientSide) {
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
	public CompoundNBT save(CompoundNBT compound) {
		ListNBT listnbt = new ListNBT();

		for(int i = 0; i < craftMatrix.getContainerSize(); ++i) {
			ItemStack itemstack = craftMatrix.getItem(i);
			if (!itemstack.isEmpty()) {
				CompoundNBT compoundnbt = new CompoundNBT();
				compoundnbt.putByte("Slot", (byte)i);
				itemstack.save(compoundnbt);
				listnbt.add(compoundnbt);
			}
		}

		compound.put("CraftingTable", listnbt);
		return super.save(compound);
	}
	private boolean reading;
	@Override
	public void load(BlockState st, CompoundNBT compound) {
		super.load(st, compound);
		reading = true;
		ListNBT listnbt = compound.getList("CraftingTable", 10);

		for(int i = 0; i < listnbt.size(); ++i) {
			CompoundNBT compoundnbt = listnbt.getCompound(i);
			int j = compoundnbt.getByte("Slot") & 255;
			if (j >= 0 && j < craftMatrix.getContainerSize()) {
				craftMatrix.setItem(j, ItemStack.of(compoundnbt));
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
		ItemStack crafted = craftResult.getItem(0);
		do {
			craft(player);
			craftedItemsList.add(crafted.copy());
			amountCrafted += crafted.getCount();
		} while(ItemStack.isSame(crafted, craftResult.getItem(0)) && (amountCrafted+crafted.getCount()) < crafted.getMaxStackSize());

		for (ItemStack craftedItem : craftedItemsList) {
			if (!player.inventory.add(craftedItem.copy())) {
				ItemStack is = pushStack(craftedItem);
				if(!is.isEmpty()) {
					InventoryHelper.dropItemStack(level, player.getX(), player.getY(), player.getZ(), is);
				}
			}
		}

		crafted.onCraftedBy(player.level, player, amountCrafted);
		BasicEventHooks.firePlayerCraftingEvent(player, ItemHandlerHelper.copyStackWithSize(crafted, amountCrafted), craftMatrix);
	}

	public void craft(PlayerEntity thePlayer) {
		if(currentRecipe != null) {
			NonNullList<ItemStack> remainder = currentRecipe.getRemainingItems(craftMatrix);
			boolean playerInvUpdate = false;
			for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
				ItemStack slot = craftMatrix.getItem(i);
				if (i < remainder.size() && !remainder.get(i).isEmpty()) {
					if (!slot.isEmpty() && slot.getCount() > 1) {
						ItemStack is = pushStack(remainder.get(i).copy());
						if(!is.isEmpty()) {
							if(!thePlayer.inventory.add(is)) {
								InventoryHelper.dropItemStack(level, thePlayer.getX(), thePlayer.getY(), thePlayer.getZ(), is);
							}
						}
						craftMatrix.removeItem(i, 1);
					} else {
						craftMatrix.setItem(i, remainder.get(i).copy());
					}
				} else if (!slot.isEmpty()) {
					if (slot.getCount() == 1) {
						StoredItemStack is = pullStack(new StoredItemStack(slot), 1);
						if(is == null && (getSorting() & (1 << 8)) != 0) {
							for(int j = 0;j<thePlayer.inventory.getContainerSize();j++) {
								ItemStack st = thePlayer.inventory.getItem(j);
								if(ItemStack.isSame(slot, st) && ItemStack.tagMatches(slot, st)) {
									st = thePlayer.inventory.removeItem(j, 1);
									if(!st.isEmpty()) {
										is = new StoredItemStack(st, 1);
										playerInvUpdate = true;
										break;
									}
								}
							}
						}
						if(is == null)craftMatrix.setItem(i, ItemStack.EMPTY);
						else craftMatrix.setItem(i, is.getActualStack());
					} else {
						craftMatrix.removeItem(i, 1);
					}
				}
			}
			if(playerInvUpdate)thePlayer.containerMenu.broadcastChanges();
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
		if (currentRecipe == null || !currentRecipe.matches(craftMatrix, level)) {
			currentRecipe = level.getRecipeManager().getRecipeFor(IRecipeType.CRAFTING, craftMatrix, level).orElse(null);
		}

		if (currentRecipe == null) {
			craftResult.setItem(0, ItemStack.EMPTY);
		} else {
			craftResult.setItem(0, currentRecipe.assemble(craftMatrix));
		}

		craftingListeners.forEach(ContainerCraftingTerminal::onCraftMatrixChanged);

		if (!reading) {
			setChanged();
		}
	}

	public void clear() {
		for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
			ItemStack st = craftMatrix.removeItemNoUpdate(i);
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
						for (int k = 0;k < player.inventory.getContainerSize();k++) {
							if(ItemStack.isSame(player.inventory.getItem(k), items[i][j])) {
								stack = player.inventory.removeItem(k, 1);
								br = true;
								break;
							}
						}
						if (br)
							break;
					}
				}
				if (!stack.isEmpty()) {
					craftMatrix.setItem(i, stack);
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
