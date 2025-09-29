package com.tom.storagemod.inventory;

import net.minecraft.world.item.ItemStack;

public class StoredItemStack {
	private ItemStack stack;
	private long count;
	private int hash;
	private boolean hashZero;
	private String displayNameCache;

	public StoredItemStack(ItemStack stack, long count) {
		this.stack = stack;
		this.count = count;
	}

	public StoredItemStack(ItemStack stack) {
		this.stack = stack.copy();
		this.stack.setCount(1);
		this.count = stack.getCount();
	}

	public StoredItemStack(ItemStack stack, long count, int hash) {
		this.stack = stack;
		this.count = count;
		this.hash = hash;
		if (hash == 0)hashZero = true;
	}

	public StoredItemStack(StoredItemStack st) {
		this.stack = st.stack;
		this.count = st.count;
		this.hash = st.hash;
		this.hashZero = st.hashZero;
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

	@Override
	public int hashCode() {
		if(hash == 0 && !hashZero) {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((stack == null) ? 0 : stack.getItem().hashCode());
			result = prime * result + ((stack == null) ? 0 : stack.getComponentsPatch().hashCode());
			hash = result;
			if (hash == 0)hashZero = true;
			return result;
		}
		return hash;
	}

	public String getDisplayName() {
		if (displayNameCache == null)
			displayNameCache = stack.getHoverName().getString();
		return displayNameCache;
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
		} else if (!ItemStack.isSameItemSameComponents(stack, other.stack)) return false;
		return true;
	}

	public boolean equals(StoredItemStack other) {
		if (this == other) return true;
		if (other == null) return false;
		if (count != other.count) return false;
		if (stack == null) {
			if (other.stack != null) return false;
		} else if (!ItemStack.isSameItemSameComponents(stack, other.stack)) return false;
		return true;
	}

	public boolean equalItem(StoredItemStack other) {
		if (this == other) return true;
		if (other == null) return false;
		if (stack == null) {
			if (other.stack != null) return false;
		} else if (!ItemStack.isSameItemSameComponents(stack, other.stack)) return false;
		return true;
	}

	public void grow(long c) {
		count += c;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public int getMaxStackSize() {
		return stack.getMaxStackSize();
	}

	public long getStackCount() {
    if (count == 0) {
      return 0;
    }

		return (long) Math.ceil((double) count / stack.getMaxStackSize());
	}

	public static StoredItemStack merge(StoredItemStack a, StoredItemStack b) {
		if (a == null)return b;
		if (b == null)return a;
		return new StoredItemStack(a.stack, a.count + b.count, a.hashCode());
	}

	public boolean equalDetails(StoredItemStack pc) {
		return pc.count == count;
	}
}
