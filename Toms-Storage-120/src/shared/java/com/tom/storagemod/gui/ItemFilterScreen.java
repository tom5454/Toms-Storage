package com.tom.storagemod.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ItemFilterScreen extends AbstractFilteredScreen<ItemFilterMenu> {
	private static final ResourceLocation DISPENSER_GUI_TEXTURES = new ResourceLocation("textures/gui/container/dispenser.png");

	private GuiButton buttonAllowList, buttonMatchNBT;

	public ItemFilterScreen(ItemFilterMenu container, Inventory playerInventory, Component textComponent) {
		super(container, playerInventory, textComponent);
	}

	@Override
	protected void init() {
		super.init();
		//this.titleX = (this.xSize - this.font.getStringPropertyWidth(this.title)) / 2;

		buttonAllowList = addRenderableWidget(new GuiButton(leftPos - 18, topPos + 5, 0, b -> {
			click(1, buttonAllowList.getState() != 1);
		}));
		buttonAllowList.tooltipFactory = s -> Tooltip.create(Component.translatable("tooltip.toms_storage.allowList_" + s));

		buttonMatchNBT = addRenderableWidget(new GuiButton(leftPos - 18, topPos + 5 + 18, 1, b -> {
			click(0, buttonMatchNBT.getState() != 1);
		}));
		buttonMatchNBT.tooltipFactory = s -> Tooltip.create(Component.translatable("tooltip.toms_storage.matchNBT_" + s));
	}

	private void click(int id, boolean val) {
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, (id << 1) | (val ? 1 : 0));
	}

	@Override
	public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		buttonMatchNBT.setState(menu.matchNBT ? 1 : 0);
		buttonAllowList.setState(menu.allowList ? 1 : 0);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics matrixStack, float partialTicks, int x, int y) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		matrixStack.blit(DISPENSER_GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}
}
