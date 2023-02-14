package com.tom.storagemod.util;

import java.util.HashSet;
import java.util.Set;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.Content;

public class PolyFilter implements ItemPredicate {
	private BlockFace face;
	private Set<ItemStack> filter;
	private long lastCheck;

	public PolyFilter(BlockFace face) {
		this.face = face;
		this.filter = new HashSet<>();
	}

	private void updateFilter() {
		long time = face.level().getGameTime();
		if(lastCheck != time && time % 10 == 1) {
			lastCheck = time;
			filter.clear();
			Storage<ItemVariant> st = ItemStorage.SIDED.find(face.level(), face.pos(), face.from());
			if(st != null) {
				Util.stream(st.iterator()).filter(s -> !s.isResourceBlank()).
				map(s -> new StoredItemStack(s.getResource().toStack())).
				distinct().map(StoredItemStack::getStack).forEach(filter::add);
			}
		}
	}

	@Override
	public boolean test(ItemVariant stack) {
		updateFilter();
		for (ItemStack f : filter) {
			if(stack.matches(f))return true;
		}
		return false;
	}

	@Override
	public boolean configMatch(ItemStack stack) {
		return stack.getItem() == Content.polyItemFliter.get();
	}
}
