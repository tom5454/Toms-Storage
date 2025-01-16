package com.tom.storagemod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class Config {
	public static final List<String> defaultMultiblocks = Arrays.asList("");
	public static final List<String> defaultBlockedMods = Arrays.asList("");
	public static final List<String> defaultBlockedBlocks = Arrays.asList("");
	private static final Config INSTANCE = new Config();

	public boolean onlyTrims;
	public int invRange;
	public int invConnectorMax = 0;
	public int wirelessRange;
	public Set<Block> multiblockInvs = new HashSet<>();
	public int advWirelessRange;
	public int wirelessTermBeaconLvl, wirelessTermBeaconLvlDim;
	public int invLinkBeaconLvl, invLinkBeaconLvlDim;
	public int invDupScanSize;
	private Set<String> blockedMods = new HashSet<>();
	private Set<Block> blockedBlocks = new HashSet<>();

	public static Config get() {
		return INSTANCE;
	}

	public static class Server {
		public IntValue inventoryConnectorRange;
		public IntValue inventoryCableConnectorMaxCables;
		public IntValue wirelessRange;
		public BooleanValue onlyTrimsConnect;
		public IntValue advWirelessRange;
		public IntValue wirelessTermBeaconLvl, wirelessTermBeaconLvlDim;
		public IntValue invLinkBeaconLvl, invLinkBeaconLvlDim;
		public IntValue invDupScanSize;

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

			invLinkBeaconLvl = builder.comment("Inventory Cable Connector requied beacon level for inventory linking",
					"Value of 0 only requires a single beacon block",
					"Value of -1 disables this feature entirely").
					translation("tomsstorage.config.inv_link_beacon_lvl").
					defineInRange("invLinkBeaconLvl", 1, -1, 4);

			invLinkBeaconLvlDim = builder.comment("Inventory Cable Connector requied beacon level for cross dimensional access",
					"Value of 0 only requires a single beacon block",
					"Value of -1 disables this feature entirely").
					translation("tomsstorage.config.inv_link_beacon_lvl_dim").
					defineInRange("invLinkBeaconLvlDim", 2, -1, 4);

			invDupScanSize = builder.comment("Inventory Connector duplicate finder max slot count",
					"Value of 0 only disables").
					translation("tomsstorage.config.inv_dup_scan_size").
					defineInRange("invDupScanSize", 100, 0, Integer.MAX_VALUE);
		}
	}

	public static class Common {
		public ConfigValue<List<? extends String>> multiblockInvs;
		public ConfigValue<List<? extends String>> blockedMods;
		public ConfigValue<List<? extends String>> blockedBlocks;

		public Common(ForgeConfigSpec.Builder builder) {
			builder.comment("IMPORTANT NOTICE:",
					"THIS IS ONLY THE COMMON CONFIG. It does not contain all the values adjustable for Tom's Simple Storage",
					"The settings have been moved to toms_storage-server.toml",
					"That file is PER WORLD, meaning you have to go into 'saves/<world name>/serverconfig' to adjust it. Those changes will then only apply for THAT WORLD.",
					"You can then take that config file and put it in the 'defaultconfigs' folder to make it apply automatically to all NEW worlds you generate FROM THERE ON.",
					"This may appear confusing to many of you, but it is a new sensible way to handle configuration, because the server configuration is synced when playing multiplayer.").
			define("importantInfo", true);

			multiblockInvs = builder.comment("List of multiblock inventory blocks").
					translation("tomsstorage.config.multiblock_inv").
					defineList("multiblockInv", defaultMultiblocks, s -> true);

			blockedMods = builder.comment("List of mod ids whose blocks is ignored by the inventory connector").
					translation("tomsstorage.config.inv_blocked_mods").
					defineList("blockedMods", defaultBlockedMods, s -> true);

			blockedBlocks = builder.comment("List of block ids ignored by the inventory connector").
					translation("tomsstorage.config.inv_blocked_blocks").
					defineList("blockedBlocks", defaultBlockedBlocks, s -> true);
		}
	}

	static final ForgeConfigSpec commonSpec;
	public static final Common COMMON;
	static {
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		commonSpec = specPair.getRight();
		COMMON = specPair.getLeft();
	}

	static final ForgeConfigSpec serverSpec;
	public static final Server SERVER;
	static {
		final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
		serverSpec = specPair.getRight();
		SERVER = specPair.getLeft();
	}

	private void load(ModConfig modConfig) {
		if(modConfig.getType() == Type.SERVER) {
			onlyTrims = SERVER.onlyTrimsConnect.get();
			invRange = SERVER.inventoryConnectorRange.get() * SERVER.inventoryConnectorRange.get();
			invConnectorMax = SERVER.inventoryCableConnectorMaxCables.get();
			wirelessRange = SERVER.wirelessRange.get();
			advWirelessRange = SERVER.advWirelessRange.get();
			wirelessTermBeaconLvl = SERVER.wirelessTermBeaconLvl.get();
			wirelessTermBeaconLvlDim = SERVER.wirelessTermBeaconLvlDim.get();
			invLinkBeaconLvl = SERVER.invLinkBeaconLvl.get();
			invLinkBeaconLvlDim = SERVER.invLinkBeaconLvlDim.get();
			invDupScanSize = SERVER.invDupScanSize.get();
		} else if(modConfig.getType() == Type.COMMON) {
			multiblockInvs = COMMON.multiblockInvs.get().stream().map(ResourceLocation::tryParse).filter(e -> e != null).map(ForgeRegistries.BLOCKS::getValue).
					filter(e -> e != null && e != Blocks.AIR).collect(Collectors.toSet());

			blockedMods = new HashSet<>(COMMON.blockedMods.get());

			blockedBlocks = COMMON.blockedBlocks.get().stream().map(ResourceLocation::tryParse).filter(e -> e != null).map(ForgeRegistries.BLOCKS::getValue).
					filter(e -> e != null && e != Blocks.AIR).collect(Collectors.toSet());
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

	public Set<Block> getBlockedBlocks() {
		return blockedBlocks;
	}

	public Set<String> getBlockedMods() {
		return blockedMods;
	}
}
