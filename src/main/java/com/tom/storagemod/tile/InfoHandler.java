package com.tom.storagemod.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class InfoHandler implements IItemHandler {
	private static final ItemStack STACK = new ItemStack(Items.BARRIER, Integer.MAX_VALUE);
	public static final LazyOptional<IItemHandler> INSTANCE = LazyOptional.of(InfoHandler::new);
	static {
		STACK.setTag(new CompoundNBT());
		CompoundNBT d = new CompoundNBT();
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
