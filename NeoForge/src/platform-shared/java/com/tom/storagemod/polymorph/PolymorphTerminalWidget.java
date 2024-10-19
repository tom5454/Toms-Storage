package com.tom.storagemod.polymorph;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import com.illusivesoulworks.polymorph.api.client.PolymorphWidgets;
import com.illusivesoulworks.polymorph.api.client.widgets.PlayerRecipesWidget;

import com.tom.storagemod.menu.CraftingTerminalMenu;
import com.tom.storagemod.screen.CraftingTerminalScreen;

public class PolymorphTerminalWidget extends PlayerRecipesWidget {
	protected final CraftingTerminalMenu menu;

	public PolymorphTerminalWidget(CraftingTerminalScreen containerScreen) {
		super(containerScreen, containerScreen.getMenu().getCraftingResultSlot());
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

	public static void register() {
		PolymorphWidgets.getInstance().registerWidget(screen -> {
			if (screen instanceof CraftingTerminalScreen s) {
				return new PolymorphTerminalWidget(s);
			}

			return null;
		});
	}
}
