package com.tom.storagemod.util;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.Content;
import com.tom.storagemod.components.TagFilterComponent;

public class TagFilter implements ItemPredicate {
	private ItemStack stack;
	private boolean allowList;
	private List<TagKey<Item>> tags;

	public TagFilter(ItemStack stack) {
		this.stack = stack;
		TagFilterComponent f = stack.get(Content.tagFilterComponent.get());
		if (f != null) {
			tags = new ArrayList<>(f.tags());
			allowList = f.allowList();
		} else
			tags = new ArrayList<>();
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

	public boolean isAllowList() {
		return allowList;
	}

	public void setAllowList(boolean allowList) {
		this.allowList = allowList;
	}

	public void flush() {
		TagFilterComponent c = new TagFilterComponent(new ArrayList<>(tags), allowList);
		stack.applyComponents(DataComponentPatch.builder().set(Content.tagFilterComponent.get(), c).build());
	}

	public List<TagKey<Item>> getTags() {
		return tags;
	}

	public void setTags(List<ResourceLocation> tags) {
		this.tags = tags.stream().map(t -> TagKey.create(Registries.ITEM, t)).toList();
	}
}
