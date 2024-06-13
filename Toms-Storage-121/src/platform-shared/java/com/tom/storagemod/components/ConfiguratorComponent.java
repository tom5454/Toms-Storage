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

public record ConfiguratorComponent(BlockPos bound, boolean isBound, boolean selecting, boolean showInvBox, boolean massSelect, BlockPos boxStart, List<BlockPos> selection) {
	private static final ConfiguratorComponent EMPTY = new ConfiguratorComponent(BlockPos.ZERO, false, false, false, false, BlockPos.ZERO, Collections.emptyList());

	public static final Codec<ConfiguratorComponent> CODEC = RecordCodecBuilder.<ConfiguratorComponent>mapCodec(b -> {
		return b.group(
				BlockPos.CODEC.fieldOf("bound").forGetter(ConfiguratorComponent::bound),
				Codec.BOOL.fieldOf("is_bound").forGetter(ConfiguratorComponent::isBound),
				Codec.BOOL.fieldOf("selecting").forGetter(ConfiguratorComponent::selecting),
				Codec.BOOL.fieldOf("show_inv_box").forGetter(ConfiguratorComponent::showInvBox),
				Codec.BOOL.fieldOf("mass_select").forGetter(ConfiguratorComponent::massSelect),
				BlockPos.CODEC.fieldOf("box_start").forGetter(ConfiguratorComponent::boxStart),
				Codec.list(BlockPos.CODEC).fieldOf("selection").forGetter(ConfiguratorComponent::selection)
				).apply(b, ConfiguratorComponent::new);
	}).codec();

	public static ConfiguratorComponent empty() {
		return EMPTY;
	}

	public ConfiguratorComponent hiddenItem() {
		return new ConfiguratorComponent(bound, isBound, selecting, false, false, boxStart, selection);
	}

	public ConfiguratorComponent showInv(List<BlockPos> sel) {
		return new ConfiguratorComponent(bound, isBound, selecting, true, massSelect, boxStart, sel);
	}

	public ConfiguratorComponent massSelectStart(BlockPos pos) {
		return new ConfiguratorComponent(bound, isBound, selecting, showInvBox, true, pos, selection);
	}

	public ConfiguratorComponent massSelectEnd() {
		return new ConfiguratorComponent(bound, isBound, selecting, showInvBox, false, BlockPos.ZERO, selection);
	}

	public ConfiguratorComponent massSelectEnd(List<BlockPos> sel) {
		return new ConfiguratorComponent(bound, isBound, selecting, showInvBox, false, BlockPos.ZERO, sel);
	}

	public ConfiguratorComponent setSelection(List<BlockPos> sel) {
		return new ConfiguratorComponent(bound, isBound, selecting, showInvBox, massSelect, boxStart, sel);
	}

	public static class Configurator {
		private ItemStack stack;
		private ConfiguratorComponent comp;

		public Configurator(ItemStack stack) {
			this.stack = stack;
			this.comp = stack.get(Content.configuratorComponent.get());
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
			set(comp.massSelectStart(pos));
		}

		public void massSelectEnd() {
			set(comp.massSelectEnd());
		}

		public void clear() {
			set(EMPTY);
		}

		public void showInvBox(Collection<BlockPos> sel) {
			set(comp.showInv(new ArrayList<>(sel)));
		}

		public void massSelectEnd(Collection<BlockPos> sel) {
			set(comp.massSelectEnd(new ArrayList<>(sel)));
		}

		public void setSelection(Collection<BlockPos> sel) {
			set(comp.setSelection(new ArrayList<>(sel)));
		}

		public void startSelection(BlockPos bind, Collection<BlockPos> sel) {
			set(new ConfiguratorComponent(bind, true, true, false, false, BlockPos.ZERO, new ArrayList<>(sel)));
		}

		private void set(ConfiguratorComponent c) {
			comp = c;
			stack.set(Content.configuratorComponent.get(), c);
		}
	}
}
