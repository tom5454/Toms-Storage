package com.tom.storagemod.block;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.TickerUtil;
import com.tom.storagemod.tile.InventoryConnectorBlockEntity;

public class InventoryConnectorBlock extends BaseEntityBlock implements IInventoryCable {

	public InventoryConnectorBlock() {
		super(Block.Properties.of(Material.WOOD).strength(3));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new InventoryConnectorBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state,
			BlockEntityType<T> type) {
		return TickerUtil.createTicker(world, false, true);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_149645_1_) {
		return RenderShape.MODEL;
	}

	@Override
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip,
			TooltipFlag flagIn) {
		StorageModClient.tooltip("inventory_connector", tooltip);
	}

	@Override
	public List<BlockPos> next(Level world, BlockState state, BlockPos pos) {
		return Collections.emptyList();
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player,
			InteractionHand handIn, BlockHitResult hit) {
		if(!worldIn.isClientSide) {
			BlockEntity tile = worldIn.getBlockEntity(pos);
			if(tile instanceof InventoryConnectorBlockEntity) {
				InventoryConnectorBlockEntity te = (InventoryConnectorBlockEntity) tile;
				player.displayClientMessage(Component.translatable("chat.toms_storage.inventory_connector.free_slots", te.getFreeSlotCount(), te.getInvSize()), true);
			}
		}
		return InteractionResult.SUCCESS;
	}
}
