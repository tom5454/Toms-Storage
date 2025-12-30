package com.tom.storagemod.jade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.BasicInventoryHopperBlock;
import com.tom.storagemod.block.InventoryConnectorBlock;
import com.tom.storagemod.block.entity.BasicInventoryHopperBlockEntity;
import com.tom.storagemod.block.entity.InventoryConnectorBlockEntity;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
	public static final ResourceLocation INVENTORY_CONNECTOR = ResourceLocation.tryBuild(StorageMod.modid, "inventory_connector");
	public static final ResourceLocation INVENTORY_CONFIG = ResourceLocation.tryBuild(StorageMod.modid, "inventory_config");
	public static final ResourceLocation INVENTORY_HOPPER = ResourceLocation.tryBuild(StorageMod.modid, "inventory_hopper");

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(InventoryConnectorProvider.INSTANCE, InventoryConnectorBlockEntity.class);
		registration.registerBlockDataProvider(InventoryConfigProvider.INSTANCE, BlockEntity.class);
		registration.registerBlockDataProvider(InventoryHopperProvider.INSTANCE, BasicInventoryHopperBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(InventoryConnectorProvider.INSTANCE, InventoryConnectorBlock.class);
		registration.registerBlockComponent(InventoryConfigProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(InventoryHopperProvider.INSTANCE, BasicInventoryHopperBlock.class);
	}
}
