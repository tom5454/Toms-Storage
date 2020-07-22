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
import com.tom.storagemod.tile.TileEntityCraftingTerminal;

public class CraftingTerminal extends StorageTerminalBase {

	public CraftingTerminal() {
		super();
		setRegistryName("ts.crafting_terminal");
	}

	@Override
	public BlockEntity createBlockEntity(BlockView worldIn) {
		return new TileEntityCraftingTerminal();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void buildTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		ClientProxy.tooltip("crafting_terminal", tooltip);
	}
}
