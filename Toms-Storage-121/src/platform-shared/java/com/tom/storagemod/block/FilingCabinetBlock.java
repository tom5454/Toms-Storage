package com.tom.storagemod.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;

import com.mojang.serialization.MapCodec;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.entity.FilingCabinetBlockEntity;
import com.tom.storagemod.client.ClientUtil;

public class FilingCabinetBlock extends BaseEntityBlock {
	public static final MapCodec<FilingCabinetBlock> CODEC = ChestBlock.simpleCodec(properties -> new FilingCabinetBlock());
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

	public FilingCabinetBlock() {
		super(Block.Properties.of().mapColor(MapColor.STONE).sound(SoundType.STONE).strength(3));
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> tooltip,
			TooltipFlag tooltipFlag) {
		ItemContainerContents c = itemStack.get(DataComponents.CONTAINER);
		if (c != null)
			tooltip.add(Component.translatable("tooltip.toms_storage.content_stored"));
		ClientUtil.tooltip("filing_cabinet", tooltip);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_149645_1_) {
		return RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (world.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		BlockEntity blockEntity_1 = world.getBlockEntity(pos);
		if (blockEntity_1 instanceof FilingCabinetBlockEntity be) {
			player.openMenu(be);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new FilingCabinetBlockEntity(p_153215_, p_153216_);
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockState playerWillDestroy(Level p_56212_, BlockPos p_56213_, BlockState p_56214_, Player p_56215_) {
		BlockEntity blockentity = p_56212_.getBlockEntity(p_56213_);
		if (blockentity instanceof FilingCabinetBlockEntity shulkerboxblockentity) {
			if (!p_56212_.isClientSide && p_56215_.isCreative() && !shulkerboxblockentity.getInv().isEmpty()) {
				ItemStack itemstack = new ItemStack(this);
				itemstack.applyComponents(blockentity.collectComponents());
				ItemEntity itementity = new ItemEntity(
						p_56212_, p_56213_.getX() + 0.5, p_56213_.getY() + 0.5, p_56213_.getZ() + 0.5, itemstack
						);
				itementity.setDefaultPickUpDelay();
				p_56212_.addFreshEntity(itementity);
			}
		}

		return super.playerWillDestroy(p_56212_, p_56213_, p_56214_, p_56215_);
	}

	@Override
	protected List<ItemStack> getDrops(BlockState p_287632_, LootParams.Builder p_287691_) {
		BlockEntity blockentity = p_287691_.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
		if (blockentity instanceof FilingCabinetBlockEntity shulkerboxblockentity) {
			p_287691_ = p_287691_.withDynamicDrop(ShulkerBoxBlock.CONTENTS, p_56219_ -> {
				for (int i = 0; i < shulkerboxblockentity.getInv().getContainerSize(); i++) {
					p_56219_.accept(shulkerboxblockentity.getInv().getItem(i));
				}
			});
		}

		return super.getDrops(p_287632_, p_287691_);
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader p_304539_, BlockPos p_56203_, BlockState p_56204_) {
		ItemStack itemstack = super.getCloneItemStack(p_304539_, p_56203_, p_56204_);
		p_304539_.getBlockEntity(p_56203_, Content.filingCabinetBE.get()).ifPresent(p_323411_ -> p_323411_.saveToItem(itemstack, p_304539_.registryAccess()));
		return itemstack;
	}
}
