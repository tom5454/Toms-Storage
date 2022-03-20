package com.tom.storagemod.block;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.tile.TileEntityInventoryCableConnectorFiltered;

public class BlockInventoryCableConnectorFiltered extends BlockInventoryCableConnector {

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		tooltip.add(new TranslatableText("tooltip.toms_storage.filtered"));
		StorageModClient.tooltip("inventory_cable_connector", tooltip);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityInventoryCableConnectorFiltered(pos, state);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		}

		BlockEntity blockEntity_1 = world.getBlockEntity(pos);
		if (blockEntity_1 instanceof NamedScreenHandlerFactory) {
			player.openHandledScreen((NamedScreenHandlerFactory)blockEntity_1);
		}
		return ActionResult.SUCCESS;
	}
}
