package com.tom.storagemod.item;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.StorageTags;

public class ItemWirelessTerminal extends Item implements WirelessTerminal {

	public ItemWirelessTerminal() {
		super(new Settings().group(StorageMod.STORAGE_MOD_TAB).maxCount(1));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, World worldIn, List<Text> tooltip, TooltipContext flagIn) {
		StorageModClient.tooltip("wireless_terminal", tooltip, StorageMod.CONFIG.wirelessRange);
	}

	@Override
	public TypedActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		BlockHitResult lookingAt = (BlockHitResult) playerIn.raycast(StorageMod.CONFIG.wirelessRange, 0f, true);
		BlockState state = worldIn.getBlockState(lookingAt.getBlockPos());
		if(state.isIn(StorageTags.REMOTE_ACTIVATE)) {
			ActionResult r = state.onUse(worldIn, playerIn, handIn, lookingAt);
			return new TypedActionResult<>(r, playerIn.getStackInHand(handIn));
		} else {
			return super.use(worldIn, playerIn, handIn);
		}
	}

	public static boolean isPlayerHolding(PlayerEntity player) {
		return player.getStackInHand(Hand.MAIN_HAND).getItem() == StorageMod.wirelessTerminal ||
				player.getStackInHand(Hand.OFF_HAND).getItem() == StorageMod.wirelessTerminal;
	}

	@Override
	public int getRange(PlayerEntity pl, ItemStack stack) {
		return StorageMod.CONFIG.wirelessRange;
	}

	@Override
	public void open(PlayerEntity sender, ItemStack t) {
	}

	@Override
	public boolean canOpen(ItemStack t) {
		return false;
	}
}
