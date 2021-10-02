package com.tom.storagemod.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityCraftingTerminal;

public class CraftingTerminal extends StorageTerminalBase {

	public CraftingTerminal() {
		super();
		setRegistryName("ts.crafting_terminal");
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityCraftingTerminal(pos, state);
	}

	@Override
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip,
			TooltipFlag flagIn) {
		ClientProxy.tooltip("crafting_terminal", tooltip);
	}
}
