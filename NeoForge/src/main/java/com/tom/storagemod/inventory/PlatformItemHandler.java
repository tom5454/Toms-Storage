package com.tom.storagemod.inventory;

import java.util.Set;

import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import com.tom.storagemod.inventory.IInventoryAccess.IInventory;

public class PlatformItemHandler extends DelegatingResourceHandler<ItemResource> implements IProxy {
	private IInventory access;

	public PlatformItemHandler(IInventory access) {
		super(() -> access.getInventoryAccess().getPlatformHandler());
		this.access = access;
	}

	@Override
	public IInventoryAccess getRootHandler(Set<IProxy> dejaVu) {
		return access.getInventoryAccess().getRootHandler(dejaVu);
	}
}
