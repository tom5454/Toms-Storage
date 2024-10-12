package com.tom.storagemod.block;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.util.BlockFace;
import com.tom.storagemod.util.TickerUtil;

public abstract class AbstractInventoryHopperBlock extends BaseEntityBlock implements IInventoryCable {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

	protected AbstractInventoryHopperBlock(Properties p_49224_) {
		super(p_49224_);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN).setValue(ENABLED, Boolean.valueOf(true)));
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> tooltip,
			TooltipFlag tooltipFlag) {
		ClientUtil.tooltip("inventory_hopper", tooltip);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state,
			BlockEntityType<T> type) {
		return TickerUtil.createTicker(world, false, true);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ENABLED);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_149645_1_) {
		return RenderShape.MODEL;
	}

	@Override
	public List<BlockFace> nextScan(Level world, BlockState state, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public boolean canConnectFrom(BlockState state, Direction dir) {
		return state.getValue(FACING).getAxis() == dir.getAxis();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		switch (state.getValue(FACING)) {
		case DOWN:
			return Shapes.or(box(5, 0, 5, 11, 6, 11), box(3, 6, 3, 13, 16, 13));
		case EAST:
			return Shapes.or(box(10, 5, 5, 16, 11, 11), box(0, 3, 3, 10, 13, 13));
		case NORTH:
			return Shapes.or(box(5, 5, 0, 11, 11, 6), box(3, 3, 6, 13, 13, 16));
		case SOUTH:
			return Shapes.or(box(5, 5, 10, 11, 11, 16), box(3, 3, 0, 13, 13, 10));
		case UP:
			return Shapes.or(box(5, 10, 5, 11, 16, 11), box(3, 0, 3, 13, 10, 13));
		case WEST:
			return Shapes.or(box(0, 5, 5, 6, 11, 11), box(6, 3, 3, 16, 13, 13));
		default:
			break;
		}
		return Shapes.block();
	}

	@Override
	public void onPlace(BlockState p_54110_, Level p_54111_, BlockPos p_54112_, BlockState p_54113_, boolean p_54114_) {
		if (!p_54113_.is(p_54110_.getBlock())) {
			this.updateState(p_54111_, p_54112_, p_54110_);
		}
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block,
			Orientation orientation, boolean bl) {
		this.updateState(level, blockPos, blockState);
	}

	private void updateState(Level worldIn, BlockPos pos, BlockState state) {
		boolean flag = !worldIn.hasNeighborSignal(pos);
		if (flag != state.getValue(ENABLED)) {
			worldIn.setBlock(pos, state.setValue(ENABLED, Boolean.valueOf(flag)), 4);
		}
	}
}
