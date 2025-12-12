package com.tom.storagemod.screen.widget;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;

public class ToggleButton extends IconButton {
	private Identifier iconOn;
	private boolean state;
	private Tooltip ttTrue, ttFalse;

	public static class Builder {
		private int x, y;
		private Component name = Component.empty();
		private Identifier on, off;

		public Builder(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public Builder name(Component name) {
			this.name = name;
			return this;
		}

		public Builder iconOn(Identifier icon) {
			this.on = icon;
			return this;
		}

		public Builder iconOff(Identifier icon) {
			this.off = icon;
			return this;
		}

		public ToggleButton build(BooleanConsumer pressable) {
			return new ToggleButton(x, y, name, off, on, pressable);
		}
	}

	public static Builder builder(int x, int y) {
		return new Builder(x, y);
	}

	protected ToggleButton(int x, int y, Component name, Identifier iconOff, Identifier iconOn, BooleanConsumer pressable) {
		super(x, y, name, iconOff, onPress(pressable));
		this.iconOn = iconOn;
	}

	public void setState(boolean state) {
		this.state = state;
		super.setTooltip(state ? ttTrue : ttFalse);
	}

	public boolean getState() {
		return state;
	}

	@Override
	public Identifier getIcon() {
		return state ? iconOn : icon;
	}

	private static ButtonPressHandler onPress(BooleanConsumer stateUpdate) {
		return (b, ev) -> stateUpdate.accept(!((ToggleButton)b).state);
	}

	@Override
	public void setTooltip(Tooltip p_259796_) {
		setTooltip(p_259796_, p_259796_);
	}

	public void setTooltip(Tooltip ttFalse, Tooltip ttTrue) {
		this.ttFalse = ttFalse;
		this.ttTrue = ttTrue;
		super.setTooltip(state ? ttTrue : ttFalse);
	}
}
