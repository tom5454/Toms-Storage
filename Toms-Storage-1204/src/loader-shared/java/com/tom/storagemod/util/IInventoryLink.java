package com.tom.storagemod.util;

import java.util.UUID;

import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

public interface IInventoryLink {
	IItemHandler getInventoryFrom(Level fromWorld, int fromLevel);
	UUID getChannel();
}
