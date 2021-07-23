package com.tom.storagemod;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class Config {
	public static boolean onlyTrims;
	public static int invRange;
	public static int invConnectorMax = 0;
	public static int wirelessRange;

	public static class Server {
		public IntValue inventoryConnectorRange;
		public IntValue inventoryCableConnectorMaxCables;
		public IntValue wirelessRange;
		public BooleanValue onlyTrimsConnect;

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
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading configEvent) {
		StorageMod.LOGGER.info("Loaded Tom's Simple Storage config file {}", configEvent.getConfig().getFileName());
		load();
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
		StorageMod.LOGGER.info("Tom's Simple Storage config just got changed on the file system!");
		load();
	}
}
