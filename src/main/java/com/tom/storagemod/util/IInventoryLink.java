package com.tom.storagemod.util;

import java.util.UUID;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.world.ServerWorld;

public interface IInventoryLink {
	Storage<ItemVariant> getInventoryFrom(ServerWorld world, int lvl);
	UUID getChannel();
}
