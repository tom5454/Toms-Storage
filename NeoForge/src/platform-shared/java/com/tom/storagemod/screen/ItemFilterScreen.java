package com.tom.storagemod.screen;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.menu.ItemFilterMenu;
import com.tom.storagemod.screen.widget.ToggleButton;

public class ItemFilterScreen extends AbstractFilteredScreen<ItemFilterMenu> {
	private static final ResourceLocation DISPENSER_GUI_TEXTURES = ResourceLocation.parse("textures/gui/container/dispenser.png");

	private ToggleButton buttonAllowList, buttonMatchNBT;

	public ItemFilterScreen(ItemFilterMenu container, Inventory playerInventory, Component textComponent) {
		super(container, playerInventory, textComponent);
	}

	@Override
	protected void init() {
		super.init();
		//this.titleX = (this.xSize - this.font.getStringPropertyWidth(this.title)) / 2;

		buttonAllowList = addRenderableWidget(ToggleButton.builder(leftPos - 18, topPos + 5).
				iconOff(ResourceLocation.tryBuild(StorageMod.modid, "icons/deny")).
				iconOn(ResourceLocation.tryBuild(StorageMod.modid, "icons/allow")).
				build(s -> click(1, s)));
		buttonAllowList.setTooltip(Tooltip.create(Component.translatable("tooltip.toms_storage.denyList")), Tooltip.create(Component.translatable("tooltip.toms_storage.allowList")));

		buttonMatchNBT = addRenderableWidget(ToggleButton.builder(leftPos - 18, topPos + 5 + 18).
				iconOff(ResourceLocation.tryBuild(StorageMod.modid, "icons/match_tag_off")).
				iconOn(ResourceLocation.tryBuild(StorageMod.modid, "icons/match_tag_on")).
				build(s -> click(0, s)));
		buttonMatchNBT.setTooltip(Tooltip.create(Component.translatable("tooltip.toms_storage.matchNBT_off")), Tooltip.create(Component.translatable("tooltip.toms_storage.matchNBT_on")));
	}

	private void click(int id, boolean val) {
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, (id << 1) | (val ? 1 : 0));
	}

	@Override
	public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
		buttonMatchNBT.setState(menu.matchNBT);
		buttonAllowList.setState(menu.allowList);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics matrixStack, float partialTicks, int x, int y) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		matrixStack.blit(RenderType::guiTextured, DISPENSER_GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	public void getExclusionAreas(Consumer<Box> consumer) {
		consumer.accept(new Box(leftPos - 20, topPos, 25, 40));
	}
}
