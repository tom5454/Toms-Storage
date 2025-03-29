package com.tom.storagemod.block;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.MapCodec;

import com.tom.storagemod.block.entity.CraftingTerminalBlockEntity;
import com.tom.storagemod.client.ClientUtil;

public class CraftingTerminalBlock extends AbstractStorageTerminalBlock {
	public static final MapCodec<CraftingTerminalBlock> CODEC = simpleCodec(CraftingTerminalBlock::new);

	public CraftingTerminalBlock(Block.Properties pr) {
		super(pr);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CraftingTerminalBlockEntity(pos, state);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, Consumer<Component> tooltip,
			TooltipFlag tooltipFlag) {
		ClientUtil.tooltip("crafting_terminal", tooltip);
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState p_393880_, ServerLevel p_393720_, BlockPos p_394177_, boolean p_394178_) {
		Containers.updateNeighboursAfterDestroy(p_393880_, p_393720_, p_394177_);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}
}
