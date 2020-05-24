package com.tom.storagemod.item;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageTags;
import com.tom.storagemod.proxy.ClientProxy;

public class ItemWirelessTerminal extends Item {

	public ItemWirelessTerminal() {
		super(new Properties().group(StorageMod.STORAGE_MOD_TAB));
		setRegistryName("ts.wireless_terminal");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		ClientProxy.tooltip("wireless_terminal", tooltip);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
		BlockRayTraceResult lookingAt = (BlockRayTraceResult) playerIn.pick(Config.wirelessRange, 0f, true);
		BlockState state = worldIn.getBlockState(lookingAt.getPos());
		if(StorageTags.REMOTE_ACTIVATE.contains(state.getBlock())) {
			ActionResultType r = state.onBlockActivated(worldIn, playerIn, handIn, lookingAt);
			return new ActionResult<>(r, playerIn.getHeldItem(handIn));
		} else {
			return super.onItemRightClick(worldIn, playerIn, handIn);
		}
	}

	public static boolean isPlayerHolding(PlayerEntity player) {
		return player.getHeldItemMainhand().getItem() == StorageMod.wirelessTerminal ||
				player.getHeldItemOffhand().getItem() == StorageMod.wirelessTerminal;
	}
}
