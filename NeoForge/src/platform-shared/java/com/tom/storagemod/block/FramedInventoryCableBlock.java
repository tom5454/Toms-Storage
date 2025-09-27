package com.tom.storagemod.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.mojang.serialization.MapCodec;

import com.tom.storagemod.block.entity.PaintedBlockEntity;
import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.inventory.InventoryCableNetwork;
import com.tom.storagemod.util.BlockFace;

public class FramedInventoryCableBlock extends BaseEntityBlock implements IInventoryCable, IPaintable, NeoForgeBlock, BlockWithTooltip, IConfiguratorHighlight {
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final MapCodec<FramedInventoryCableBlock> CODEC = simpleCodec(FramedInventoryCableBlock::new);
	private final Function<BlockState, VoxelShape> highlightShapes;

	public FramedInventoryCableBlock(Block.Properties pr) {
		super(pr);
		registerDefaultState(defaultBlockState()
				.setValue(DOWN, false)
				.setValue(UP, false)
				.setValue(NORTH, false)
				.setValue(EAST, false)
				.setValue(SOUTH, false)
				.setValue(WEST, false));
		highlightShapes = makeShapes(4f);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, Consumer<Component> tooltip,
			TooltipFlag tooltipFlag) {
		tooltip.accept(Component.translatable("tooltip.toms_storage.paintable"));
		ClientUtil.tooltip("inventory_cable", tooltip);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST);
	}

	@Override
	public List<BlockFace> nextScan(Level world, BlockState state, BlockPos pos) {
		List<BlockFace> next = new ArrayList<>();
		for (Direction d : Direction.values()) {
			if(state.getValue(InventoryCableBlock.DIR_TO_PROPERTY[d.ordinal()]))next.add(new BlockFace(pos.relative(d), d.getOpposite()));
		}
		return next;
	}

	@Override
	public boolean isFunctionalNode() {
		return false;
	}

	@Override
	protected BlockState updateShape(BlockState blockState, LevelReader levelReader,
			ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2,
			BlockState blockState2, RandomSource randomSource) {
		return blockState.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), IInventoryCable.canConnect(blockState2, direction));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withConnectionProperties(context.getLevel(), context.getClickedPos());
	}

	public BlockState withConnectionProperties(LevelAccessor blockView_1, BlockPos blockPos_1) {
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

	@Override
	public boolean paint(Level world, BlockPos pos, BlockState to) {
		BlockEntity te = world.getBlockEntity(pos);
		if(te != null && te instanceof PaintedBlockEntity)
			return ((PaintedBlockEntity)te).setPaintedBlockState(to);
		return false;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PaintedBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_149645_1_) {
		return RenderShape.MODEL;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public void destroy(LevelAccessor p_49860_, BlockPos p_49861_, BlockState p_49862_) {
		if (p_49860_ instanceof ServerLevel l)
			InventoryCableNetwork.getNetwork(l).markNodeInvalid(p_49861_);
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block,
			@Nullable Orientation orientation, boolean bl) {
		super.neighborChanged(blockState, level, blockPos, block, orientation, bl);
		if (!level.isClientSide()) {
			InventoryCableNetwork n = InventoryCableNetwork.getNetwork(level);
			n.markNodeInvalid(blockPos);
			if (orientation != null) {
				for (var d : orientation.getDirections()) {
					n.markNodeInvalid(blockPos.relative(d));
				}
			} else {
				for (var d : Direction.values()) {
					n.markNodeInvalid(blockPos.relative(d));
				}
			}
		}
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
		if (level instanceof ServerLevel l) {
			InventoryCableNetwork n = InventoryCableNetwork.getNetwork(l);
			n.markNodeInvalid(pos);
			n.markNodeInvalid(neighbor);
		}
	}

	@Override
	public int getHighlightColor() {
		return 0xFF0000;
	}

	private Function<BlockState, VoxelShape> makeShapes(float p_55162_) {
		VoxelShape voxelshape = Block.cube(p_55162_);
		Map<Direction, VoxelShape> map = Shapes.rotateAll(Block.boxZ(p_55162_, 0.0, 8.0));
		return this.getShapeForEachState(p_393372_ -> {
			VoxelShape voxelshape1 = voxelshape;

			for (Entry<Direction, BooleanProperty> entry : PipeBlock.PROPERTY_BY_DIRECTION.entrySet()) {
				if (p_393372_.getValue(entry.getValue())) {
					voxelshape1 = Shapes.or(map.get(entry.getKey()), voxelshape1);
				}
			}

			return voxelshape1;
		});
	}

	@Override
	public VoxelShape getHighlightShape(BlockState state, BlockGetter level, BlockPos pos) {
		return highlightShapes.apply(state);
	}
}