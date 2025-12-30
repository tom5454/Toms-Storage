package com.tom.storagemod.jade;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import com.tom.storagemod.util.Priority;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum InventoryConfigComponentProvider implements IBlockComponentProvider {
	INSTANCE;

	@Override
	public Identifier getUid() {
		return JadePlugin.INVENTORY_CONFIG;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getServerData().getBooleanOr("bf", false)) {
			boolean skip = accessor.getServerData().getBooleanOr("skip", false);
			ValueInput in = TagValueInput.create(ProblemReporter.DISCARDING, Minecraft.getInstance().level.registryAccess(), accessor.getServerData());
			var f = in.read("filter", ItemStack.CODEC).orElse(ItemStack.EMPTY);
			tooltip.add(Component.translatable("tooltip.toms_storage.block_filter"));
			Priority pr = Priority.VALUES[Math.abs(accessor.getServerData().getByteOr("pr", (byte) 0)) % Priority.VALUES.length];
			if (skip) {
				tooltip.add(Component.translatable("tooltip.toms_storage.block_filter.skip"));
			} else {
				tooltip.add(Component.translatable("tooltip.toms_storage.priority_" + pr.name().toLowerCase(Locale.ROOT)));
				if (!f.isEmpty()) {
					JadeUtil.addFilterInfo(tooltip, accessor, f);
				}
			}
		}
	}
}
