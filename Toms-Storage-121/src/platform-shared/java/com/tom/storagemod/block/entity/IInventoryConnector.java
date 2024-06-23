package com.tom.storagemod.block.entity;

import java.util.Collection;

import com.tom.storagemod.inventory.IInventoryAccess;

public interface IInventoryConnector {
	IInventoryAccess getMergedHandler();
	Collection<IInventoryAccess> getConnectedInventories();
	Collection<IInventoryConnector> getConnectedConnectors();
	boolean isValid();
}
