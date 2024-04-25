package com.tom.storagemod.util;

import java.util.ArrayList;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.Content;
import com.tom.storagemod.components.SimpleItemFilterComponent;

public class AbstractSimpleItemFilter {
	protected SimpleContainer filter;
	protected ItemStack stack;
	protected boolean matchNBT, allowList;

	public AbstractSimpleItemFilter(ItemStack stack) {
		this.stack = stack;
		SimpleItemFilterComponent c = stack.get(Content.simpleItemFilterComponent.get());
		if (c != null) {
			filter = new SimpleContainer(c.stacks().toArray(ItemStack[]::new));
			matchNBT = c.matchComp();
			allowList = c.allowList();
		} else
			filter = new SimpleContainer(9);
	}

	public void flush() {
		SimpleItemFilterComponent c = new SimpleItemFilterComponent(new ArrayList<>(filter.getItems()), matchNBT, allowList);
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
