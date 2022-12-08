package com.tom.storagemod.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.StorageTags;

public class AdvWirelessTerminalItem extends Item implements WirelessTerminal {

	public AdvWirelessTerminalItem() {
		super(new Properties().tab(StorageMod.STORAGE_MOD_TAB).stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		StorageModClient.tooltip("adv_wireless_terminal", tooltip, Config.advWirelessRange, Config.wirelessTermBeaconLvl, Config.wirelessTermBeaconLvlDim);
		if(stack.hasTag() && stack.getTag().contains("BindX")) {
			int x = stack.getTag().getInt("BindX");
			int y = stack.getTag().getInt("BindY");
			int z = stack.getTag().getInt("BindZ");
			String dim = stack.getTag().getString("BindDim");
			tooltip.add(Component.translatable("tooltip.toms_storage.adv_wireless_terminal.bound", x, y, z, dim));
		}
		tooltip.add(Component.translatable("tooltip.toms_storage.adv_wireless_terminal.keybind", Component.translatable("tooltip.toms_storage.adv_wireless_terminal.keybind.outline", Component.keybind("key.toms_storage.open_terminal")).withStyle(ChatFormatting.GREEN)));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
		return new InteractionResultHolder<>(activateTerminal(worldIn, playerIn.getItemInHand(handIn), playerIn, handIn), playerIn.getItemInHand(handIn));
	}

	public static InteractionResult activateTerminal(Level worldIn, ItemStack stack, Player playerIn, InteractionHand handIn) {
		if(stack.hasTag() && stack.getTag().contains("BindX")) {
			if(!worldIn.isClientSide) {
				int x = stack.getTag().getInt("BindX");
				int y = stack.getTag().getInt("BindY");
				int z = stack.getTag().getInt("BindZ");
				String dim = stack.getTag().getString("BindDim");
				Level termWorld = worldIn.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dim)));
				if(termWorld.isLoaded(new BlockPos(x, y, z))) {
					BlockHitResult lookingAt = new BlockHitResult(new Vec3(x, y, z), Direction.UP, new BlockPos(x, y, z), true);
					BlockState state = termWorld.getBlockState(lookingAt.getBlockPos());
					if(state.is(StorageTags.REMOTE_ACTIVATE)) {
						InteractionResult r = state.use(termWorld, playerIn, handIn, lookingAt);
						return r;
					} else {
						playerIn.displayClientMessage(Component.translatable("chat.toms_storage.terminal_invalid_block"), true);
					}
				} else {
					playerIn.displayClientMessage(Component.translatable("chat.toms_storage.terminal_out_of_range"), true);
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
			if(!c.getLevel().isClientSide) {
				BlockPos pos = c.getClickedPos();
				BlockState state = c.getLevel().getBlockState(pos);
				if(state.is(StorageTags.REMOTE_ACTIVATE)) {
					ItemStack stack = c.getItemInHand();
					if(!stack.hasTag())stack.setTag(new CompoundTag());
					stack.getTag().putInt("BindX", pos.getX());
					stack.getTag().putInt("BindY", pos.getY());
					stack.getTag().putInt("BindZ", pos.getZ());
					stack.getTag().putString("BindDim", c.getLevel().dimension().location().toString());
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
		return Config.advWirelessRange;
	}

	@Override
	public void open(Player sender, ItemStack t) {
		activateTerminal(sender.level, t, sender, InteractionHand.MAIN_HAND);
	}

	@Override
	public boolean canOpen(ItemStack t) {
		return true;
	}
}
