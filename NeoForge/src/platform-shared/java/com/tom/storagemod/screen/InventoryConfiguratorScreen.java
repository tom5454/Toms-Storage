package com.tom.storagemod.screen;

import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.item.IItemFilter;
import com.tom.storagemod.menu.InventoryConfiguratorMenu;
import com.tom.storagemod.menu.slot.ItemFilterSlot;
import com.tom.storagemod.screen.widget.EnumCycleButton;
import com.tom.storagemod.screen.widget.IconButton;
import com.tom.storagemod.screen.widget.ToggleButton;
import com.tom.storagemod.util.Priority;

public class InventoryConfiguratorScreen extends TSContainerScreen<InventoryConfiguratorMenu> {
	private static final ResourceLocation GUI_TEXTURES = ResourceLocation.tryBuild(StorageMod.modid, "textures/gui/inventory_configurator.png");
	protected EnumCycleButton<Priority> buttonPriority;
	protected EnumCycleButton<Direction> buttonSide;
	protected IconButton buttonAddConnected, buttonRemoveAll, buttonRemoveFilter;
	protected ToggleButton buttonSkip, buttonKeepLast;

	public InventoryConfiguratorScreen(InventoryConfiguratorMenu menu, Inventory p_97742_, Component p_97743_) {
		super(menu, p_97742_, p_97743_);
	}

	@Override
	protected void init() {
		super.init();

		buttonPriority = addRenderableWidget(new EnumCycleButton<>(leftPos - 18, topPos + 5, Component.translatable("narrator.toms_storage.priority"), "priority", Priority.VALUES, n -> {
			buttonPriority.setState(n);
			click(n.ordinal() << 3);
		}));

		buttonAddConnected = addRenderableWidget(new IconButton(leftPos - 18, topPos + 5 + 18, Component.translatable("narrator.toms_storage.add"), ResourceLocation.tryBuild(StorageMod.modid, "icons/add"), b -> {
			click(1);
		}));
		buttonAddConnected.setTooltip(Tooltip.create(Component.translatable("tooltip.toms_storage.inv_config.add_connected")));

		buttonRemoveAll = addRenderableWidget(new IconButton(leftPos - 18, topPos + 5 + 18 * 2, Component.translatable("narrator.toms_storage.removeAll"), ResourceLocation.tryBuild(StorageMod.modid, "icons/deny"), b -> {
			click(2);
		}));
		buttonRemoveAll.setTooltip(Tooltip.create(Component.translatable("tooltip.toms_storage.inv_config.remove_all_connected")));

		buttonSide = addRenderableWidget(new EnumCycleButton<>(leftPos - 18, topPos + 5 + 18 * 3, Component.translatable("narrator.toms_storage.side"), "side", Direction.values(), n -> {
			buttonSide.setState(n);
			click((n.ordinal() << 3) | 3);
		}));

		buttonSkip = addRenderableWidget(ToggleButton.builder(leftPos - 18, topPos + 5 + 18 * 4).
				name(Component.translatable("narrator.toms_storage.skip")).
				iconOff(ResourceLocation.tryBuild(StorageMod.modid, "icons/include_inv")).
				iconOn(ResourceLocation.tryBuild(StorageMod.modid, "icons/skip_inv")).
				build(s -> {
					click(((s ? 1 : 0) << 3) | 4);
				}));
		buttonSkip.setTooltip(
				Tooltip.create(Component.translatable("tooltip.toms_storage.inv_config.include")),
				Tooltip.create(Component.translatable("tooltip.toms_storage.inv_config.skip")));

		buttonKeepLast = addRenderableWidget(ToggleButton.builder(leftPos - 18, topPos + 5 + 18 * 5).
				name(Component.translatable("narrator.toms_storage.skip")).
				iconOff(ResourceLocation.tryBuild(StorageMod.modid, "icons/keep_last_off")).
				iconOn(ResourceLocation.tryBuild(StorageMod.modid, "icons/keep_last_1")).
				build(s -> {
					click(((s ? 1 : 0) << 3) | 5);
				}));
		buttonKeepLast.setTooltip(
				Tooltip.create(Component.translatable("tooltip.toms_storage.keepLast_off")),
				Tooltip.create(Component.translatable("tooltip.toms_storage.keepLast_on")));

		buttonPriority.tooltipFactory = s -> Tooltip.create(Component.translatable("tooltip.toms_storage.priority_" + s.name().toLowerCase(Locale.ROOT)));
		buttonSide.tooltipFactory = s -> Tooltip.create(Component.translatable("tooltip.toms_storage.side_" + s.name().toLowerCase(Locale.ROOT)));

		buttonRemoveFilter = addRenderableWidget(new IconButton(leftPos + imageWidth - 22, topPos - 22, Component.translatable("narrator.toms_storage.removeFilter"), ResourceLocation.tryBuild(StorageMod.modid, "icons/deny"), b -> {
			click(7);
		}));
		buttonRemoveFilter.setTooltip(Tooltip.create(Component.translatable("tooltip.toms_storage.inv_config.remove_filter")));
	}

	@Override
	public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack, mouseX, mouseY, partialTicks);
		buttonPriority.setState(menu.priority);
		buttonSide.setState(menu.side);
		buttonSkip.setState(menu.skip);
		buttonKeepLast.setState(menu.keepLast);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics matrixStack, float partialTicks, int x, int y) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		matrixStack.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	private void click(int id) {
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
	}

	@Override
	public boolean mouseClicked(double x, double y, int btn) {
		if (btn == 1 && hoveredSlot instanceof ItemFilterSlot && hoveredSlot.getItem().getItem() instanceof IItemFilter) {
			click((hoveredSlot.index << 3) | 6);
			return true;
		}
		return super.mouseClicked(x, y, btn);
	}
}
