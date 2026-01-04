package com.tom.storagemod.jei;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import com.tom.storagemod.Content;
import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.menu.CraftingTerminalMenu;
import com.tom.storagemod.util.IAutoFillTerminal;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeTransferRegistration;

public class CraftingTerminalTransferHandler<C extends AbstractContainerMenu & IAutoFillTerminal> implements IRecipeTransferHandler<C, RecipeHolder<CraftingRecipe>> {
	private final Class<C> containerClass;
	private final IRecipeTransferHandlerHelper helper;
	private static final IRecipeTransferError ERROR_INSTANCE = new IRecipeTransferError() {
		@Override public IRecipeTransferError.Type getType() { return IRecipeTransferError.Type.INTERNAL; }
	};

	public CraftingTerminalTransferHandler(Class<C> containerClass, IRecipeTransferHandlerHelper helper) {
		this.containerClass = containerClass;
		this.helper = helper;
	}

	@Override
	public Class<C> getContainerClass() {
		return containerClass;
	}

	@Override
	public @Nullable IRecipeTransferError transferRecipe(C container, RecipeHolder<CraftingRecipe> recipe,
			IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		if (container instanceof IAutoFillTerminal term) {
			if (!term.isSmartSearch() && !doTransfer)return null;
			List<IRecipeSlotView> missing = new ArrayList<>();
			List<IRecipeSlotView> views = recipeSlots.getSlotViews();
			List<ItemStack[]> inputs = new ArrayList<>();
			Set<StoredItemStack> stored = new HashSet<>(term.getStoredItems());
			for (IRecipeSlotView view : views) {
				if(view.getRole() == RecipeIngredientRole.INPUT || view.getRole() == RecipeIngredientRole.CRAFTING_STATION) {
					ItemStack[] list = view.getIngredients(VanillaTypes.ITEM_STACK).toArray(ItemStack[]::new);
					if(list.length == 0)inputs.add(null);
					else {
						inputs.add(list);

						boolean found = false;
						for (ItemStack stack : list) {
							if (stack != null && player.getInventory().findSlotMatchingItem(stack) != -1) {
								found = true;
								break;
							}
						}

						if (!found) {
							for (ItemStack stack : list) {
								StoredItemStack s = new StoredItemStack(stack);
								if(stored.contains(s)) {
									found = true;
									break;
								}
							}
						}

						if (!found) {
							missing.add(view);
						}
					}
				}
			}
			if (doTransfer) {
				var recipeId = recipe.id();
				if (recipeId != null) {
					CompoundTag compound = new CompoundTag();
					compound.putString("fill", recipeId.identifier().toString());
					term.sendMessage(compound);
				}
			}

			if(!missing.isEmpty()) {
				return new TransferWarning(helper.createUserErrorForMissingSlots(Component.translatable("tooltip.toms_storage.items_missing"), missing));
			}
		} else {
			return ERROR_INSTANCE;
		}
		return null;
	}

	public static void registerTransferHandlers(IRecipeTransferRegistration recipeTransferRegistry) {
		recipeTransferRegistry.addRecipeTransferHandler(new CraftingTerminalTransferHandler<>(CraftingTerminalMenu.class, recipeTransferRegistry.getTransferHelper()), RecipeTypes.CRAFTING);
	}

	private static class TransferWarning implements IRecipeTransferError {
		private final IRecipeTransferError parent;

		public TransferWarning(IRecipeTransferError parent) {
			this.parent = parent;
		}

		@Override
		public Type getType() {
			return Type.COSMETIC;
		}

		@Override
		public void showError(GuiGraphics guiGraphics, int mouseX, int mouseY, IRecipeSlotsView recipeSlotsView,
				int recipeX, int recipeY) {
			this.parent.showError(guiGraphics, mouseX, mouseY, recipeSlotsView, recipeX, recipeY);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<MenuType<C>> getMenuType() {
		return Optional.<MenuType<C>>ofNullable((MenuType<C>) Content.craftingTerminalMenu.get());
	}

	@Override
	public IRecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
		return RecipeTypes.CRAFTING;
	}
}
