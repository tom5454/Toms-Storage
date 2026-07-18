package com.tom.storagemod.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.mojang.serialization.MapCodec;

import com.tom.storagemod.block.entity.InventoryProxyBlockEntity;
import com.tom.storagemod.block.entity.PaintedBlockEntity;
import com.tom.storagemod.client.ClientUtil;

public class InventoryProxyBlock extends BaseEntityBlock implements IPaintable, IConfiguratorHighlight {
	public static final MapCodec<InventoryProxyBlock> CODEC = ChestBlock.simpleCodec(properties -> new InventoryProxyBlock());
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	public InventoryProxyBlock() {
		super(Block.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(3));
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN));
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> tooltip,
			TooltipFlag tooltipFlag) {
		tooltip.add(Component.translatable("tooltip.toms_storage.paintable"));
		ClientUtil.tooltip("inventory_proxy", tooltip);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack item, BlockState state, Level world,
			BlockPos pos, Player player, InteractionHand hand, BlockHitResult p_316140_) {
		if (tryScrape(item, state, world, pos, player, hand)) {
			return ItemInteractionResult.sidedSuccess(world.isClientSide());
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
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
