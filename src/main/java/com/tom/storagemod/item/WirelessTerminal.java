package com.tom.storagemod.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface WirelessTerminal {
	int getRange(Player pl, ItemStack stack);
}
