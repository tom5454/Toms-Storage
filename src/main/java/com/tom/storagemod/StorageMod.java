package com.tom.storagemod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.tom.storagemod.block.BlockOpenCrate;
import com.tom.storagemod.block.InventoryConnector;
import com.tom.storagemod.block.StorageTerminal;
import com.tom.storagemod.block.TileEntityInventoryConnector;
import com.tom.storagemod.block.TileEntityOpenCrate;
import com.tom.storagemod.block.TileEntityStorageTerminal;
import com.tom.storagemod.gui.ContainerStorageTerminal;
import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.proxy.IProxy;
import com.tom.storagemod.proxy.ServerProxy;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("toms_storage")
public class StorageMod
{
	public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);
	public static InventoryConnector connector;
	public static StorageTerminal terminal;
	public static Block inventoryTrim;
	public static BlockOpenCrate openCrate;

	public static TileEntityType<TileEntityInventoryConnector> connectorTile;
	public static TileEntityType<TileEntityStorageTerminal> terminalTile;
	public static TileEntityType<TileEntityOpenCrate> openCrateTile;
	public static ContainerType<ContainerStorageTerminal> storageTerminal;

	// Directly reference a log4j logger.
	private static final Logger LOGGER = LogManager.getLogger();

	public StorageMod() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the enqueueIMC method for modloading
		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		// Register the processIMC method for modloading
		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
		// Register the doClientStuff method for modloading
		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event)
	{
		// some preinit code
		LOGGER.info("Tom's Storage Setup starting");
		proxy.setup();
	}

	/*private void doClientStuff(final FMLClientSetupEvent event) {
		// do something that can only be done on the client
		//LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
		proxy.clientSetup();

	}

	private void enqueueIMC(final InterModEnqueueEvent event)
	{
		// some example code to dispatch IMC to another mod
		//InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
	}

	private void processIMC(final InterModProcessEvent event)
	{
		// some example code to receive and process InterModComms from other mods
		LOGGER.info("Got IMC {}", event.getIMCStream().
				map(m->m.getMessageSupplier().get()).
				collect(Collectors.toList()));
	}*/
	// You can use SubscribeEvent and let the Event Bus discover methods to call

	/*@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event) {
		// do something when the server starts
		//LOGGER.info("HELLO from server starting");
	}*/

	// You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
	// Event bus for receiving Registry Events)
	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
			connector = new InventoryConnector();
			terminal = new StorageTerminal();
			openCrate = new BlockOpenCrate();
			inventoryTrim = new Block(Block.Properties.create(Material.WOOD).hardnessAndResistance(3).harvestTool(ToolType.AXE)).setRegistryName("ts.trim");
			blockRegistryEvent.getRegistry().register(connector);
			blockRegistryEvent.getRegistry().register(terminal);
			blockRegistryEvent.getRegistry().register(openCrate);
			blockRegistryEvent.getRegistry().register(inventoryTrim);
		}

		@SubscribeEvent
		public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
			registerItemForBlock(itemRegistryEvent, connector);
			registerItemForBlock(itemRegistryEvent, terminal);
			registerItemForBlock(itemRegistryEvent, openCrate);
			registerItemForBlock(itemRegistryEvent, inventoryTrim);
		}

		private static void registerItemForBlock(RegistryEvent.Register<Item> itemRegistryEvent, Block block) {
			itemRegistryEvent.getRegistry().register(new BlockItem(block, new Item.Properties().group(ItemGroup.REDSTONE)).setRegistryName(block.getRegistryName()));
		}

		@SubscribeEvent
		public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> tileRegistryEvent) {
			connectorTile = TileEntityType.Builder.create(TileEntityInventoryConnector::new, connector).build(null);
			connectorTile.setRegistryName("ts.inventory_connector.tile");
			terminalTile = TileEntityType.Builder.create(TileEntityStorageTerminal::new, terminal).build(null);
			terminalTile.setRegistryName("ts.storage_terminal.tile");
			openCrateTile = TileEntityType.Builder.create(TileEntityOpenCrate::new, openCrate).build(null);
			openCrateTile.setRegistryName("ts.open_crate.tile");
			tileRegistryEvent.getRegistry().register(connectorTile);
			tileRegistryEvent.getRegistry().register(terminalTile);
			tileRegistryEvent.getRegistry().register(openCrateTile);
		}

		@SubscribeEvent
		public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> containerRegistryEvent) {
			storageTerminal = new ContainerType<>(ContainerStorageTerminal::new);
			storageTerminal.setRegistryName("ts.storage_terminal.container");
			containerRegistryEvent.getRegistry().register(storageTerminal);
		}
	}
}
