package com.tom.storagemod.block;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.tile.StorageTerminalBlockEntity;

public class StorageTerminalBlock extends AbstractStorageTerminalBlock {

	public StorageTerminalBlock() {
		super();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip,
			TooltipFlag flagIn) {
		StorageModClient.tooltip("storage_terminal", tooltip);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos paramBlockPos, BlockState paramBlockState) {
		return new StorageTerminalBlockEntity(paramBlockPos, paramBlockState);
	}
}
