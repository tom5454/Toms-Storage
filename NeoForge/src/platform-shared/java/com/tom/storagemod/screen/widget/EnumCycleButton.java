package com.tom.storagemod.screen.widget;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import com.tom.storagemod.StorageMod;

public class EnumCycleButton<T extends Enum<T>> extends IconButton {
	private T state;
	public Function<T, Tooltip> tooltipFactory;
	public Function<T, ResourceLocation> icon;

	public EnumCycleButton(int x, int y, Component name, String prefix, T[] el, Consumer<T> stateUpdate) {
		super(x, y, name, null, onPress(el, stateUpdate));
		this.icon = e -> ResourceLocation.tryBuild(StorageMod.modid, "icons/" + prefix + "_" + e.name().toLowerCase(Locale.ROOT));
	}

	@Override
	protected MutableComponent createNarrationMessage() {
		return wrapDefaultNarrationMessage(name.copy().append(" ").append(Component.translatable("narrator.toms_storage.button_state." + state.name().toLowerCase(Locale.ROOT))));
	}

	@Override
	public ResourceLocation getIcon() {
		return icon.apply(state);
	}

	public void setState(T state) {
		if(this.state != state && tooltipFactory != null)setTooltip(tooltipFactory.apply(state));
		this.state = state;
	}

	public T getState() {
		return state;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Enum<T>> OnPress onPress(T[] el, Consumer<T> stateUpdate) {
		return b -> stateUpdate.accept(el[(((EnumCycleButton<T>) b).getState().ordinal() + el.length + (Screen.hasShiftDown() ? -1 : 1)) % el.length]);
	}
}
