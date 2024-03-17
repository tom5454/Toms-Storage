package com.tom.storagemod;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

public class Config {
	public static boolean onlyTrims;
	public static int invRange;
	public static int invConnectorMax = 0;
	public static int wirelessRange, advWirelessRange;
	public static int wirelessTermBeaconLvl, wirelessTermBeaconLvlDim;
	public static Set<Block> multiblockInvs = new HashSet<>();

	public static class Server {
		public IntValue inventoryConnectorRange;
		public IntValue inventoryCableConnectorMaxCables;
		public IntValue wirelessRange, advWirelessRange;
		public IntValue wirelessTermBeaconLvl, wirelessTermBeaconLvlDim;
		public BooleanValue onlyTrimsConnect;
		public ConfigValue<List<? extends String>> multiblockInvs;

		private Server(ForgeConfigSpec.Builder builder) {
			inventoryConnectorRange = builder.comment("Inventory Connector Range").
					translation("tomsstorage.config.inventory_connector_range").
					defineInRange("inventoryConnectorRange", 16, 4, 256);

			onlyTrimsConnect = builder.comment("Only Allow Trims to Connect Inventories").
					translation("tomsstorage.config.only_trims_connect").
					define("onlyTrimsConnect", false);

			inventoryCableConnectorMaxCables = builder.comment("Inventory Cable Connector Maximum number of cables").
					translation("tomsstorage.config.inv_cable_connector_max_scan").
					defineInRange("invCableConnectorMaxScanSize", 2048, 16, Integer.MAX_VALUE);

			wirelessRange = builder.comment("Wireless terminal reach").
					translation("tomsstorage.config.wireless_reach").
					defineInRange("wirelessReach", 16, 4, 64);

			multiblockInvs = builder.comment("Multiblock inventories").
					translation("tomsstorage.config.multiblock_inv").
					defineList("multiblockInv", Collections.emptyList(), s -> true);

			advWirelessRange = builder.comment("Wireless terminal reach").
					translation("tomsstorage.config.adv_wireless_range").
					defineInRange("advWirelessRange", 64, 16, 512);

			wirelessTermBeaconLvl = builder.comment("Adv Wireless terminal requied beacon level for infinite range",
					"Value of 0 only requires a single beacon block nearby",
					"Value of -1 disables this feature entirely").
					translation("tomsstorage.config.adv_wireless_beacon_lvl").
					defineInRange("wirelessTermBeaconLvl", 1, -1, 4);

			wirelessTermBeaconLvlDim = builder.comment("Adv Wireless terminal requied beacon level for cross dimensional access",
					"Value of 0 only requires a single beacon block nearby",
					"Value of -1 disables this feature entirely").
					translation("tomsstorage.config.adv_wireless_beacon_lvl_dim").
					defineInRange("wirelessTermBeaconLvlDim", 4, -1, 4);
		}
	}

	static final ForgeConfigSpec serverSpec;
	public static final Server SERVER;
	static {
		final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
		serverSpec = specPair.getRight();
		SERVER = specPair.getLeft();
	}

	private static void load() {
		onlyTrims = SERVER.onlyTrimsConnect.get();
		invRange = SERVER.inventoryConnectorRange.get() * SERVER.inventoryConnectorRange.get();
		invConnectorMax = SERVER.inventoryCableConnectorMaxCables.get();
		wirelessRange = SERVER.wirelessRange.get();
		multiblockInvs = SERVER.multiblockInvs.get().stream().map(ResourceLocation::new).map(ForgeRegistries.BLOCKS::getValue).
				filter(e -> e != null && e != Blocks.AIR).collect(Collectors.toSet());
		advWirelessRange = SERVER.advWirelessRange.get();
		wirelessTermBeaconLvl = SERVER.wirelessTermBeaconLvl.get();
		wirelessTermBeaconLvlDim = SERVER.wirelessTermBeaconLvlDim.get();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent) {
		StorageMod.LOGGER.info("Loaded Tom's Simple Storage config file {}", configEvent.getConfig().getFileName());
		load();
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfig.Reloading configEvent) {
		StorageMod.LOGGER.info("Tom's Simple Storage config just got changed on the file system!");
		load();
	}
}
