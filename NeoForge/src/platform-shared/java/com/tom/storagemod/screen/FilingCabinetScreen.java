package com.tom.storagemod.screen;

import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.menu.FilingCabinetMenu;

public class FilingCabinetScreen extends TSContainerScreen<FilingCabinetMenu> {
	private static final Identifier CONTAINER_BACKGROUND = Identifier.parse("textures/gui/container/generic_54.png");
	private static final Identifier SCROLLER_SPRITE = Identifier.parse("container/creative_inventory/scroller");
	private static final Identifier SIDE_SCROLLBAR = Identifier.tryBuild(StorageMod.modid, "textures/gui/side_scrollbar.png");
	private final int containerRows;
	private int lastScroll;
	protected float currentScroll;
	protected boolean isScrolling;

	public FilingCabinetScreen(FilingCabinetMenu inv, Inventory p_97742_, Component p_97743_) {
		super(inv, p_97742_, p_97743_, 176, 114 + inv.getRowCount() * 18);
		this.containerRows = inv.getRowCount();
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractRenderState(graphics, mouseX, mouseY, a);

		if (this.insideScrollbar(mouseX, mouseY)) {
			graphics.requestCursor(this.isScrolling ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
		}

		int xscr = this.leftPos + 174;
		int yscr = this.topPos + 18;
		int yscr2 = yscr + containerRows * 18;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, xscr, yscr + (int) ((yscr2 - yscr - 17) * this.currentScroll), 12, 15);
	}

	protected boolean insideScrollbar(final double xm, final double ym) {
		int xo = this.leftPos;
		int yo = this.topPos;
		int xscr = xo + 174;
		int yscr = yo + 18;
		int xscr2 = xscr + 14;
		int yscr2 = yscr + containerRows * 18;
		return xm >= xscr && ym >= yscr && xm < xscr2 && ym < yscr2;
	}

	@Override
	public void extractBackground(final GuiGraphicsExtractor gr, final int mouseX, final int mouseY,
			final float a) {
		super.extractBackground(gr, mouseX, mouseY, a);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		gr.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, i, j, 0, 0, this.imageWidth, this.containerRows * 18 + 17, 256, 256);
		gr.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, i, j + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96, 256, 256);
		gr.blit(RenderPipelines.GUI_TEXTURED, SIDE_SCROLLBAR, i + 170, j, 0, 0, 24, 115, 24, 115, 24, 115);
	}

	@Override
	protected void containerTick() {
		int i = (this.menu.getContainerSize() + 9 - 1) / 9 - containerRows;
		int scroll = (int) (currentScroll * i + 0.5D);

		if (lastScroll != scroll) {
			scroll(scroll);
			this.lastScroll = scroll;
		}
	}

	private void scroll(int id) {
		getMenu().setRow(id);
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
	}

	@Override
	public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double xd, double p_mouseScrolled_5_) {
		int i = (this.menu.getContainerSize() + 9 - 1) / 9 - 5;
		this.currentScroll = (float)(this.currentScroll - p_mouseScrolled_5_ / i);
		this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);
		return true;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 1 && insideScrollbar(event.x(), event.y())) {
			this.isScrolling = true;
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.isScrolling) {
			int yscr = this.topPos + 18;
			int yscr2 = yscr + containerRows * 18;
			currentScroll = ((float)event.y() - yscr - 7.5F) / (yscr2 - yscr - 15F);
			currentScroll = Mth.clamp(currentScroll, 0.0F, 1.0F);
			return true;
		} else {
			return super.mouseDragged(event, dx, dy);
		}
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == 1) {
			this.isScrolling = false;
		}
		return super.mouseReleased(event);
	}

	@Override
	public void getExclusionAreas(Consumer<Box> consumer) {
	}
}
