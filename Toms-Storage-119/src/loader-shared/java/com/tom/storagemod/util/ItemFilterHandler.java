package com.tom.storagemod.util;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.item.IItemFilter;

public class ItemFilterHandler implements ItemPredicate {
	private Container filter;
	private ItemPredicate[] predicates;
	private BlockFace face;

	public ItemFilterHandler(Container filter, BlockFace face) {
		this.filter = filter;
		this.face = face;
		this.predicates = new ItemPredicate[0];
	}

	@Override
	public boolean test(ItemStack stack) {
		if(predicates.length != filter.getContainerSize()) {
			predicates = new ItemPredicate[filter.getContainerSize()];
		}
		for(int i = 0;i<predicates.length;i++) {
			ItemStack is = filter.getItem(i);
			if(is.isEmpty())continue;
			if(is.getItem() instanceof IItemFilter f) {
				if (predicates[i] == null || !predicates[i].configMatch(is)) {
					predicates[i] = f.createFilter(face, is);
				}
				if(predicates[i].test(stack))return true;
				else continue;
			} else if(predicates[i] != null) {
				predicates[i] = null;
			}
			if(ItemStack.isSame(stack, is))return true;
		}
		return false;
	}
}
