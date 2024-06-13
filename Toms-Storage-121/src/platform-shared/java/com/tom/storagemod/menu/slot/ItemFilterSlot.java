package com.tom.storagemod.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import com.tom.storagemod.item.IItemFilter;

public class ItemFilterSlot extends Slot {

	public ItemFilterSlot(Container inv, int slotIndex, int posX, int posY) {
		super(inv, slotIndex, posX, posY);
	}

	@Override
	public boolean mayPickup(Player p_82869_1_) {
		return getItem().getItem() instanceof IItemFilter;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}
}
