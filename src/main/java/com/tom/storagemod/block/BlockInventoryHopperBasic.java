package com.tom.storagemod.block;

import java.util.Collections;
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

import com.tom.storagemod.TickerUtil;
import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityInventoryHopperBasic;

public class BlockInventoryHopperBasic extends BaseEntityBlock implements IInventoryCable {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

	public BlockInventoryHopperBasic() {
		super(Block.Properties.of(Material.WOOD).strength(3).noOcclusion());
		registerDefaultState(defaultBlockState()
				.setValue(FACING, Direction.DOWN).setValue(ENABLED, Boolean.valueOf(true)));
	}

	@Override
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip,
			TooltipFlag flagIn) {
		ClientProxy.tooltip("inventory_hopper", tooltip);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityInventoryHopperBasic(pos, state);
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
	public List<BlockPos> next(Level world, BlockState state, BlockPos pos) {
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
	public InteractionResult use(BlockState st, Level world, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult trace) {
		if(!world.isClientSide) {
			ItemStack is = player.getItemInHand(hand);
			if(!is.isEmpty()) {
				BlockEntity te = world.getBlockEntity(pos);
				if(te instanceof TileEntityInventoryHopperBasic) {
					((TileEntityInventoryHopperBasic)te).setFilter(is.copy());
					Component txt = ((TileEntityInventoryHopperBasic)te).getFilter().getHoverName();
					player.displayClientMessage(Component.translatable("tooltip.toms_storage.filter_item", txt), true);
				}
			} else {
				BlockEntity te = world.getBlockEntity(pos);
				if(te instanceof TileEntityInventoryHopperBasic) {
					if(player.isShiftKeyDown()) {
						((TileEntityInventoryHopperBasic)te).setFilter(ItemStack.EMPTY);
						player.displayClientMessage(Component.translatable("tooltip.toms_storage.filter_item", Component.translatable("tooltip.toms_storage.empty")), true);
					} else {
						ItemStack s = ((TileEntityInventoryHopperBasic)te).getFilter();
						Component txt = s.isEmpty() ? Component.translatable("tooltip.toms_storage.empty") : s.getHoverName();
						player.displayClientMessage(Component.translatable("tooltip.toms_storage.filter_item", txt), true);
					}
				}
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		this.updateState(worldIn, pos, state);
	}

	private void updateState(Level worldIn, BlockPos pos, BlockState state) {
		boolean flag = !worldIn.hasNeighborSignal(pos);
		if (flag != state.getValue(ENABLED)) {
			worldIn.setBlock(pos, state.setValue(ENABLED, Boolean.valueOf(flag)), 4);
		}
	}
}
