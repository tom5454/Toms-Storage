package com.tom.storagemod.block;

import java.util.Collections;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.TickerUtil;
import com.tom.storagemod.tile.LevelEmitterBlockEntity;

public class LevelEmitterBlock extends BaseEntityBlock implements IInventoryCable {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public LevelEmitterBlock() {
		super(Block.Properties.of(Material.WOOD).strength(3).noOcclusion());
		registerDefaultState(defaultBlockState()
				.setValue(FACING, Direction.DOWN).setValue(POWERED, Boolean.valueOf(false)));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip,
			TooltipFlag flagIn) {
		StorageModClient.tooltip("level_emitter", tooltip);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos paramBlockPos, BlockState paramBlockState) {
		return new LevelEmitterBlockEntity(paramBlockPos, paramBlockState);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state,
			BlockEntityType<T> type) {
		return TickerUtil.createTicker(world, false, true);
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		if (!blockState.getValue(POWERED)) {
			return 0;
		} else {
			return blockState.getValue(FACING).getOpposite() == side ? 15 : 0;
		}
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
		if (!blockState.getValue(POWERED)) {
			return 0;
		} else {
			return 15;
		}
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
		return defaultBlockState().setValue(FACING, context.getClickedFace());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_149645_1_) {
		return RenderShape.MODEL;
	}

	@Override
	public List<BlockPos> next(Level world, BlockState state, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public boolean canConnectFrom(BlockState state, Direction dir) {
		return state.getValue(FACING).getOpposite() == dir;
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
	@Environment(EnvType.CLIENT)
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
		if (stateIn.getValue(POWERED)) {
			Direction direction = stateIn.getValue(FACING).getOpposite();
			double d0 = pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d1 = pos.getY() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d2 = pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			float f = -7.0F;

			f = f / 16.0F;
			double d3 = f * direction.getStepX();
			double d4 = f * direction.getStepZ();
			worldIn.addParticle(DustParticleOptions.REDSTONE, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult rtr) {
		if (world.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		BlockEntity blockEntity_1 = world.getBlockEntity(pos);
		if (blockEntity_1 instanceof LevelEmitterBlockEntity) {
			player.openMenu((LevelEmitterBlockEntity)blockEntity_1);
		}
		return InteractionResult.SUCCESS;
	}
}
