package com.tom.storagemod.block.entity;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.tom.storagemod.Content;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.menu.CraftingTerminalMenu;
import com.tom.storagemod.polymorph.PolymorphHelper;
import com.tom.storagemod.util.CraftingMatrix;
import com.tom.storagemod.util.Util;

public class CraftingTerminalBlockEntity extends StorageTerminalBlockEntity {
	private Optional<RecipeHolder<CraftingRecipe>> currentRecipe = Optional.empty();
	private final CraftingContainer craftMatrix = new CraftingMatrix(3, 3, () -> {
		if (level != null && !level.isClientSide) {
			onCraftingMatrixChanged();
		}
		setChanged();
	});
	private ResultContainer craftResult = new ResultContainer();
	private HashSet<CraftingTerminalMenu> craftingListeners = new HashSet<>();
	private boolean refillingGrid;
	private int craftingCooldown;
	private WeakReference<Player> polymorphPlayer;

	public CraftingTerminalBlockEntity(BlockPos pos, BlockState state) {
		super(Content.craftingTerminalBE.get(), pos, state);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory plInv, Player arg2) {
		return new CraftingTerminalMenu(id, plInv, this);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("menu.toms_storage.crafting_terminal");
	}

	@Override
	public void saveAdditional(ValueOutput compound) {
		super.saveAdditional(compound);
		Util.storeItems(craftMatrix, "CraftingTable", compound);
	}

	private boolean reading;
	@Override
	public void loadAdditional(ValueInput compound) {
		super.loadAdditional(compound);
		reading = true;
		Util.loadItems(craftMatrix, "CraftingTable", compound);
		reading = false;
	}

	public CraftingContainer getCraftingInv() {
		return craftMatrix;
	}

	public ResultContainer getCraftResult() {
		return craftResult;
	}

	public void craft(Player thePlayer) {
		if(currentRecipe.isPresent()) {
			CraftingInput.Positioned craftinginput$positioned = this.craftMatrix.asPositionedCraftInput();
			CraftingInput craftinginput = craftinginput$positioned.input();
			NonNullList<ItemStack> remainder = currentRecipe.get().value().getRemainingItems(craftinginput);
			boolean playerInvUpdate = false;
			refillingGrid = true;
			int x = craftinginput$positioned.left();
			int y = craftinginput$positioned.top();
			for (int k = 0; k < craftinginput.height(); k++) {
				for (int l = 0; l < craftinginput.width(); l++) {
					int i = l + x + (k + y) * this.craftMatrix.getWidth();
					ItemStack slot = this.craftMatrix.getItem(i);
					ItemStack oldItem = slot.copy();
					ItemStack rem = remainder.get(l + k * craftinginput.width());
					if (!slot.isEmpty()) {
						craftMatrix.removeItem(i, 1);
						slot = craftMatrix.getItem(i);
					}
					if(slot.isEmpty() && !oldItem.isEmpty()) {
						StoredItemStack is = pullStack(new StoredItemStack(oldItem), 1);
						if(is == null && (getModes() & 0x20) != 0) {
							for(int j = 0;j<thePlayer.getInventory().getContainerSize();j++) {
								ItemStack st = thePlayer.getInventory().getItem(j);
								if(ItemStack.isSameItemSameComponents(oldItem, st)) {
									st = thePlayer.getInventory().removeItem(j, 1);
									if(!st.isEmpty()) {
										is = new StoredItemStack(st, 1);
										playerInvUpdate = true;
										break;
									}
								}
							}
						}
						if(is != null) {
							craftMatrix.setItem(i, is.getActualStack());
							slot = craftMatrix.getItem(i);
						}
					}
					if (rem.isEmpty()) {
						continue;
					}
					if (slot.isEmpty()) {
						craftMatrix.setItem(i, rem);
						continue;
					}
					rem = pushStack(rem);
					if(rem.isEmpty())continue;
					if (thePlayer.getInventory().add(rem)) continue;
					thePlayer.drop(rem, false);
				}
			}
			refillingGrid = false;
			onCraftingMatrixChanged();
			craftingCooldown += craftResult.getItem(0).getCount();
			if(playerInvUpdate)thePlayer.containerMenu.broadcastChanges();
		}
	}

	public void unregisterCrafting(Player playerIn, CraftingTerminalMenu containerCraftingTerminal) {
		craftingListeners.remove(containerCraftingTerminal);
		if (polymorphPlayer != null && polymorphPlayer.get() == playerIn)
			polymorphPlayer = null;
	}

	public void registerCrafting(CraftingTerminalMenu containerCraftingTerminal) {
		craftingListeners.add(containerCraftingTerminal);
	}

	protected void onCraftingMatrixChanged() {
		if(refillingGrid)return;
		CraftingInput input = craftMatrix.asCraftInput();
		if (currentRecipe.isEmpty() || !currentRecipe.get().value().matches(input, level)) {
			currentRecipe = getRecipe(input);
		}

		if (currentRecipe.isEmpty()) {
			craftResult.setItem(0, ItemStack.EMPTY);
		} else {
			craftResult.setItem(0, currentRecipe.get().value().assemble(input, level.registryAccess()));
		}

		craftingListeners.forEach(CraftingTerminalMenu::onCraftMatrixChanged);
		craftResult.setRecipeUsed(currentRecipe.orElse(null));

		if (!reading) {
			setChanged();
		}
	}

	private Optional<RecipeHolder<CraftingRecipe>> getRecipe(CraftingInput input) {
		if (StorageMod.polymorph && polymorphPlayer != null) {
			Player pl = polymorphPlayer.get();
			return PolymorphHelper.getRecipe(pl, RecipeType.CRAFTING, input, level);
		}
		return level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
	}

	public void clear(Player player) {
		for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
			ItemStack st = craftMatrix.removeItemNoUpdate(i);
			if(!st.isEmpty()) {
				StoredItemStack st0 = pushStack(new StoredItemStack(st));
				if (st0 != null) {
					var is = st0.getActualStack();
					player.getInventory().add(is);
					if (!is.isEmpty())
						dropItem(is);
				}
			}
		}
		onCraftingMatrixChanged();
	}

	@Override
	public void updateServer() {
		super.updateServer();
		craftingCooldown = 0;
	}

	public boolean canCraft() {
		return craftingCooldown + craftResult.getItem(0).getCount() <= craftResult.getItem(0).getMaxStackSize();
	}

	public void polymorphUpdate(Player playerIn) {
		polymorphPlayer = new WeakReference<>(playerIn);
		currentRecipe = Optional.empty();
		onCraftingMatrixChanged();
	}

	public void setCraftSlot(int slot, ItemStack actualStack) {
		craftMatrix.setItem(slot, actualStack);
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		Containers.dropContents(level, pos, craftMatrix);
	}
}
