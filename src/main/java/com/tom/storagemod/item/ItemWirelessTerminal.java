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

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageTags;
import com.tom.storagemod.proxy.ClientProxy;

public class ItemWirelessTerminal extends Item implements WirelessTerminal {

	public ItemWirelessTerminal() {
		super(new Properties().tab(StorageMod.STORAGE_MOD_TAB).stacksTo(1));
		setRegistryName("ts.wireless_terminal");
	}

	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		ClientProxy.tooltip("wireless_terminal", tooltip);
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		BlockRayTraceResult lookingAt = (BlockRayTraceResult) playerIn.pick(Config.wirelessRange, 0f, true);
		BlockState state = worldIn.getBlockState(lookingAt.getBlockPos());
		if(StorageTags.REMOTE_ACTIVATE.contains(state.getBlock())) {
			ActionResultType r = state.use(worldIn, playerIn, handIn, lookingAt);
			return new ActionResult<>(r, playerIn.getItemInHand(handIn));
		} else {
			return super.use(worldIn, playerIn, handIn);
		}
	}

	public static boolean isPlayerHolding(PlayerEntity player) {
		return player.getMainHandItem().getItem() == StorageMod.wirelessTerminal ||
				player.getOffhandItem().getItem() == StorageMod.wirelessTerminal;
	}

	@Override
	public int getRange(PlayerEntity pl, ItemStack stack) {
		return Config.wirelessRange;
	}
	@Override
	public void open(PlayerEntity playerIn, ItemStack t) {}

	@Override
	public boolean canOpen(ItemStack t) {
		return false;
	}
}
