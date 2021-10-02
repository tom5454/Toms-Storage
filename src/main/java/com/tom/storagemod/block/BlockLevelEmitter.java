package com.tom.storagemod.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityLevelEmitter;

public class BlockLevelEmitter extends ContainerBlock implements IInventoryCable {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public BlockLevelEmitter() {
		super(Block.Properties.of(Material.WOOD).strength(3).noOcclusion().harvestTool(ToolType.AXE));
		setRegistryName("ts.level_emitter");
		registerDefaultState(defaultBlockState()
				.setValue(FACING, Direction.DOWN).setValue(POWERED, Boolean.valueOf(false)));
	}

	@Override
	public void appendHoverText(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		ClientProxy.tooltip("level_emitter", tooltip);
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn) {
		return new TileEntityLevelEmitter();
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	public int getDirectSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		if (!blockState.getValue(POWERED)) {
			return 0;
		} else {
			return blockState.getValue(FACING).getOpposite() == side ? 15 : 0;
		}
	}

	@Override
	public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
		if (!blockState.getValue(POWERED)) {
			return 0;
		} else {
			return 15;
		}
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return state.getValue(FACING) != side;
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
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return defaultBlockState().setValue(FACING, context.getClickedFace());
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

	@Override
	public BlockRenderType getRenderShape(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	public List<BlockPos> next(World world, BlockState state, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public boolean canConnectFrom(BlockState state, Direction dir) {
		return state.getValue(FACING).getOpposite() == dir;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		switch (state.getValue(FACING)) {
		case DOWN:
			return VoxelShapes.or(box(5, 0, 5, 11, 6, 11), box(3, 6, 3, 13, 16, 13));
		case EAST:
			return VoxelShapes.or(box(10, 5, 5, 16, 11, 11), box(0, 3, 3, 10, 13, 13));
		case NORTH:
			return VoxelShapes.or(box(5, 5, 0, 11, 11, 6), box(3, 3, 6, 13, 13, 16));
		case SOUTH:
			return VoxelShapes.or(box(5, 5, 10, 11, 11, 16), box(3, 3, 0, 13, 13, 10));
		case UP:
			return VoxelShapes.or(box(5, 10, 5, 11, 16, 11), box(3, 0, 3, 13, 10, 13));
		case WEST:
			return VoxelShapes.or(box(0, 5, 5, 6, 11, 11), box(6, 3, 3, 16, 13, 13));
		default:
			break;
		}
		return VoxelShapes.block();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (stateIn.getValue(POWERED)) {
			Direction direction = stateIn.getValue(FACING).getOpposite();
			double d0 = pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d1 = pos.getY() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d2 = pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			float f = -7.0F;

			f = f / 16.0F;
			double d3 = f * direction.getStepX();
			double d4 = f * direction.getStepZ();
			worldIn.addParticle(RedstoneParticleData.REDSTONE, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos,
			PlayerEntity player, Hand hand, BlockRayTraceResult rtr) {
		if (world.isClientSide) {
			return ActionResultType.SUCCESS;
		}

		TileEntity blockEntity_1 = world.getBlockEntity(pos);
		if (blockEntity_1 instanceof TileEntityLevelEmitter) {
			player.openMenu((TileEntityLevelEmitter)blockEntity_1);
		}
		return ActionResultType.SUCCESS;
	}
}
