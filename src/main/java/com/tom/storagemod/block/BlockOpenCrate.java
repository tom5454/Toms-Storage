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
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager.Builder;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.TickerUtil;
import com.tom.storagemod.tile.TileEntityOpenCrate;

public class BlockOpenCrate extends BlockWithEntity {

	public BlockOpenCrate() {
		super(Block.Settings.of(Material.WOOD).strength(3));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		StorageModClient.tooltip("open_crate", tooltip);
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
				with(Properties.FACING, ctx.getPlayerLookDirection().getOpposite());
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos paramBlockPos, BlockState paramBlockState) {
		return new TileEntityOpenCrate(paramBlockPos, paramBlockState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return TickerUtil.createTicker(world, false, true);
	}
}
