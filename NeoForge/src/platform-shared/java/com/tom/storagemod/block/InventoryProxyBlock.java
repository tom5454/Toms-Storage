package com.tom.storagemod.block;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.mojang.serialization.MapCodec;

import com.tom.storagemod.block.entity.InventoryProxyBlockEntity;
import com.tom.storagemod.block.entity.PaintedBlockEntity;
import com.tom.storagemod.client.ClientUtil;

public class InventoryProxyBlock extends BaseEntityBlock implements IPaintable, BlockWithTooltip, IConfiguratorHighlight {
	public static final MapCodec<InventoryProxyBlock> CODEC = simpleCodec(InventoryProxyBlock::new);
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;

	public InventoryProxyBlock(Block.Properties pr) {
		super(pr);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN));
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, Consumer<Component> tooltip,
			TooltipFlag tooltipFlag) {
		tooltip.accept(Component.translatable("tooltip.toms_storage.paintable"));
		ClientUtil.tooltip("inventory_proxy", tooltip);
	}

	@Override
	public boolean paint(Level world, BlockPos pos, BlockState to) {
		BlockEntity te = world.getBlockEntity(pos);
		if(te != null && te instanceof PaintedBlockEntity)
			return ((PaintedBlockEntity)te).setPaintedBlockState(to);
		return false;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new InventoryProxyBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_149645_1_) {
		return RenderShape.MODEL;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
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
	public int getHighlightColor() {
		return 0x880088;
	}

	@Override
	public VoxelShape getHighlightShape(BlockState state, BlockGetter level, BlockPos pos) {
		switch (state.getValue(FACING)) {
		case DOWN:
			return Shapes.or(box(4, 2, 4, 12, 12, 12), box(2, 0, 2, 14, 2, 14));
		case EAST:
			return Shapes.or(box(4, 4, 4, 14, 12, 12), box(14, 2, 2, 16, 14, 14));
		case NORTH:
			return Shapes.or(box(4, 4, 2, 12, 12, 12), box(2, 2, 0, 14, 14, 2));
		case SOUTH:
			return Shapes.or(box(4, 4, 4, 12, 12, 14), box(2, 2, 14, 14, 14, 16));
		case UP:
			return Shapes.or(box(4, 4, 4, 12, 14, 12), box(2, 14, 2, 14, 16, 14));
		case WEST:
			return Shapes.or(box(2, 4, 4, 12, 12, 12), box(0, 2, 2, 2, 14, 14));
		default:
			break;
		}
		return Shapes.block();
	}
}
