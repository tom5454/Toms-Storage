package com.tom.storagemod.item;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.IPaintable;
import com.tom.storagemod.proxy.ClientProxy;

public class ItemPaintKit extends Item {

	public ItemPaintKit() {
		super(new Properties().durability(100).tab(StorageMod.STORAGE_MOD_TAB));
		setRegistryName("ts.paint_kit");
	}

	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		ClientProxy.tooltip("paint_kit", tooltip);
	}

	@Override
	public ActionResultType useOn(ItemUseContext context) {
		if(!context.getLevel().isClientSide) {
			if(context.isSecondaryUseActive()) {
				BlockState state = context.getLevel().getBlockState(context.getClickedPos());
				TileEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
				if(tile == null && state.canOcclude()) {
					ItemStack is = context.getItemInHand();
					if(!is.hasTag())is.setTag(new CompoundNBT());
					is.getTag().put("block", NBTUtil.writeBlockState(state));
					//ITextComponent tc = new TranslationTextComponent("tooltip.toms_storage.set_paint", state.getBlock().getNameTextComponent().applyTextStyle(TextFormatting.GREEN));
					//context.getPlayer().sendStatusMessage(tc, true);
				}
				return ActionResultType.SUCCESS;
			} else {
				BlockState state = context.getLevel().getBlockState(context.getClickedPos());
				ItemStack is = context.getItemInHand();
				if(is.hasTag() && is.getTag().contains("block") && state.getBlock() instanceof IPaintable) {
					if(((IPaintable)state.getBlock()).paint(context.getLevel(), context.getClickedPos(), NBTUtil.readBlockState(is.getTag().getCompound("block")))) {
						PlayerEntity playerentity = context.getPlayer();
						context.getLevel().playSound(playerentity, context.getClickedPos(), SoundEvents.BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
						if(playerentity != null) {
							is.hurtAndBreak(1, context.getPlayer(), p -> p.broadcastBreakEvent(context.getHand()));
							if(is.isEmpty()) {
								playerentity.setItemInHand(context.getHand(), new ItemStack(Items.BUCKET));
							}
						}
					}
					return ActionResultType.SUCCESS;
				}
			}
		} else {
			BlockState state = context.getLevel().getBlockState(context.getClickedPos());
			if(context.isSecondaryUseActive())return ActionResultType.SUCCESS;
			if(state.getBlock() instanceof IPaintable) {
				return ActionResultType.SUCCESS;
			}
		}
		return super.useOn(context);
	}

	@Override
	public ITextComponent getName(ItemStack is) {
		IFormattableTextComponent tc = (IFormattableTextComponent) super.getName(is);
		if(is.hasTag() && is.getTag().contains("block")) {
			BlockState st = NBTUtil.readBlockState(is.getTag().getCompound("block"));
			tc.append(" (");
			tc.append(new TranslationTextComponent(st.getBlock().getDescriptionId()).withStyle(TextFormatting.GREEN));
			tc.append(")");
		}
		return tc;
	}
}
