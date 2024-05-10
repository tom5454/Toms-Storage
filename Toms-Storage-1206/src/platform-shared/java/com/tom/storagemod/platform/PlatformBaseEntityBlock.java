package com.tom.storagemod.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class PlatformBaseEntityBlock extends BaseEntityBlock {

	protected PlatformBaseEntityBlock(Properties properties) {
		super(properties);
	}

	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult hit) {
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
		return use(blockState, level, blockPos, player, InteractionHand.MAIN_HAND, blockHitResult);
	}

	public static InteractionResult use(BlockState state, Level termWorld, Player playerIn, InteractionHand handIn, BlockHitResult lookingAt) {
		var res = state.useItemOn(playerIn.getItemInHand(handIn), termWorld, playerIn, handIn, lookingAt);
		if (res == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION)
			return state.useWithoutItem(termWorld, playerIn, lookingAt);
		return res.result();
	}
}
