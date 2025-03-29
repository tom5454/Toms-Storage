package com.tom.storagemod.block;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.MapCodec;

import com.tom.storagemod.Content;
import com.tom.storagemod.client.ClientUtil;

public class TrimBlock extends Block implements IPaintable, BlockWithTooltip {
	public static final MapCodec<TrimBlock> CODEC = simpleCodec(TrimBlock::new);

	public TrimBlock(Block.Properties pr) {
		super(pr);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, Consumer<Component> tooltip,
			TooltipFlag tooltipFlag) {
		tooltip.accept(Component.translatable("tooltip.toms_storage.paintable"));
		ClientUtil.tooltip("trim", tooltip);
	}

	@Override
	public boolean paint(Level world, BlockPos pos, BlockState to) {
		world.setBlockAndUpdate(pos, Content.paintedTrim.get().defaultBlockState());
		return Content.paintedTrim.get().paint(world, pos, to);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}
}
