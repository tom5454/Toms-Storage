package com.tom.storagemod;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public class TickerUtil {

	public static <T extends BlockEntity> BlockEntityTicker<T> createTicker(Level world, boolean client, boolean server) {
		return world.isClientSide ? (client ? (a, b, c, tile) -> ((TickableClient)tile).updateClient() : null) : (server ? (a, b, c, tile) -> ((TickableServer)tile).updateServer() : null);
	}

	public static interface TickableClient {
		public void updateClient();
	}

	public static interface TickableServer {
		public void updateServer();
	}
}
