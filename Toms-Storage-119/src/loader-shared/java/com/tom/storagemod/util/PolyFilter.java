package com.tom.storagemod.util;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraftforge.common.capabilities.ForgeCapabilities;

import com.tom.storagemod.Content;

public class PolyFilter implements ItemPredicate {
	private BlockFace face;
	private Set<ItemStack> filter;
	private long lastCheck;

	public PolyFilter(BlockFace face) {
		this.face = face;
		this.filter = new HashSet<>();
	}

	private void updateFilter() {
		long time = face.level().getGameTime();
		if(lastCheck != time && time % 10 == 1) {
			lastCheck = time;
			filter.clear();
			BlockEntity be = face.getBlockEntity();
			if(be != null) {
				be.getCapability(ForgeCapabilities.ITEM_HANDLER, face.from()).ifPresent(ih -> {
					IntStream.range(0, ih.getSlots()).mapToObj(ih::getStackInSlot).filter(s -> !s.isEmpty()).
					map(StoredItemStack::new).distinct().map(StoredItemStack::getStack).forEach(filter::add);
				});
			}
		}
	}

	@Override
	public boolean test(ItemStack stack) {
		updateFilter();
		for(ItemStack is : filter) {
			if(ItemStack.isSame(stack, is))return true;
		}
		return false;
	}

	@Override
	public boolean configMatch(ItemStack stack) {
		return stack.getItem() == Content.polyItemFliter.get();
	}

}
