package com.tom.storagemod.block;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.tile.TileEntityInventoryCableConnectorFiltered;

public class BlockInventoryCableConnectorFiltered extends BlockWithEntity implements IInventoryCable {
	public static final BooleanProperty UP = Properties.UP;
	public static final BooleanProperty DOWN = Properties.DOWN;
	public static final BooleanProperty NORTH = Properties.NORTH;
	public static final BooleanProperty SOUTH = Properties.SOUTH;
	public static final BooleanProperty EAST = Properties.EAST;
	public static final BooleanProperty WEST = Properties.WEST;
	public static final DirectionProperty FACING = Properties.FACING;
	//public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);
	private static final Direction[] FACING_VALUES = Direction.values();
	protected VoxelShape[][] shapes;

	public BlockInventoryCableConnectorFiltered() {
		super(Block.Settings.of(Material.WOOD).strength(3).nonOpaque());//.harvestTool(ToolType.AXE)
		this.shapes = this.makeShapes(0.125f);
		setDefaultState(getDefaultState()
				.with(DOWN, false)
				.with(UP, false)
				.with(NORTH, false)
				.with(EAST, false)
				.with(SOUTH, false)
				.with(WEST, false)
				.with(FACING, Direction.DOWN));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		tooltip.add(new TranslatableText("tooltip.toms_storage.filtered"));
		StorageModClient.tooltip("inventory_cable_connector", tooltip);
	}

