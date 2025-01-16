package com.tom.storagemod.polymorph;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.client.recipe.widget.PersistentRecipesWidget;

import com.tom.storagemod.gui.CraftingTerminalMenu;
import com.tom.storagemod.gui.CraftingTerminalScreen;

public class PolymorphTerminalWidget extends PersistentRecipesWidget {
	protected final CraftingTerminalMenu menu;
	private final Slot outputSlot;

	public PolymorphTerminalWidget(CraftingTerminalScreen containerScreen) {
		super(containerScreen);
		outputSlot = containerScreen.getMenu().getCraftingResultSlot();
		menu = containerScreen.getMenu();
	}

	@Override
	public void selectRecipe(ResourceLocation resourceLocation) {
		super.selectRecipe(resourceLocation);
		var mc = Minecraft.getInstance();
		mc.level.getRecipeManager().byKey(resourceLocation).ifPresent(recipe -> {
			mc.gameMode.handleInventoryButtonClick((this.menu).containerId, 1);
		});
	}

	@Override
	public Slot getOutputSlot() {
		return outputSlot;
	}

	public static void register() {
		PolymorphApi.client().registerWidget(screen -> {
			if (screen instanceof CraftingTerminalScreen s) {
				return new PolymorphTerminalWidget(s);
			}

			return null;
		});
	}
}
