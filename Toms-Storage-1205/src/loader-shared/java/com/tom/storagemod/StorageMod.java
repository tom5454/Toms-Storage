package com.tom.storagemod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.platform.Platform;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(StorageMod.modid)
public class StorageMod {
	public static final String modid = "toms_storage";

	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();

	public StorageMod(IEventBus bus) {
		// Register the setup method for modloading
		bus.addListener(this::setup);
		// Register the doClientStuff method for modloading
		bus.addListener(this::doClientStuff);
		if (FMLEnvironment.dist == Dist.CLIENT)StorageModClient.preInit(bus);
		bus.addListener(this::registerCapabilities);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
		bus.register(Config.get());
		bus.register(NetworkHandler.class);

		Content.init();

		Platform.register(bus);
	}

	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("Tom's Storage Setup starting");
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		StorageModClient.clientSetup();
	}

	private void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Content.openCrateTile.get(), (be, side) -> new InvWrapper(be));
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Content.connectorTile.get(), (be, side) -> be.getInventory());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Content.invCableConnectorFilteredTile.get(), (be, side) -> be.getInvHandler());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Content.invCableConnectorTile.get(), (be, side) -> be.getInvHandler());
		event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Content.invProxyTile.get(), (be, side) -> be.invHandler);
	}
}
