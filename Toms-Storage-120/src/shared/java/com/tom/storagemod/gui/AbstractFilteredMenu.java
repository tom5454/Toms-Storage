package com.tom.storagemod.gui;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.item.IItemFilter;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.util.IDataReceiver;

public abstract class AbstractFilteredMenu extends AbstractContainerMenu implements IDataReceiver {
	protected final Inventory pinv;

	protected AbstractFilteredMenu(MenuType<?> type, int wid, Inventory pinv) {
		super(type, wid);
		this.pinv = pinv;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int slot) {
		return ItemStack.EMPTY;
	}

	@Override
	public void clicked(int slotId, int dragType, ClickType click, Player player) {
		Slot slot = slotId > -1 && slotId < slots.size() ? slots.get(slotId) : null;
		if (slot instanceof PhantomSlot) {
			ItemStack s = getCarried().copy();
			if(!s.isEmpty())s.setCount(1);
			slot.set(s);
			return;
		}
		if (slot instanceof FilterSlot) {
			ItemStack c = getCarried();
			ItemStack s = slot.getItem();
			boolean cf = !c.isEmpty() && c.getItem() instanceof IItemFilter;
			boolean sf = !s.isEmpty() && s.getItem() instanceof IItemFilter;
			if(sf && cf || (sf && c.isEmpty())) {
				super.clicked(slotId, dragType, click, player);
				return;
			}
			if(sf)return;
			if(cf) {
				slot.set(ItemStack.EMPTY);
				super.clicked(slotId, dragType, click, player);
				return;
			} else {
				c = c.copy();
				if(!c.isEmpty())c.setCount(1);
				slot.set(c);
				return;
			}
		}
		super.clicked(slotId, dragType, click, player);
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return !(slot instanceof PhantomSlot || slot instanceof FilterSlot);
	}

	public void setPhantom(Slot slot, ItemStack ingredient) {
		CompoundTag tag = new CompoundTag();
		CompoundTag t = new CompoundTag();
		tag.put("setPhantom", t);
		t.putInt("id", slot.index);
		t.put("item", ingredient.save(new CompoundTag()));
		NetworkHandler.sendDataToServer(tag);
	}

	@Override
	public void receive(CompoundTag tag) {
		if(pinv.player.isSpectator())return;
		if(tag.contains("setPhantom")) {
			CompoundTag t = tag.getCompound("setPhantom");
			int slotId = t.getInt("id");
			ItemStack item = ItemStack.of(t.getCompound("item"));
			Slot slot = slotId > -1 && slotId < slots.size() ? slots.get(slotId) : null;
			if (slot instanceof PhantomSlot) {
				if(!item.isEmpty()) {
					item.setCount(1);
					slot.set(item);
				}
			} else if (slot instanceof FilterSlot && !(item.getItem() instanceof IItemFilter)) {
				ItemStack s = slot.getItem();
				boolean sf = !s.isEmpty() && s.getItem() instanceof IItemFilter;
				if(!sf && !item.isEmpty()) {
					item.setCount(1);
					slot.set(item);
				}
			}
		}
	}
}
