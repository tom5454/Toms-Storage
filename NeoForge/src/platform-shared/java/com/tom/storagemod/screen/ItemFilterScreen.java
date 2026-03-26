package com.tom.storagemod.screen;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.menu.ItemFilterMenu;
import com.tom.storagemod.screen.widget.ToggleButton;

public class ItemFilterScreen extends AbstractFilteredScreen<ItemFilterMenu> {
	private static final Identifier DISPENSER_GUI_TEXTURES = Identifier.parse("textures/gui/container/dispenser.png");

	private ToggleButton buttonAllowList, buttonMatchNBT;

	public ItemFilterScreen(ItemFilterMenu container, Inventory playerInventory, Component textComponent) {
		super(container, playerInventory, textComponent);
	}

	@Override
	protected void init() {
		super.init();
		//this.titleX = (this.xSize - this.font.getStringPropertyWidth(this.title)) / 2;

		buttonAllowList = addRenderableWidget(ToggleButton.builder(leftPos - 18, topPos + 5).
				iconOff(Identifier.tryBuild(StorageMod.modid, "icons/deny")).
				iconOn(Identifier.tryBuild(StorageMod.modid, "icons/allow")).
				build(s -> click(1, s)));
		buttonAllowList.setTooltip(Tooltip.create(Component.translatable("tooltip.toms_storage.denyList")), Tooltip.create(Component.translatable("tooltip.toms_storage.allowList")));

		buttonMatchNBT = addRenderableWidget(ToggleButton.builder(leftPos - 18, topPos + 5 + 18).
				iconOff(Identifier.tryBuild(StorageMod.modid, "icons/match_tag_off")).
				iconOn(Identifier.tryBuild(StorageMod.modid, "icons/match_tag_on")).
				build(s -> click(0, s)));
		buttonMatchNBT.setTooltip(Tooltip.create(Component.translatable("tooltip.toms_storage.matchNBT_off")), Tooltip.create(Component.translatable("tooltip.toms_storage.matchNBT_on")));
	}

	private void click(int id, boolean val) {
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, (id << 1) | (val ? 1 : 0));
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		buttonMatchNBT.setState(menu.matchNBT);
		buttonAllowList.setState(menu.allowList);
		super.extractRenderState(graphics, mouseX, mouseY, a);
	}

	@Override
	public void extractBackground(final GuiGraphicsExtractor gr, final int mouseX, final int mouseY,
			final float a) {
		super.extractBackground(gr, mouseX, mouseY, a);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		gr.blit(RenderPipelines.GUI_TEXTURED, DISPENSER_GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	public void getExclusionAreas(Consumer<Box> consumer) {
		consumer.accept(new Box(leftPos - 20, topPos, 25, 40));
	}
}
