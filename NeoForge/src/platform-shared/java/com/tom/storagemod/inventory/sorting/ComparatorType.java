package com.tom.storagemod.inventory.sorting;

import java.lang.ref.SoftReference;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.inventory.TerminalItemStack;

public class ComparatorType implements Comparator<TerminalItemStack> {

    private static SoftReference<Map<Item, Integer>> LOOKUP = new SoftReference<>(null);
    private static long lastAccess;
    private static boolean searchTabReady;

    private static Map<Item, Integer> makeLookup() {
        Map<Item, Integer> order = new HashMap<>();
        var searchTab = BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(CreativeModeTabs.SEARCH);
        var searchItems = searchTab.getSearchTabDisplayItems();
        searchTabReady = !searchItems.isEmpty();

        if (searchTabReady) {
            // Preserve Tom's original creative-search ordering when available.
            int index = 0;
            for (ItemStack stack : searchItems) {
                order.putIfAbsent(stack.getItem(), index++);
            }
        } else {
            // Use the visible order of creative category tabs if SEARCH is not built.
            int index = 0;
            for (var tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
                if (tab == searchTab) continue;
                for (ItemStack stack : tab.getDisplayItems()) {
                    order.putIfAbsent(stack.getItem(), index++);
                }
            }
        }

        // Include modded/hidden items that are not in any creative tab.
        int index = order.size();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!order.containsKey(item)) order.put(item, index++);
        }
        return order;
    }

    private static Map<Item, Integer> getLookup() {
        var lookup = LOOKUP.get();
        long now = System.currentTimeMillis();
        // Retry soon if creative SEARCH was empty, rather than caching fallback for 5 minutes.
        long cacheDuration = searchTabReady ? 5 * 60 * 1000L : 2 * 1000L;
        if (lookup == null || now - lastAccess > cacheDuration) {
            lookup = makeLookup();
            LOOKUP = new SoftReference<>(lookup);
            lastAccess = now;
        }
        return lookup;
    }

    @Override
    public int compare(TerminalItemStack in1, TerminalItemStack in2) {
        var lookup = getLookup();
        int val1 = lookup.getOrDefault(in1.getStack().getItem(), Integer.MAX_VALUE);
        int val2 = lookup.getOrDefault(in2.getStack().getItem(), Integer.MAX_VALUE);
        int cmp = Integer.compare(val1, val2);
        if (cmp != 0) return cmp;
        return in1.getDisplayName().compareTo(in2.getDisplayName());
    }
}
