package com.tom.storagemod.item;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.entity.InventoryConnectorBlockEntity;
import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.components.ConfiguratorComponent;
import com.tom.storagemod.inventory.BlockFilter;
import com.tom.storagemod.inventory.PlatformInventoryAccess;
import com.tom.storagemod.menu.InventoryConfiguratorMenu;
import com.tom.storagemod.platform.PlatformItem;

public class InventoryConfiguratorItem extends PlatformItem implements ILeftClickListener {

	public InventoryConfiguratorItem(Item.Properties pr) {
		super(pr);
	}

	@Override
	public InteractionResult onRightClick(Player player, ItemStack stack, BlockPos pos, InteractionHand hand) {
		//Right click
		if (!player.level().isClientSide)
			action(player.level(), player, stack, true, pos, hand);
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		action(level, player, player.getItemInHand(hand), true, null, hand);
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean onLeftClick(ItemStack itemstack, BlockPos pos, Player player) {
		//Left Click
		if (!player.level().isClientSide)
			action(player.level(), player, itemstack, false, pos, InteractionHand.MAIN_HAND);
		return true;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
		if (entity instanceof Player player) {
			if (stack != player.getItemInHand(InteractionHand.MAIN_HAND) && stack != player.getItemInHand(InteractionHand.OFF_HAND)) {
				ConfiguratorComponent c = stack.get(Content.configuratorComponent.get());
				if (c.showInvBox() || c.massSelect()) {
					stack.set(Content.configuratorComponent.get(), c.hiddenItem(level.getGameTime()));
				}
			}
		}
	}

	private void action(Level level, Player player, ItemStack stack, boolean rclk, BlockPos useOn, InteractionHand hand) {
		var c = new ConfiguratorComponent.Configurator(stack, level.getGameTime());
		if (c.debounce()) {
			return;
		}
		if (c.isBound()) {
			int x = c.bound().getX();
			int y = c.bound().getY();
			int z = c.bound().getZ();
			BlockPos pos = new BlockPos(x, y, z);
			if (player.distanceToSqr(x, y, z) < 16*16 && level.isLoaded(pos)) {
				BlockFilter f = BlockFilter.getOrCreateFilterAt(level, pos);
				if (f == null) {
					stack.set(Content.configuratorComponent.get(), ConfiguratorComponent.empty());
				} else if (useOn != null && c.selecting() && !(player.isSecondaryUseActive() && rclk)) {
					if (rclk) {
						if (!useOn.equals(pos)) {
							if (!f.getConnectedBlocks().remove(useOn))
								f.addConnected(level, useOn);
							c.setSelection(f.getConnectedBlocks());
							level.blockEntityChanged(pos);
							return;
						}
					} else if (!player.isSecondaryUseActive()) {
						//box select
						if (c.massSelect()) {
							int sx = c.boxStart().getX();
							int sy = c.boxStart().getY();
							int sz = c.boxStart().getZ();

							AABB bb = new AABB(sx, sy, sz, useOn.getX(), useOn.getY(), useOn.getZ());

							if (bb.getXsize() * bb.getYsize() * bb.getZsize() < 64) {
								BlockPos.betweenClosedStream(bb).forEach(p -> f.addConnected(level, p));
								level.blockEntityChanged(pos);
								c.massSelectEnd(f.getConnectedBlocks());
							} else {
								player.displayClientMessage(Component.translatable("chat.toms_storage.area_too_big"), true);
								c.massSelectEnd();
							}
						} else {
							c.massSelectStart(useOn);
						}
						return;
					} else {
						c.massSelectEnd();
						return;
					}
					c.clear();
				} else if(rclk) {
					openMenu(level, player, pos, f, hand);
					c.clear();
					return;
				}
			}
		}
		if (useOn != null && player.distanceToSqr(useOn.getX(), useOn.getY(), useOn.getZ()) < 256) {
			if (!rclk && level.getBlockEntity(useOn) instanceof InventoryConnectorBlockEntity ic) {
				c.clear();
				c.showInvBox(ic.getConnectedBlocks());
				/*MultiInventoryAccess mih = (MultiInventoryAccess) ic.getMergedHandler();
				System.out.println("Network Diagnostics:");
				for (var ih : mih.getConnected()) {
					System.out.println(ih + " " + IPriority.get(ih));
				}*/
				return;
			}
			BlockFilter f = BlockFilter.findBlockFilterAt(level, useOn);
			if (f == null)return;
			if (rclk) {
				//Open UI
				openMenu(level, player, useOn, f, hand);
				f.getConnectedBlocks().forEach(pos -> {
					if (!pos.equals(f.getMainPos()))
						PlatformInventoryAccess.removeBlockFilterAt(level, pos);
				});
			}
			c.showInvBox(f.getConnectedBlocks());
		}
	}

	private void openMenu(Level level, Player player, BlockPos pos, BlockFilter f, InteractionHand hand) {
		BlockEntity be = level.getBlockEntity(pos);
		Component blockName = null;
		if (be instanceof Nameable n)blockName = n.getDisplayName();
		if (blockName == null)blockName = level.getBlockState(pos).getBlock().getName();
		final var fbn = blockName;
		player.openMenu(new MenuProvider() {

			@Override
			public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
				return new InventoryConfiguratorMenu(p_39954_, p_39955_, pos, f, hand);
			}

			@Override
			public Component getDisplayName() {
				return Component.translatable("menu.toms_storage.inventory_configurator", fbn);
			}
		});
	}

	@Override
	public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, TooltipDisplay p_399753_,
			Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
		ClientUtil.tooltip("inventory_configurator", tooltip);
	}
}
