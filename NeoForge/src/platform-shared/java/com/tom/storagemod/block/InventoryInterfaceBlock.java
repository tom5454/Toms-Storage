package com.tom.storagemod.block;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;

import com.mojang.serialization.MapCodec;

import com.tom.storagemod.block.entity.InventoryInterfaceBlockEntity;
import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.inventory.InventoryCableNetwork;
import com.tom.storagemod.util.TickerUtil;

public class InventoryInterfaceBlock extends BaseEntityBlock implements IInventoryCable, IInventoryNode, NeoForgeBlock, BlockWithTooltip {
	public static final MapCodec<InventoryInterfaceBlock> CODEC = simpleCodec(InventoryInterfaceBlock::new);

	public InventoryInterfaceBlock(Block.Properties pr) {
		super(pr);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new InventoryInterfaceBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state,
			BlockEntityType<T> type) {
		return TickerUtil.createTicker(world, false, true);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, Consumer<Component> tooltip,
			TooltipFlag tooltipFlag) {
		ClientUtil.tooltip("inventory_interface", tooltip);
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
	public void destroy(LevelAccessor p_49860_, BlockPos p_49861_, BlockState p_49862_) {
		if (p_49860_ instanceof ServerLevel l)
			InventoryCableNetwork.getNetwork(l).markNodeInvalid(p_49861_);
	}

	@Override
	protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block,
			@Nullable Orientation orientation, boolean bl) {
		super.neighborChanged(blockState, level, blockPos, block, orientation, bl);
		if (!level.isClientSide()) {
			InventoryCableNetwork n = InventoryCableNetwork.getNetwork(level);
			n.markNodeInvalid(blockPos);
			if (orientation != null) {
				for (var d : orientation.getDirections()) {
					n.markNodeInvalid(blockPos.relative(d));
				}
			} else {
				for (var d : Direction.values()) {
					n.markNodeInvalid(blockPos.relative(d));
				}
			}
		}
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
		if (level instanceof ServerLevel l) {
			InventoryCableNetwork n = InventoryCableNetwork.getNetwork(l);
			n.markNodeInvalid(pos);
			n.markNodeInvalid(neighbor);
		}
	}
}
