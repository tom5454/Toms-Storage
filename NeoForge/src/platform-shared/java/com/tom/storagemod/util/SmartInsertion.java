package com.tom.storagemod.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.inventory.IInventoryAccess;
import com.tom.storagemod.inventory.InventorySlot;
import com.tom.storagemod.inventory.StoredItemStack;

public class SmartInsertion {

	public static ItemStack push(List<IInventoryAccess> connected, ItemStack stack) {
		if (stack.isEmpty()) return stack;

		StoredItemStack sis = new StoredItemStack(stack);
		List<AccessWithCount> sameItem = new ArrayList<>();
		List<AccessWithCount> sameFamily = new ArrayList<>();
		List<AccessWithCount> others = new ArrayList<>();

		Set<TagKey<Item>> itemTags = getItemTags(stack.getItem());

		for (IInventoryAccess ia : connected) {
			long count = ia.tracker().countItems(sis);
			if (count > 0) {
				sameItem.add(new AccessWithCount(ia, count));
			} else if (!itemTags.isEmpty() && hasSameFamily(ia, itemTags)) {
				sameFamily.add(new AccessWithCount(ia, ia.tracker().getTotalItems()));
			} else {
				others.add(new AccessWithCount(ia, ia.tracker().getTotalItems()));
			}
		}

		sameItem.sort((a, b) -> Long.compare(b.count, a.count));
		sameFamily.sort((a, b) -> Long.compare(b.count, a.count));
		others.sort((a, b) -> Long.compare(b.count, a.count));

		// 1. Try same item first
		for (AccessWithCount awc : sameItem) {
			stack = awc.access.pushStack(stack);
			if (stack.isEmpty()) return ItemStack.EMPTY;
		}

		// 2. Try same family next
		for (AccessWithCount awc : sameFamily) {
			stack = awc.access.pushStack(stack);
			if (stack.isEmpty()) return ItemStack.EMPTY;
		}

		// 3. Fallback to others (respecting original priority order)
		for (AccessWithCount awc : others) {
			stack = awc.access.pushStack(stack);
			if (stack.isEmpty()) return ItemStack.EMPTY;
		}

		return stack;
	}

	public static InventorySlot findSlotDest(List<IInventoryAccess> connected, StoredItemStack stack) {
		if (stack == null) return null;

		List<AccessWithCount> sameItem = new ArrayList<>();
		List<AccessWithCount> sameFamily = new ArrayList<>();
		List<AccessWithCount> others = new ArrayList<>();

		Set<TagKey<Item>> itemTags = getItemTags(stack.getStack().getItem());

		for (IInventoryAccess ia : connected) {
			long count = ia.tracker().countItems(stack);
			if (count > 0) {
				sameItem.add(new AccessWithCount(ia, count));
			} else if (!itemTags.isEmpty() && hasSameFamily(ia, itemTags)) {
				sameFamily.add(new AccessWithCount(ia, ia.tracker().getTotalItems()));
			} else {
				others.add(new AccessWithCount(ia, ia.tracker().getTotalItems()));
			}
		}

		sameItem.sort((a, b) -> Long.compare(b.count, a.count));
		sameFamily.sort((a, b) -> Long.compare(b.count, a.count));
		others.sort((a, b) -> Long.compare(b.count, a.count));

		for (AccessWithCount awc : sameItem) {
			InventorySlot is = awc.access.tracker().findSlotDest(stack);
			if (is != null) return is;
		}

		for (AccessWithCount awc : sameFamily) {
			InventorySlot is = awc.access.tracker().findSlotDest(stack);
			if (is != null) return is;
		}

		for (AccessWithCount awc : others) {
			InventorySlot is = awc.access.tracker().findSlotDest(stack);
			if (is != null) return is;
		}

		return null;
	}

	private record AccessWithCount(IInventoryAccess access, long count) {}

	private static Set<TagKey<Item>> getItemTags(Item item) {
		Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(item);
		return holder.tags()
				.filter(tag -> !isGenericTag(tag))
				.collect(Collectors.toSet());
	}

	private static boolean isGenericTag(TagKey<Item> tag) {
		String path = tag.location().getPath();
		return path.equals("items") || path.equals("completable_item_tag");
	}

	private static boolean hasSameFamily(IInventoryAccess ia, Set<TagKey<Item>> tags) {
		// Optimization: streamWrappedStacks is usually cached
		return ia.tracker().streamWrappedStacks(false).anyMatch(sis -> {
			Holder<Item> holder = BuiltInRegistries.ITEM.wrapAsHolder(sis.getStack().getItem());
			return holder.tags().anyMatch(tags::contains);
		});
	}
}
