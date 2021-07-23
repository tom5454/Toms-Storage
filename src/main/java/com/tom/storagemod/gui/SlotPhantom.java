package com.tom.storagemod.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotPhantom extends Slot {

	public SlotPhantom(Container inv, int slotIndex, int posX, int posY) {
		super(inv, slotIndex, posX, posY);
	}

	@Override
	public boolean mayPickup(Player p_82869_1_) {
		return false;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean mayPlace(ItemStack p_75214_1_) {
		return true;
	}
}
