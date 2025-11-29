package com.tom.storagemod.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.lwjgl.glfw.GLFW;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class PopupMenuManager {
	private final Screen screen;
	private int x, y;
	private List<PopupElement> menu;
	private int selected;

	public PopupMenuManager(Screen screen) {
		this.screen = screen;
	}

	public void open(double x, double y, PopupElement... menu) {
		this.menu = Arrays.asList(menu);
		this.x = (int) x;
		this.y = (int) y;
		this.selected = -1;
	}

	public void replace(PopupElement... menu) {
		this.menu = Arrays.asList(menu);
		this.selected = -1;
	}

	public boolean render(GuiGraphics g, Font font, int pMouseX, int pMouseY) {
		if(menu != null) {
			g.setTooltipForNextFrame(font,
					IntStream.range(0, menu.size())
					.mapToObj(i -> menu.get(i).getHoveredText(pMouseX - x, pMouseY - y - i * 10, selected == i).getVisualOrderText())
					.toList(),
					DefaultTooltipPositioner.INSTANCE, x - 12, y + 12, true);
		}
		return menu == null;
	}

	private static boolean isHovering(int pX, int pY, int pWidth, int pHeight, double pMouseX, double pMouseY) {
		return pMouseX >= pX - 1 && pMouseX < pX + pWidth + 1 && pMouseY >= pY - 1 && pMouseY < pY + pHeight + 1;
	}

	public boolean mouseClick(MouseButtonEvent event) {
		if(menu != null) {
			for(int i = 0;i<menu.size();i++) {
				if (isHovering(x, y + i*10, 100, 8, event.x(), event.y())) {
					menu.get(i).onClick(new MouseButtonEvent(event.x() - x, event.y() - y - i * 10, event.buttonInfo()));
					selected = i;
					return true;
				}
			}
			menu = null;
			return true;
		}
		return false;
	}

	public boolean keyPressed(KeyEvent event) {
		if(menu != null) {
			if (selected != -1) {
				if(menu.get(selected).keyPressed(event))return true;
			}
			if (event.key() == GLFW.GLFW_KEY_DOWN) {
				selected = (selected + 1) % menu.size();
			} else if (event.key() == GLFW.GLFW_KEY_UP) {
				selected = (selected + menu.size() - 1) % menu.size();
			} else if (event.key() == 256) {
				menu = null;
			}
			return true;
		}
		return false;
	}

	public boolean charTyped(CharacterEvent characterEvent) {
		return menu != null && selected != -1 && menu.get(selected).charTyped(characterEvent);
	}

	public static interface PopupElement {
		MutableComponent getText();
		void activate();

		default void onClick(MouseButtonEvent event) {
			activate();
		}

		default boolean keyPressed(KeyEvent event) {
			if(event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
				activate();
				return true;
			}
			return false;
		}

		default boolean charTyped(CharacterEvent characterEvent) {
			return false;
		}

		default Component getHoveredText(int pMouseX, int pMouseY, boolean selected) {
			return getText().withStyle(isHovering(0, 0, 100, 8, pMouseX, pMouseY) || selected ? ChatFormatting.AQUA : ChatFormatting.GRAY);
		}
	}

	public static class ButtonElement implements PopupElement {
		private final Supplier<MutableComponent> text;
		private final Runnable action;

		public ButtonElement(Supplier<MutableComponent> text, Runnable action) {
			this.text = text;
			this.action = action;
		}

		@Override
		public MutableComponent getText() {
			return text.get();
		}

		@Override
		public void activate() {
			action.run();
		}
	}

	public static class TextFieldElement implements PopupElement {
		private final Supplier<MutableComponent> text;
		private final Consumer<String> action;
		private boolean activated;
		private EditBox box;
		private String value;

		public TextFieldElement(Supplier<MutableComponent> text, Consumer<String> action, Font font, String value) {
			this.text = text;
			this.action = action;
			box = new EditBox(font, 0, 0, 0, 0, null);
			this.value = value;
			box.setValue(value);
			box.setCanLoseFocus(false);
			box.setFocused(true);
		}

		@Override
		public MutableComponent getText() {
			if(activated) {
				StringBuilder sb = new StringBuilder(box.getValue());
				sb.insert(box.getCursorPosition(), '_');
				return Component.translatable("tooltip.toms_trading_network.textfield", text.get(), Component.literal(sb.toString()).withStyle(ChatFormatting.YELLOW));
			}
			return Component.translatable("tooltip.toms_trading_network.textfield", text.get(), Component.literal(box.getValue()));
		}

		@Override
		public void activate() {
			if(activated) {
				this.value = box.getValue();
				action.accept(value);
			}
			activated = !activated;
		}

		@Override
		public boolean keyPressed(KeyEvent event) {
			if (activated && event.key() == 256) {
				box.setValue(value);
				activated = false;
				return true;
			}
			if(PopupElement.super.keyPressed(event))return true;
			if(activated)return box.keyPressed(event);
			return false;
		}

		@Override
		public boolean charTyped(CharacterEvent characterEvent) {
			if(activated)return box.charTyped(characterEvent);
			return false;
		}
	}

	public void close() {
		menu = null;
	}
}
