package com.tom.storagemod.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.BlockView;

import com.tom.fabriclibs.ext.IBlock;
import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityInventoryConnector;

public class InventoryConnector extends BlockWithEntity implements IBlock {

	public InventoryConnector() {
		super(Block.Settings.of(Material.WOOD).strength(3));//.harvestTool(ToolType.AXE)
		setRegistryName("ts.inventory_connector");
	}

	@Override
	public BlockEntity createBlockEntity(BlockView worldIn) {
		return new TileEntityInventoryConnector();
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	//@OnlyIn(Dist.CLIENT)
	public void buildTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		ClientProxy.tooltip("inventory_connector", tooltip);
	}
}
