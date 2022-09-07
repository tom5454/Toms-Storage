package com.tom.storagemod.tile;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.util.math.Direction;

/**
 * Backport from 1.19
 * */
public interface SidedStorageBlockEntity {
	Storage<ItemVariant> getItemStorage(Direction side);
}
