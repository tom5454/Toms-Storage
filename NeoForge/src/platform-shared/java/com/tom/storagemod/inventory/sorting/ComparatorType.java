package com.tom.storagemod.inventory.sorting;

import java.lang.ref.SoftReference;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.inventory.StoredItemStack;

public class ComparatorType implements Comparator<StoredItemStack> {

	private static Map<Item, Integer> makeLookup() {
		var tab = BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(CreativeModeTabs.SEARCH);
		var items = tab.getSearchTabDisplayItems().stream().map(ItemStack::getItem).toArray(Item[]::new);
		Map<Item, Integer> itemOrderLookup = new HashMap<>();
		for (int i = 0; i < items.length; i++) {
			Item item = items[i];
			itemOrderLookup.putIfAbsent(item, i);
		}
		return itemOrderLookup;
	}

	private static SoftReference<Map<Item, Integer>> LOOKUP = new SoftReference<>(null);
	private static long lastAccess;

	private static Map<Item, Integer> getLookup() {
		var l = LOOKUP.get();
		long now = System.currentTimeMillis();
		if (l == null || (now - lastAccess > 5 * 60 * 1000)) {
			l = makeLookup();
			LOOKUP = new SoftReference<>(l);
			lastAccess = now;
		}
		return l;
	}

	@Override
	public int compare(StoredItemStack in1, StoredItemStack in2) {
		var lookup = getLookup();
		int val1 = lookup.getOrDefault(in1.getStack().getItem(), Integer.MAX_VALUE);
		int val2 = lookup.getOrDefault(in2.getStack().getItem(), Integer.MAX_VALUE);
		int cmp = Integer.compare(val1, val2);
		if (cmp != 0)return cmp;
		return in1.getDisplayName().compareTo(in2.getDisplayName());
	}
}