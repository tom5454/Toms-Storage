package com.tom.storagemod.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityInventoryCableConnectorFiltered;

public class BlockInventoryCableConnectorFiltered extends ContainerBlock implements IInventoryCable {
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	//public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);
	private static final Direction[] FACING_VALUES = Direction.values();
	protected VoxelShape[][] shapes;

	public BlockInventoryCableConnectorFiltered() {
		super(Block.Properties.create(Material.WOOD).hardnessAndResistance(3).notSolid().harvestTool(ToolType.AXE));
		setRegistryName("ts.inventory_cable_connector_filtered");
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
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("tooltip.toms_storage.filtered"));
		ClientProxy.tooltip("inventory_cable_connector", tooltip);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new TileEntityInventoryCableConnectorFiltered();
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST, FACING);//COLOR
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		Direction f = stateIn.get(FACING);
		if(facing == f)
			return stateIn.with(SixWayBlock.FACING_TO_PROPERTY_MAP.get(facing), !facingState.isAir(worldIn, facingPos));
		else
			return stateIn.with(SixWayBlock.FACING_TO_PROPERTY_MAP.get(facing), IInventoryCable.canConnect(facingState, facing.getOpposite()));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return withConnectionProperties(getDefaultState().with(FACING, context.getFace().getOpposite()), context.getWorld(), context.getPos())
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

	public BlockState withConnectionProperties(BlockState state, IWorld blockView_1, BlockPos blockPos_1) {
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
	public BlockState rotate(BlockState blockState_1, Rotation blockRotation_1) {
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
	public BlockState mirror(BlockState blockState_1, Mirror blockMirror_1) {
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
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return this.shapes[state.get(FACING).ordinal()][this.getShapeIndex(state)];
	}

	protected int getShapeIndex(BlockState state) {
		int i = 0;

		for(int j = 0; j < FACING_VALUES.length; ++j) {
			if (state.get(SixWayBlock.FACING_TO_PROPERTY_MAP.get(FACING_VALUES[j]))) {
				i |= 1 << j;
			}
		}

		return i;
	}
	private VoxelShape[][] makeShapes(float apothem) {
		float f = 0.5F - apothem;
		float f1 = 0.5F + apothem;
		VoxelShape voxelshape = Block.makeCuboidShape(f * 16.0F, f * 16.0F, f * 16.0F, f1 * 16.0F, f1 * 16.0F, f1 * 16.0F);
		VoxelShape[] avoxelshape = new VoxelShape[FACING_VALUES.length];

		for(int i = 0; i < FACING_VALUES.length; ++i) {
			Direction direction = FACING_VALUES[i];
			avoxelshape[i] = VoxelShapes.create(0.5D + Math.min((-apothem), direction.getXOffset() * 0.5D), 0.5D + Math.min((-apothem), direction.getYOffset() * 0.5D), 0.5D + Math.min((-apothem), direction.getZOffset() * 0.5D), 0.5D + Math.max(apothem, direction.getXOffset() * 0.5D), 0.5D + Math.max(apothem, direction.getYOffset() * 0.5D), 0.5D + Math.max(apothem, direction.getZOffset() * 0.5D));
		}

		VoxelShape[] avoxelshape1 = new VoxelShape[64];

		for(int k = 0; k < 64; ++k) {
			VoxelShape voxelshape1 = voxelshape;

			for(int j = 0; j < FACING_VALUES.length; ++j) {
				if ((k & 1 << j) != 0) {
					voxelshape1 = VoxelShapes.or(voxelshape1, avoxelshape[j]);
				}
			}

			avoxelshape1[k] = voxelshape1;
		}

		VoxelShape[][] ret = new VoxelShape[6][64];

		for(int i = 0; i < FACING_VALUES.length; ++i) {
			Direction direction = FACING_VALUES[i];
			VoxelShape s = VoxelShapes.or(createShape(direction, 16, 0, 16, 0, 2, 0),
					createShape(direction, 10, 3, 10, 3, 2, 2),
					createShape(direction, 6, 5, 6, 5, 2, 4));
			for (int j = 0; j < avoxelshape1.length; j++) {
				ret[i][j] = VoxelShapes.or(avoxelshape1[j], s);
			}
		}

		return ret;
	}

	private static VoxelShape createShape(Direction dir, float width, float widthoff, float height, float heightoff, float depth, float depthoff) {
		switch (dir) {
		case DOWN:
			return Block.makeCuboidShape(heightoff, depthoff, widthoff, height+heightoff, depth+depthoff, width+widthoff);
		case EAST:
			return Block.makeCuboidShape(16f-depth, heightoff, widthoff, 16f-depthoff, height+heightoff, width+widthoff);
		case NORTH:
			return Block.makeCuboidShape(widthoff, heightoff, depthoff, width+widthoff, height+heightoff, depth+depthoff);
		case SOUTH:
			return Block.makeCuboidShape(widthoff, heightoff, 16f-depth, width+widthoff, height+heightoff, 16f-depthoff);
		case UP:
			return Block.makeCuboidShape(heightoff, 16f-depth, widthoff, height+heightoff, 16-depthoff, width+widthoff);
		case WEST:
			return Block.makeCuboidShape(depthoff, heightoff, widthoff, depth+depthoff, height+heightoff, width+widthoff);
		default:
			break;
		}
		return Block.makeCuboidShape(0, 0, 0, 16, 16, 16);
	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult hit) {
		if (world.isRemote) {
			return ActionResultType.SUCCESS;
		}

		TileEntity blockEntity_1 = world.getTileEntity(pos);
		if (blockEntity_1 instanceof INamedContainerProvider) {
			player.openContainer((INamedContainerProvider)blockEntity_1);
		}
		return ActionResultType.SUCCESS;
	}
}
