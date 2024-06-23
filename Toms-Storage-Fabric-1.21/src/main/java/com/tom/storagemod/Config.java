package com.tom.storagemod;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;

@me.shedaniel.autoconfig.annotation.Config(name = "toms_storage")
public class Config implements ConfigData {
	public boolean onlyTrims = false, runMultithreaded = true;
	public int invConnectorScanRange = 16;
	public int wirelessRange = 16;
	public int invConnectorMaxCables = 2048;
	public int advWirelessRange = 64;
	@Tooltip
	public int wirelessTermBeaconLvl = 1, wirelessTermBeaconLvlCrossDim = 4;
	public int invLinkBeaconLvl = 0, invLinkBeaconRange = 4096, invLinkBeaconLvlSameDim = 1, invLinkBeaconLvlCrossDim = 2;
	//public int inventoryConnectorMaxSlots = Integer.MAX_VALUE;

	public static Config get() {
		return StorageMod.CONFIG;
	}
}
