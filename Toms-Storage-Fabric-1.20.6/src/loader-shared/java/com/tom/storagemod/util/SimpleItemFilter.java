package com.tom.storagemod.util;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.ItemStack;

public class SimpleItemFilter extends AbstractSimpleItemFilter implements ItemPredicate {

	public SimpleItemFilter(ItemStack stack) {
		super(stack);
	}

	@Override
	public boolean test(ItemVariant stack) {
		return test0(stack) == allowList;
	}

	private boolean test0(ItemVariant stack) {
		for(int i = 0;i<filter.getContainerSize();i++) {
			ItemStack f = filter.getItem(i);
			if(f.isEmpty())continue;
			if(stack.isOf(f.getItem()) && (!matchNBT || stack.componentsMatch(f.getComponentsPatch())))return true;
		}
		return false;
	}

	@Override
	public boolean configMatch(ItemStack stack) {
		return stack == this.stack;
	}
}
