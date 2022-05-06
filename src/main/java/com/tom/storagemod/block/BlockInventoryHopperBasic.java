package com.tom.storagemod.block;

import java.util.Collections;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
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
import com.tom.storagemod.TickerUtil;
import com.tom.storagemod.tile.TileEntityInventoryHopperBasic;

public class BlockInventoryHopperBasic extends BlockWithEntity implements IInventoryCable {
	public static final DirectionProperty FACING = Properties.FACING;
	public static final BooleanProperty ENABLED = Properties.ENABLED;

	public BlockInventoryHopperBasic() {
		super(Block.Settings.of(Material.WOOD).strength(3).nonOpaque());
		setDefaultState(getDefaultState()
				.with(FACING, Direction.DOWN).with(ENABLED, Boolean.valueOf(true)));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		StorageModClient.tooltip("inventory_hopper", tooltip);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityInventoryHopperBasic(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return TickerUtil.createTicker(world, false, true);
	}

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
		return getDefaultState().with(FACING, context.getSide().getOpposite());
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, ENABLED);
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
		return state.get(FACING).getAxis() == dir.getAxis();
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
	public ActionResult onUse(BlockState state, World world, BlockPos pos,
			PlayerEntity player, Hand hand, BlockHitResult rtr) {
		if(!world.isClient) {
			ItemStack is = player.getStackInHand(hand);
			if(!is.isEmpty()) {
				BlockEntity te = world.getBlockEntity(pos);
				if(te instanceof TileEntityInventoryHopperBasic) {
					((TileEntityInventoryHopperBasic)te).setFilter(is.copy());
					Text txt = ((TileEntityInventoryHopperBasic)te).getFilter().getName();
					player.sendMessage(Text.translatable("tooltip.toms_storage.filter_item", txt), true);
				}
			} else {
				BlockEntity te = world.getBlockEntity(pos);
				if(te instanceof TileEntityInventoryHopperBasic) {
					if(player.isSneaking()) {
						((TileEntityInventoryHopperBasic)te).setFilter(ItemStack.EMPTY);
						player.sendMessage(Text.translatable("tooltip.toms_storage.filter_item", Text.translatable("tooltip.toms_storage.empty")), true);
					} else {
						ItemStack s = ((TileEntityInventoryHopperBasic)te).getFilter();
						Text txt = s.isEmpty() ? Text.translatable("tooltip.toms_storage.empty") : s.getName();
						player.sendMessage(Text.translatable("tooltip.toms_storage.filter_item", txt), true);
					}
				}
			}
		}
		return ActionResult.SUCCESS;
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		updateEnabled(world, pos, state);
	}

	private void updateEnabled(World world, BlockPos pos, BlockState state) {
		boolean bl = !world.isReceivingRedstonePower(pos);
		if (bl != state.get(ENABLED).booleanValue()) {
			world.setBlockState(pos, state.with(ENABLED, Boolean.valueOf(bl)), 4);
		}
	}
}
