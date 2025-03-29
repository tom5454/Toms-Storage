package com.tom.storagemod.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.tom.storagemod.block.entity.InventoryConnectorBlockEntity;
import com.tom.storagemod.block.entity.InventoryConnectorBlockEntity.UsageInfo;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.ui.ProgressStyle;

public enum InventoryConnectorProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
	INSTANCE;

	@Override
	public ResourceLocation getUid() {
		return JadePlugin.INVENTORY_CONNECTOR;
	}

	@Override
	public void appendServerData(CompoundTag data, BlockAccessor accessor) {
		InventoryConnectorBlockEntity te = (InventoryConnectorBlockEntity) accessor.getBlockEntity();
		UsageInfo usage = te.getUsage();
		data.putInt("free", usage.free());
		data.putInt("all", usage.all());
		data.putInt("blocks", usage.blocks());
	}

	@Override
	public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
		if (accessor.getServerData().contains("free")) {
			int free = accessor.getServerData().getIntOr("free", 0);
			int all = accessor.getServerData().getIntOr("all", 0);
			int blocks = accessor.getServerData().getIntOr("blocks", 0);
			IElementHelper elements = IElementHelper.get();
			final ProgressStyle progressStyle = elements.progressStyle().color(0xFF8e691d, 0xFF342f26);
			tooltip.add(Component.translatable("tooltip.toms_storage.connector_info.size", free, all));
			tooltip.add(elements.progress((all - free) / (float) all, null, progressStyle, BoxStyle.getNestedBox(), true));
			tooltip.add(Component.translatable("tooltip.toms_storage.connector_info.inv", blocks));
		}
	}
}
