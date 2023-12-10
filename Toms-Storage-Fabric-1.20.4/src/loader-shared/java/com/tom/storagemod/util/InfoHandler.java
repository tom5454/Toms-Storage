package com.tom.storagemod.util;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InfoHandler {
	private static final ItemStack STACK = new ItemStack(Items.BARRIER, Integer.MAX_VALUE);
	static {
		STACK.setTag(new CompoundTag());
		CompoundTag d = new CompoundTag();
		STACK.getTag().put("display", d);
		d.putString("Name", "{\"translate\":\"tooltip.toms_storage.loop\",\"color\":\"red\",\"italic\":false}");
	}

	public static final Storage<ItemVariant> INSTANCE = FilteringStorage.readOnlyOf(InventoryStorage.of(new SimpleContainer(STACK) {

		@Override
		public ItemStack removeItemType(Item item, int count) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeItemNoUpdate(int slot) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack removeItem(int slot, int amount) {
			return ItemStack.EMPTY;
		}

	}, Direction.DOWN));
}
