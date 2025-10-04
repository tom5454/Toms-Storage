package com.tom.storagemod.inventory.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import com.tom.storagemod.Content;
import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.util.BlockFaceReference;

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
			ResourceHandler<ItemResource> ih = face.level().getCapability(Capabilities.Item.BLOCK, face.pos(), face.from());
			if(ih != null) {
				IntStream.range(0, ih.size()).mapToObj(i -> {
					var res = ih.getResource(i);
					return new StoredItemStack(res.toStack(), ih.getAmountAsLong(i));
				}).filter(s -> s != null).
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
