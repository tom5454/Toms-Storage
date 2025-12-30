package com.tom.storagemod.jade;

import net.minecraft.resources.Identifier;
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
	public static final Identifier INVENTORY_CONNECTOR = Identifier.tryBuild(StorageMod.modid, "inventory_connector");
	public static final Identifier INVENTORY_CONFIG = Identifier.tryBuild(StorageMod.modid, "inventory_config");
	public static final Identifier INVENTORY_HOPPER = Identifier.tryBuild(StorageMod.modid, "inventory_hopper");

	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerBlockDataProvider(InventoryConnectorDataProvider.INSTANCE, InventoryConnectorBlockEntity.class);
		registration.registerBlockDataProvider(InventoryConfigDataProvider.INSTANCE, BlockEntity.class);
		registration.registerBlockDataProvider(InventoryHopperDataProvider.INSTANCE, BasicInventoryHopperBlockEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerBlockComponent(InventoryConnectorComponentProvider.INSTANCE, InventoryConnectorBlock.class);
		registration.registerBlockComponent(InventoryConfigComponentProvider.INSTANCE, Block.class);
		registration.registerBlockComponent(InventoryHopperComponentProvider.INSTANCE, BasicInventoryHopperBlock.class);
	}
}
