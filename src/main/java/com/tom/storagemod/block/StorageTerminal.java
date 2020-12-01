package com.tom.storagemod.block;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.BlockView;

import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.tile.TileEntityStorageTerminal;

public class StorageTerminal extends StorageTerminalBase {

	public StorageTerminal() {
		super();
	}

	@Override
	public BlockEntity createBlockEntity(BlockView worldIn) {
		return new TileEntityStorageTerminal();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		StorageModClient.tooltip("storage_terminal", tooltip);
	}
}