	@Override
	public BlockEntity createBlockEntity(BlockView worldIn) {
		return new TileEntityInventoryCableConnectorFiltered();
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, FACING);//COLOR
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
		Direction f = stateIn.get(FACING);
		if(facing == f)
			return stateIn.with(ConnectingBlock.FACING_PROPERTIES.get(facing), !facingState.isAir());
		else
			return stateIn.with(ConnectingBlock.FACING_PROPERTIES.get(facing), IInventoryCable.canConnect(facingState, facing.getOpposite()));
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return withConnectionProperties(getDefaultState().with(FACING, context.getSide().getOpposite()), context.getWorld(), context.getBlockPos())
				//with(COLOR, context.getItem().hasTag() ? DyeColor.byId(context.getItem().getTag().getInt("color")) : DyeColor.WHITE).
				;
	}

	@Override
	public boolean canConnectFrom(BlockState state, Direction dir) {
		return state.get(FACING) != dir.getOpposite();
	}

	@Override
	public List<BlockPos> next(World world, BlockState state, BlockPos pos) {
		Direction f = state.get(FACING);
		List<BlockPos> next = new ArrayList<>();
		for (Direction d : Direction.values()) {
			if(d != f && state.get(BlockInventoryCable.DIR_TO_PROPERTY[d.ordinal()]))next.add(pos.offset(d));
		}
		return next;
	}

	public BlockState withConnectionProperties(BlockState state, WorldAccess blockView_1, BlockPos blockPos_1) {
		BlockState block_1 = blockView_1.getBlockState(blockPos_1.down());
		BlockState block_2 = blockView_1.getBlockState(blockPos_1.up());
		BlockState block_3 = blockView_1.getBlockState(blockPos_1.north());
		BlockState block_4 = blockView_1.getBlockState(blockPos_1.east());
		BlockState block_5 = blockView_1.getBlockState(blockPos_1.south());
		BlockState block_6 = blockView_1.getBlockState(blockPos_1.west());

		return state
				.with(DOWN, canConnect(state, block_1, Direction.DOWN))
				.with(UP, canConnect(state, block_2, Direction.UP))
				.with(NORTH, canConnect(state, block_3, Direction.NORTH))
				.with(EAST, canConnect(state, block_4, Direction.EAST))
				.with(SOUTH, canConnect(state, block_5, Direction.SOUTH))
				.with(WEST, canConnect(state, block_6, Direction.WEST));
	}

	@SuppressWarnings("deprecation")
	private boolean canConnect(BlockState state, BlockState block, Direction dir) {
		Direction f = state.get(FACING);
		return (dir != f && IInventoryCable.canConnect(block, dir)) || (dir == f && !block.isAir());
	}

	/*@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		//this.shapes = this.makeShapes(0.125f);
		ItemStack held = player.getHeldItem(handIn);
		DyeColor color = DyeColor.getColor(held);
		if(color != null) {
			if(!player.isCreative())held.shrink(1);
			worldIn.setBlockState(pos, state.with(COLOR, color));
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}*/

	@Override
	public BlockState rotate(BlockState blockState_1, BlockRotation blockRotation_1) {
		switch (blockRotation_1) {
		case CLOCKWISE_180:
			return blockState_1.with(NORTH, blockState_1.get(SOUTH)).with(EAST, blockState_1.get(WEST)).with(SOUTH, blockState_1.get(NORTH)).with(WEST, blockState_1.get(EAST));
		case CLOCKWISE_90:
			return blockState_1.with(NORTH, blockState_1.get(EAST)).with(EAST, blockState_1.get(SOUTH)).with(SOUTH, blockState_1.get(WEST)).with(WEST, blockState_1.get(NORTH));
		case COUNTERCLOCKWISE_90:
			return blockState_1.with(NORTH, blockState_1.get(WEST)).with(EAST, blockState_1.get(NORTH)).with(SOUTH, blockState_1.get(EAST)).with(WEST, blockState_1.get(SOUTH));
		default:
			break;
		}
		return blockState_1;
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState mirror(BlockState blockState_1, BlockMirror blockMirror_1) {
		switch (blockMirror_1) {
		case FRONT_BACK:
			return blockState_1.with(NORTH, blockState_1.get(SOUTH)).with(SOUTH, blockState_1.get(NORTH));
		case LEFT_RIGHT:
			return blockState_1.with(EAST, blockState_1.get(WEST)).with(WEST, blockState_1.get(EAST));
		default:
			break;
		}
		return super.mirror(blockState_1, blockMirror_1);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return this.shapes[state.get(FACING).ordinal()][this.getShapeIndex(state)];
	}

	protected int getShapeIndex(BlockState state) {
		int i = 0;

		for(int j = 0; j < FACING_VALUES.length; ++j) {
			if (state.get(ConnectingBlock.FACING_PROPERTIES.get(FACING_VALUES[j]))) {
				i |= 1 << j;
			}
		}

		return i;
	}
	private VoxelShape[][] makeShapes(float apothem) {
		float f = 0.5F - apothem;
		float f1 = 0.5F + apothem;
		VoxelShape voxelshape = Block.createCuboidShape(f * 16.0F, f * 16.0F, f * 16.0F, f1 * 16.0F, f1 * 16.0F, f1 * 16.0F);
		VoxelShape[] avoxelshape = new VoxelShape[FACING_VALUES.length];

		for(int i = 0; i < FACING_VALUES.length; ++i) {
			Direction direction = FACING_VALUES[i];
			avoxelshape[i] = VoxelShapes.cuboid(0.5D + Math.min((-apothem), direction.getOffsetX() * 0.5D), 0.5D + Math.min((-apothem), direction.getOffsetY() * 0.5D), 0.5D + Math.min((-apothem), direction.getOffsetZ() * 0.5D), 0.5D + Math.max(apothem, direction.getOffsetX() * 0.5D), 0.5D + Math.max(apothem, direction.getOffsetY() * 0.5D), 0.5D + Math.max(apothem, direction.getOffsetZ() * 0.5D));
		}

		VoxelShape[] avoxelshape1 = new VoxelShape[64];

		for(int k = 0; k < 64; ++k) {
			VoxelShape voxelshape1 = voxelshape;

			for(int j = 0; j < FACING_VALUES.length; ++j) {
				if ((k & 1 << j) != 0) {
					voxelshape1 = VoxelShapes.union(voxelshape1, avoxelshape[j]);
				}
			}

			avoxelshape1[k] = voxelshape1;
		}

		VoxelShape[][] ret = new VoxelShape[6][64];

		for(int i = 0; i < FACING_VALUES.length; ++i) {
			Direction direction = FACING_VALUES[i];
			VoxelShape s = VoxelShapes.union(createShape(direction, 16, 0, 16, 0, 2, 0),
					createShape(direction, 10, 3, 10, 3, 2, 2),
					createShape(direction, 6, 5, 6, 5, 2, 4));
			for (int j = 0; j < avoxelshape1.length; j++) {
				ret[i][j] = VoxelShapes.union(avoxelshape1[j], s);
			}
		}

		return ret;
	}

	private static VoxelShape createShape(Direction dir, float width, float widthoff, float height, float heightoff, float depth, float depthoff) {
		switch (dir) {
		case DOWN:
			return Block.createCuboidShape(heightoff, depthoff, widthoff, height+heightoff, depth+depthoff, width+widthoff);
		case EAST:
			return Block.createCuboidShape(16f-depth, heightoff, widthoff, 16f-depthoff, height+heightoff, width+widthoff);
		case NORTH:
			return Block.createCuboidShape(widthoff, heightoff, depthoff, width+widthoff, height+heightoff, depth+depthoff);
		case SOUTH:
			return Block.createCuboidShape(widthoff, heightoff, 16f-depth, width+widthoff, height+heightoff, 16f-depthoff);
		case UP:
			return Block.createCuboidShape(heightoff, 16f-depth, widthoff, height+heightoff, 16-depthoff, width+widthoff);
		case WEST:
			return Block.createCuboidShape(depthoff, heightoff, widthoff, depth+depthoff, height+heightoff, width+widthoff);
		default:
			break;
		}
		return Block.createCuboidShape(0, 0, 0, 16, 16, 16);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}

		BlockEntity blockEntity_1 = world.getBlockEntity(pos);
		if (blockEntity_1 instanceof NamedScreenHandlerFactory) {
			player.openHandledScreen((NamedScreenHandlerFactory)blockEntity_1);
		}
		return ActionResult.SUCCESS;
	}
}
