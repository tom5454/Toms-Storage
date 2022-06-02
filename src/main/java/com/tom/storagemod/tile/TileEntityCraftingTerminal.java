package com.tom.storagemod.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.ItemHandlerHelper;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.gui.ContainerCraftingTerminal;

public class TileEntityCraftingTerminal extends TileEntityStorageTerminal {
	private AbstractContainerMenu craftingContainer = new AbstractContainerMenu(MenuType.CRAFTING, 0) {
		@Override
		public boolean stillValid(Player player) {
			return false;
		}

		@Override
		public void slotsChanged(Container inventory) {
			if (level != null && !level.isClientSide) {
				onCraftingMatrixChanged();
			}
		}
	};
	private CraftingRecipe currentRecipe;
	private final CraftingContainer craftMatrix = new CraftingContainer(craftingContainer, 3, 3);
	private ResultContainer craftResult = new ResultContainer();
	private HashSet<ContainerCraftingTerminal> craftingListeners = new HashSet<>();
	public TileEntityCraftingTerminal(BlockPos pos, BlockState state) {
		super(StorageMod.craftingTerminalTile, pos, state);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory plInv, Player arg2) {
		return new ContainerCraftingTerminal(id, plInv, this);
	}

	@Override
	public Component getDisplayName() {
		return new TranslatableComponent("ts.crafting_terminal");
	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);

		ListTag listnbt = new ListTag();

		for(int i = 0; i < craftMatrix.getContainerSize(); ++i) {
			ItemStack itemstack = craftMatrix.getItem(i);
			if (!itemstack.isEmpty()) {
				CompoundTag compoundnbt = new CompoundTag();
				compoundnbt.putByte("Slot", (byte)i);
				itemstack.save(compoundnbt);
				listnbt.add(compoundnbt);
			}
		}

		compound.put("CraftingTable", listnbt);
	}

	private boolean reading;
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		reading = true;
		ListTag listnbt = compound.getList("CraftingTable", 10);

		for(int i = 0; i < listnbt.size(); ++i) {
			CompoundTag compoundnbt = listnbt.getCompound(i);
			int j = compoundnbt.getByte("Slot") & 255;
			if (j >= 0 && j < craftMatrix.getContainerSize()) {
				craftMatrix.setItem(j, ItemStack.of(compoundnbt));
			}
		}
		reading = false;
	}

	public CraftingContainer getCraftingInv() {
		return craftMatrix;
	}

	public ResultContainer getCraftResult() {
		return craftResult;
	}

	public void craftShift(Player player) {
		List<ItemStack> craftedItemsList = new ArrayList<>();
		int amountCrafted = 0;
		ItemStack crafted = craftResult.getItem(0);
		do {
			craft(player);
			craftedItemsList.add(crafted.copy());
			amountCrafted += crafted.getCount();
		} while(ItemStack.isSame(crafted, craftResult.getItem(0)) && (amountCrafted+crafted.getCount()) <= crafted.getMaxStackSize());

		for (ItemStack craftedItem : craftedItemsList) {
			if (!player.getInventory().add(craftedItem.copy())) {
				ItemStack is = pushStack(craftedItem);
				if(!is.isEmpty()) {
					Containers.dropItemStack(level, player.getX(), player.getY(), player.getZ(), is);
				}
			}
		}

		crafted.onCraftedBy(player.level, player, amountCrafted);
		ForgeEventFactory.firePlayerCraftingEvent(player, ItemHandlerHelper.copyStackWithSize(crafted, amountCrafted), craftMatrix);
	}

	public void craft(Player thePlayer) {
		if(currentRecipe != null) {
			ForgeHooks.setCraftingPlayer(thePlayer);
			NonNullList<ItemStack> remainder = currentRecipe.getRemainingItems(craftMatrix);
			ForgeHooks.setCraftingPlayer(null);
			boolean playerInvUpdate = false;
			for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
				ItemStack slot = craftMatrix.getItem(i);
				if (i < remainder.size() && !remainder.get(i).isEmpty()) {
					if (!slot.isEmpty() && slot.getCount() > 1) {
						ItemStack is = pushStack(remainder.get(i).copy());
						if(!is.isEmpty()) {
							if(!thePlayer.getInventory().add(is)) {
								Containers.dropItemStack(level, thePlayer.getX(), thePlayer.getY(), thePlayer.getZ(), is);
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
							for(int j = 0;j<thePlayer.getInventory().getContainerSize();j++) {
								ItemStack st = thePlayer.getInventory().getItem(j);
								if(ItemStack.isSame(slot, st) && ItemStack.tagMatches(slot, st)) {
									st = thePlayer.getInventory().removeItem(j, 1);
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
			currentRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftMatrix, level).orElse(null);
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

	public void handlerItemTransfer(Player player, ItemStack[][] items) {
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
						for (int k = 0;k < player.getInventory().getContainerSize();k++) {
							if(ItemStack.isSame(player.getInventory().getItem(k), items[i][j])) {
								stack = player.getInventory().removeItem(k, 1);
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
