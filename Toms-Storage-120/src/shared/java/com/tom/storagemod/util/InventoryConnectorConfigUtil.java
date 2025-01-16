package com.tom.storagemod.util;

import net.minecraft.world.level.block.Block;

import com.tom.storagemod.Config;

public class InventoryConnectorConfigUtil {

	public static boolean canConnect(Block block) {
		if (Config.get().getBlockedBlocks().contains(block))
			return false;
		if (!Config.get().getBlockedMods().isEmpty()) {
			var modid = block.builtInRegistryHolder().key().location().getNamespace();
			if (Config.get().getBlockedMods().contains(modid))
				return false;
		}
		return true;
	}
}
