package com.tom.storagemod.util;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

public class InfoHandler {
	private static final ItemStack STACK = new ItemStack(Items.BARRIER, Integer.MAX_VALUE);
	static {
		STACK.setNbt(new NbtCompound());
		NbtCompound d = new NbtCompound();
		STACK.getNbt().put("display", d);
		d.putString("Name", "{\"translate\":\"tooltip.toms_storage.loop\",\"color\":\"red\",\"italic\":false}");
	}

	public static final Storage<ItemVariant> INSTANCE = FilteringStorage.readOnlyOf(InventoryStorage.of(new SimpleInventory(STACK) {

		@Override
		public ItemStack removeItem(Item item, int count) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeStack(int slot) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeStack(int slot, int amount) {
			return ItemStack.EMPTY;
		}

	}, Direction.DOWN));
}
