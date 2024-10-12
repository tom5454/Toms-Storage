package com.tom.storagemod.inventory;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import com.tom.storagemod.block.entity.IInventoryConnector;

public interface IInventoryLink {
	IInventoryConnector getConnector();
	boolean isAccessibleFrom(ServerLevel world, BlockPos blockPos, int level);
	UUID getChannel();
}
