package com.tom.storagemod.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import com.tom.storagemod.StorageMod;

public class ContainerFiltered extends ScreenHandler {
	private final Inventory dispenserInventory;

	public ContainerFiltered(int p_i50087_1_, PlayerInventory p_i50087_2_) {
		this(p_i50087_1_, p_i50087_2_, new SimpleInventory(9));
	}

	public ContainerFiltered(int p_i50088_1_, PlayerInventory p_i50088_2_, Inventory p_i50088_3_) {
		super(StorageMod.filteredConatiner, p_i50088_1_);
		checkSize(p_i50088_3_, 9);
		this.dispenserInventory = p_i50088_3_;
		p_i50088_3_.onOpen(p_i50088_2_.player);

		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				this.addSlot(new SlotPhantom(p_i50088_3_, j + i * 3, 62 + j * 18, 17 + i * 18));
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
	public boolean canUse(PlayerEntity playerIn) {
		return this.dispenserInventory.canPlayerUse(playerIn);
	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
	 * inventory and the other inventory(s).
	 */
	@Override
	public ItemStack transferSlot(PlayerEntity playerIn, int index) {
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasStack()) {
			if (index < 9) {
			} else {
				ItemStack is = slot.getStack().copy();
				is.setCount(1);
				for(int i = 0;i<9;i++) {
					Slot sl = this.slots.get(i);
					if(ItemStack.areItemsEqual(sl.getStack(), is))break;
					if(sl.getStack().isEmpty()) {
						sl.setStack(is);
						break;
					}
				}
			}
		}

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack onSlotClick(int slotId, int dragType, SlotActionType click, PlayerEntity player) {
		Slot slot = slotId > -1 && slotId < slots.size() ? slots.get(slotId) : null;
		if (slot instanceof SlotPhantom) {
			ItemStack s = player.inventory.getCursorStack().copy();
			if(!s.isEmpty())s.setCount(1);
			slot.setStack(s);
			return player.inventory.getCursorStack();
		}
		return super.onSlotClick(slotId, dragType, click, player);
	}
}
