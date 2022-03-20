package com.tom.storagemod.util;

import java.util.UUID;

import net.minecraft.server.world.ServerWorld;

public interface IInventoryLink {
	InventoryWrapper getInventoryFrom(ServerWorld world, int lvl);
	UUID getChannel();
}
