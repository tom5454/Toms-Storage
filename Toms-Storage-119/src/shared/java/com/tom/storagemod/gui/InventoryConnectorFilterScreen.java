package com.tom.storagemod.gui;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

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
			click(0, buttonAllowList.state != 1 ? 1 : 0);
		}));

		buttonPriority = addRenderableWidget(new GuiButton(leftPos - 18, topPos + 5 + 18, 2, b -> {
			menu.priority = Priority.VALUES[(buttonPriority.state + 1) % Priority.VALUES.length];
			click(1, menu.priority.ordinal());
		}));

		buttonKeepLast = addRenderableWidget(new GuiButton(leftPos - 18, topPos + 5 + 36, 3, b -> {
			click(2, buttonKeepLast.state != 1 ? 1 : 0);
		}));
	}

	private void click(int id, int v) {
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, (id << 4) | v);
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		buttonAllowList.state = menu.allowList ? 1 : 0;
		buttonPriority.state = menu.priority.ordinal();
		buttonKeepLast.state = menu.keepLastInSlot ? 1 : 0;
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);

		if (buttonAllowList.isHoveredOrFocused()) {
			renderTooltip(matrixStack, Component.translatable("tooltip.toms_storage.allowList_" + buttonAllowList.state), mouseX, mouseY);
		}

		if (buttonPriority.isHoveredOrFocused()) {
			renderTooltip(matrixStack, Component.translatable("tooltip.toms_storage.priority_" + buttonPriority.state), mouseX, mouseY);
		}

		if (buttonKeepLast.isHoveredOrFocused()) {
			renderTooltip(matrixStack, Component.translatable("tooltip.toms_storage.keepLast_" + buttonKeepLast.state), mouseX, mouseY);
		}
	}

	@Override
	protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, DISPENSER_GUI_TEXTURES);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}
}
