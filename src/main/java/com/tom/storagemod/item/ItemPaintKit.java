package com.tom.storagemod.item;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.block.IPaintable;

public class ItemPaintKit extends Item {

	public ItemPaintKit() {
		super(new Settings().maxDamage(100).group(StorageMod.STORAGE_MOD_TAB));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, World worldIn, List<Text> tooltip, TooltipContext flagIn) {
		StorageModClient.tooltip("paint_kit", tooltip);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if(!context.getWorld().isClient) {
			if(context.shouldCancelInteraction()) {
				BlockState state = context.getWorld().getBlockState(context.getBlockPos());
				BlockEntity tile = context.getWorld().getBlockEntity(context.getBlockPos());
				if(tile == null && state.isFullCube(context.getWorld(), context.getBlockPos())) {
					ItemStack is = context.getStack();
					if(!is.hasTag())is.setTag(new NbtCompound());
					is.getTag().put("block", NbtHelper.fromBlockState(state));
					//ITextComponent tc = new TranslationTextComponent("tooltip.toms_storage.set_paint", state.getBlock().getNameTextComponent().applyTextStyle(TextFormatting.GREEN));
					//context.getPlayer().sendStatusMessage(tc, true);
				}
				return ActionResult.SUCCESS;
			} else {
				BlockState state = context.getWorld().getBlockState(context.getBlockPos());
				ItemStack is = context.getStack();
				if(is.hasTag() && is.getTag().contains("block") && state.getBlock() instanceof IPaintable) {
					if(((IPaintable)state.getBlock()).paint(context.getWorld(), context.getBlockPos(), NbtHelper.toBlockState(is.getTag().getCompound("block")))) {
						PlayerEntity playerentity = context.getPlayer();
						context.getWorld().playSound(playerentity, context.getBlockPos(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
						if(playerentity != null) {
							is.damage(1, context.getPlayer(), p -> p.sendToolBreakStatus(context.getHand()));
							if(is.isEmpty()) {
								playerentity.setStackInHand(context.getHand(), new ItemStack(Items.BUCKET));
							}
						}
					}
					return ActionResult.SUCCESS;
				}
			}
		} else {
			BlockState state = context.getWorld().getBlockState(context.getBlockPos());
			if(context.shouldCancelInteraction())return ActionResult.SUCCESS;
			if(state.getBlock() instanceof IPaintable) {
				return ActionResult.SUCCESS;
			}
		}
		return super.useOnBlock(context);
	}

	@Override
	public Text getName(ItemStack is) {
		Text tcS = super.getName(is);
		MutableText tc = (MutableText) tcS;
		if(is.hasTag() && is.getTag().contains("block")) {
			BlockState st = NbtHelper.toBlockState(is.getTag().getCompound("block"));
			tc.append(" (");
			tc.append(st.getBlock().getName().formatted(Formatting.GREEN));
			tc.append(")");
		}
		return tc;
	}
}
