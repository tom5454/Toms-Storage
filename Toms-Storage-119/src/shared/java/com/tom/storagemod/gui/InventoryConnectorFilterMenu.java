package com.tom.storagemod.gui;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.Content;
import com.tom.storagemod.item.IItemFilter;
import com.tom.storagemod.tile.FilteredInventoryCableConnectorBlockEntity;
import com.tom.storagemod.util.DataSlots;
import com.tom.storagemod.util.Priority;

public class InventoryConnectorFilterMenu extends AbstractFilteredMenu {
	private final Container dispenserInventory;
	public boolean allowList, keepLastInSlot;
	public Priority priority = Priority.NORMAL;
	private FilteredInventoryCableConnectorBlockEntity te;

	public InventoryConnectorFilterMenu(int wid, Inventory pinv) {
		this(wid, pinv, new SimpleContainer(9));
		addDataSlot(DataSlots.set(v -> allowList = v == 1));
		addDataSlot(DataSlots.set(v -> priority = Priority.VALUES[v]));
		addDataSlot(DataSlots.set(v -> keepLastInSlot = v == 1));
	}

	public InventoryConnectorFilterMenu(int id, Inventory pinv, FilteredInventoryCableConnectorBlockEntity te) {
		this(id, pinv, te.getFilter());
		this.te = te;
		addDataSlot(DataSlots.get(() -> te.isAllowList() ? 1 : 0));
		addDataSlot(DataSlots.get(() -> te.getPriority().ordinal()));
		addDataSlot(DataSlots.get(() -> te.isKeepLastInSlot() ? 1 : 0));
	}

	private InventoryConnectorFilterMenu(int wid, Inventory pinv, Container inv) {
		super(Content.invCableConnectorFilteredConatiner.get(), wid, pinv);
		checkContainerSize(inv, 9);
		this.dispenserInventory = inv;
		inv.startOpen(pinv.player);

		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				this.addSlot(new FilterSlot(inv, j + i * 3, 62 + j * 18, 17 + i * 18));
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
		int st = btn & 0b1111;
		btn >>= 4;
		if(btn == 0) {
			te.setAllowList(st == 1);
		} else if(btn == 1) {
			te.setPriority(Priority.VALUES[st]);
		} else if(btn == 2) {
			te.setKeepLastInSlot(st == 1);
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
				if(slot.getItem().getItem() instanceof IItemFilter)
					return quickMoveStack0(playerIn, index);
			} else {
				ItemStack is = slot.getItem();
				if(is.getItem() instanceof IItemFilter)
					return quickMoveStack0(playerIn, index);
				is = is.copy();
				is.setCount(1);
				for(int i = 0;i<9;i++) {
					Slot sl = this.slots.get(i);
					if(ItemStack.isSame(sl.getItem(), is))break;
					if(sl.getItem().isEmpty()) {
						sl.set(is);
						break;
					}
				}
			}
		}

		return ItemStack.EMPTY;
	}

	private ItemStack quickMoveStack0(Player p_39444_, int p_39445_) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(p_39445_);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			if (p_39445_ < 9) {
				if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemstack1, 0, 9, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(p_39444_, itemstack1);
		}

		return itemstack;
	}

	/**
	 * Called when the container is closed.
	 */
	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
		this.dispenserInventory.stopOpen(playerIn);
	}
}
