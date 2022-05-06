package com.tom.storagemod.block;

import java.util.Collections;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.TickerUtil;
import com.tom.storagemod.tile.TileEntityInventoryConnector;

public class InventoryConnector extends BlockWithEntity implements IInventoryCable {

	public InventoryConnector() {
		super(Block.Settings.of(Material.WOOD).strength(3));//.harvestTool(ToolType.AXE)
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityInventoryConnector(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state,
			BlockEntityType<T> type) {
		return TickerUtil.createTicker(world, false, true);
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		StorageModClient.tooltip("inventory_connector", tooltip);
	}

	@Override
	public List<BlockPos> next(World world, BlockState state, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockHitResult hit) {
		if(!world.isClient) {
			BlockEntity tile = world.getBlockEntity(pos);
			if(tile instanceof TileEntityInventoryConnector) {
				TileEntityInventoryConnector te = (TileEntityInventoryConnector) tile;
				player.sendMessage(Text.translatable("chat.toms_storage.inventory_connector.free_slots", te.getFreeSlotCount(), te.size()), true);
			}
		}
		return ActionResult.SUCCESS;
	}
}
