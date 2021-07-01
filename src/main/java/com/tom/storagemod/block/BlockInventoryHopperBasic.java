package com.tom.storagemod.block;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.world.World;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityInventoryHopperBasic;

public class BlockInventoryHopperBasic extends ContainerBlock implements IInventoryCable {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;
	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;

	public BlockInventoryHopperBasic() {
		super(Block.Properties.of(Material.WOOD).strength(3).noOcclusion().harvestTool(ToolType.AXE));
		setRegistryName("ts.inventory_hopper_basic");
		registerDefaultState(defaultBlockState()
				.setValue(FACING, Direction.DOWN).setValue(ENABLED, Boolean.valueOf(true)));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		ClientProxy.tooltip("inventory_hopper", tooltip);
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn) {
		return new TileEntityInventoryHopperBasic();
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
		return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, ENABLED);
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
		return state.getValue(FACING).getAxis() == dir.getAxis();
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
	public ActionResultType use(BlockState st, World world, BlockPos pos,
			PlayerEntity player, Hand hand, BlockRayTraceResult trace) {
		if(!world.isClientSide) {
			ItemStack is = player.getItemInHand(hand);
			if(!is.isEmpty()) {
				TileEntity te = world.getBlockEntity(pos);
				if(te instanceof TileEntityInventoryHopperBasic) {
					((TileEntityInventoryHopperBasic)te).setFilter(is.copy());
					ITextComponent txt = ((TileEntityInventoryHopperBasic)te).getFilter().getHoverName();
					player.displayClientMessage(new TranslationTextComponent("tooltip.toms_storage.filter_item", txt), true);
				}
			} else {
				TileEntity te = world.getBlockEntity(pos);
				if(te instanceof TileEntityInventoryHopperBasic) {
					if(player.isShiftKeyDown()) {
						((TileEntityInventoryHopperBasic)te).setFilter(ItemStack.EMPTY);
						player.displayClientMessage(new TranslationTextComponent("tooltip.toms_storage.filter_item", new TranslationTextComponent("tooltip.toms_storage.empty")), true);
					} else {
						ItemStack s = ((TileEntityInventoryHopperBasic)te).getFilter();
						ITextComponent txt = s.isEmpty() ? new TranslationTextComponent("tooltip.toms_storage.empty") : s.getHoverName();
						player.displayClientMessage(new TranslationTextComponent("tooltip.toms_storage.filter_item", txt), true);
					}
				}
			}
		}
		return ActionResultType.SUCCESS;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		this.updateState(worldIn, pos, state);
	}

	private void updateState(World worldIn, BlockPos pos, BlockState state) {
		boolean flag = !worldIn.hasNeighborSignal(pos);
		if (flag != state.getValue(ENABLED)) {
			worldIn.setBlock(pos, state.setValue(ENABLED, Boolean.valueOf(flag)), 4);
		}
	}
}
