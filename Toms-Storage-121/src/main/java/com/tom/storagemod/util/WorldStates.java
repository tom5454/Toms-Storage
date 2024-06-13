package com.tom.storagemod.util;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import com.tom.storagemod.inventory.IChangeTrackerAccess;
import com.tom.storagemod.inventory.IInventoryAccess.IInventoryChangeTracker;
import com.tom.storagemod.inventory.InventoryCableNetwork;
import com.tom.storagemod.inventory.InventoryChangeTracker;

public class WorldStates {
	public static Map<IItemHandler, IInventoryChangeTracker> trackers = new WeakHashMap<>();
	public static Map<Level, InventoryCableNetwork> cableNetworks = new HashMap<>();

	public static void clearWorldStates() {
		trackers.clear();
		cableNetworks.clear();
	}

	public static IInventoryChangeTracker getTracker(IItemHandler h) {
		if (h instanceof IChangeTrackerAccess a)return a.tracker();
		return trackers.computeIfAbsent(h, InventoryChangeTracker::new);
	}
}
