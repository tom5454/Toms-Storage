package com.tom.storagemod.util;

import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LimitedContainer implements Container {
	private final Container delegate;
	private int sizeLimit;
	private int startOffset;

	public LimitedContainer(Container delegate, int sizeLimit) {
		this.delegate = delegate;
		this.sizeLimit = sizeLimit;
	}

	@Override
	public void clearContent() {
		delegate.clearContent();
	}

	@Override
	public int getContainerSize() {
		return Math.min(sizeLimit, delegate.getContainerSize() - startOffset);
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public ItemStack getItem(int pSlot) {
		return delegate.getItem(startOffset + pSlot);
	}

	@Override
	public ItemStack removeItem(int pSlot, int pAmount) {
		return delegate.removeItem(startOffset + pSlot, pAmount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int pSlot) {
		return delegate.removeItemNoUpdate(startOffset + pSlot);
	}

	@Override
	public void setItem(int pSlot, ItemStack pStack) {
		delegate.setItem(startOffset + pSlot, pStack);
	}

	@Override
	public int getMaxStackSize() {
		return delegate.getMaxStackSize();
	}

	@Override
	public void setChanged() {
		delegate.setChanged();
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return delegate.stillValid(pPlayer);
	}

	@Override
	public void startOpen(ContainerUser pPlayer) {
		delegate.startOpen(pPlayer);
	}

	@Override
	public void stopOpen(ContainerUser pPlayer) {
		delegate.stopOpen(pPlayer);
	}

	@Override
	public boolean canPlaceItem(int pIndex, ItemStack pStack) {
		return delegate.canPlaceItem(startOffset + pIndex, pStack);
	}

	@Override
	public int countItem(Item pItem) {
		return delegate.countItem(pItem);
	}

	@Override
	public boolean hasAnyOf(Set<Item> pSet) {
		return delegate.hasAnyOf(pSet);
	}

	@Override
	public boolean hasAnyMatching(Predicate<ItemStack> p_216875_) {
		return delegate.hasAnyMatching(p_216875_);
	}

	public void setSizeLimit(int sizeLimit) {
		this.sizeLimit = sizeLimit;
	}

	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}
}
