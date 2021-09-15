package com.tom.storagemod.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		ClientProxy.tooltip("wireless_terminal", tooltip);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		BlockHitResult lookingAt = (BlockHitResult) playerIn.pick(Config.wirelessRange, 0f, true);
		BlockState state = worldIn.getBlockState(lookingAt.getBlockPos());
		if(StorageTags.REMOTE_ACTIVATE.contains(state.getBlock())) {
			InteractionResult r = state.use(worldIn, playerIn, handIn, lookingAt);
			return new InteractionResultHolder<>(r, playerIn.getItemInHand(handIn));
		} else {
			return super.use(worldIn, playerIn, handIn);
		}
	}

	public static boolean isPlayerHolding(Player player) {
		return player.getMainHandItem().getItem() == StorageMod.wirelessTerminal ||
				player.getOffhandItem().getItem() == StorageMod.wirelessTerminal;
	}

	@Override
	public int getRange(Player pl, ItemStack stack) {
		return Config.wirelessRange;
	}
}
