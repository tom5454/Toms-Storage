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
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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
		StorageModClient.tooltip("adv_wireless_terminal", tooltip);
		if(stack.hasTag() && stack.getTag().contains("BindX")) {
			int x = stack.getTag().getInt("BindX");
			int y = stack.getTag().getInt("BindY");
			int z = stack.getTag().getInt("BindZ");
			String dim = stack.getTag().getString("BindDim");
			tooltip.add(new TranslatableText("tooltip.toms_storage.adv_wireless_terminal.bound", x, y, z, dim));
		}
	}

	@Override
	public TypedActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getStackInHand(handIn);
		if(stack.hasTag() && stack.getTag().contains("BindX")) {
			int x = stack.getTag().getInt("BindX");
			int y = stack.getTag().getInt("BindY");
			int z = stack.getTag().getInt("BindZ");
			String dim = stack.getTag().getString("BindDim");
			if(worldIn.getRegistryKey().getValue().toString().equals(dim)) {
				if(playerIn.squaredDistanceTo(new Vec3d(x, y, z)) < StorageMod.CONFIG.advWirelessRange * StorageMod.CONFIG.advWirelessRange) {
					BlockHitResult lookingAt = new BlockHitResult(new Vec3d(x, y, z), Direction.UP, new BlockPos(x, y, z), true);
					BlockState state = worldIn.getBlockState(lookingAt.getBlockPos());
					if(StorageTags.REMOTE_ACTIVATE.contains(state.getBlock())) {
						ActionResult r = state.onUse(worldIn, playerIn, handIn, lookingAt);
						return new TypedActionResult<>(r, playerIn.getStackInHand(handIn));
					} else {
						playerIn.sendMessage(new TranslatableText("chat.toms_storage.terminal_invalid_block"), true);
					}
				} else {
					playerIn.sendMessage(new TranslatableText("chat.toms_storage.terminal_out_of_range"), true);
				}
			}
		}
		return TypedActionResult.pass(playerIn.getStackInHand(handIn));
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext c) {
		if(c.shouldCancelInteraction() && !c.getWorld().isClient) {
			BlockPos pos = c.getBlockPos();
			BlockState state = c.getWorld().getBlockState(pos);
			if(StorageTags.REMOTE_ACTIVATE.contains(state.getBlock())) {
				ItemStack stack = c.getStack();
				if(!stack.hasTag())stack.setTag(new NbtCompound());
				stack.getTag().putInt("BindX", pos.getX());
				stack.getTag().putInt("BindY", pos.getY());
				stack.getTag().putInt("BindZ", pos.getZ());
				stack.getTag().putString("BindDim", c.getWorld().getRegistryKey().getValue().toString());
				if(c.getPlayer() != null)
					c.getPlayer().sendMessage(new TranslatableText("chat.toms_storage.terminal_bound"), true);
				return ActionResult.SUCCESS;
			}
		}
		return ActionResult.PASS;
	}

	@Override
	public int getRange(PlayerEntity pl, ItemStack stack) {
		return StorageMod.CONFIG.advWirelessRange;
	}
}
