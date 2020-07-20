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
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityPainted;

public class BlockInventoryCableFramed extends ContainerBlock implements IInventoryCable, IPaintable {
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;

	public BlockInventoryCableFramed() {
		super(Block.Properties.create(Material.WOOD).hardnessAndResistance(2).notSolid().harvestTool(ToolType.AXE));
		setRegistryName("ts.inventory_cable_framed");
		setDefaultState(getDefaultState()
				.with(DOWN, false)
				.with(UP, false)
				.with(NORTH, false)
				.with(EAST, false)
				.with(SOUTH, false)
				.with(WEST, false));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("tooltip.toms_storage.paintable"));
		ClientProxy.tooltip("inventory_cable", tooltip);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
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
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		return stateIn.with(SixWayBlock.FACING_TO_PROPERTY_MAP.get(facing), IInventoryCable.canConnect(facingState, facing.getOpposite()));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return withConnectionProperties(context.getWorld(), context.getPos());
	}

	public BlockState withConnectionProperties(IWorld blockView_1, BlockPos blockPos_1) {
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
	public boolean paint(World world, BlockPos pos, BlockState to) {
		TileEntity te = world.getTileEntity(pos);
		if(te != null && te instanceof TileEntityPainted)
			return ((TileEntityPainted)te).setPaintedBlockState(to);
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new TileEntityPainted();
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
		return false;
	}


}
