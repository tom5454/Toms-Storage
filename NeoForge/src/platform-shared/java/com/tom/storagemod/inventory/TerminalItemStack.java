package com.tom.storagemod.inventory;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public class TerminalItemStack extends StoredItemStack {
	private String displayNameCache;
	private String descriptionIdCache;
	private String namespaceCache;
	private int usedSlotCount = 1;

	public TerminalItemStack(ItemStack stack, long count, int hash) {
		super(stack, count, hash);
	}

	public TerminalItemStack(ItemStack stack, long count) {
		super(stack, count);
	}

	public TerminalItemStack(ItemStack stack) {
		super(stack);
	}

	public TerminalItemStack(StoredItemStack st) {
		super(st);
		if (st instanceof TerminalItemStack ts)
			usedSlotCount = ts.usedSlotCount;
	}

	public static TerminalItemStack merge(TerminalItemStack a, TerminalItemStack b) {
		if (a == null)return b;
		if (b == null)return a;
		TerminalItemStack stack = new TerminalItemStack(a.stack, a.count + b.count, a.hashCode());
		stack.usedSlotCount = a.usedSlotCount + b.usedSlotCount;
		return stack;
	}

	public String getDisplayName() {
		if (displayNameCache == null)
			displayNameCache = stack.getHoverName().getString();
		return displayNameCache;
	}

	public String getDescriptionId() {
		if (descriptionIdCache == null) {
			descriptionIdCache = stack.getDescriptionId();
		}

		return descriptionIdCache;
	}

	public String getNamespace() {
		if (namespaceCache == null) {
			namespaceCache = BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace();
		}

		return namespaceCache;
	}

	public void setUsedSlotCount(int usedSlotCount) {
		this.usedSlotCount = usedSlotCount;
	}

	public int getUsedSlotCount() {
		return usedSlotCount;
	}

	public boolean equalDetails(TerminalItemStack pc) {
		return pc.count == count && pc.usedSlotCount == usedSlotCount;
	}
}
