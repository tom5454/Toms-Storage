package com.tom.storagemod.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
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

import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.tile.TileEntityLevelEmitter;

public class BlockLevelEmitter extends BlockWithEntity implements IInventoryCable {
	public static final DirectionProperty FACING = Properties.FACING;
	public static final BooleanProperty POWERED = Properties.POWERED;

	public BlockLevelEmitter() {
		super(Block.Settings.of(Material.WOOD).strength(3).nonOpaque());//.harvestTool(ToolType.AXE)
		setDefaultState(getDefaultState()
				.with(FACING, Direction.DOWN).with(POWERED, Boolean.valueOf(false)));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		StorageModClient.tooltip("level_emitter", tooltip);
	}

	@Override
	public BlockEntity createBlockEntity(BlockView worldIn) {
		return new TileEntityLevelEmitter();
	}

	@Override
	public boolean emitsRedstonePower(BlockState state) {
		return true;
	}

	@Override
	public int getStrongRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
		if (!blockState.get(POWERED)) {
			return 0;
		} else {
			return blockState.get(FACING).getOpposite() == side ? 15 : 0;
		}
	}

	@Override
	public int getWeakRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
		if (!blockState.get(POWERED)) {
			return 0;
		} else {
			return 15;
		}
	}

	/*@Override
	public boolean canConnectRedstone(BlockState state, BlockView world, BlockPos pos, Direction side) {
		return state.get(FACING) != side;
	}*/

	@Override
	public BlockState rotate(BlockState state, BlockRotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.get(FACING)));
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return getDefaultState().with(FACING, context.getSide());
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	public List<BlockPos> next(World world, BlockState state, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public boolean canConnectFrom(BlockState state, Direction dir) {
		return state.get(FACING).getOpposite() == dir;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		switch (state.get(FACING)) {
		case DOWN:
			return VoxelShapes.union(createCuboidShape(5, 0, 5, 11, 6, 11), createCuboidShape(3, 6, 3, 13, 16, 13));
		case EAST:
			return VoxelShapes.union(createCuboidShape(10, 5, 5, 16, 11, 11), createCuboidShape(0, 3, 3, 10, 13, 13));
		case NORTH:
			return VoxelShapes.union(createCuboidShape(5, 5, 0, 11, 11, 6), createCuboidShape(3, 3, 6, 13, 13, 16));
		case SOUTH:
			return VoxelShapes.union(createCuboidShape(5, 5, 10, 11, 11, 16), createCuboidShape(3, 3, 0, 13, 13, 10));
		case UP:
			return VoxelShapes.union(createCuboidShape(5, 10, 5, 11, 16, 11), createCuboidShape(3, 0, 3, 13, 10, 13));
		case WEST:
			return VoxelShapes.union(createCuboidShape(0, 5, 5, 6, 11, 11), createCuboidShape(6, 3, 3, 16, 13, 13));
		default:
			break;
		}
		return VoxelShapes.fullCube();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (stateIn.get(POWERED)) {
			Direction direction = stateIn.get(FACING).getOpposite();
			double d0 = pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d1 = pos.getY() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d2 = pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			float f = -7.0F;

			f = f / 16.0F;
			double d3 = f * direction.getOffsetX();
			double d4 = f * direction.getOffsetZ();
			worldIn.addParticle(DustParticleEffect.DEFAULT, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos,
			PlayerEntity player, Hand hand, BlockHitResult rtr) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}

		BlockEntity blockEntity_1 = world.getBlockEntity(pos);
		if (blockEntity_1 instanceof TileEntityLevelEmitter) {
			player.openHandledScreen((TileEntityLevelEmitter)blockEntity_1);
		}
		return ActionResult.SUCCESS;
	}
}
