package com.tom.storagemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.mojang.serialization.MapCodec;

import com.tom.storagemod.block.entity.BasicInventoryHopperBlockEntity;
import com.tom.storagemod.item.IItemFilter;

public class BasicInventoryHopperBlock extends AbstractInventoryHopperBlock {
	public static final MapCodec<BasicInventoryHopperBlock> CODEC = simpleCodec(BasicInventoryHopperBlock::new);

	public BasicInventoryHopperBlock(Block.Properties pr) {
		super(pr);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BasicInventoryHopperBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack is, BlockState state, Level world,
			BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (is.isEmpty())return InteractionResult.TRY_WITH_EMPTY_HAND;
		if(!world.isClientSide()) {
			BlockEntity te = world.getBlockEntity(pos);
			if(te instanceof BasicInventoryHopperBlockEntity h) {
				if(!h.getFilter().isEmpty() && h.getFilter().getItem() instanceof IItemFilter) {
					popResource(world, pos, h.getFilter());
				}
				h.setFilter(is.copy());
				if(is.getItem() instanceof IItemFilter) {
					player.setItemInHand(hand, ItemStack.EMPTY);
				}
				Component txt = h.getFilter().getHoverName();
				player.displayClientMessage(Component.translatable("tooltip.toms_storage.filter_item", txt), true);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player,
			BlockHitResult hit) {
		if(!world.isClientSide()) {
			BlockEntity te = world.getBlockEntity(pos);
			if(te instanceof BasicInventoryHopperBlockEntity h) {
				if(player.isShiftKeyDown()) {
					if(!h.getFilter().isEmpty() && h.getFilter().getItem() instanceof IItemFilter) {
						player.getInventory().add(h.getFilter());
					}
					h.setFilter(ItemStack.EMPTY);
					player.displayClientMessage(Component.translatable("tooltip.toms_storage.filter_item", Component.translatable("tooltip.toms_storage.empty")), true);
				} else {
					ItemStack s = h.getFilter();
					Component txt = s.isEmpty() ? Component.translatable("tooltip.toms_storage.empty") : s.getHoverName();
					player.displayClientMessage(Component.translatable("tooltip.toms_storage.filter_item", txt), true);
				}
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}
}
