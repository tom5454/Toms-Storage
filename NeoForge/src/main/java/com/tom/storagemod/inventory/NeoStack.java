package com.tom.storagemod.inventory;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public record NeoStack(ItemResource resource, long count) {

	public NeoStack(ResourceHandler<ItemResource> handler, int slot) {
		this(handler.getResource(slot), handler.getAmountAsLong(slot));
	}
}
