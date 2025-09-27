package com.tom.storagemod.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.tom.storagemod.Config;
import com.tom.storagemod.Content;
import com.tom.storagemod.StorageTags;
import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.components.WorldPos;

public class AdvWirelessTerminalItem extends Item implements WirelessTerminal {

	public AdvWirelessTerminalItem(Item.Properties pr) {
		super(pr);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, TooltipDisplay p_399753_,
			Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
		ClientUtil.tooltip("adv_wireless_terminal", tooltip, Config.get().advWirelessRange);
		if (Config.get().wirelessTermBeaconLvl != -1) {
			ClientUtil.tooltip("adv_wireless_terminal.beacon", false, tooltip);
			ClientUtil.tooltip("adv_wireless_terminal.beacon1", false, tooltip, Config.get().wirelessTermBeaconLvl);
			if (Config.get().wirelessTermBeaconLvlCrossDim != -1) {
				ClientUtil.tooltip("adv_wireless_terminal.beacon2", false, tooltip, Config.get().wirelessTermBeaconLvlCrossDim);
			}
		}
		WorldPos pos = itemStack.get(Content.boundPosComponent.get());
		if(pos != null) {
			int x = pos.pos().getX();
			int y = pos.pos().getY();
			int z = pos.pos().getZ();
			String dim = pos.dim().location().toString();
			tooltip.accept(Component.translatable("tooltip.toms_storage.adv_wireless_terminal.bound", x, y, z, dim));
		}
		tooltip.accept(Component.translatable("tooltip.toms_storage.adv_wireless_terminal.keybind", Component.translatable("tooltip.toms_storage.adv_wireless_terminal.keybind.outline", Component.keybind("key.toms_storage.open_terminal")).withStyle(ChatFormatting.GREEN)));
	}

	@Override
	public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
		return activateTerminal(worldIn, playerIn.getItemInHand(handIn), playerIn, handIn);
	}

	public static InteractionResult activateTerminal(Level worldIn, ItemStack stack, Player playerIn, InteractionHand handIn) {
		WorldPos pos = stack.get(Content.boundPosComponent.get());
		if(pos != null) {
			if(!worldIn.isClientSide()) {
				int x = pos.pos().getX();
				int y = pos.pos().getY();
				int z = pos.pos().getZ();
				Level termWorld = worldIn.getServer().getLevel(pos.dim());
				if (termWorld.isLoaded(new BlockPos(x, y, z))) {
					BlockHitResult lookingAt = new BlockHitResult(new Vec3(x, y, z), Direction.UP, new BlockPos(x, y, z), true);
					BlockState state = termWorld.getBlockState(lookingAt.getBlockPos());
					if(state.is(StorageTags.REMOTE_ACTIVATE)) {
						InteractionResult r = state.useWithoutItem(termWorld, playerIn, lookingAt);
						return r;
					} else {
						playerIn.displayClientMessage(Component.translatable("chat.toms_storage.terminal_invalid_block"), true);
					}
				} else {
					playerIn.displayClientMessage(Component.translatable("chat.toms_storage.terminal_not_loaded"), true);
				}
			} else {
				return InteractionResult.CONSUME;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResult useOn(UseOnContext c) {
		if(c.isSecondaryUseActive()) {
			if(!c.getLevel().isClientSide()) {
				BlockPos pos = c.getClickedPos();
				BlockState state = c.getLevel().getBlockState(pos);
				if(state.is(StorageTags.REMOTE_ACTIVATE)) {
					ItemStack stack = c.getItemInHand();
					stack.applyComponents(DataComponentPatch.builder().set(Content.boundPosComponent.get(), new WorldPos(c.getLevel().dimension(), pos)).build());
					if(c.getPlayer() != null)
						c.getPlayer().displayClientMessage(Component.translatable("chat.toms_storage.terminal_bound"), true);
					return InteractionResult.SUCCESS;
				}
			} else
				return InteractionResult.CONSUME;
		}
		return InteractionResult.PASS;
	}

	@Override
	public int getRange(Player pl, ItemStack stack) {
		return Config.get().advWirelessRange;
	}

	@Override
	public void open(Player sender, ItemStack t) {
		activateTerminal(sender.level(), t, sender, InteractionHand.MAIN_HAND);
	}

	@Override
	public boolean canOpen(ItemStack t) {
		return true;
	}
}
