package com.tom.storagemod.rei;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.screen.AbstractStorageTerminalScreen;
import com.tom.storagemod.util.IAutoFillTerminal;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;

public class ReiTransferHandler implements TransferHandler {
	private CategoryIdentifier<?> CRAFTING = CategoryIdentifier.of("minecraft", "plugins/crafting");

	@Override
	public Result handle(Context context) {
		if(context.getMenu() instanceof IAutoFillTerminal) {
			if (!context.getDisplay().getCategoryIdentifier().equals(CRAFTING) || context.getMinecraft().screen == context.getContainerScreen())
				return Result.createNotApplicable();
			if (context.getContainerScreen() instanceof AbstractStorageTerminalScreen scr && !context.isActuallyCrafting() && !scr.isSmartItemSearchOn()) {
				return Result.createSuccessful().blocksFurtherHandling();
			}
			Display recipe = context.getDisplay();
			ItemStack[][] stacks = recipe.getInputEntries().stream().map(l ->
			l.stream().filter(es -> es.getDefinition().getValueType() == ItemStack.class).
			map(es -> es.getValue()).filter(e -> e != null).toArray(ItemStack[]::new)
					).toArray(ItemStack[][]::new);
			IAutoFillTerminal term = (IAutoFillTerminal) context.getMenu();
			List<Integer> missing = new ArrayList<>();
			int width = recipe instanceof SimpleGridMenuDisplay ? ((SimpleGridMenuDisplay)recipe).getWidth() : Integer.MAX_VALUE;
			Set<StoredItemStack> stored = new HashSet<>(term.getStoredItems());
			{
				int i = 0;
				for (ItemStack[] list : stacks) {
					if(list.length > 0) {
						boolean found = false;
						for (ItemStack stack : list) {
							if (stack != null && context.getMinecraft().player.getInventory().findSlotMatchingItem(stack) != -1) {
								found = true;
								break;
							}
						}

						if (!found) {
							for (ItemStack stack : list) {
								StoredItemStack s = new StoredItemStack(stack);
								if(stored.contains(s)) {
									found = true;
									break;
								}
							}
						}

						if (!found) {
							missing.add(width == 1 ? i * 3 : width == 2 ? ((i % 2) + i / 2 * 3) : i);
						}
					}
					i++;
				}
			}
			if (context.isActuallyCrafting()) {
				var recipeId = recipe.getDisplayLocation();
				if (recipeId.isPresent()) {
					CompoundTag compound = new CompoundTag();
					compound.putString("fill", recipeId.get().toString());
					term.sendMessage(compound);
					return Result.createSuccessful().blocksFurtherHandling();
				}
			}
			if(!missing.isEmpty()) {
				return Result.createSuccessful().color(0x67aaaa00).blocksFurtherHandling(false).
						renderer((gr, mouseX, mouseY, delta, widgets, bounds, d) -> {
							int i = 0;
							for (Widget widget : widgets) {
								if (widget instanceof Slot && ((Slot) widget).getNoticeMark() == Slot.INPUT) {
									if (missing.contains(i++)) {
										gr.pose().pushPose();
										gr.pose().translate(0, 0, 400);
										Rectangle innerBounds = ((Slot) widget).getInnerBounds();
										gr.fill(innerBounds.x, innerBounds.y, innerBounds.getMaxX(), innerBounds.getMaxY(), 0x40ff0000);
										gr.pose().popPose();
									}
								}
							}
						});
			}
			return Result.createSuccessful().blocksFurtherHandling();
		}
		return Result.createNotApplicable();
	}
}
