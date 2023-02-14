package com.tom.storagemod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.tom.storagemod.platform.Platform;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;

@me.shedaniel.autoconfig.annotation.Config(name = "toms_storage")
public class Config implements ConfigData {
	public boolean onlyTrims = false;
	public int invRange = 16;
	public int wirelessRange = 16;
	public int invConnectorMaxCables = 2048;
	public List<String> multiblockInv = new ArrayList<>();
	public int advWirelessRange = 64;
	@Tooltip
	public int wirelessTermBeaconLvl = 1, wirelessTermBeaconLvlDim = 4;
	public int invLinkBeaconLvl = 1, invLinkBeaconLvlDim = 2;
	/*public int invDupScanSize = 100;*/

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
		if(wirelessTermBeaconLvl < 0 || wirelessTermBeaconLvl > 4) {
			wirelessTermBeaconLvl = 1;
			StorageMod.LOGGER.warn("wirelessTermBeaconLvl out of bounds, resetting to default");
		}
		if(wirelessTermBeaconLvlDim < 0 || wirelessTermBeaconLvlDim > 4) {
			wirelessTermBeaconLvlDim = 4;
			StorageMod.LOGGER.warn("wirelessTermBeaconLvlDim out of bounds, resetting to default");
		}
		if(invLinkBeaconLvl < 0 || invLinkBeaconLvl > 4) {
			invLinkBeaconLvl = 1;
			StorageMod.LOGGER.warn("invLinkBeaconLvl out of bounds, resetting to default");
		}
		if(invLinkBeaconLvlDim < 0 || invLinkBeaconLvlDim > 4) {
			invLinkBeaconLvlDim = 2;
			StorageMod.LOGGER.warn("invLinkBeaconLvlDim out of bounds, resetting to default");
		}
		/*if(invDupScanSize < 0) {
			invDupScanSize = 100;
			StorageMod.LOGGER.warn("invDupScanSize out of bounds, resetting to default");
		}*/
		StorageMod.LOGGER.info("Config loaded");
	}

	private void reloadConfig() {
		StorageMod.multiblockInvs = multiblockInv.stream().map(ResourceLocation::new).map(Platform::getBlockById).filter(e -> e != null && e != Blocks.AIR).collect(Collectors.toSet());
	}

	public static Set<Block> getMultiblockInvs() {
		if(StorageMod.multiblockInvs == null)StorageMod.CONFIG.reloadConfig();
		return StorageMod.multiblockInvs;
	}

	public static Config get() {
		return StorageMod.CONFIG;
	}
}
