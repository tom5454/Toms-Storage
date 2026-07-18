package com.tom.storagemod.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tom.storagemod.block.entity.PaintedBlockEntity;

public class FramedInventoryCableConnectorBlock extends InventoryCableConnectorBlock implements IPaintable, IConfiguratorHighlight {

	public FramedInventoryCableConnectorBlock() {
		super(false);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> tooltip,
			TooltipFlag tooltipFlag) {
		tooltip.add(Component.translatable("tooltip.toms_storage.paintable"));
		super.appendHoverText(itemStack, tooltipContext, tooltip, tooltipFlag);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack item, BlockState state, Level world,
			BlockPos pos, Player player, InteractionHand hand, BlockHitResult p_316140_) {
		if (tryScrape(item, state, world, pos, player, hand)) {
			return ItemInteractionResult.sidedSuccess(world.isClientSide());
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public boolean paint(Level world, BlockPos pos, BlockState to) {
		BlockEntity te = world.getBlockEntity(pos);
		if(te != null && te instanceof PaintedBlockEntity)
			return ((PaintedBlockEntity)te).setPaintedBlockState(to);
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}

	@Override
	public int getHighlightColor() {
		return 0xFFFF00;
	}

	@Override
	public VoxelShape getHighlightShape(BlockState state, BlockGetter level, BlockPos pos) {
		return super.getShape(state, level, pos, null);
	}
}