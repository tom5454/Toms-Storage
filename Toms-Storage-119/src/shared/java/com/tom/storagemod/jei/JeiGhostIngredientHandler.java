package com.tom.storagemod.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.gui.AbstractFilteredMenu;
import com.tom.storagemod.gui.AbstractFilteredScreen;
import com.tom.storagemod.gui.FilterSlot;
import com.tom.storagemod.gui.PhantomSlot;
import com.tom.storagemod.item.IItemFilter;

import mezz.jei.api.gui.handlers.IGhostIngredientHandler;

@SuppressWarnings("rawtypes")
public class JeiGhostIngredientHandler implements IGhostIngredientHandler<AbstractFilteredScreen> {

	@SuppressWarnings("unchecked")
	@Override
	public <I> List<Target<I>> getTargets(AbstractFilteredScreen gui, I ingredient, boolean doStart) {
		if (ingredient instanceof ItemStack stack) {
			List<Target<ItemStack>> targets = new ArrayList<>();
			boolean filter = stack.getItem() instanceof IItemFilter;
			for (Slot slot : gui.getMenu().slots) {
				if (slot instanceof PhantomSlot) {
					targets.add(new SlotTarget(gui, slot));
				} else if (!filter && slot instanceof FilterSlot) {
					ItemStack s = slot.getItem();
					boolean sf = !s.isEmpty() && s.getItem() instanceof IItemFilter;
					if(!sf)targets.add(new SlotTarget(gui, slot));
				}
			}
			return (List) targets;
		}
		return Collections.emptyList();
	}

	@Override
	public void onComplete() {
	}

	private static class SlotTarget implements Target<ItemStack> {
		private Slot slot;
		private Rect2i area;
		private AbstractFilteredScreen gui;

		public SlotTarget(AbstractFilteredScreen gui, Slot slot) {
			this.slot = slot;
			this.gui = gui;
			this.area = new Rect2i(gui.getGuiLeft() + slot.x, gui.getGuiTop() + slot.y, 16, 16);
		}

		@Override
		public Rect2i getArea() {
			return area;
		}

		@Override
		public void accept(ItemStack ingredient) {
			((AbstractFilteredMenu) gui.getMenu()).setPhantom(slot, ingredient);
		}
	}
}
