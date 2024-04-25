package com.tom.storagemod.util;

import net.minecraft.world.item.ItemStack;

public class SimpleItemFilter extends AbstractSimpleItemFilter implements ItemPredicate {

	public SimpleItemFilter(ItemStack stack) {
		super(stack);
	}

	@Override
	public boolean test(ItemStack stack) {
		return test0(stack) == allowList;
	}

	private boolean test0(ItemStack stack) {
		for(int i = 0;i<filter.getContainerSize();i++) {
			ItemStack is = filter.getItem(i);
			if(is.isEmpty())continue;
			if(ItemStack.isSameItem(stack, is) && (!matchNBT || ItemStack.isSameItemSameComponents(stack, is)))return true;
		}
		return false;
	}

	@Override
	public boolean configMatch(ItemStack stack) {
		return stack == this.stack;
	}
}
