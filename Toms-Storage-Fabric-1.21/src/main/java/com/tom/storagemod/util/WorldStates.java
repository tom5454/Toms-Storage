package com.tom.storagemod.util;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.level.Level;

import com.tom.storagemod.inventory.IChangeTrackerAccess;
import com.tom.storagemod.inventory.IInventoryAccess.IInventoryChangeTracker;
import com.tom.storagemod.inventory.InventoryCableNetwork;
import com.tom.storagemod.inventory.InventoryChangeTracker;

public class WorldStates {
	public static Map<Storage<ItemVariant>, IInventoryChangeTracker> trackers = new WeakHashMap<>();
	public static Map<Level, InventoryCableNetwork> cableNetworks = new HashMap<>();

	public static void clearWorldStates() {
		trackers.clear();
		cableNetworks.clear();
	}

	public static IInventoryChangeTracker getTracker(Storage<ItemVariant> h) {
		if (h instanceof IChangeTrackerAccess a)return a.tracker();
		return trackers.computeIfAbsent(h, InventoryChangeTracker::new);
	}
}
