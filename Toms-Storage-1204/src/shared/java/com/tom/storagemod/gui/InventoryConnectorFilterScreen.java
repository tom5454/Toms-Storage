package com.tom.storagemod.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.tom.storagemod.util.Priority;

public class InventoryConnectorFilterScreen extends AbstractFilteredScreen<InventoryConnectorFilterMenu> {
	private static final ResourceLocation DISPENSER_GUI_TEXTURES = new ResourceLocation("textures/gui/container/dispenser.png");
	private GuiButton buttonAllowList, buttonPriority, buttonKeepLast;

	public InventoryConnectorFilterScreen(InventoryConnectorFilterMenu container, Inventory playerInventory, Component textComponent) {
		super(container, playerInventory, textComponent);
	}

	@Override
	protected void init() {
		super.init();
		//this.titleX = (this.xSize - this.font.getStringPropertyWidth(this.title)) / 2;
		buttonAllowList = addRenderableWidget(new GuiButton(leftPos - 18, topPos + 5, 0, b -> {
			click(0, buttonAllowList.getState() != 1 ? 1 : 0);
		}));
		buttonAllowList.tooltipFactory = s -> Tooltip.create(Component.translatable("tooltip.toms_storage.allowList_" + s));

		buttonPriority = addRenderableWidget(new GuiButton(leftPos - 18, topPos + 5 + 18, 2, b -> {
			menu.priority = Priority.VALUES[(buttonPriority.getState() + 1) % Priority.VALUES.length];
			click(1, menu.priority.ordinal());
		}));
		buttonPriority.tooltipFactory = s -> Tooltip.create(Component.translatable("tooltip.toms_storage.priority_" + s));

		buttonKeepLast = addRenderableWidget(new GuiButton(leftPos - 18, topPos + 5 + 36, 3, b -> {
			click(2, buttonKeepLast.getState() != 1 ? 1 : 0);
		}));
		buttonKeepLast.tooltipFactory = s -> Tooltip.create(Component.translatable("tooltip.toms_storage.keepLast_" + s));
	}

	private void click(int id, int v) {
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, (id << 4) | v);
	}

	@Override
	public void render(GuiGraphics st, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(st, mouseX, mouseY, partialTicks);
		buttonAllowList.setState(menu.allowList ? 1 : 0);
		buttonPriority.setState(menu.priority.ordinal());
		buttonKeepLast.setState(menu.keepLastInSlot ? 1 : 0);
		super.render(st, mouseX, mouseY, partialTicks);
		this.renderTooltip(st, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics matrixStack, float partialTicks, int x, int y) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		matrixStack.blit(DISPENSER_GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}
}
