package com.tom.storagemod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;

import com.tom.storagemod.api.MultiblockInventoryAPI;
import com.tom.storagemod.inventory.PlatformItemHandler;
import com.tom.storagemod.inventory.VanillaMultiblockInventories;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.platform.Platform;

//The value here should match an entry in the META-INF/mods.toml file
@Mod(StorageMod.modid)
public class StorageMod {
	public static final String modid = "toms_storage";

	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();

	public static boolean polymorph;

	public StorageMod(ModContainer mc, IEventBus bus) {
		// Register the setup method for modloading
		bus.addListener(this::setup);
		// Register the doClientStuff method for modloading
		bus.addListener(this::doClientStuff);
		if (FMLEnvironment.getDist() == Dist.CLIENT)StorageModClient.preInit(mc, bus);
		bus.addListener(this::registerCapabilities);
		bus.addListener(this::enqueueIMC);

		mc.registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
		mc.registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
		bus.register(Config.get());
		bus.register(NetworkHandler.class);

		Content.init();

		Platform.register(bus);

		polymorph = ModList.get().isLoaded("polymorph");
	}

	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("Tom's Storage Setup starting");
		MultiblockInventoryAPI.EVENT.register(VanillaMultiblockInventories::checkChest);
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		StorageModClient.clientSetup();
		//if (polymorph)PolymorphTerminalWidget.register();
	}

	private void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.Item.BLOCK, Content.openCrateBE.get(), (be, side) -> VanillaContainerWrapper.of(be));
		event.registerBlockEntity(Capabilities.Item.BLOCK, Content.connectorBE.get(), (be, side) -> new PlatformItemHandler(be));
		event.registerBlockEntity(Capabilities.Item.BLOCK, Content.invInterfaceBE.get(), (be, side) -> new PlatformItemHandler(be));
		event.registerBlockEntity(Capabilities.Item.BLOCK, Content.filingCabinetBE.get(), (be, side) -> VanillaContainerWrapper.of(be.getInv()));
		event.registerBlockEntity(Capabilities.Item.BLOCK, Content.invProxyBE.get(), (be, side) -> new PlatformItemHandler(be));
	}

	public void enqueueIMC(InterModEnqueueEvent e) {
		if(ModList.get().isLoaded("theoneprobe"))
			InterModComms.sendTo("theoneprobe", "getTheOneProbe", () -> {
				try {
					return Class.forName("com.tom.storagemod.top.TheOneProbeHandler").getConstructor().newInstance();
				} catch (Throwable e1) {
					LOGGER.warn("Failed to init TheOneProbeHandler", e1);
					return null;
				}
			});
	}
}
