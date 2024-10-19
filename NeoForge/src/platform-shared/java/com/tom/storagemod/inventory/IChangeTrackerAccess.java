package com.tom.storagemod.inventory;

import com.tom.storagemod.inventory.IInventoryAccess.IInventoryChangeTracker;

public interface IChangeTrackerAccess {
	IInventoryChangeTracker tracker();
}
