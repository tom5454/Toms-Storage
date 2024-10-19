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

import com.tom.storagemod.Config;
import com.tom.storagemod.Content;
import com.tom.storagemod.StorageTags;
import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.platform.Platform;

public class WirelessTerminalItem extends Item implements WirelessTerminal {

	public WirelessTerminalItem() {
		super(new Properties().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> tooltip,
			TooltipFlag tooltipFlag) {
		ClientUtil.tooltip("wireless_terminal", tooltip, Config.get().wirelessRange);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		BlockHitResult lookingAt = (BlockHitResult) playerIn.pick(Config.get().wirelessRange, 0f, true);
		BlockState state = worldIn.getBlockState(lookingAt.getBlockPos());
		if(state.is(StorageTags.REMOTE_ACTIVATE)) {
			InteractionResult r = Platform.checkUse(worldIn, lookingAt, playerIn, handIn);
			if(r == null)r = state.useWithoutItem(worldIn, playerIn, lookingAt);
			return new InteractionResultHolder<>(r, playerIn.getItemInHand(handIn));
		} else {
			return super.use(worldIn, playerIn, handIn);
		}
	}

	public static boolean isPlayerHolding(Player player) {
		return player.getMainHandItem().getItem() == Content.wirelessTerminal.get() ||
				player.getOffhandItem().getItem() == Content.wirelessTerminal.get();
	}

	@Override
	public int getRange(Player pl, ItemStack stack) {
		return Config.get().wirelessRange;
	}

	@Override
	public void open(Player sender, ItemStack t) {
	}

	@Override
	public boolean canOpen(ItemStack t) {
		return false;
	}
}
