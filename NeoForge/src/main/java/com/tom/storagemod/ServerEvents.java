package com.tom.storagemod;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;

import com.tom.storagemod.block.entity.BlockFilterAttachment;
import com.tom.storagemod.item.ILeftClickListener;
import com.tom.storagemod.platform.Platform;

@EventBusSubscriber(modid = StorageMod.modid)
public class ServerEvents {

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onBlockBreak(BreakEvent event) {
		BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
		if (be == null || !be.hasData(Platform.BLOCK_FILTER))return;
		BlockFilterAttachment d = be.getData(Platform.BLOCK_FILTER);
		d.getFilter().dropContents(event.getLevel(), event.getPos());
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onLeftClick(LeftClickBlock event) {
		if (event.getEntity().level().isClientSide())return;
		ItemStack is = event.getEntity().getItemInHand(InteractionHand.MAIN_HAND);
		if (is.getItem() instanceof ILeftClickListener l) {
			if (l.onLeftClick(is, event.getPos(), event.getEntity()))
				event.setCanceled(true);
		}
	}

}
