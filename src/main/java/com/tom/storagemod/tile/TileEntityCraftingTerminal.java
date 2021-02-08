package com.tom.storagemod.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.gui.ContainerCraftingTerminal;

public class TileEntityCraftingTerminal extends TileEntityStorageTerminal {
	private ScreenHandler craftingContainer = new ScreenHandler(ScreenHandlerType.CRAFTING, 0) {

		@Override
		public void onContentChanged(Inventory inventory) {
			if (world != null && !world.isClient) {
				onCraftingMatrixChanged();
			}
		}

		@Override
		public boolean canUse(PlayerEntity paramPlayerEntity) {
			return false;
		}
	};
	private CraftingRecipe currentRecipe;
	private final CraftingInventory craftMatrix = new CraftingInventory(craftingContainer, 3, 3);
	private CraftingResultInventory craftResult = new CraftingResultInventory();
	private HashSet<ContainerCraftingTerminal> craftingListeners = new HashSet<>();
	public TileEntityCraftingTerminal(BlockPos pos, BlockState state) {
		super(StorageMod.craftingTerminalTile, pos, state);
	}

	@Override
	public ScreenHandler createMenu(int id, PlayerInventory plInv, PlayerEntity arg2) {
		return new ContainerCraftingTerminal(id, plInv, this);
	}

	@Override
	public Text getDisplayName() {
		return new TranslatableText("ts.crafting_terminal");
	}

	@Override
	public CompoundTag toTag(CompoundTag compound) {
		ListTag listnbt = new ListTag();

		for(int i = 0; i < craftMatrix.size(); ++i) {
			ItemStack itemstack = craftMatrix.getStack(i);
			if (!itemstack.isEmpty()) {
				CompoundTag CompoundTag = new CompoundTag();
				CompoundTag.putByte("Slot", (byte)i);
				itemstack.toTag(CompoundTag);
				listnbt.add(CompoundTag);
			}
		}

		compound.put("CraftingTable", listnbt);
		return super.toTag(compound);
	}
	private boolean reading;
	@Override
	public void fromTag(CompoundTag compound) {
		super.fromTag(compound);
		reading = true;
		ListTag listnbt = compound.getList("CraftingTable", 10);

		for(int i = 0; i < listnbt.size(); ++i) {
			CompoundTag CompoundTag = listnbt.getCompound(i);
			int j = CompoundTag.getByte("Slot") & 255;
			if (j >= 0 && j < craftMatrix.size()) {
				craftMatrix.setStack(j, ItemStack.fromTag(CompoundTag));
			}
		}
		reading = false;
	}

	public CraftingInventory getCraftingInv() {
		return craftMatrix;
	}

	public CraftingResultInventory getCraftResult() {
		return craftResult;
	}

	public void craftShift(PlayerEntity player) {
		List<ItemStack> craftedItemsList = new ArrayList<>();
		int amountCrafted = 0;
		ItemStack crafted = craftResult.getStack(0);
		do {
			craft(player);
			craftedItemsList.add(crafted.copy());
			amountCrafted += crafted.getCount();
		} while(ItemStack.areItemsEqual(crafted, craftResult.getStack(0)) && (amountCrafted+crafted.getCount()) < crafted.getMaxCount());

		for (ItemStack craftedItem : craftedItemsList) {
			if (!player.getInventory().insertStack(craftedItem.copy())) {
				ItemStack is = pushStack(craftedItem);
				if(!is.isEmpty()) {
					ItemScatterer.spawn(world, player.getX(), player.getY(), player.getZ(), is);
				}
			}
		}

		crafted.onCraft(player.world, player, amountCrafted);
		//BasicEventHooks.firePlayerCraftingEvent(player, ItemHandlerHelper.copyStackWithSize(crafted, amountCrafted), craftMatrix);
	}

	public void craft(PlayerEntity thePlayer) {
		if(currentRecipe != null) {
			DefaultedList<ItemStack> remainder = currentRecipe.getRemainingStacks(craftMatrix);
			boolean playerInvUpdate = false;
			for (int i = 0; i < craftMatrix.size(); i++) {
				ItemStack slot = craftMatrix.getStack(i);
				if (i < remainder.size() && !remainder.get(i).isEmpty()) {
					if (!slot.isEmpty() && slot.getCount() > 1) {
						ItemStack is = pushStack(remainder.get(i).copy());
						if(!is.isEmpty()) {
							if(!thePlayer.getInventory().insertStack(is)) {
								ItemScatterer.spawn(world, thePlayer.getX(), thePlayer.getY(), thePlayer.getZ(), is);
							}
						}
						craftMatrix.removeStack(i, 1);
					} else {
						craftMatrix.setStack(i, remainder.get(i).copy());
					}
				} else if (!slot.isEmpty()) {
					if (slot.getCount() == 1) {
						StoredItemStack is = pullStack(new StoredItemStack(slot), 1);
						if(is == null && (getSorting() & (1 << 8)) != 0) {
							for(int j = 0;j<thePlayer.getInventory().size();j++) {
								ItemStack st = thePlayer.getInventory().getStack(j);
								if(ItemStack.areItemsEqual(slot, st) && ItemStack.areTagsEqual(slot, st)) {
									st = thePlayer.getInventory().removeStack(j, 1);
									if(!st.isEmpty()) {
										is = new StoredItemStack(st, 1);
										playerInvUpdate = true;
										break;
									}
								}
							}
						}
						if(is == null)craftMatrix.setStack(i, ItemStack.EMPTY);
						else craftMatrix.setStack(i, is.getActualStack());
					} else {
						craftMatrix.removeStack(i, 1);
					}
				}
			}
			if(playerInvUpdate)thePlayer.currentScreenHandler.sendContentUpdates();
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
			currentRecipe = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftMatrix, world).orElse(null);
		}

		if (currentRecipe == null) {
			craftResult.setStack(0, ItemStack.EMPTY);
		} else {
			craftResult.setStack(0, currentRecipe.craft(craftMatrix));
		}

		craftingListeners.forEach(ContainerCraftingTerminal::onCraftMatrixChanged);

		if (!reading) {
			markDirty();
		}
	}

	public void clear() {
		for (int i = 0; i < craftMatrix.size(); i++) {
			ItemStack st = craftMatrix.removeStack(i);
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
						for (int k = 0;k < player.getInventory().size();k++) {
							if(ItemStack.areItemsEqual(player.getInventory().getStack(k), items[i][j])) {
								stack = player.getInventory().removeStack(k, 1);
								br = true;
								break;
							}
						}
						if (br)
							break;
					}
				}
				if (!stack.isEmpty()) {
					craftMatrix.setStack(i, stack);
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
