package com.tom.storagemod.block;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.tile.StorageTerminalBlockEntity;

public class StorageTerminalBlock extends AbstractStorageTerminalBlock {

	public StorageTerminalBlock() {
		super();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		StorageModClient.tooltip("storage_terminal", tooltip);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos paramBlockPos, BlockState paramBlockState) {
		return new StorageTerminalBlockEntity(paramBlockPos, paramBlockState);
	}
}
