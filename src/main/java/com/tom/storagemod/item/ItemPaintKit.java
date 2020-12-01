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
import net.minecraft.world.World;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.IPaintable;
import com.tom.storagemod.proxy.ClientProxy;

public class ItemPaintKit extends Item {

	public ItemPaintKit() {
		super(new Properties().maxDamage(100).group(StorageMod.STORAGE_MOD_TAB));
		setRegistryName("ts.paint_kit");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		ClientProxy.tooltip("paint_kit", tooltip);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		if(!context.getWorld().isRemote) {
			if(context.hasSecondaryUseForPlayer()) {
				BlockState state = context.getWorld().getBlockState(context.getPos());
				TileEntity tile = context.getWorld().getTileEntity(context.getPos());
				if(tile == null && state.isSolid()) {
					ItemStack is = context.getItem();
					if(!is.hasTag())is.setTag(new CompoundNBT());
					is.getTag().put("block", NBTUtil.writeBlockState(state));
					//ITextComponent tc = new TranslationTextComponent("tooltip.toms_storage.set_paint", state.getBlock().getNameTextComponent().applyTextStyle(TextFormatting.GREEN));
					//context.getPlayer().sendStatusMessage(tc, true);
				}
				return ActionResultType.SUCCESS;
			} else {
				BlockState state = context.getWorld().getBlockState(context.getPos());
				ItemStack is = context.getItem();
				if(is.hasTag() && is.getTag().contains("block") && state.getBlock() instanceof IPaintable) {
					if(((IPaintable)state.getBlock()).paint(context.getWorld(), context.getPos(), NBTUtil.readBlockState(is.getTag().getCompound("block")))) {
						PlayerEntity playerentity = context.getPlayer();
						context.getWorld().playSound(playerentity, context.getPos(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
						if(playerentity != null) {
							is.damageItem(1, context.getPlayer(), p -> p.sendBreakAnimation(context.getHand()));
							if(is.isEmpty()) {
								playerentity.setHeldItem(context.getHand(), new ItemStack(Items.BUCKET));
							}
						}
					}
					return ActionResultType.SUCCESS;
				}
			}
		} else {
			BlockState state = context.getWorld().getBlockState(context.getPos());
			if(context.hasSecondaryUseForPlayer())return ActionResultType.SUCCESS;
			if(state.getBlock() instanceof IPaintable) {
				return ActionResultType.SUCCESS;
			}
		}
		return super.onItemUse(context);
	}

	@Override
	public ITextComponent getDisplayName(ItemStack is) {
		IFormattableTextComponent tc = (IFormattableTextComponent) super.getDisplayName(is);
		if(is.hasTag() && is.getTag().contains("block")) {
			BlockState st = NBTUtil.readBlockState(is.getTag().getCompound("block"));
			tc.appendString(" (");
			tc.append(st.getBlock().getTranslatedName().mergeStyle(TextFormatting.GREEN));
			tc.appendString(")");
		}
		return tc;
	}
}
