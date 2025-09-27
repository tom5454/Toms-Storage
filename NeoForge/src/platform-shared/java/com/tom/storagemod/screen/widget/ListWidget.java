package com.tom.storagemod.screen.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import com.tom.storagemod.StorageMod;

public abstract class ListWidget<T> extends AbstractWidget {
	protected static final WidgetSprites LIST_BUTTON_SPRITES = new WidgetSprites(
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_button"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_button_disabled"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_button_hovered")
			);

	protected static final WidgetSprites LIST_BUTTON_SPRITES_S = new WidgetSprites(
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_button_selected"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_button_disabled"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_button_selected_hovered")
			);

	protected static final WidgetSprites SCROLL_SPRITES = new WidgetSprites(
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_scroll"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_scroll_disabled"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_scroll_hovered")
			);

	private final int elemH;
	protected float currentScroll;
	protected boolean isScrolling;
	protected boolean wasClicking;
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

	public void preRender(int mouseX, int mouseY) {
		boolean flag = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_RELEASE;

		int k = this.getX() + 56;
		int l = this.getY() - 1;
		int i1 = k + 14;
		int j1 = l + 58;

		if (!this.wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1) {
			this.isScrolling = this.needsScrollBars();
		}

		if (!flag) {
			this.isScrolling = false;
		}
		this.wasClicking = flag;

		if (this.isScrolling) {
			this.currentScroll = (mouseY - l - 4.5F) / (j1 - l - 9.0F);
			this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);
		}
	}

	@Override
	protected void renderWidget(GuiGraphics st, int mouseX, int mouseY, float p_268085_) {
		int x = this.getX() + getWidth() - 6;
		int y = this.getY() - 1 + (int) ((getHeight() - 9) * this.currentScroll);
		boolean isHovered = mouseX >= x && mouseY >= y && mouseX < x + 5 && mouseY < y + 9;
		st.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLL_SPRITES.get(this.needsScrollBars(), isHovered), x, y, 5, 9);
	}

	public void tooltip(GuiGraphics matrixStack, int mouseX, int mouseY) {
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
		public void renderWidget(GuiGraphics st, int mouseX, int mouseY, float pt) {
			if (this.visible) {
				T id = getId();
				if(id != null) {
					int x = getX();
					int y = getY();
					this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
					var spr = (id.equals(selected) ? LIST_BUTTON_SPRITES_S : LIST_BUTTON_SPRITES).get(this.active, this.isHoveredOrFocused());
					st.blitSprite(RenderPipelines.GUI_TEXTURED, spr, this.getX(), this.getY(), this.getWidth(), this.getHeight());
					renderEntry(st, this.getX(), this.getY(), this.getWidth(), this.getHeight(), id, mouseX, mouseY, pt);
					int c = this.active ? 0xFFFFFF : 0xA0A0A0;
					int v = c | Mth.ceil(this.alpha * 255.0f) << 24;
					int k = this.getX() + 2;
					int l = this.getX() + this.getWidth() - 2;
					AbstractWidget.renderScrollingString(st, getFont(), toComponent(id), k, this.getY(), l, this.getY() + this.getHeight(), v);
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
	protected abstract void renderTooltip(GuiGraphics graphics, T data, int mouseX, int mouseY);

	protected void selectionChanged(T to) {
	}

	protected void renderEntry(GuiGraphics st, int x2, int y2, int width, int height, T id, int mouseX, int mouseY, float pt) {
	}
}
