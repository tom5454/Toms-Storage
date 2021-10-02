package com.tom.storagemod.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import net.minecraftforge.common.ToolType;

import com.tom.storagemod.proxy.ClientProxy;

public class BlockInventoryCable extends SixWayBlock implements IWaterLoggable, IInventoryCable {
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty[] DIR_TO_PROPERTY = new BooleanProperty[] {DOWN, UP, NORTH, SOUTH, WEST, EAST};

	public BlockInventoryCable() {
		super(0.125f, Block.Properties.of(Material.WOOD).strength(2).harvestTool(ToolType.AXE));
		setRegistryName("ts.inventory_cable");
		registerDefaultState(defaultBlockState()
				.setValue(DOWN, false)
				.setValue(UP, false)
				.setValue(NORTH, false)
				.setValue(EAST, false)
				.setValue(SOUTH, false)
				.setValue(WEST, false)
				.setValue(WATERLOGGED, false));
	}

	@Override
	public void appendHoverText(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		ClientProxy.tooltip("inventory_cable", tooltip);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, WATERLOGGED);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (stateIn.getValue(WATERLOGGED)) {
			worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
		}

		return stateIn.setValue(SixWayBlock.PROPERTY_BY_DIRECTION.get(facing), IInventoryCable.canConnect(facingState, facing));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());
		return withConnectionProperties(context.getLevel(), context.getClickedPos()).
				setValue(WATERLOGGED, Boolean.valueOf(ifluidstate.getType() == Fluids.WATER));
	}

	@Override
	public List<BlockPos> next(World world, BlockState state, BlockPos pos) {
		List<BlockPos> next = new ArrayList<>();
		for (Direction d : Direction.values()) {
			if(state.getValue(DIR_TO_PROPERTY[d.ordinal()]))next.add(pos.relative(d));
		}
		return next;
	}

	public BlockState withConnectionProperties(IWorld blockView_1, BlockPos blockPos_1) {
		BlockState block_1 = blockView_1.getBlockState(blockPos_1.below());
		BlockState block_2 = blockView_1.getBlockState(blockPos_1.above());
		BlockState block_3 = blockView_1.getBlockState(blockPos_1.north());
		BlockState block_4 = blockView_1.getBlockState(blockPos_1.east());
		BlockState block_5 = blockView_1.getBlockState(blockPos_1.south());
		BlockState block_6 = blockView_1.getBlockState(blockPos_1.west());

		return defaultBlockState()
				.setValue(DOWN, IInventoryCable.canConnect(block_1, Direction.DOWN))
				.setValue(UP, IInventoryCable.canConnect(block_2, Direction.UP))
				.setValue(NORTH, IInventoryCable.canConnect(block_3, Direction.NORTH))
				.setValue(EAST, IInventoryCable.canConnect(block_4, Direction.EAST))
				.setValue(SOUTH, IInventoryCable.canConnect(block_5, Direction.SOUTH))
				.setValue(WEST, IInventoryCable.canConnect(block_6, Direction.WEST));
	}

	@Override
	public BlockState rotate(BlockState blockState_1, Rotation blockRotation_1) {
		switch (blockRotation_1) {
		case CLOCKWISE_180:
			return blockState_1.setValue(NORTH, blockState_1.getValue(SOUTH)).setValue(EAST, blockState_1.getValue(WEST)).setValue(SOUTH, blockState_1.getValue(NORTH)).setValue(WEST, blockState_1.getValue(EAST));
		case CLOCKWISE_90:
			return blockState_1.setValue(NORTH, blockState_1.getValue(EAST)).setValue(EAST, blockState_1.getValue(SOUTH)).setValue(SOUTH, blockState_1.getValue(WEST)).setValue(WEST, blockState_1.getValue(NORTH));
		case COUNTERCLOCKWISE_90:
			return blockState_1.setValue(NORTH, blockState_1.getValue(WEST)).setValue(EAST, blockState_1.getValue(NORTH)).setValue(SOUTH, blockState_1.getValue(EAST)).setValue(WEST, blockState_1.getValue(SOUTH));
		default:
			break;
		}
		return blockState_1;
	}



	@SuppressWarnings("deprecation")
	@Override
	public BlockState mirror(BlockState blockState_1, Mirror blockMirror_1) {
		switch (blockMirror_1) {
		case FRONT_BACK:
			return blockState_1.setValue(NORTH, blockState_1.getValue(SOUTH)).setValue(SOUTH, blockState_1.getValue(NORTH));
		case LEFT_RIGHT:
			return blockState_1.setValue(EAST, blockState_1.getValue(WEST)).setValue(WEST, blockState_1.getValue(EAST));
		default:
			break;
		}


		return super.mirror(blockState_1, blockMirror_1);
	}
}
