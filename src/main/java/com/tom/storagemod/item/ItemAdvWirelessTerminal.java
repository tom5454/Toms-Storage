package com.tom.storagemod.item;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageTags;
import com.tom.storagemod.proxy.ClientProxy;

public class ItemAdvWirelessTerminal extends Item implements WirelessTerminal {

	public ItemAdvWirelessTerminal() {
		super(new Properties().tab(StorageMod.STORAGE_MOD_TAB).stacksTo(1));
		setRegistryName("ts.adv_wireless_terminal");
	}

	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		ClientProxy.tooltip("adv_wireless_terminal", tooltip);
		if(stack.hasTag() && stack.getTag().contains("BindX")) {
			int x = stack.getTag().getInt("BindX");
			int y = stack.getTag().getInt("BindY");
			int z = stack.getTag().getInt("BindZ");
			String dim = stack.getTag().getString("BindDim");
			tooltip.add(new TranslationTextComponent("tooltip.toms_storage.adv_wireless_terminal.bound", x, y, z, dim));
		}
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		ItemStack stack = playerIn.getItemInHand(handIn);
		if(stack.hasTag() && stack.getTag().contains("BindX")) {
			int x = stack.getTag().getInt("BindX");
			int y = stack.getTag().getInt("BindY");
			int z = stack.getTag().getInt("BindZ");
			String dim = stack.getTag().getString("BindDim");
			if(worldIn.dimension().location().toString().equals(dim)) {
				if(playerIn.distanceToSqr(new Vector3d(x, y, z)) < Config.advWirelessRange * Config.advWirelessRange) {
					BlockRayTraceResult lookingAt = new BlockRayTraceResult(new Vector3d(x, y, z), Direction.UP, new BlockPos(x, y, z), true);
					BlockState state = worldIn.getBlockState(lookingAt.getBlockPos());
					if(StorageTags.REMOTE_ACTIVATE.contains(state.getBlock())) {
						ActionResultType r = state.use(worldIn, playerIn, handIn, lookingAt);
						return new ActionResult<>(r, playerIn.getItemInHand(handIn));
					} else {
						playerIn.displayClientMessage(new TranslationTextComponent("chat.toms_storage.terminal_invalid_block"), true);
					}
				} else {
					playerIn.displayClientMessage(new TranslationTextComponent("chat.toms_storage.terminal_out_of_range"), true);
				}
			}
		}
		return ActionResult.pass(playerIn.getItemInHand(handIn));
	}

	@Override
	public ActionResultType useOn(ItemUseContext c) {
		if(c.isSecondaryUseActive() && !c.getLevel().isClientSide) {
			BlockPos pos = c.getClickedPos();
			BlockState state = c.getLevel().getBlockState(pos);
			if(StorageTags.REMOTE_ACTIVATE.contains(state.getBlock())) {
				ItemStack stack = c.getItemInHand();
				if(!stack.hasTag())stack.setTag(new CompoundNBT());
				stack.getTag().putInt("BindX", pos.getX());
				stack.getTag().putInt("BindY", pos.getY());
				stack.getTag().putInt("BindZ", pos.getZ());
				stack.getTag().putString("BindDim", c.getLevel().dimension().location().toString());
				if(c.getPlayer() != null)
					c.getPlayer().displayClientMessage(new TranslationTextComponent("chat.toms_storage.terminal_bound"), true);
				return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.PASS;
	}

	@Override
	public int getRange(PlayerEntity pl, ItemStack stack) {
		return Config.advWirelessRange;
	}
}
