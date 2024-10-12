package com.tom.storagemod.inventory;

import java.util.Comparator;
import java.util.function.Function;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public class StoredItemStack {
	private ItemStack stack;
	private long count;
	private int hash;
	private boolean hashZero;

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

	public static class ComparatorAmount implements IStoredItemStackComparator {
		public boolean reversed;

		public ComparatorAmount(boolean reversed) {
			this.reversed = reversed;
		}

		@Override
		public int compare(StoredItemStack in1, StoredItemStack in2) {
			int c = in2.getQuantity() > in1.getQuantity() ? 1 : (in1.getQuantity() == in2.getQuantity() ? in1.getStack().getHoverName().getString().compareTo(in2.getStack().getHoverName().getString()) : -1);
			return this.reversed ? -c : c;
		}

		@Override
		public boolean isReversed() {
			return reversed;
		}

		@Override
		public int type() {
			return 0;
		}

		@Override
		public void setReversed(boolean rev) {
			reversed  = rev;
		}
	}

	public static class ComparatorName implements IStoredItemStackComparator {
		public boolean reversed;

		public ComparatorName(boolean reversed) {
			this.reversed = reversed;
		}

		@Override
		public int compare(StoredItemStack in1, StoredItemStack in2) {
			int c = in1.getDisplayName().compareTo(in2.getDisplayName());
			return this.reversed ? -c : c;
		}

		@Override
		public boolean isReversed() {
			return reversed;
		}

		@Override
		public int type() {
			return 1;
		}

		@Override
		public void setReversed(boolean rev) {
			reversed = rev;
		}
	}

	public static class ComparatorModName implements IStoredItemStackComparator {
		public boolean reversed;

		public ComparatorModName(boolean reversed) {
			this.reversed = reversed;
		}

		@Override
		public int compare(StoredItemStack in1, StoredItemStack in2) {
			String m1 = BuiltInRegistries.ITEM.getKey(in1.getStack().getItem()).getNamespace();
			String m2 = BuiltInRegistries.ITEM.getKey(in2.getStack().getItem()).getNamespace();
			int c1 = m1.compareTo(m2);
			int c2 = in1.getDisplayName().compareTo(in2.getDisplayName());
			int c = c1 == 0 ? c2 : c1;
			return this.reversed ? -c : c;
		}

		@Override
		public boolean isReversed() {
			return reversed;
		}

		@Override
		public void setReversed(boolean rev) {
			reversed = rev;
		}

		@Override
		public int type() {
			return 2;
		}
	}

	public static interface IStoredItemStackComparator extends Comparator<StoredItemStack> {
		boolean isReversed();
		void setReversed(boolean rev);
		int type();
	}

	public static enum SortingTypes {
		AMOUNT(ComparatorAmount::new),
		NAME(ComparatorName::new),
		BY_MOD(ComparatorModName::new),
		;
		public static final SortingTypes[] VALUES = values();
		private final Function<Boolean, IStoredItemStackComparator> factory;
		private SortingTypes(Function<Boolean, IStoredItemStackComparator> factory) {
			this.factory = factory;
		}

		public IStoredItemStackComparator create(boolean rev) {
			return factory.apply(rev);
		}
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
		return stack.getHoverName().getString();
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

	public static StoredItemStack merge(StoredItemStack a, StoredItemStack b) {
		if (a == null)return b;
		if (b == null)return a;
		return new StoredItemStack(a.stack, a.count + b.count, a.hashCode());
	}

	public boolean equalDetails(StoredItemStack pc) {
		return pc.count == count;
	}
}
