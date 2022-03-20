package com.tom.storagemod.block;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.tile.TileEntityPainted;

public class BlockInventoryCableFramed extends Block implements IInventoryCable, IPaintable {
	public static final BooleanProperty UP = Properties.UP;
	public static final BooleanProperty DOWN = Properties.DOWN;
	public static final BooleanProperty NORTH = Properties.NORTH;
	public static final BooleanProperty SOUTH = Properties.SOUTH;
	public static final BooleanProperty EAST = Properties.EAST;
	public static final BooleanProperty WEST = Properties.WEST;

	public BlockInventoryCableFramed() {
		super(Block.Settings.of(Material.WOOD).strength(2).nonOpaque());
		setDefaultState(getDefaultState()
				.with(DOWN, false)
				.with(UP, false)
				.with(NORTH, false)
				.with(EAST, false)
				.with(SOUTH, false)
				.with(WEST, false));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		tooltip.add(new TranslatableText("tooltip.toms_storage.paintable"));
		StorageModClient.tooltip("inventory_cable", tooltip);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST);
	}

	@Override
	public List<BlockPos> next(World world, BlockState state, BlockPos pos) {
		List<BlockPos> next = new ArrayList<>();
		for (Direction d : Direction.values()) {
			if(state.get(BlockInventoryCable.DIR_TO_PROPERTY[d.ordinal()]))next.add(pos.offset(d));
		}
		return next;
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
		return stateIn.with(ConnectingBlock.FACING_PROPERTIES.get(facing), IInventoryCable.canConnect(facingState, facing));
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return withConnectionProperties(context.getWorld(), context.getBlockPos());
	}

	public BlockState withConnectionProperties(WorldAccess blockView_1, BlockPos blockPos_1) {
		BlockState block_1 = blockView_1.getBlockState(blockPos_1.down());
		BlockState block_2 = blockView_1.getBlockState(blockPos_1.up());
		BlockState block_3 = blockView_1.getBlockState(blockPos_1.north());
		BlockState block_4 = blockView_1.getBlockState(blockPos_1.east());
		BlockState block_5 = blockView_1.getBlockState(blockPos_1.south());
		BlockState block_6 = blockView_1.getBlockState(blockPos_1.west());

		return getDefaultState()
				.with(DOWN, IInventoryCable.canConnect(block_1, Direction.DOWN))
				.with(UP, IInventoryCable.canConnect(block_2, Direction.UP))
				.with(NORTH, IInventoryCable.canConnect(block_3, Direction.NORTH))
				.with(EAST, IInventoryCable.canConnect(block_4, Direction.EAST))
				.with(SOUTH, IInventoryCable.canConnect(block_5, Direction.SOUTH))
				.with(WEST, IInventoryCable.canConnect(block_6, Direction.WEST));
	}

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
	public boolean paint(World world, BlockPos pos, BlockState to) {
		world.setBlockState(pos, StorageMod.invCablePainted.getDefaultState(), 2);
		BlockEntity te = world.getBlockEntity(pos);
		if(te != null && te instanceof TileEntityPainted)
			return ((TileEntityPainted)te).setPaintedBlockState(to);
		return false;
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
		return false;
	}
}
