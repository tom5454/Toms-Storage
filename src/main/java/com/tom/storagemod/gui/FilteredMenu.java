package com.tom.storagemod.gui;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.StorageMod;

public class FilteredMenu extends AbstractContainerMenu {
	private final Container dispenserInventory;

	public FilteredMenu(int p_i50087_1_, Inventory p_i50087_2_) {
		this(p_i50087_1_, p_i50087_2_, new SimpleContainer(9));
	}

	public FilteredMenu(int p_i50088_1_, Inventory p_i50088_2_, Container p_i50088_3_) {
		super(StorageMod.filteredConatiner, p_i50088_1_);
		checkContainerSize(p_i50088_3_, 9);
		this.dispenserInventory = p_i50088_3_;
		p_i50088_3_.startOpen(p_i50088_2_.player);

		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				this.addSlot(new PhantomSlot(p_i50088_3_, j + i * 3, 62 + j * 18, 17 + i * 18));
			}
		}

		for(int k = 0; k < 3; ++k) {
			for(int i1 = 0; i1 < 9; ++i1) {
				this.addSlot(new Slot(p_i50088_2_, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
			}
		}

		for(int l = 0; l < 9; ++l) {
			this.addSlot(new Slot(p_i50088_2_, l, 8 + l * 18, 142));
		}

	}

	/**
	 * Determines whether supplied player can use this container
	 */
	@Override
	public boolean stillValid(Player playerIn) {
		return this.dispenserInventory.stillValid(playerIn);
	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
	 * inventory and the other inventory(s).
	 */
	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			if (index < 9) {
			} else {
				ItemStack is = slot.getItem().copy();
				is.setCount(1);
				for(int i = 0;i<9;i++) {
					Slot sl = this.slots.get(i);
					if(ItemStack.isSameIgnoreDurability(sl.getItem(), is))break;
					if(sl.getItem().isEmpty()) {
						sl.set(is);
						break;
					}
				}
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public void clicked(int slotId, int dragType, ClickType click, Player player) {
		Slot slot = slotId > -1 && slotId < slots.size() ? slots.get(slotId) : null;
		if (slot instanceof PhantomSlot) {
			ItemStack s = getCarried().copy();
			if(!s.isEmpty())s.setCount(1);
			slot.set(s);
		}
		super.clicked(slotId, dragType, click, player);
	}
}
