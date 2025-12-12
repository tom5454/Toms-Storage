package com.tom.storagemod.util;

import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageTags;

public class InventoryConnectorConfigUtil {
	public static boolean canConnect(BlockState blockState) {
		if (blockState.is(StorageTags.INV_CONNECTOR_SKIP))return false;
		var block = blockState.getBlock();
		if (Config.get().getBlockedBlocks().contains(block))
			return false;
		if (!Config.get().getBlockedMods().isEmpty()) {
			var modid = block.builtInRegistryHolder().key().identifier().getNamespace();
			if (Config.get().getBlockedMods().contains(modid))
				return false;
		}
		return true;
	}
}
