package com.tom.storagemod;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.world.World;

public class TickerUtil {

	public static <T extends BlockEntity> BlockEntityTicker<T> createTicker(World world, boolean client, boolean server) {
		return world.isClient ? (client ? (a, b, c, tile) -> ((TickableClient)tile).updateClient() : null) : (server ? (a, b, c, tile) -> ((TickableServer)tile).updateServer() : null);
	}

	public static interface TickableClient {
		public void updateClient();
	}

	public static interface TickableServer {
		public void updateServer();
	}
}
