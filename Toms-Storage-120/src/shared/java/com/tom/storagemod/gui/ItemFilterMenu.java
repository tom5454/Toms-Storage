package com.tom.storagemod.gui;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.Content;
import com.tom.storagemod.util.DataSlots;
import com.tom.storagemod.util.SimpleItemFilter;

public class ItemFilterMenu extends AbstractFilteredMenu {
	private final Container dispenserInventory;
	private SimpleItemFilter filter;
	public boolean matchNBT, allowList;

	public ItemFilterMenu(int wid, Inventory pinv) {
		this(wid, pinv, new SimpleContainer(9));
		addDataSlot(DataSlots.set(v -> matchNBT = v == 1));
		addDataSlot(DataSlots.set(v -> allowList = v == 1));
	}

	public ItemFilterMenu(int wid, Inventory pinv, SimpleItemFilter filter) {
		this(wid, pinv, filter.getContainer());
		this.filter = filter;
		addDataSlot(DataSlots.get(() -> filter.isMatchNBT() ? 1 : 0));
		addDataSlot(DataSlots.get(() -> filter.isAllowList() ? 1 : 0));
	}

	private ItemFilterMenu(int wid, Inventory pinv, Container inv) {
		super(Content.itemFilterConatiner.get(), wid, pinv);
		checkContainerSize(inv, 9);
		this.dispenserInventory = inv;
		inv.startOpen(pinv.player);

		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				this.addSlot(new PhantomSlot(inv, j + i * 3, 62 + j * 18, 17 + i * 18));
			}
		}

		for(int k = 0; k < 3; ++k) {
			for(int i1 = 0; i1 < 9; ++i1) {
				this.addSlot(new Slot(pinv, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
			}
		}

		for(int l = 0; l < 9; ++l) {
			this.addSlot(new Slot(pinv, l, 8 + l * 18, 142));
		}
	}

	@Override
	public boolean clickMenuButton(Player p_38875_, int btn) {
		boolean v = (btn & 1) != 0;
		btn >>= 1;
		if(btn == 0) {
			filter.setMatchNBT(v);
		} else if(btn == 1) {
			filter.setAllowList(v);
		}
		return false;
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
					if(ItemStack.isSameItemSameTags(sl.getItem(), is))break;
					if(sl.getItem().isEmpty()) {
						sl.set(is);
						break;
					}
				}
			}
		}

		return ItemStack.EMPTY;
	}

	/**
	 * Called when the container is closed.
	 */
	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
		this.dispenserInventory.stopOpen(playerIn);
		if(filter != null)filter.flush();
	}
}
