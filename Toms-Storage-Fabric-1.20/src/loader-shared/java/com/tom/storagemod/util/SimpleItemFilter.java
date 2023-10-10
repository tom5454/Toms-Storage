package com.tom.storagemod.util;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class SimpleItemFilter implements ItemPredicate {
	private SimpleContainer filter;
	private ItemStack stack;
	private boolean matchNBT, allowList;

	public SimpleItemFilter(ItemStack stack) {
		this.stack = stack;
		filter = new SimpleContainer(9);
		CompoundTag tag = stack.getOrCreateTag();
		filter.fromTag(tag.getList("Filter", 10));
		matchNBT = tag.getBoolean("matchNBT");
		allowList = tag.getBoolean("allowlist");
	}

	@Override
	public boolean test(ItemVariant stack) {
		return test0(stack) == allowList;
	}

	private boolean test0(ItemVariant stack) {
		for(int i = 0;i<filter.getContainerSize();i++) {
			ItemStack f = filter.getItem(i);
			if(f.isEmpty())continue;
			if(stack.isOf(f.getItem()) && (!matchNBT || stack.nbtMatches(f.getTag())))return true;
		}
		return false;
	}

	@Override
	public boolean configMatch(ItemStack stack) {
		return stack == this.stack;
	}

	public void flush() {
		CompoundTag tag = stack.getOrCreateTag();
		tag.put("Filter", filter.createTag());
		tag.putBoolean("matchNBT", matchNBT);
		tag.putBoolean("allowlist", allowList);
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
