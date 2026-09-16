package com.tom.storagemod.screen.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.platform.cursor.CursorTypes;

import com.tom.storagemod.StorageMod;

public abstract class ListWidget<T> extends AbstractWidget {
	protected static final WidgetSprites LIST_BUTTON_SPRITES = new WidgetSprites(
			Identifier.tryBuild(StorageMod.modid, "widget/small_button"),
			Identifier.tryBuild(StorageMod.modid, "widget/small_button_disabled"),
			Identifier.tryBuild(StorageMod.modid, "widget/small_button_hovered")
			);

	protected static final WidgetSprites LIST_BUTTON_SPRITES_S = new WidgetSprites(
			Identifier.tryBuild(StorageMod.modid, "widget/small_button_selected"),
			Identifier.tryBuild(StorageMod.modid, "widget/small_button_disabled"),
			Identifier.tryBuild(StorageMod.modid, "widget/small_button_selected_hovered")
			);

	protected static final WidgetSprites SCROLL_SPRITES = new WidgetSprites(
			Identifier.tryBuild(StorageMod.modid, "widget/small_scroll"),
			Identifier.tryBuild(StorageMod.modid, "widget/small_scroll_disabled"),
			Identifier.tryBuild(StorageMod.modid, "widget/small_scroll_hovered")
			);

	private final int elemH;
	protected float currentScroll;
	protected boolean isScrolling;
	protected Supplier<List<T>> list;
	protected T selected;
	private List<ListEntry> listEntries = new ArrayList<>();

	public ListWidget(int x, int y, int w, int h, int elemH, Component narrator) {
		super(x, y, w, h, narrator);
		this.elemH = elemH;
		for(int i = 0;i<getLines();i++) {
			listEntries.add(new ListEntry(x, y + i * elemH, i));
		}
	}

	private boolean insideScrollbar(final double xm, final double ym) {
		int xo = this.getX();
		int yo = this.getY();
		int xscr = xo + 56;
		int yscr = yo - 1;
		int xscr2 = xscr + 14;
		int yscr2 = yscr + 58;
		return xm >= xscr && ym >= yscr && xm < xscr2 && ym < yscr2 && needsScrollBars();
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor st, int mouseX, int mouseY, float pt) {
		int x = this.getX() + getWidth() - 6;
		int y = this.getY() - 1 + (int) ((getHeight() - 9) * this.currentScroll);
		boolean isHovered = mouseX >= x && mouseY >= y && mouseX < x + 5 && mouseY < y + 9;
		st.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_SPRITES.get(this.needsScrollBars(), isHovered), x, y, 5, 9);

		if (this.insideScrollbar(mouseX, mouseY)) {
			if (this.needsScrollBars()) {
				st.requestCursor(this.isScrolling ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
			} else {
				st.requestCursor(CursorTypes.NOT_ALLOWED);
			}
		}
	}

	public void tooltip(GuiGraphicsExtractor matrixStack, int mouseX, int mouseY) {
		listEntries.stream().filter(s -> s.isHovered()).findFirst().ifPresent(le -> {
			T id = le.getId();
			if(id != null)renderTooltip(matrixStack, id, mouseX, mouseY);
		});
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput p_259858_) {
	}

	@Override
	public boolean mouseScrolled(double p_94734_, double p_94735_, double p_94736_, double dir) {
		return scroll(dir);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 1) {
			if (this.insideScrollbar(event.x(), event.y())) {
				this.isScrolling = this.needsScrollBars();
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.isScrolling) {
			int yscr = this.getY() - 1;
			int yscr2 = yscr + 58;
			currentScroll = ((float)event.y() - yscr - 4.5F) / (yscr2 - yscr - 9.0F);
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

	private boolean scroll(double dir) {
		if (!this.needsScrollBars()) {
			return false;
		} else {
			int i = list.get().size() - getLines();
			this.currentScroll = (float)(this.currentScroll - dir / i);
			this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);
			return true;
		}
	}

	public boolean needsScrollBars() {
		return list.get().size() > getLines();
	}

	public class ListEntry extends Button {
		private int id;

		public ListEntry(int x, int y, int id) {
			super(x, y, ListWidget.this.width - 8, elemH, Component.empty(), null, DEFAULT_NARRATION);
			this.id = id;
			addButton(this);
		}

		/**
		 * Draws this button to the screen.
		 */
		@Override
		public void extractContents(GuiGraphicsExtractor st, int mouseX, int mouseY, float pt) {
			if (this.visible) {
				T id = getId();
				if(id != null) {
					int x = getX();
					int y = getY();
					this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
					var spr = (id.equals(selected) ? LIST_BUTTON_SPRITES_S : LIST_BUTTON_SPRITES).get(this.active, this.isHoveredOrFocused());
					st.blitSprite(RenderPipelines.GUI_TEXTURED, spr, this.getX(), this.getY(), this.getWidth(), this.getHeight());
					renderEntry(st, this.getX(), this.getY(), this.getWidth(), this.getHeight(), id, mouseX, mouseY, pt);
					extractScrollingStringOverContents(st.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE), toComponent(id), 2);
				}
			}
		}

		@Override
		public boolean mouseScrolled(double p_94734_, double p_94735_, double p_94736_, double dir) {
			return scroll(dir);
		}

		@Override
		public void onPress(InputWithModifiers input) {
			T id = getId();
			if(id != null) {
				selected = id;
				selectionChanged(id);
			}
		}

		private T getId() {
			List<T> l = list.get();
			int i = l.size() - getLines();
			int j = (int) (currentScroll * i + 0.5D);
			if (j < 0) {
				j = 0;
			}
			if(this.id + j < l.size()) {
				return l.get(this.id + j);
			}
			return null;
		}
	}

	public int getLines() {
		return height / elemH;
	}

	public void setList(Supplier<List<T>> list) {
		this.list = list;
	}

	public void setSelected(T selected) {
		this.selected = selected;
	}

	public T getSelected() {
		return selected;
	}

	public void setCurrentScroll(float currentScroll) {
		this.currentScroll = currentScroll;
	}

	protected abstract Font getFont();
	protected abstract void addButton(AbstractWidget listEntry);
	protected abstract Component toComponent(T data);
	protected abstract void renderTooltip(GuiGraphicsExtractor graphics, T data, int mouseX, int mouseY);

	protected void selectionChanged(T to) {
	}

	protected void renderEntry(GuiGraphicsExtractor st, int x2, int y2, int width, int height, T id, int mouseX, int mouseY, float pt) {
	}
}
