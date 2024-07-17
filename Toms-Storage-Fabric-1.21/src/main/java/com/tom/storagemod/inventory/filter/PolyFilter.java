package com.tom.storagemod.inventory.filter;

import java.util.HashSet;
import java.util.Set;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.Content;
import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.util.BlockFaceReference;
import com.tom.storagemod.util.Util;

public class PolyFilter implements ItemFilter {
	private BlockFaceReference face;
	private Set<ItemStack> filter;
	private long lastCheck;

	public PolyFilter(BlockFaceReference face) {
		this.face = face;
		this.filter = new HashSet<>();
	}

	@Override
	public void updateState() {
		long time = face.level().getGameTime();
		if(time - lastCheck >= 10) {
			lastCheck = time;
			filter.clear();
			Storage<ItemVariant> st = ItemStorage.SIDED.find(face.level(), face.pos(), face.from());
			if(st != null) {
				Util.stream(st.iterator()).filter(s -> !s.isResourceBlank()).
				map(s -> new StoredItemStack(s.getResource().toStack())).
				distinct().map(StoredItemStack::getStack).forEach(filter::add);
			}
		}
	}

	@Override
	public boolean test(StoredItemStack stack) {
		for(ItemStack is : filter) {
			if(ItemStack.isSameItemSameComponents(stack.getStack(), is))return true;
		}
		return false;
	}

	@Override
	public boolean configMatch(ItemStack stack) {
		return stack.getItem() == Content.polyItemFilter.get();
	}
}
