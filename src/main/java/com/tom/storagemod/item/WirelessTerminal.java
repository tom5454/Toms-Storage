package com.tom.storagemod.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface WirelessTerminal {
	int getRange(PlayerEntity pl, ItemStack stack);
	void open(PlayerEntity sender, ItemStack t);
	boolean canOpen(ItemStack t);
}
