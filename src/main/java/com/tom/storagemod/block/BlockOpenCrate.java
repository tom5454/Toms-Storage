package com.tom.storagemod.block;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.world.BlockView;

import com.tom.fabriclibs.ext.IBlock;
import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityOpenCrate;

public class BlockOpenCrate extends BlockWithEntity implements IBlock {

	public BlockOpenCrate() {
		super(Block.Settings.of(Material.WOOD).strength(3));//.harvestTool(ToolType.AXE)
		setRegistryName("ts.open_crate");
	}

	@Override
	public BlockEntity createBlockEntity(BlockView worldIn) {
		return new TileEntityOpenCrate();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void buildTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		ClientProxy.tooltip("open_crate", tooltip);
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.add(Properties.FACING);
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rot) {
		return state.with(Properties.FACING, rot.rotate(state.get(Properties.FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.get(Properties.FACING)));
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().
				with(Properties.FACING, ctx.getPlayerFacing().getOpposite());
	}
}
