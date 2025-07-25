package com.tom.storagemod.inventory;

import java.util.Set;

public interface IProxy {
	IInventoryAccess getRootHandler(Set<IProxy> dejaVu);
}
