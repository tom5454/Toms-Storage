package com.tom.storagemod.item;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.KeybindText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.StorageTags;

public class ItemAdvWirelessTerminal extends Item implements WirelessTerminal {

	public ItemAdvWirelessTerminal() {
		super(new Settings().group(StorageMod.STORAGE_MOD_TAB).maxCount(1));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, World worldIn, List<Text> tooltip, TooltipContext flagIn) {
		StorageModClient.tooltip("adv_wireless_terminal", tooltip, StorageMod.CONFIG.advWirelessRange, StorageMod.CONFIG.wirelessTermBeaconLvl, StorageMod.CONFIG.wirelessTermBeaconLvlDim);
		if(stack.hasNbt() && stack.getNbt().contains("BindX")) {
			int x = stack.getNbt().getInt("BindX");
			int y = stack.getNbt().getInt("BindY");
			int z = stack.getNbt().getInt("BindZ");
			String dim = stack.getNbt().getString("BindDim");
			tooltip.add(new TranslatableText("tooltip.toms_storage.adv_wireless_terminal.bound", x, y, z, dim));
		}
		tooltip.add(new TranslatableText("tooltip.toms_storage.adv_wireless_terminal.keybind", new TranslatableText("tooltip.toms_storage.adv_wireless_terminal.keybind.outline", new KeybindText("key.toms_storage.open_terminal")).formatted(Formatting.GREEN)));
	}

	@Override
	public TypedActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		return new TypedActionResult<>(activateTerminal(worldIn, playerIn.getStackInHand(handIn), playerIn, handIn), playerIn.getStackInHand(handIn));
	}

	public static ActionResult activateTerminal(World worldIn, ItemStack stack, PlayerEntity playerIn, Hand handIn) {
		if(stack.hasNbt() && stack.getNbt().contains("BindX")) {
			if(!worldIn.isClient) {
				int x = stack.getNbt().getInt("BindX");
				int y = stack.getNbt().getInt("BindY");
				int z = stack.getNbt().getInt("BindZ");
				String dim = stack.getNbt().getString("BindDim");
				World termWorld = worldIn.getServer().getWorld(RegistryKey.of(Registry.WORLD_KEY, new Identifier(dim)));
				if(termWorld.canSetBlock(new BlockPos(x, y, z))) {
					BlockHitResult lookingAt = new BlockHitResult(new Vec3d(x, y, z), Direction.UP, new BlockPos(x, y, z), true);
					BlockState state = termWorld.getBlockState(lookingAt.getBlockPos());
					if(state.isIn(StorageTags.REMOTE_ACTIVATE)) {
						ActionResult r = state.onUse(termWorld, playerIn, handIn, lookingAt);
						return r;
					} else {
						playerIn.sendMessage(new TranslatableText("chat.toms_storage.terminal_invalid_block"), true);
					}
				} else {
					playerIn.sendMessage(new TranslatableText("chat.toms_storage.terminal_out_of_range"), true);
				}
			} else {
				return ActionResult.CONSUME;
			}
		}
		return ActionResult.PASS;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext c) {
		if(c.shouldCancelInteraction()) {
			if(!c.getWorld().isClient) {
				BlockPos pos = c.getBlockPos();
				BlockState state = c.getWorld().getBlockState(pos);
				if(state.isIn(StorageTags.REMOTE_ACTIVATE)) {
					ItemStack stack = c.getStack();
					if(!stack.hasNbt())stack.setNbt(new NbtCompound());
					stack.getNbt().putInt("BindX", pos.getX());
					stack.getNbt().putInt("BindY", pos.getY());
					stack.getNbt().putInt("BindZ", pos.getZ());
					stack.getNbt().putString("BindDim", c.getWorld().getRegistryKey().getValue().toString());
					if(c.getPlayer() != null)
						c.getPlayer().sendMessage(new TranslatableText("chat.toms_storage.terminal_bound"), true);
					return ActionResult.SUCCESS;
				}
			} else
				return ActionResult.CONSUME;
		}
		return ActionResult.PASS;
	}

	@Override
	public int getRange(PlayerEntity pl, ItemStack stack) {
		return StorageMod.CONFIG.advWirelessRange;
	}

	@Override
	public void open(PlayerEntity sender, ItemStack t) {
		activateTerminal(sender.world, t, sender, Hand.MAIN_HAND);
	}

	@Override
	public boolean canOpen(ItemStack t) {
		return true;
	}
}
