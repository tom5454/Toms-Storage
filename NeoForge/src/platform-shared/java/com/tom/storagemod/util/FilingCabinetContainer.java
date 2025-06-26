package com.tom.storagemod.util;

import java.util.function.Predicate;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class FilingCabinetContainer implements Container {
	private SimpleContainer inv;
	private Item item;
	private Runnable onChange;
	private Predicate<Player> isValidFor;

	public FilingCabinetContainer(int size, Runnable onChange, Predicate<Player> isValidFor) {
		this.inv = new SimpleContainer(size);
		this.onChange = onChange;
		this.isValidFor = isValidFor;
	}

	@Override
	public void clearContent() {
		inv.clearContent();
	}

	@Override
	public int getContainerSize() {
		return inv.getContainerSize();
	}

	@Override
	public boolean isEmpty() {
		return inv.isEmpty();
	}

	@Override
	public ItemStack getItem(int p_18941_) {
		return inv.getItem(p_18941_);
	}

	@Override
	public ItemStack removeItem(int p_18942_, int p_18943_) {
		ItemStack is = inv.removeItem(p_18942_, p_18943_);
		if (inv.isEmpty())item = null;
		return is;
	}

	@Override
	public ItemStack removeItemNoUpdate(int p_18951_) {
		ItemStack is = inv.removeItemNoUpdate(p_18951_);
		if (inv.isEmpty())item = null;
		return is;
	}

	@Override
	public void setItem(int p_18944_, ItemStack stack) {
		inv.setItem(p_18944_, stack);
		if (!stack.isEmpty())
			item = stack.getItem();
		else if (inv.isEmpty())item = null;
	}

	@Override
	public boolean stillValid(Player p_18946_) {
		return isValidFor.test(p_18946_);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (item != null)return stack.getItem() == item;
		return stack.getMaxStackSize() == 1;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public void setChanged() {
		if(onChange != null)
			onChange.run();
	}

	public NonNullList<ItemStack> getItems() {
		return inv.getItems();
	}

	public void fromTag(ValueInput tag, String id) {
		inv.fromItemList(tag.listOrEmpty(id, ItemStack.CODEC));
		ItemStack is = inv.getItem(0);
		if (is.isEmpty())item = null;
		else item = is.getItem();
	}

	public void storeTag(ValueOutput tag, String id) {
		inv.storeAsItemList(tag.list(id, ItemStack.CODEC));
	}
}
