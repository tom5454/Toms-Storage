package com.tom.storagemod.util;

import java.util.UUID;

import net.minecraft.world.level.Level;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public interface IInventoryLink {
	LazyOptional<IItemHandler> getInventoryFrom(Level fromWorld, int fromLevel);
	UUID getChannel();
}
