package com.tom.storagemod.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.Content;
import com.tom.storagemod.components.TagFilterComponent;

public class AbstractTagFilter {
	protected ItemStack stack;
	protected boolean allowList;
	protected List<TagKey<Item>> tags;

	public AbstractTagFilter(ItemStack stack) {
		this.stack = stack;
		TagFilterComponent f = stack.get(Content.tagFilterComponent.get());
		if (f != null) {
			tags = new ArrayList<>(f.tags());
			allowList = f.allowList();
		} else
			tags = new ArrayList<>();
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

	public void setTags(List<Identifier> tags) {
		this.tags = tags.stream().map(t -> TagKey.create(Registries.ITEM, t)).toList();
	}
}
