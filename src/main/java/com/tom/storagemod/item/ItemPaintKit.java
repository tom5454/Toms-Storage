package com.tom.storagemod.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
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

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.IPaintable;
import com.tom.storagemod.proxy.ClientProxy;

public class ItemPaintKit extends Item {

	public ItemPaintKit() {
		super(new Properties().durability(100).tab(StorageMod.STORAGE_MOD_TAB));
	}

	@Override
	public void appendHoverText(ItemStack p_41421_, Level p_41422_, List<Component> tooltip, TooltipFlag p_41424_) {
		ClientProxy.tooltip("paint_kit", tooltip);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if(!context.getLevel().isClientSide) {
			if(context.isSecondaryUseActive()) {
				BlockState state = context.getLevel().getBlockState(context.getClickedPos());
				BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
				if(tile == null && state.canOcclude() && Block.isShapeFullBlock(state.getShape(context.getLevel(), context.getClickedPos()))) {
					ItemStack is = context.getItemInHand();
					if(!is.hasTag())is.setTag(new CompoundTag());
					is.getTag().put("block", NbtUtils.writeBlockState(state));
				}
				return InteractionResult.SUCCESS;
			} else {
				BlockState state = context.getLevel().getBlockState(context.getClickedPos());
				ItemStack is = context.getItemInHand();
				if(is.hasTag() && is.getTag().contains("block") && state.getBlock() instanceof IPaintable) {
					if(((IPaintable)state.getBlock()).paint(context.getLevel(), context.getClickedPos(), NbtUtils.readBlockState(is.getTag().getCompound("block")))) {
						Player playerentity = context.getPlayer();
						context.getLevel().playSound(playerentity, context.getClickedPos(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
						if(playerentity != null) {
							is.hurtAndBreak(1, context.getPlayer(), p -> p.broadcastBreakEvent(context.getHand()));
							if(is.isEmpty()) {
								playerentity.setItemInHand(context.getHand(), new ItemStack(Items.BUCKET));
							}
						}
					}
					return InteractionResult.SUCCESS;
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
		if(is.hasTag() && is.getTag().contains("block")) {
			BlockState st = NbtUtils.readBlockState(is.getTag().getCompound("block"));
			tc.append(" (");
			tc.append(st.getBlock().getName().withStyle(ChatFormatting.GREEN));
			tc.append(")");
		}
		return tc;
	}
}
