package com.tom.storagemod.jei;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.gui.AbstractFilteredScreen;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;

public interface JeiGhostIngredientHandlerPlatform extends IGhostIngredientHandler<AbstractFilteredScreen> {

	@Override
	default <I> List<Target<I>> getTargets(AbstractFilteredScreen gui, I ingredient, boolean doStart) {
		if (ingredient instanceof ItemStack stack) {
			return getTargets(gui, stack, doStart);
		}
		return Collections.emptyList();
	}

	@Override
	default <I> List<Target<I>> getTargetsTyped(AbstractFilteredScreen gui, ITypedIngredient<I> ingredient, boolean doStart) {
		if (ingredient.getType() == VanillaTypes.ITEM_STACK) {
			ItemStack stack = (ItemStack) ingredient.getIngredient();
			return getTargets(gui, stack, doStart);
		}
		return Collections.emptyList();
	}

	<I> List<Target<I>> getTargets(AbstractFilteredScreen gui, ItemStack stack, boolean doStart);
}
