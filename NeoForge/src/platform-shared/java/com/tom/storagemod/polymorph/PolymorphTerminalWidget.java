package com.tom.storagemod.polymorph;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

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
	public void selectRecipe(Identifier Identifier) {
		super.selectRecipe(Identifier);
		var mc = Minecraft.getInstance();
		/*mc.getConnection().recipes().byKey(Identifier).ifPresent(recipe -> {
			mc.gameMode.handleInventoryButtonClick((this.menu).containerId, 1);
		});*/
	}

	@Override
	public void initChildWidgets() {
		var mc = Minecraft.getInstance();
		mc.gameMode.handleInventoryButtonClick((this.menu).containerId, 1);
		super.initChildWidgets();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float renderPartialTicks) {
		resetWidgetOffsets();

		super.render(guiGraphics, mouseX, mouseY, renderPartialTicks);
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
