package com.tom.storagemod.jade;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum InventoryHopperComponentProvider implements IBlockComponentProvider {
	INSTANCE;

	@Override
	public Identifier getUid() {
		return JadePlugin.INVENTORY_HOPPER;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		ValueInput in = TagValueInput.create(ProblemReporter.DISCARDING, Minecraft.getInstance().level.registryAccess(), accessor.getServerData());
		var f = in.read("filter", ItemStack.CODEC).orElse(ItemStack.EMPTY);
		if (!f.isEmpty()) {
			JadeUtil.addFilterInfo(tooltip, accessor, f);
		}
		if (!accessor.getServerData().getBooleanOr("enabled", true)) {
			tooltip.add(Component.translatable("tooltip.toms_storage.disabled_by_redstone"));
		}
	}
}
