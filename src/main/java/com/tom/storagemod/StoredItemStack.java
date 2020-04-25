package com.tom.storagemod;

import java.util.Comparator;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class StoredItemStack {
	private ItemStack stack;
	private long count;
	private static final String ITEM_COUNT_NAME = "c", ITEMSTACK_NAME = "s";
	public StoredItemStack(ItemStack stack, long count) {
		this.stack = stack;
		this.count = count;
	}
	public StoredItemStack(ItemStack stack) {
		this.stack = stack.copy();
		this.stack.setCount(1);
		this.count = stack.getCount();
	}

	public ItemStack getStack() {
		return stack;
	}

	public long getQuantity() {
		return count;
	}

	public ItemStack getActualStack() {
		ItemStack s = stack.copy();
		s.setCount((int) count);
		return s;
	}

	public void writeToNBT(CompoundNBT tag) {
		tag.putLong(ITEM_COUNT_NAME, getQuantity());
		tag.put(ITEMSTACK_NAME, stack.write(new CompoundNBT()));
		tag.getCompound(ITEMSTACK_NAME).remove("Count");
	}

	public static StoredItemStack readFromNBT(CompoundNBT tag) {
		ItemStack cheat = ItemStack.read(tag);
		tag.getCompound(ITEMSTACK_NAME).putByte("Count", (byte) 1);
		StoredItemStack stack = new StoredItemStack(!cheat.isEmpty() ? cheat : ItemStack.read(tag.getCompound(ITEMSTACK_NAME)), !cheat.isEmpty() ? cheat.getCount() : tag.getLong(ITEM_COUNT_NAME));
		return !stack.stack.isEmpty() ? stack : null;
	}

	public static class ComparatorAmount implements Comparator<StoredItemStack> {
		public boolean reversed;

		public ComparatorAmount(boolean reversed) {
			this.reversed = reversed;
		}

		@Override
		public int compare(StoredItemStack in1, StoredItemStack in2) {
			int c = in2.getQuantity() > in1.getQuantity() ? 1 : (in1.getQuantity() == in2.getQuantity() ? in1.getStack().getDisplayName().getUnformattedComponentText().compareTo(in2.getStack().getDisplayName().getUnformattedComponentText()) : -1);
			return this.reversed ? -c : c;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stack == null) ? 0 : stack.getItem().hashCode());
		result = prime * result + ((stack == null || !stack.hasTag()) ? 0 : stack.getTag().hashCode());
		//System.out.println(result + " " + stack);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		StoredItemStack other = (StoredItemStack) obj;
		//if (count != other.count) return false;
		if (stack == null) {
			if (other.stack != null) return false;
		} else if (!ItemStack.areItemsEqual(stack, other.stack)) return false;
		return true;
	}

	public boolean equals(StoredItemStack other) {
		if (this == other) return true;
		if (other == null) return false;
		if (count != other.count) return false;
		if (stack == null) {
			if (other.stack != null) return false;
		} else if (!ItemStack.areItemsEqual(stack, other.stack)) return false;
		return true;
	}

	public void grow(long c) {
		count += c;
	}
}
