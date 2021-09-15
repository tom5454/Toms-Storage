package com.tom.storagemod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import me.shedaniel.autoconfig.ConfigData;

@me.shedaniel.autoconfig.annotation.Config(name = "toms_storage")
public class Config implements ConfigData {
	public boolean onlyTrims = false;
	public int invRange = 16;
	public int wirelessRange = 16;
	public int invConnectorMaxCables = 2048;
	public List<String> multiblockInv = new ArrayList<>();
	public int advWirelessRange = 64;

	@Override
	public void validatePostLoad() throws ValidationException {
		if(invRange < 4 || invRange > 64) {
			invRange = 16;
			StorageMod.LOGGER.warn("Inventory Connector Range out of bounds, resetting to default");
		}
		if(wirelessRange < 4 || wirelessRange > 64) {
			wirelessRange = 16;
			StorageMod.LOGGER.warn("Wireless Range out of bounds, resetting to default");
		}
		if(invConnectorMaxCables < 4) {
			invConnectorMaxCables = 2048;
			StorageMod.LOGGER.warn("Inventory Cable Range out of bounds, resetting to default");
		}
		if(advWirelessRange < 16 || advWirelessRange > 512) {
			advWirelessRange = 64;
			StorageMod.LOGGER.warn("Adv Wireless Range out of bounds, resetting to default");
		}
		StorageMod.LOGGER.info("Config loaded");
	}

	private void reloadConfig() {
		StorageMod.multiblockInvs = multiblockInv.stream().map(Identifier::new).map(Registry.BLOCK::get).filter(e -> e != null && e != Blocks.AIR).collect(Collectors.toSet());
	}

	public static Set<Block> getMultiblockInvs() {
		if(StorageMod.multiblockInvs == null)StorageMod.CONFIG.reloadConfig();
		return StorageMod.multiblockInvs;
	}
}
