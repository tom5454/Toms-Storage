package com.tom.storagemod.util;

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
	public boolean test(ItemStack stack) {
		return test0(stack) == allowList;
	}

	private boolean test0(ItemStack stack) {
		for (int i = 0; i < tags.size(); i++) {
			if(stack.is(tags.get(i)))return true;
		}
		return false;
	}
}
