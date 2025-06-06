package com.tom.storagemod.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.tom.storagemod.Content;

public record ConfiguratorComponent(BlockPos bound, boolean isBound, boolean selecting, boolean showInvBox, boolean massSelect, BlockPos boxStart, List<BlockPos> selection, long lastAction) {
	private static final ConfiguratorComponent EMPTY = new ConfiguratorComponent(BlockPos.ZERO, false, false, false, false, BlockPos.ZERO, Collections.emptyList(), 0L);

	public static final Codec<ConfiguratorComponent> CODEC = RecordCodecBuilder.<ConfiguratorComponent>mapCodec(b -> {
		return b.group(
				BlockPos.CODEC.fieldOf("bound").forGetter(ConfiguratorComponent::bound),
				Codec.BOOL.fieldOf("is_bound").forGetter(ConfiguratorComponent::isBound),
				Codec.BOOL.fieldOf("selecting").forGetter(ConfiguratorComponent::selecting),
				Codec.BOOL.fieldOf("show_inv_box").forGetter(ConfiguratorComponent::showInvBox),
				Codec.BOOL.fieldOf("mass_select").forGetter(ConfiguratorComponent::massSelect),
				BlockPos.CODEC.fieldOf("box_start").forGetter(ConfiguratorComponent::boxStart),
				Codec.list(BlockPos.CODEC).fieldOf("selection").forGetter(ConfiguratorComponent::selection),
				Codec.LONG.fieldOf("last_action").forGetter(ConfiguratorComponent::lastAction)
				).apply(b, ConfiguratorComponent::new);
	}).codec();

	public static ConfiguratorComponent empty() {
		return EMPTY;
	}

	public ConfiguratorComponent hiddenItem(long actionTime) {
		return new ConfiguratorComponent(bound, isBound, selecting, false, false, boxStart, selection, actionTime);
	}

	public ConfiguratorComponent showInv(List<BlockPos> sel, long actionTime) {
		return new ConfiguratorComponent(bound, isBound, selecting, true, massSelect, boxStart, sel, actionTime);
	}

	public ConfiguratorComponent massSelectStart(BlockPos pos, long actionTime) {
		return new ConfiguratorComponent(bound, isBound, selecting, showInvBox, true, pos, selection, actionTime);
	}

	public ConfiguratorComponent massSelectEnd(long actionTime) {
		return new ConfiguratorComponent(bound, isBound, selecting, showInvBox, false, BlockPos.ZERO, selection, actionTime);
	}

	public ConfiguratorComponent massSelectEnd(List<BlockPos> sel, long actionTime) {
		return new ConfiguratorComponent(bound, isBound, selecting, showInvBox, false, BlockPos.ZERO, sel, actionTime);
	}

	public ConfiguratorComponent setSelection(List<BlockPos> sel, long actionTime) {
		return new ConfiguratorComponent(bound, isBound, selecting, showInvBox, massSelect, boxStart, sel, actionTime);
	}

	public static class Configurator {
		private ItemStack stack;
		private ConfiguratorComponent comp;
		private long actionTime;

		public Configurator(ItemStack stack, long actionTime) {
			this.stack = stack;
			this.comp = stack.get(Content.configuratorComponent.get());
			this.actionTime = actionTime;
		}

		public boolean debounce() {
			return actionTime - comp.lastAction() < 3;
		}

		public BlockPos bound() {
			return comp.bound();
		}

		public BlockPos boxStart() {
			return comp.boxStart();
		}

		public boolean isBound() {
			return comp.isBound();
		}

		public boolean massSelect() {
			return comp.massSelect();
		}

		public boolean selecting() {
			return comp.selecting();
		}

		public List<BlockPos> selection() {
			return comp.selection();
		}

		public boolean showInvBox() {
			return comp.showInvBox();
		}

		public void massSelectStart(BlockPos pos) {
			set(comp.massSelectStart(pos, actionTime));
		}

		public void massSelectEnd() {
			set(comp.massSelectEnd(actionTime));
		}

		public void clear() {
			set(EMPTY);
		}

		public void showInvBox(Collection<BlockPos> sel) {
			set(comp.showInv(new ArrayList<>(sel), actionTime));
		}

		public void massSelectEnd(Collection<BlockPos> sel) {
			set(comp.massSelectEnd(new ArrayList<>(sel), actionTime));
		}

		public void setSelection(Collection<BlockPos> sel) {
			set(comp.setSelection(new ArrayList<>(sel), actionTime));
		}

		public void startSelection(BlockPos bind, Collection<BlockPos> sel) {
			set(new ConfiguratorComponent(bind, true, true, false, false, BlockPos.ZERO, new ArrayList<>(sel), actionTime));
		}

		private void set(ConfiguratorComponent c) {
			comp = c;
			stack.set(Content.configuratorComponent.get(), c);
		}
	}
}
