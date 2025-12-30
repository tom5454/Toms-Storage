package com.tom.storagemod.block;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.mojang.serialization.MapCodec;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.entity.PaintedBlockEntity;
import com.tom.storagemod.client.ClientUtil;

public class PaintedTrimBlock extends BaseEntityBlock implements IPaintable, BlockWithTooltip, IConfiguratorHighlight {
	public static final MapCodec<PaintedTrimBlock> CODEC = simpleCodec(PaintedTrimBlock::new);

	public PaintedTrimBlock(Block.Properties pr) {
		super(pr);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, Consumer<Component> tooltip,
			TooltipFlag tooltipFlag) {
		tooltip.accept(Component.translatable("tooltip.toms_storage.paintable"));
		ClientUtil.tooltip("trim", tooltip);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack item, BlockState state, Level world,
			BlockPos pos, Player player, InteractionHand hand, BlockHitResult p_316140_) {
		if (tryScrape(item, state, world, pos, player, hand)) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
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
		return new PaintedBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_149645_1_) {
		return RenderShape.MODEL;
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader p_382795_, BlockPos p_383120_, BlockState p_382830_,
			boolean p_388788_) {
		return new ItemStack(Content.inventoryTrim.get());
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public int getHighlightColor() {
		return 0x888888;
	}

	@Override
	public VoxelShape getHighlightShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.block();
	}
}
