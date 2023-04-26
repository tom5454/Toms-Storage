package com.tom.storagemod.util;

import java.util.UUID;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.server.level.ServerLevel;

public interface IInventoryLink {
	Storage<ItemVariant> getInventoryFrom(ServerLevel world, int lvl);
	UUID getChannel();
}
