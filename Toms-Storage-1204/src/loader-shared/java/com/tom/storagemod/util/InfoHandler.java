package com.tom.storagemod.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;

public class InfoHandler implements IItemHandler {
	private static final ItemStack STACK = new ItemStack(Items.BARRIER, Integer.MAX_VALUE);
	public static final InfoHandler INSTANCE = new InfoHandler();
	static {
		STACK.setTag(new CompoundTag());
		CompoundTag d = new CompoundTag();
		STACK.getTag().put("display", d);
		d.putString("Name", "{\"translate\":\"tooltip.toms_storage.loop\",\"color\":\"red\",\"italic\":false}");
	}
	@Override
	public int getSlots() {
		return 1;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slot == 0 ? STACK : ItemStack.EMPTY;
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		return stack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 0;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return false;
	}

}
