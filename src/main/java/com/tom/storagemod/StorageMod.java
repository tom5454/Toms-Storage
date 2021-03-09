package com.tom.storagemod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.tom.storagemod.block.BlockInventoryCable;
import com.tom.storagemod.block.BlockInventoryCableConnector;
import com.tom.storagemod.block.BlockInventoryCableConnectorFiltered;
import com.tom.storagemod.block.BlockInventoryCableFramed;
import com.tom.storagemod.block.BlockInventoryHopperBasic;
import com.tom.storagemod.block.BlockInventoryProxy;
import com.tom.storagemod.block.BlockLevelEmitter;
import com.tom.storagemod.block.BlockOpenCrate;
import com.tom.storagemod.block.BlockPaintedTrim;
import com.tom.storagemod.block.BlockTrim;
import com.tom.storagemod.block.CraftingTerminal;
import com.tom.storagemod.block.InventoryConnector;
import com.tom.storagemod.block.StorageTerminal;
import com.tom.storagemod.gui.ContainerCraftingTerminal;
import com.tom.storagemod.gui.ContainerFiltered;
import com.tom.storagemod.gui.ContainerLevelEmitter;
import com.tom.storagemod.gui.ContainerStorageTerminal;
import com.tom.storagemod.item.ItemBlockPainted;
import com.tom.storagemod.item.ItemPaintKit;
import com.tom.storagemod.item.ItemWirelessTerminal;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.proxy.IProxy;
import com.tom.storagemod.proxy.ServerProxy;
import com.tom.storagemod.tile.TileEntityCraftingTerminal;
import com.tom.storagemod.tile.TileEntityInventoryCableConnector;
import com.tom.storagemod.tile.TileEntityInventoryCableConnectorFiltered;
import com.tom.storagemod.tile.TileEntityInventoryConnector;
import com.tom.storagemod.tile.TileEntityInventoryHopperBasic;
import com.tom.storagemod.tile.TileEntityInventoryProxy;
import com.tom.storagemod.tile.TileEntityLevelEmitter;
import com.tom.storagemod.tile.TileEntityOpenCrate;
import com.tom.storagemod.tile.TileEntityPainted;
import com.tom.storagemod.tile.TileEntityStorageTerminal;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(StorageMod.modid)
public class StorageMod {
	public static final String modid = "toms_storage";
	public static IProxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);
	public static InventoryConnector connector;
	public static StorageTerminal terminal;
	public static BlockTrim inventoryTrim;
	public static BlockOpenCrate openCrate;
	public static BlockPaintedTrim paintedTrim;
	public static BlockInventoryCable invCable;
	public static BlockInventoryCableFramed invCableFramed;
	public static BlockInventoryCableConnector invCableConnector;
	public static BlockInventoryCableConnectorFiltered invCableConnectorFiltered;
	public static BlockInventoryProxy invProxy;
	public static CraftingTerminal craftingTerminal;
	public static BlockInventoryHopperBasic invHopperBasic;
	public static BlockLevelEmitter levelEmitter;

	public static ItemPaintKit paintingKit;
	public static ItemWirelessTerminal wirelessTerminal;

	public static TileEntityType<TileEntityInventoryConnector> connectorTile;
	public static TileEntityType<TileEntityStorageTerminal> terminalTile;
	public static TileEntityType<TileEntityOpenCrate> openCrateTile;
	public static TileEntityType<TileEntityPainted> paintedTile;
	public static TileEntityType<TileEntityInventoryCableConnector> invCableConnectorTile;
	public static TileEntityType<TileEntityInventoryCableConnectorFiltered> invCableConnectorFilteredTile;
	public static TileEntityType<TileEntityInventoryProxy> invProxyTile;
	public static TileEntityType<TileEntityCraftingTerminal> craftingTerminalTile;
	public static TileEntityType<TileEntityInventoryHopperBasic> invHopperBasicTile;
	public static TileEntityType<TileEntityLevelEmitter> levelEmitterTile;

	public static ContainerType<ContainerStorageTerminal> storageTerminal;
	public static ContainerType<ContainerCraftingTerminal> craftingTerminalCont;
	public static ContainerType<ContainerFiltered> filteredConatiner;
	public static ContainerType<ContainerLevelEmitter> levelEmitterConatiner;

	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();

	public StorageMod() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the enqueueIMC method for modloading
		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
		// Register the processIMC method for modloading
		//FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
		// Register the doClientStuff method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.serverSpec);
		FMLJavaModLoadingContext.get().getModEventBus().register(Config.class);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("Tom's Storage Setup starting");
		proxy.setup();
		NetworkHandler.init();
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		proxy.clientSetup();
	}

	public static final ItemGroup STORAGE_MOD_TAB = new ItemGroup("toms_storage") {

		@Override
		@OnlyIn(Dist.CLIENT)
		public ItemStack createIcon() {
			return new ItemStack(terminal);
		}
	};

	// You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
	// Event bus for receiving Registry Events)
	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
			connector = new InventoryConnector();
			terminal = new StorageTerminal();
			openCrate = new BlockOpenCrate();
			inventoryTrim = new BlockTrim();
			paintedTrim = new BlockPaintedTrim();
			invCable = new BlockInventoryCable();
			invCableFramed = new BlockInventoryCableFramed();
			invCableConnector = new BlockInventoryCableConnector();
			invCableConnectorFiltered = new BlockInventoryCableConnectorFiltered();
			invProxy = new BlockInventoryProxy();
			craftingTerminal = new CraftingTerminal();
			invHopperBasic = new BlockInventoryHopperBasic();
			levelEmitter = new BlockLevelEmitter();
			blockRegistryEvent.getRegistry().register(connector);
			blockRegistryEvent.getRegistry().register(terminal);
			blockRegistryEvent.getRegistry().register(openCrate);
			blockRegistryEvent.getRegistry().register(inventoryTrim);
			blockRegistryEvent.getRegistry().register(paintedTrim);
			blockRegistryEvent.getRegistry().register(invCable);
			blockRegistryEvent.getRegistry().register(invCableFramed);
			blockRegistryEvent.getRegistry().register(invCableConnector);
			blockRegistryEvent.getRegistry().register(invCableConnectorFiltered);
			blockRegistryEvent.getRegistry().register(invProxy);
			blockRegistryEvent.getRegistry().register(craftingTerminal);
			blockRegistryEvent.getRegistry().register(invHopperBasic);
			blockRegistryEvent.getRegistry().register(levelEmitter);
		}

		@SubscribeEvent
		public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
			paintingKit = new ItemPaintKit();
			wirelessTerminal = new ItemWirelessTerminal();

			registerItemForBlock(itemRegistryEvent, connector);
			registerItemForBlock(itemRegistryEvent, terminal);
			registerItemForBlock(itemRegistryEvent, openCrate);
			registerItemForBlock(itemRegistryEvent, inventoryTrim);
			itemRegistryEvent.getRegistry().register(new ItemBlockPainted(paintedTrim));
			registerItemForBlock(itemRegistryEvent, invCable);
			itemRegistryEvent.getRegistry().register(new ItemBlockPainted(invCableFramed, new Item.Properties().group(STORAGE_MOD_TAB)));
			registerItemForBlock(itemRegistryEvent, invCableConnector);
			registerItemForBlock(itemRegistryEvent, invCableConnectorFiltered);
			itemRegistryEvent.getRegistry().register(new ItemBlockPainted(invProxy, new Item.Properties().group(STORAGE_MOD_TAB)));
			registerItemForBlock(itemRegistryEvent, craftingTerminal);
			registerItemForBlock(itemRegistryEvent, invHopperBasic);
			registerItemForBlock(itemRegistryEvent, levelEmitter);

			itemRegistryEvent.getRegistry().register(paintingKit);
			itemRegistryEvent.getRegistry().register(wirelessTerminal);
		}

		private static void registerItemForBlock(RegistryEvent.Register<Item> itemRegistryEvent, Block block) {
			itemRegistryEvent.getRegistry().register(new BlockItem(block, new Item.Properties().group(STORAGE_MOD_TAB)).setRegistryName(block.getRegistryName()));
		}

		@SubscribeEvent
		public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> tileRegistryEvent) {
			connectorTile = TileEntityType.Builder.create(TileEntityInventoryConnector::new, connector).build(null);
			connectorTile.setRegistryName("ts.inventory_connector.tile");
			terminalTile = TileEntityType.Builder.create(TileEntityStorageTerminal::new, terminal).build(null);
			terminalTile.setRegistryName("ts.storage_terminal.tile");
			openCrateTile = TileEntityType.Builder.create(TileEntityOpenCrate::new, openCrate).build(null);
			openCrateTile.setRegistryName("ts.open_crate.tile");
			paintedTile = TileEntityType.Builder.create(TileEntityPainted::new, paintedTrim, invCableFramed).build(null);
			paintedTile.setRegistryName("ts.painted.tile");
			invCableConnectorTile = TileEntityType.Builder.create(TileEntityInventoryCableConnector::new, invCableConnector).build(null);
			invCableConnectorTile.setRegistryName("ts.inventory_cable_connector.tile");
			invCableConnectorFilteredTile = TileEntityType.Builder.create(TileEntityInventoryCableConnectorFiltered::new, invCableConnectorFiltered).build(null);
			invCableConnectorFilteredTile.setRegistryName("ts.inventory_cable_connector_filtered.tile");
			invProxyTile = TileEntityType.Builder.create(TileEntityInventoryProxy::new, invProxy).build(null);
			invProxyTile.setRegistryName("ts.inventory_proxy.tile");
			craftingTerminalTile = TileEntityType.Builder.create(TileEntityCraftingTerminal::new, craftingTerminal).build(null);
			craftingTerminalTile.setRegistryName("ts.crafting_terminal.tile");
			invHopperBasicTile = TileEntityType.Builder.create(TileEntityInventoryHopperBasic::new, invHopperBasic).build(null);
			invHopperBasicTile.setRegistryName("ts.inventoty_hopper_basic.tile");
			levelEmitterTile = TileEntityType.Builder.create(TileEntityLevelEmitter::new, levelEmitter).build(null);
			levelEmitterTile.setRegistryName("ts.level_emitter.tile");
			tileRegistryEvent.getRegistry().register(connectorTile);
			tileRegistryEvent.getRegistry().register(terminalTile);
			tileRegistryEvent.getRegistry().register(openCrateTile);
			tileRegistryEvent.getRegistry().register(paintedTile);
			tileRegistryEvent.getRegistry().register(invCableConnectorTile);
			tileRegistryEvent.getRegistry().register(invCableConnectorFilteredTile);
			tileRegistryEvent.getRegistry().register(invProxyTile);
			tileRegistryEvent.getRegistry().register(craftingTerminalTile);
			tileRegistryEvent.getRegistry().register(invHopperBasicTile);
			tileRegistryEvent.getRegistry().register(levelEmitterTile);
		}

		@SubscribeEvent
		public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> containerRegistryEvent) {
			storageTerminal = new ContainerType<>(ContainerStorageTerminal::new);
			storageTerminal.setRegistryName("ts.storage_terminal.container");
			craftingTerminalCont = new ContainerType<>(ContainerCraftingTerminal::new);
			craftingTerminalCont.setRegistryName("ts.crafting_terminal.container");
			filteredConatiner = new ContainerType<>(ContainerFiltered::new);
			filteredConatiner.setRegistryName("ts.filtered.container");
			levelEmitterConatiner = new ContainerType<>(ContainerLevelEmitter::new);
			levelEmitterConatiner.setRegistryName("ts.level_emitter.container");
			containerRegistryEvent.getRegistry().register(storageTerminal);
			containerRegistryEvent.getRegistry().register(craftingTerminalCont);
			containerRegistryEvent.getRegistry().register(filteredConatiner);
			containerRegistryEvent.getRegistry().register(levelEmitterConatiner);
		}
	}
}
