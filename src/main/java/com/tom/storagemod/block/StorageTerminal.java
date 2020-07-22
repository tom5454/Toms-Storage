package com.tom.storagemod.block;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.BlockView;

import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityStorageTerminal;

public class StorageTerminal extends StorageTerminalBase {

	public StorageTerminal() {
		super();
		setRegistryName("ts.storage_terminal");
	}

	@Override
	public BlockEntity createBlockEntity(BlockView worldIn) {
		return new TileEntityStorageTerminal();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void buildTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		ClientProxy.tooltip("storage_terminal", tooltip);
	}
}
