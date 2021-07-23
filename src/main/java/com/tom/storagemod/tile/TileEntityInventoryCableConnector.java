package com.tom.storagemod.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.StorageMod;

public class TileEntityInventoryCableConnector extends TileEntityInventoryCableConnectorBase {

	public TileEntityInventoryCableConnector(BlockPos pos, BlockState state) {
		super(StorageMod.invCableConnectorTile, pos, state);
	}

}
