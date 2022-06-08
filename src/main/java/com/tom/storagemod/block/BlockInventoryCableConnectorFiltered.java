package com.tom.storagemod.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityInventoryCableConnectorFiltered;

public class BlockInventoryCableConnectorFiltered extends BlockInventoryCableConnector {

	public BlockInventoryCableConnectorFiltered() {
		super(false);
	}

	@Override
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip,
			TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.toms_storage.filtered"));
		ClientProxy.tooltip("inventory_cable_connector", tooltip);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityInventoryCableConnectorFiltered(pos, state);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player,
			InteractionHand handIn, BlockHitResult hit) {
		if (world.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		BlockEntity blockEntity_1 = world.getBlockEntity(pos);
		if (blockEntity_1 instanceof MenuProvider) {
			player.openMenu((MenuProvider)blockEntity_1);
		}
		return InteractionResult.SUCCESS;
	}
}
