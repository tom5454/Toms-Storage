package com.tom.storagemod.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tom.storagemod.Config;
import com.tom.storagemod.TickerUtil;
import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityInventoryCableConnector;

public class BlockInventoryCableConnector extends BaseEntityBlock implements IInventoryCable {
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	private static final Direction[] FACING_VALUES = Direction.values();
	protected VoxelShape[][] shapes;

	public BlockInventoryCableConnector() {
		this(false);
	}

	protected BlockInventoryCableConnector(boolean dummy) {
		super(Block.Properties.of(Material.WOOD).strength(3).noOcclusion());
		this.shapes = this.makeShapes(0.125f);
		registerDefaultState(defaultBlockState()
				.setValue(DOWN, false)
				.setValue(UP, false)
				.setValue(NORTH, false)
				.setValue(EAST, false)
				.setValue(SOUTH, false)
				.setValue(WEST, false)
				.setValue(FACING, Direction.DOWN));
	}

	@Override
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip,
			TooltipFlag flagIn) {
		ClientProxy.tooltip("inventory_cable_connector", tooltip);
		ClientProxy.tooltip("inventory_cable_connector_link", false, tooltip, Config.invLinkBeaconLvl, Config.invLinkBeaconLvlDim);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityInventoryCableConnector(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state,
			BlockEntityType<T> type) {
		return TickerUtil.createTicker(world, false, true);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_149645_1_) {
		return RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, FACING);//COLOR
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		Direction f = stateIn.getValue(FACING);
		if(facing == f)
			return stateIn.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(facing), !facingState.isAir());
		else
			return stateIn.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(facing), IInventoryCable.canConnect(facingState, facing));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return withConnectionProperties(defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite()), context.getLevel(), context.getClickedPos())
				;
	}

	@Override
	public boolean canConnectFrom(BlockState state, Direction dir) {
		return state.getValue(FACING) != dir;
	}

	@Override
	public List<BlockPos> next(Level world, BlockState state, BlockPos pos) {
		Direction f = state.getValue(FACING);
		List<BlockPos> next = new ArrayList<>();
		for (Direction d : Direction.values()) {
			if(d != f && state.getValue(BlockInventoryCable.DIR_TO_PROPERTY[d.ordinal()]))next.add(pos.relative(d));
		}
		return next;
	}

	public BlockState withConnectionProperties(BlockState state, LevelAccessor blockView_1, BlockPos blockPos_1) {
		BlockState block_1 = blockView_1.getBlockState(blockPos_1.below());
		BlockState block_2 = blockView_1.getBlockState(blockPos_1.above());
		BlockState block_3 = blockView_1.getBlockState(blockPos_1.north());
		BlockState block_4 = blockView_1.getBlockState(blockPos_1.east());
		BlockState block_5 = blockView_1.getBlockState(blockPos_1.south());
		BlockState block_6 = blockView_1.getBlockState(blockPos_1.west());

		return state
				.setValue(DOWN, canConnect(state, block_1, Direction.DOWN))
				.setValue(UP, canConnect(state, block_2, Direction.UP))
				.setValue(NORTH, canConnect(state, block_3, Direction.NORTH))
				.setValue(EAST, canConnect(state, block_4, Direction.EAST))
				.setValue(SOUTH, canConnect(state, block_5, Direction.SOUTH))
				.setValue(WEST, canConnect(state, block_6, Direction.WEST));
	}

	@SuppressWarnings("deprecation")
	private boolean canConnect(BlockState state, BlockState block, Direction dir) {
		Direction f = state.getValue(FACING);
		return (dir != f && IInventoryCable.canConnect(block, dir)) || (dir == f && !block.isAir());
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

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return this.shapes[state.getValue(FACING).ordinal()][this.getShapeIndex(state)];
	}

	protected int getShapeIndex(BlockState state) {
		int i = 0;

		for(int j = 0; j < FACING_VALUES.length; ++j) {
			if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(FACING_VALUES[j]))) {
				i |= 1 << j;
			}
		}

		return i;
	}
	private VoxelShape[][] makeShapes(float apothem) {
		float f = 0.5F - apothem;
		float f1 = 0.5F + apothem;
		VoxelShape voxelshape = Block.box(f * 16.0F, f * 16.0F, f * 16.0F, f1 * 16.0F, f1 * 16.0F, f1 * 16.0F);
		VoxelShape[] avoxelshape = new VoxelShape[FACING_VALUES.length];

		for(int i = 0; i < FACING_VALUES.length; ++i) {
			Direction direction = FACING_VALUES[i];
			avoxelshape[i] = Shapes.box(0.5D + Math.min((-apothem), direction.getStepX() * 0.5D), 0.5D + Math.min((-apothem), direction.getStepY() * 0.5D), 0.5D + Math.min((-apothem), direction.getStepZ() * 0.5D), 0.5D + Math.max(apothem, direction.getStepX() * 0.5D), 0.5D + Math.max(apothem, direction.getStepY() * 0.5D), 0.5D + Math.max(apothem, direction.getStepZ() * 0.5D));
		}

		VoxelShape[] avoxelshape1 = new VoxelShape[64];

		for(int k = 0; k < 64; ++k) {
			VoxelShape voxelshape1 = voxelshape;

			for(int j = 0; j < FACING_VALUES.length; ++j) {
				if ((k & 1 << j) != 0) {
					voxelshape1 = Shapes.or(voxelshape1, avoxelshape[j]);
				}
			}

			avoxelshape1[k] = voxelshape1;
		}

		VoxelShape[][] ret = new VoxelShape[6][64];

		for(int i = 0; i < FACING_VALUES.length; ++i) {
			Direction direction = FACING_VALUES[i];
			VoxelShape s = Shapes.or(createShape(direction, 16, 0, 16, 0, 2, 0),
					createShape(direction, 10, 3, 10, 3, 2, 2),
					createShape(direction, 6, 5, 6, 5, 2, 4));
			for (int j = 0; j < avoxelshape1.length; j++) {
				ret[i][j] = Shapes.or(avoxelshape1[j], s);
			}
		}

		return ret;
	}

	private static VoxelShape createShape(Direction dir, float width, float widthoff, float height, float heightoff, float depth, float depthoff) {
		switch (dir) {
		case DOWN:
			return box(heightoff, depthoff, widthoff, height+heightoff, depth+depthoff, width+widthoff);
		case EAST:
			return box(16f-depth, heightoff, widthoff, 16f-depthoff, height+heightoff, width+widthoff);
		case NORTH:
			return box(widthoff, heightoff, depthoff, width+widthoff, height+heightoff, depth+depthoff);
		case SOUTH:
			return box(widthoff, heightoff, 16f-depth, width+widthoff, height+heightoff, 16f-depthoff);
		case UP:
			return box(heightoff, 16f-depth, widthoff, height+heightoff, 16-depthoff, width+widthoff);
		case WEST:
			return box(depthoff, heightoff, widthoff, depth+depthoff, height+heightoff, width+widthoff);
		default:
			break;
		}
		return Block.box(0, 0, 0, 16, 16, 16);
	}

	public static VoxelShape box(double xMin, double yMin, double zMin, double xMax, double yMax, double zMax) {
		return Block.box(Math.min(xMin, xMax), Math.min(yMin, yMax), Math.min(zMin, zMax), Math.max(xMin, xMax), Math.max(yMin, yMax), Math.max(zMin, zMax));
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
			InteractionHand handIn, BlockHitResult hit) {
		Direction f = state.getValue(FACING);
		BlockState pointedAt = world.getBlockState(pos.relative(f));
		if(player.getItemInHand(handIn).isEmpty() && pointedAt.is(Blocks.BEACON)) {
			if (world.isClientSide) {
				return InteractionResult.SUCCESS;
			}

			BlockEntity blockEntity_1 = world.getBlockEntity(pos);
			if (blockEntity_1 instanceof TileEntityInventoryCableConnector) {
				TileEntityInventoryCableConnector te = (TileEntityInventoryCableConnector) blockEntity_1;
				if(te.stillValid(player))
					player.openMenu(te);
				else {
					player.displayClientMessage(Component.translatable("chat.toms_storage.inv_link_access_denied"), true);
					return InteractionResult.PASS;
				}
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
