package com.tom.storagemod.block.entity;

import java.util.Set;

import com.tom.storagemod.inventory.IInventoryAccess;

public interface IInventoryConnector {
	IInventoryAccess getMergedHandler();
	Set<IInventoryAccess> getConnectedInventories();
	boolean isValid();
}
