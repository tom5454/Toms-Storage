package com.tom.storagemod.tile;

import java.util.HashSet;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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
	private boolean refillingGrid;
	private int craftingCooldown;

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
	public void writeNbt(NbtCompound compound) {
		super.writeNbt(compound);
		NbtList listnbt = new NbtList();

		for(int i = 0; i < craftMatrix.size(); ++i) {
			ItemStack itemstack = craftMatrix.getStack(i);
			if (!itemstack.isEmpty()) {
				NbtCompound tag = new NbtCompound();
				tag.putByte("Slot", (byte)i);
				itemstack.writeNbt(tag);
				listnbt.add(tag);
			}
		}

		compound.put("CraftingTable", listnbt);
	}
	private boolean reading;
	@Override
	public void readNbt(NbtCompound compound) {
		super.readNbt(compound);
		reading = true;
		NbtList listnbt = compound.getList("CraftingTable", 10);

		for(int i = 0; i < listnbt.size(); ++i) {
			NbtCompound tag = listnbt.getCompound(i);
			int j = tag.getByte("Slot") & 255;
			if (j >= 0 && j < craftMatrix.size()) {
				craftMatrix.setStack(j, ItemStack.fromNbt(tag));
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

	public void craft(PlayerEntity thePlayer) {
		if(currentRecipe != null) {
			DefaultedList<ItemStack> remainder = currentRecipe.getRemainder(craftMatrix);
			boolean playerInvUpdate = false;
			refillingGrid = true;

			for (int i = 0; i < remainder.size(); ++i) {
				ItemStack slot = craftMatrix.getStack(i);
				ItemStack oldItem = slot.copy();
				ItemStack rem = remainder.get(i);
				if (!slot.isEmpty()) {
					craftMatrix.removeStack(i, 1);
					slot = craftMatrix.getStack(i);
				}
				if(slot.isEmpty() && !oldItem.isEmpty()) {
					StoredItemStack is = pullStack(new StoredItemStack(oldItem), 1);
					if(is == null && (getSorting() & (1 << 8)) != 0) {
						for(int j = 0;j<thePlayer.getInventory().size();j++) {
							ItemStack st = thePlayer.getInventory().getStack(j);
							if(ItemStack.areItemsEqual(oldItem, st) && ItemStack.areNbtEqual(oldItem, st)) {
								st = thePlayer.getInventory().removeStack(j, 1);
								if(!st.isEmpty()) {
									is = new StoredItemStack(st, 1);
									playerInvUpdate = true;
									break;
								}
							}
						}
					}
					if(is != null) {
						craftMatrix.setStack(i, is.getActualStack());
						slot = craftMatrix.getStack(i);
					}
				}
				if (rem.isEmpty()) {
					continue;
				}
				if (slot.isEmpty()) {
					craftMatrix.setStack(i, rem);
					continue;
				}
				if (ItemStack.areItemsEqualIgnoreDamage(slot, rem) && ItemStack.areNbtEqual(slot, rem)) {
					rem.increment(slot.getCount());
					craftMatrix.setStack(i, rem);
					continue;
				}
				rem = pushStack(rem);
				if(rem.isEmpty())continue;
				if (thePlayer.getInventory().insertStack(rem)) continue;
				thePlayer.dropItem(rem, false);
			}
			refillingGrid = false;
			onCraftingMatrixChanged();
			craftingCooldown += craftResult.getStack(0).getCount();
			if(playerInvUpdate)thePlayer.currentScreenHandler.sendContentUpdates();
		}
	}

	public void unregisterCrafting(ContainerCraftingTerminal containerCraftingTerminal) {
		craftingListeners.remove(containerCraftingTerminal);
	}

	public void registerCrafting(ContainerCraftingTerminal containerCraftingTerminal) {
		craftingListeners.add(containerCraftingTerminal);
	}

	protected void onCraftingMatrixChanged() {
		if(refillingGrid)return;
		if (currentRecipe == null || !currentRecipe.matches(craftMatrix, world)) {
			currentRecipe = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, craftMatrix, world).orElse(null);
		}

		if (currentRecipe == null) {
			craftResult.setStack(0, ItemStack.EMPTY);
		} else {
			craftResult.setStack(0, currentRecipe.craft(craftMatrix));
		}

		craftingListeners.forEach(ContainerCraftingTerminal::onCraftMatrixChanged);
		craftResult.setLastRecipe(currentRecipe);

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

	@Override
	public void updateServer() {
		super.updateServer();
		craftingCooldown = 0;
	}

	public boolean canCraft() {
		return craftingCooldown + craftResult.getStack(0).getCount() <= craftResult.getStack(0).getMaxCount();
	}
}
