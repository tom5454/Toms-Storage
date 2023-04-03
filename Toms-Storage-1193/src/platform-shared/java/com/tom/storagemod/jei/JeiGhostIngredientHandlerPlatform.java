package com.tom.storagemod.jei;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.gui.AbstractFilteredScreen;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;

@SuppressWarnings("rawtypes")
public interface JeiGhostIngredientHandlerPlatform extends IGhostIngredientHandler<AbstractFilteredScreen> {

	@SuppressWarnings("unchecked")
	@Override
	default <I> List<Target<I>> getTargets(AbstractFilteredScreen gui, I ingredient, boolean doStart) {
		if (ingredient instanceof ItemStack stack) {
			return (List) getTargets(gui, stack, doStart);
		}
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	default <I> List<Target<I>> getTargetsTyped(AbstractFilteredScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
		if (ingredient.getType() == VanillaTypes.ITEM_STACK) {
			ItemStack stack = (ItemStack) ingredient.getIngredient();
			return (List) getTargets(gui, stack, doStart);
		}
		return Collections.emptyList();
	}

	List<Target<ItemStack>> getTargets(AbstractFilteredScreen gui, ItemStack stack, boolean doStart);
}
