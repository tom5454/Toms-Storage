package com.tom.storagemod.util;

import java.util.ArrayList;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.Content;
import com.tom.storagemod.components.SimpleItemFilterComponent;

public class SimpleItemFilter implements ItemPredicate {
	private SimpleContainer filter;
	private ItemStack stack;
	private boolean matchNBT, allowList;

	public SimpleItemFilter(ItemStack stack) {
		this.stack = stack;
		SimpleItemFilterComponent c = stack.get(Content.simpleItemFilterComponent.get());
		if (c != null) {
			filter = new SimpleContainer(c.stacks().toArray(ItemStack[]::new));
			matchNBT = c.matchComp();
			allowList = c.allowList();
		} else
			filter = new SimpleContainer(9);
	}

	@Override
	public boolean test(ItemVariant stack) {
		return test0(stack) == allowList;
	}

	private boolean test0(ItemVariant stack) {
		for(int i = 0;i<filter.getContainerSize();i++) {
			ItemStack f = filter.getItem(i);
			if(f.isEmpty())continue;
			if(stack.isOf(f.getItem()) && (!matchNBT || stack.componentsMatches(f.getComponentsPatch())))return true;
		}
		return false;
	}

	@Override
	public boolean configMatch(ItemStack stack) {
		return stack == this.stack;
	}

	public void flush() {
		SimpleItemFilterComponent c = new SimpleItemFilterComponent(new ArrayList<>(filter.items), matchNBT, allowList);
		stack.applyComponents(DataComponentPatch.builder().set(Content.simpleItemFilterComponent.get(), c).build());
	}

	public SimpleContainer getContainer() {
		return filter;
	}

	public boolean isMatchNBT() {
		return matchNBT;
	}

	public void setMatchNBT(boolean matchNBT) {
		this.matchNBT = matchNBT;
	}

	public boolean isAllowList() {
		return allowList;
	}

	public void setAllowList(boolean allowList) {
		this.allowList = allowList;
	}
}
