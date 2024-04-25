package com.tom.storagemod.util;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.ItemStack;

public class TagFilter extends AbstractTagFilter implements ItemPredicate {

	public TagFilter(ItemStack stack) {
		super(stack);
	}

	@Override
	public boolean configMatch(ItemStack stack) {
		return stack == this.stack;
	}

	@Override
	public boolean test(ItemVariant stack) {
		return test0(stack) == allowList;
	}

	@SuppressWarnings("deprecation")
	private boolean test0(ItemVariant stack) {
		for (int i = 0; i < tags.size(); i++) {
			if(stack.getItem().builtInRegistryHolder().is(tags.get(i)))return true;
		}
		return false;
	}
}
