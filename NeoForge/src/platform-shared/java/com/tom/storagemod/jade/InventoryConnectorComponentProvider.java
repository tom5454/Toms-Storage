package com.tom.storagemod.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

public enum InventoryConnectorComponentProvider implements IBlockComponentProvider {
	INSTANCE;

	@Override
	public ResourceLocation getUid() {
		return JadePlugin.INVENTORY_CONNECTOR;
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getServerData().contains("free")) {
			int free = accessor.getServerData().getIntOr("free", 0);
			int all = accessor.getServerData().getIntOr("all", 0);
			int blocks = accessor.getServerData().getIntOr("blocks", 0);
			//final ProgressStyle progressStyle = JadeUI.progressStyle().color(0xFF8e691d, 0xFF342f26);
			tooltip.add(Component.translatable("tooltip.toms_storage.connector_info.size", free, all));
			tooltip.add(JadeUI.progressArrow((all - free) / (float) all));//, null, progressStyle, BoxStyle.nestedBox(), true
			tooltip.add(Component.translatable("tooltip.toms_storage.connector_info.inv", blocks));
		}
	}
}
