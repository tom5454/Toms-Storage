package com.tom.storagemod;

import org.apache.commons.lang3.tuple.Pair;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public class Config {
	private static final Config INSTANCE = new Config();

	public boolean onlyTrims, runMultithreaded;
	public int invConnectorScanRange;
	public int invConnectorCableRange = 0;
	public int wirelessRange;
	public int advWirelessRange;
	public int wirelessTermBeaconLvl, wirelessTermBeaconLvlCrossDim;
	public int invLinkBeaconLvl, invLinkBeaconRange, invLinkBeaconLvlSameDim, invLinkBeaconLvlCrossDim;
	//public int inventoryConnectorMaxSlots;

	public static Config get() {
		return INSTANCE;
	}

	public static class Server {
		public IntValue invConnectorScanRange;
		public IntValue invConnectorCableRange;
		public IntValue wirelessRange;
		public BooleanValue onlyTrimsConnect, runMultithreaded;
		public IntValue advWirelessRange;
		public IntValue wirelessTermBeaconLvl, wirelessTermBeaconLvlCrossDim;
		public IntValue invLinkBeaconLvl, invLinkBeaconRange, invLinkBeaconLvlSameDim, invLinkBeaconLvlCrossDim;
		//public IntValue inventoryConnectorMaxSlots;

		private Server(ModConfigSpec.Builder builder) {
			invConnectorScanRange = builder.comment("Inventory Connector Range").
					translation("tomsstorage.config.inventory_connector_range").
					defineInRange("inventoryConnectorRange", 16, 4, 256);

			onlyTrimsConnect = builder.comment("Only Allow Trims to Connect Inventories").
					translation("tomsstorage.config.only_trims_connect").
					define("onlyTrimsConnect", false);

			invConnectorCableRange = builder.comment("Inventory Cable Connector Maximum number of cables").
					translation("tomsstorage.config.inv_cable_connector_max_scan").
					defineInRange("invCableConnectorMaxScanSize", 2048, 16, Integer.MAX_VALUE);

			wirelessRange = builder.comment("Wireless terminal reach").
					translation("tomsstorage.config.wireless_reach").
					defineInRange("wirelessReach", 16, 4, 64);

			advWirelessRange = builder.comment("Wireless terminal reach").
					translation("tomsstorage.config.adv_wireless_range").
					defineInRange("advWirelessRange", 64, 16, 512);

			wirelessTermBeaconLvl = builder.comment("Adv Wireless terminal requied beacon level for infinite range",
					"Value of 0 only requires a single beacon block nearby",
					"Value of -1 disables this feature entirely").
					translation("tomsstorage.config.adv_wireless_beacon_lvl").
					defineInRange("wirelessTermBeaconLvl", 1, -1, 4);

			wirelessTermBeaconLvlCrossDim = builder.comment("Adv Wireless terminal requied beacon level for cross dimensional access",
					"Value of 0 only requires a single beacon block nearby",
					"Value of -1 disables this feature entirely").
					translation("tomsstorage.config.adv_wireless_beacon_lvl_dim").
					defineInRange("wirelessTermBeaconLvlDim", 4, -1, 4);

			invLinkBeaconLvl = builder.comment("Inventory Cable Connector requied beacon level for inventory linking",
					"Value of 0 only requires a single beacon block",
					"Value of -1 disables this feature entirely").
					translation("tomsstorage.config.inv_link_beacon_lvl").
					defineInRange("invLinkBeaconLvl", 0, -1, 4);

			invLinkBeaconRange = builder.comment("Inventory Linking range with beacons",
					"Value of 0 disables this feature entirely").
					translation("tomsstorage.config.inv_link_beacon_range").
					defineInRange("invLinkBeaconRange", 4096, 0, Integer.MAX_VALUE);

			invLinkBeaconLvlSameDim = builder.comment("Inventory Cable Connector requied beacon level for same dimension access with unlimited range",
					"Value of 0 only requires a single beacon block",
					"Value of -1 disables this feature entirely").
					translation("tomsstorage.config.inv_link_beacon_lvl_same_dim").
					defineInRange("invLinkBeaconLvlSameDim", 1, -1, 4);

			invLinkBeaconLvlCrossDim = builder.comment("Inventory Cable Connector requied beacon level for cross dimensional access",
					"Value of 0 only requires a single beacon block",
					"Value of -1 disables this feature entirely").
					translation("tomsstorage.config.inv_link_beacon_lvl_dim").
					defineInRange("invLinkBeaconLvlCrossDim", 2, -1, 4);

			runMultithreaded = builder.comment("Use multi threading to improve performance").
					translation("tomsstorage.config.run_multithreaded").
					define("runMultithreaded", true);

			/*inventoryConnectorMaxSlots = builder.comment("Inventory Connector maximum slots").
					translation("tomsstorage.config.inv_connector_max_slots").
					defineInRange("inventoryConnectorMaxSlots", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);*/
		}
	}

	public static class Common {

		public Common(ModConfigSpec.Builder builder) {
			builder.comment("IMPORTANT NOTICE:",
					"THIS IS ONLY THE COMMON CONFIG. It does not contain all the values adjustable for Tom's Simple Storage",
					"The settings have been moved to toms_storage-server.toml",
					"That file is PER WORLD, meaning you have to go into 'saves/<world name>/serverconfig' to adjust it. Those changes will then only apply for THAT WORLD.",
					"You can then take that config file and put it in the 'defaultconfigs' folder to make it apply automatically to all NEW worlds you generate FROM THERE ON.",
					"This may appear confusing to many of you, but it is a new sensible way to handle configuration, because the server configuration is synced when playing multiplayer.").
			define("importantInfo", true);
		}
	}

	static final ModConfigSpec commonSpec;
	public static final Common COMMON;
	static {
		final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	static final ModConfigSpec serverSpec;
	public static final Server SERVER;
	static {
		final Pair<Server, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Server::new);
		serverSpec = specPair.getRight();
		SERVER = specPair.getLeft();
	}

	private void load(ModConfig modConfig) {
		if(modConfig.getType() == Type.SERVER) {
			onlyTrims = SERVER.onlyTrimsConnect.get();
			invConnectorScanRange = SERVER.invConnectorScanRange.get();
			invConnectorCableRange = SERVER.invConnectorCableRange.get();
			wirelessRange = SERVER.wirelessRange.get();
			advWirelessRange = SERVER.advWirelessRange.get();
			wirelessTermBeaconLvl = SERVER.wirelessTermBeaconLvl.get();
			wirelessTermBeaconLvlCrossDim = SERVER.wirelessTermBeaconLvlCrossDim.get();
			invLinkBeaconLvl = SERVER.invLinkBeaconLvl.get();
			invLinkBeaconRange = SERVER.invLinkBeaconRange.get();
			invLinkBeaconLvlSameDim = SERVER.invLinkBeaconLvlSameDim.get();
			invLinkBeaconLvlCrossDim = SERVER.invLinkBeaconLvlCrossDim.get();
			runMultithreaded = SERVER.runMultithreaded.getAsBoolean();
			//inventoryConnectorMaxSlots = SERVER.inventoryConnectorMaxSlots.getAsInt();
		} else if(modConfig.getType() == Type.COMMON) {
		}
	}

	@SubscribeEvent
	public void onLoad(final ModConfigEvent.Loading configEvent) {
		StorageMod.LOGGER.info("Loaded Tom's Simple Storage config file {}", configEvent.getConfig().getFileName());
		load(configEvent.getConfig());
	}

	@SubscribeEvent
	public void onFileChange(final ModConfigEvent.Reloading configEvent) {
		StorageMod.LOGGER.info("Tom's Simple Storage config just got changed on the file system!");
		load(configEvent.getConfig());
	}
}
