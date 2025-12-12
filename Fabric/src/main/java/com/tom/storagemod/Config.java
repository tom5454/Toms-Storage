package com.tom.storagemod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.Tooltip;

@me.shedaniel.autoconfig.annotation.Config(name = "toms_storage")
public class Config implements ConfigData {
	public boolean onlyTrims = false, runMultithreaded = true;
	public int invConnectorScanRange = 16;
	public int wirelessRange = 16;
	public int invConnectorMaxCables = 2048;
	public int advWirelessRange = 64;
	public int basicHopperCooldown = 10;
	@Tooltip
	public int wirelessTermBeaconLvl = 1, wirelessTermBeaconLvlCrossDim = 4;
	@Tooltip
	public int invLinkBeaconLvl = 0, invLinkBeaconRange = 4096, invLinkBeaconLvlSameDim = 1, invLinkBeaconLvlCrossDim = 2;
	//public int inventoryConnectorMaxSlots = Integer.MAX_VALUE;
	public List<String> blockedBlocks = new ArrayList<>(Arrays.asList("create:belt"));
	public List<String> blockedMods = new ArrayList<>();

	public static Config get() {
		return StorageMod.CONFIG;
	}

	public Set<Block> getBlockedBlocks() {
		if (StorageMod.blockedBlocks == null) {
			StorageMod.blockedBlocks = blockedBlocks.stream().map(Identifier::tryParse).filter(e -> e != null).
					map(id -> BuiltInRegistries.BLOCK.getValue(id)).filter(e -> e != null && e != Blocks.AIR).collect(Collectors.toSet());
		}
		return StorageMod.blockedBlocks;
	}

	public List<String> getBlockedMods() {
		return blockedMods;
	}
}
