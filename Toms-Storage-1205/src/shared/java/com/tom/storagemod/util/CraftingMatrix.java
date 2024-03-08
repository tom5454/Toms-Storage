package com.tom.storagemod.util;

import java.util.Iterator;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public class CraftingMatrix implements CraftingContainer {
	private final NonNullList<ItemStack> items;
	private final int width;
	private final int height;
	private final Runnable onChanged;

	public CraftingMatrix(int i, int j, Runnable onChanged) {
		this(i, j, onChanged, NonNullList.withSize(i * j, ItemStack.EMPTY));
	}

	public CraftingMatrix(int i, int j, Runnable onChanged, NonNullList<ItemStack> nonNullList) {
		this.items = nonNullList;
		this.width = i;
		this.height = j;
		this.onChanged = onChanged;
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	public boolean isEmpty() {
		Iterator<ItemStack> var1 = this.items.iterator();

		ItemStack itemStack;
		do {
			if (!var1.hasNext()) {
				return true;
			}

			itemStack = var1.next();
		} while (itemStack.isEmpty());

		return false;
	}

	@Override
	public ItemStack getItem(int i) {
		return i >= this.getContainerSize() ? ItemStack.EMPTY : (ItemStack) this.items.get(i);
	}

	@Override
	public ItemStack removeItemNoUpdate(int i) {
		return ContainerHelper.takeItem(this.items, i);
	}

	@Override
	public ItemStack removeItem(int i, int j) {
		ItemStack itemStack = ContainerHelper.removeItem(this.items, i, j);
		if (!itemStack.isEmpty()) {
			setChanged();
		}

		return itemStack;
	}

	@Override
	public void setItem(int i, ItemStack itemStack) {
		this.items.set(i, itemStack);
		setChanged();
	}

	@Override
	public void setChanged() {
		onChanged.run();
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void clearContent() {
		this.items.clear();
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public List<ItemStack> getItems() {
		return List.copyOf(this.items);
	}

	@Override
	public void fillStackedContents(StackedContents stackedContents) {
		Iterator<ItemStack> var2 = this.items.iterator();

		while (var2.hasNext()) {
			ItemStack itemStack = var2.next();
			stackedContents.accountSimpleStack(itemStack);
		}

	}
}
