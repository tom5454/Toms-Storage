package com.tom.storagemod.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotPhantom extends Slot {

	public SlotPhantom(Inventory inv, int slotIndex, int posX, int posY) {
		super(inv, slotIndex, posX, posY);
	}

	@Override
	public boolean canTakeItems(PlayerEntity p_82869_1_) {
		return false;
	}

	@Override
	public int getMaxItemCount() {
		return 1;
	}

	@Override
	public boolean canInsert(ItemStack p_75214_1_) {
		return true;
	}
}
