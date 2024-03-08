package com.tom.storagemod.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Content;
import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.block.IPaintable;

public class PaintKitItem extends Item {

	public PaintKitItem() {
		super(new Properties().durability(100));
	}

	@Override
	public void appendHoverText(ItemStack p_41421_, Level p_41422_, List<Component> tooltip, TooltipFlag p_41424_) {
		StorageModClient.tooltip("paint_kit", tooltip);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if(!context.getLevel().isClientSide) {
			if(context.isSecondaryUseActive()) {
				BlockState state = context.getLevel().getBlockState(context.getClickedPos());
				BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
				if(tile == null && state.canOcclude() && Block.isShapeFullBlock(state.getShape(context.getLevel(), context.getClickedPos()))) {
					ItemStack is = context.getItemInHand();
					is.applyComponents(DataComponentPatch.builder().set(Content.paintComponent.get(), state).build());
				}
				return InteractionResult.SUCCESS;
			} else {
				BlockState state = context.getLevel().getBlockState(context.getClickedPos());
				ItemStack is = context.getItemInHand();
				if (state.getBlock() instanceof IPaintable) {
					BlockState st = is.get(Content.paintComponent.get());
					if (st != null) {
						if(((IPaintable)state.getBlock()).paint(context.getLevel(), context.getClickedPos(), st)) {
							Player playerentity = context.getPlayer();
							context.getLevel().playSound(playerentity, context.getClickedPos(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
							if(playerentity != null) {
								is.hurtAndBreak(1, context.getPlayer(), context.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
								if(is.isEmpty()) {
									playerentity.setItemInHand(context.getHand(), new ItemStack(Items.BUCKET));
								}
							}
						}
						return InteractionResult.SUCCESS;
					}
				}
			}
		} else {
			BlockState state = context.getLevel().getBlockState(context.getClickedPos());
			if(context.isSecondaryUseActive())return InteractionResult.SUCCESS;
			if(state.getBlock() instanceof IPaintable) {
				return InteractionResult.SUCCESS;
			}
		}
		return super.useOn(context);
	}

	@Override
	public Component getName(ItemStack is) {
		MutableComponent tc = (MutableComponent) super.getName(is);
		BlockState st = is.get(Content.paintComponent.get());
		if (st != null) {
			tc.append(" (");
			tc.append(st.getBlock().getName().withStyle(ChatFormatting.GREEN));
			tc.append(")");
		}
		return tc;
	}
}
