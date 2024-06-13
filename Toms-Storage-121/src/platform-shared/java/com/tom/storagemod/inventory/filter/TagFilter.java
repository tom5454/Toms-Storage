package com.tom.storagemod.inventory.filter;

import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.util.AbstractTagFilter;

public class TagFilter extends AbstractTagFilter implements ItemPredicate {

	public TagFilter(ItemStack stack) {
		super(stack);
	}

	@Override
	public boolean configMatch(ItemStack stack) {
		return stack == this.stack;
	}

	@Override
	public boolean test(StoredItemStack stack) {
		return test0(stack) == allowList;
	}

	private boolean test0(StoredItemStack stack) {
		for (int i = 0; i < tags.size(); i++) {
			if(stack.getStack().is(tags.get(i)))return true;
		}
		return false;
	}
}
