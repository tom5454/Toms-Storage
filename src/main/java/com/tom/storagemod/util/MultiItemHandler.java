package com.tom.storagemod.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Unit;

import com.tom.storagemod.StorageMod;

public class MultiItemHandler implements Inventory {
	private List<InventoryWrapper> handlers = new ArrayList<>();
	private List<ItemStack[]> dupDetector = new ArrayList<>();
	private int[] invSizes = new int[0];
	private int invSize;
	private boolean calling;

	public <R> R call(BiFunction<InventoryWrapper, Integer, R> func, int slot, R def) {
		if(calling)return def;
		if(slot >= invSize)return def;
		calling = true;
		for (int i = 0; i < invSizes.length; i++) {
			if(slot >= invSizes[i])slot -= invSizes[i];
			else {
				R r = func.apply(handlers.get(i), slot);
				calling = false;
				return r;
			}
		}
		calling = false;
		return def;
	}

	@Override
	public int size() {
		return invSize;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public ItemStack getStack(int paramInt) {
		return call(InventoryWrapper::getStack, paramInt, ItemStack.EMPTY);
	}

	@Override
	public ItemStack removeStack(int paramInt1, int paramInt2) {
		return call((i, s) -> i.removeStack(s, paramInt2), paramInt1, ItemStack.EMPTY);
	}

	@Override
	public ItemStack removeStack(int paramInt) {
		return call(InventoryWrapper::removeStack, paramInt, ItemStack.EMPTY);
	}

	@Override
	public void setStack(int paramInt, ItemStack paramItemStack) {
		call((i, s) -> {
			i.setStack(s, paramItemStack);
			return Unit.INSTANCE;
		}, paramInt, Unit.INSTANCE);
	}

	@Override
	public boolean canPlayerUse(PlayerEntity paramPlayerEntity) {
		return false;
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return call((i, s) -> i.isValid(s, stack, false), slot, false);
	}

	public List<InventoryWrapper> getHandlers() {
		return handlers;
	}

	public void refresh() {
		dupDetector.clear();
		if(invSizes.length != handlers.size())invSizes = new int[handlers.size()];
		invSize = 0;
		for (int i = 0; i < invSizes.length; i++) {
			InventoryWrapper ih = handlers.get(i);
			if(ih == null)invSizes[i] = 0;
			else {
				int s = ih.size();
				invSizes[i] = s;
				invSize += s;
			}
		}
	}

	public boolean contains(Object o) {
		return handlers.contains(o);
	}

	public void add(InventoryWrapper e) {
		if(checkInv(e))
			handlers.add(e);
	}

	private boolean checkInv(InventoryWrapper h) {
		int len = Math.min(StorageMod.CONFIG.invDupScanSize, h.size());
		if(len == 0)return true;
		ItemStack[] is = new ItemStack[len];
		for(int i = 0;i<len;i++) {
			is[i] = h.getStack(i);
		}

		for (ItemStack[] st : dupDetector) {
			int l = Math.min(len, st.length);
			for (int i = 0; i < l; i++) {
				ItemStack item = st[i];
				if(!item.isEmpty() && item == is[i])
					return false;
			}
		}
		dupDetector.add(is);
		return true;
	}

	@Override
	public void clear() {
		invSize = 0;
		handlers.clear();
		dupDetector.clear();
	}

	@Override
	public void markDirty() {
	}
}
