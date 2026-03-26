package com.tom.storagemod.util;

import java.util.List;

import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.google.common.collect.Lists;

public class BasicContainer extends SimpleContainer {
	private List<ContainerListener> listeners;

	public BasicContainer(int pSize) {
		super(pSize);
	}

	public BasicContainer(ItemStack... pItems) {
		super(pItems);
	}

	public void addListener(ContainerListener p_19165_) {
		if (this.listeners == null) {
			this.listeners = Lists.newArrayList();
		}

		this.listeners.add(p_19165_);
	}

	public void removeListener(ContainerListener p_19182_) {
		if (this.listeners != null) {
			this.listeners.remove(p_19182_);
		}
	}

	@Override
	public void setChanged() {
		if (this.listeners != null) {
			for (ContainerListener containerlistener : this.listeners) {
				containerlistener.containerChanged(this);
			}
		}
	}

	public void loadItems(ValueInput.TypedInputList<ItemStackWithSlot> pContainerNbt) {
		for(int i = 0; i < this.getContainerSize(); ++i) {
			this.setItem(i, ItemStack.EMPTY);
		}

		for (final ItemStackWithSlot itemStackWithSlot : pContainerNbt) {
			if (itemStackWithSlot.isValidInContainer(getContainerSize())) {
				this.setItem(itemStackWithSlot.slot(), itemStackWithSlot.stack());
			}
		}
	}

	public void storeItems(ValueOutput.TypedOutputList<ItemStackWithSlot> output) {
		for (int i = 0; i < this.getContainerSize(); ++i) {
			ItemStack itemstack = this.getItem(i);
			if (!itemstack.isEmpty()) {
				output.add(new ItemStackWithSlot(i, itemstack));
			}
		}
	}

	@FunctionalInterface
	public interface ContainerListener {
		void containerChanged(BasicContainer basicContainer);
	}
}
