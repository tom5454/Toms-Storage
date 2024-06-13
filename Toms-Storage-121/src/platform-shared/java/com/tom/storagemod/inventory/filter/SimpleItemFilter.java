package com.tom.storagemod.inventory.filter;

import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.util.AbstractSimpleItemFilter;

public class SimpleItemFilter extends AbstractSimpleItemFilter implements ItemPredicate {
	public SimpleItemFilter(ItemStack stack) {
		super(stack);
	}

	@Override
	public boolean test(StoredItemStack stack) {
		if (matchNBT)return testNbt(stack) == allowList;
		else return test0(stack) == allowList;
	}

	private boolean test0(StoredItemStack stack) {
		for(int i = 0;i<filter.getContainerSize();i++) {
			ItemStack is = filter.getItem(i);
			if(is.isEmpty())continue;
			if(ItemStack.isSameItem(stack.getStack(), is))return true;
		}
		return false;
	}

	private boolean testNbt(StoredItemStack stack) {
		for(int i = 0;i<filter.getContainerSize();i++) {
			ItemStack is = filter.getItem(i);
			if(is.isEmpty())continue;
			if(ItemStack.isSameItemSameComponents(stack.getStack(), is))return true;
		}
		return false;
	}

	@Override
	public boolean configMatch(ItemStack stack) {
		return stack == this.stack;
	}
}
