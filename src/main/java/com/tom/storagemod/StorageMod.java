package com.tom.storagemod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import com.tom.fabriclibs.ContainerRegistry;
import com.tom.fabriclibs.DistExecutor;
import com.tom.fabriclibs.Events;
import com.tom.fabriclibs.client.RenderTypeLookup;
import com.tom.fabriclibs.event.EventBus.SubscribeEvent;
import com.tom.fabriclibs.events.init.ClientSetupEvent;
import com.tom.fabriclibs.events.init.CommonSetupEvent;
import com.tom.fabriclibs.events.init.Register;
import com.tom.fabriclibs.ext.IItem;
import com.tom.fabriclibs.ext.IRegistered;
import com.tom.fabriclibs.ext.IRegistryEntry;
import com.tom.storagemod.block.BlockInventoryCable;
import com.tom.storagemod.block.BlockInventoryCableConnector;
import com.tom.storagemod.block.BlockInventoryCableFramed;
import com.tom.storagemod.block.BlockInventoryHopperBasic;
import com.tom.storagemod.block.BlockInventoryProxy;
import com.tom.storagemod.block.BlockOpenCrate;
import com.tom.storagemod.block.BlockPaintedTrim;
import com.tom.storagemod.block.BlockTrim;
import com.tom.storagemod.block.CraftingTerminal;
import com.tom.storagemod.block.InventoryConnector;
import com.tom.storagemod.block.StorageTerminal;
import com.tom.storagemod.gui.ContainerCraftingTerminal;
import com.tom.storagemod.gui.ContainerStorageTerminal;
import com.tom.storagemod.item.ItemBlockPainted;
import com.tom.storagemod.item.ItemPaintKit;
import com.tom.storagemod.item.ItemWirelessTerminal;
import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.proxy.IProxy;
import com.tom.storagemod.proxy.ServerProxy;
import com.tom.storagemod.tile.TileEntityCraftingTerminal;
import com.tom.storagemod.tile.TileEntityInventoryCableConnector;
import com.tom.storagemod.tile.TileEntityInventoryConnector;
import com.tom.storagemod.tile.TileEntityInventoryHopperBasic;
import com.tom.storagemod.tile.TileEntityInventoryProxy;
import com.tom.storagemod.tile.TileEntityOpenCrate;
import com.tom.storagemod.tile.TileEntityPainted;
import com.tom.storagemod.tile.TileEntityStorageTerminal;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;

// The value here should match an entry in the META-INF/mods.toml file
//@Mod(StorageMod.modid)
public class StorageMod implements ModInitializer {
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();

	public static final String modid = "toms_storage";
	public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);
	public static InventoryConnector connector;
	public static StorageTerminal terminal;
	public static BlockTrim inventoryTrim;
	public static BlockOpenCrate openCrate;
	public static BlockPaintedTrim paintedTrim;
	public static BlockInventoryCable invCable;
	public static BlockInventoryCableFramed invCableFramed;
	public static BlockInventoryCableConnector invCableConnector;
	public static BlockInventoryProxy invProxy;
	public static CraftingTerminal craftingTerminal;
	public static BlockInventoryHopperBasic invHopperBasic;

	public static ItemPaintKit paintingKit;
	public static ItemWirelessTerminal wirelessTerminal;

	public static BlockEntityType<TileEntityInventoryConnector> connectorTile;
	public static BlockEntityType<TileEntityStorageTerminal> terminalTile;
	public static BlockEntityType<TileEntityOpenCrate> openCrateTile;
	public static BlockEntityType<TileEntityPainted> paintedTile;
	public static BlockEntityType<TileEntityInventoryCableConnector> invCableConnectorTile;
	public static BlockEntityType<TileEntityInventoryProxy> invProxyTile;
	public static BlockEntityType<TileEntityCraftingTerminal> craftingTerminalTile;
	public static BlockEntityType<TileEntityInventoryHopperBasic> invHopperBasicTile;

	public static ScreenHandlerType<ContainerStorageTerminal> storageTerminal;
	public static ScreenHandlerType<ContainerCraftingTerminal> craftingTerminalCont;

	public static Config CONFIG = AutoConfig.register(Config.class, GsonConfigSerializer::new).getConfig();

	public StorageMod() {
		Events.setModid(modid);
		Events.INIT_BUS.addListener(CommonSetupEvent.class, this::setup);
		Events.INIT_BUS.addListener(ClientSetupEvent.class, this::doClientStuff);
		Events.INIT_BUS.register(RegistryEvents.class);
		Events.setModid(null);
	}

	private void setup(final CommonSetupEvent event) {
		LOGGER.info("Tom's Storage Setup starting");
		proxy.setup();
		StorageTags.init();
	}

	private void doClientStuff(final ClientSetupEvent event) {
		proxy.clientSetup();
		RenderTypeLookup.setRenderLayer(invProxy, RenderLayer.getCutout());
	}

	public static final ItemGroup STORAGE_MOD_TAB = FabricItemGroupBuilder.build(new Identifier(modid, "tab"), () -> new ItemStack(terminal));

	// You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
	// Event bus for receiving Registry Events)
	@SuppressWarnings("unchecked")
	public static class RegistryEvents {
		@SubscribeEvent
		public static void onBlocksRegistry(final Register<Block> blockRegistryEvent) {
			connector = new InventoryConnector();
			terminal = new StorageTerminal();
			openCrate = new BlockOpenCrate();
			inventoryTrim = new BlockTrim();
			paintedTrim = new BlockPaintedTrim();
			invCable = new BlockInventoryCable();
			invCableFramed = new BlockInventoryCableFramed();
			invCableConnector = new BlockInventoryCableConnector();
			invProxy = new BlockInventoryProxy();
			craftingTerminal = new CraftingTerminal();
			invHopperBasic = new BlockInventoryHopperBasic();
			blockRegistryEvent.getRegistry().register(connector);
			blockRegistryEvent.getRegistry().register(terminal);
			blockRegistryEvent.getRegistry().register(openCrate);
			blockRegistryEvent.getRegistry().register(inventoryTrim);
			blockRegistryEvent.getRegistry().register(paintedTrim);
			blockRegistryEvent.getRegistry().register(invCable);
			blockRegistryEvent.getRegistry().register(invCableFramed);
			blockRegistryEvent.getRegistry().register(invCableConnector);
			blockRegistryEvent.getRegistry().register(invProxy);
			blockRegistryEvent.getRegistry().register(craftingTerminal);
			blockRegistryEvent.getRegistry().register(invHopperBasic);
		}

		@SubscribeEvent
		public static void onItemsRegistry(final Register<Item> itemRegistryEvent) {
			paintingKit = new ItemPaintKit();
			wirelessTerminal = new ItemWirelessTerminal();

			registerItemForBlock(itemRegistryEvent, connector);
			registerItemForBlock(itemRegistryEvent, terminal);
			registerItemForBlock(itemRegistryEvent, openCrate);
			registerItemForBlock(itemRegistryEvent, inventoryTrim);
			itemRegistryEvent.getRegistry().register(new ItemBlockPainted(paintedTrim));
			registerItemForBlock(itemRegistryEvent, invCable);
			itemRegistryEvent.getRegistry().register(new ItemBlockPainted(invCableFramed, new Item.Settings().group(STORAGE_MOD_TAB)));
			registerItemForBlock(itemRegistryEvent, invCableConnector);
			itemRegistryEvent.getRegistry().register(new ItemBlockPainted(invProxy, new Item.Settings().group(STORAGE_MOD_TAB)));
			registerItemForBlock(itemRegistryEvent, craftingTerminal);
			registerItemForBlock(itemRegistryEvent, invHopperBasic);

			itemRegistryEvent.getRegistry().register(paintingKit);
			itemRegistryEvent.getRegistry().register(wirelessTerminal);
		}

		private static void registerItemForBlock(Register<Item> itemRegistryEvent, Block block) {
			itemRegistryEvent.getRegistry().register(((IItem) new BlockItem(block, new Item.Settings().group(STORAGE_MOD_TAB))).setRegistryName(((IRegistered) block).getRegistryName()));
		}

		@SubscribeEvent
		public static void onTileRegistry(final Register<BlockEntityType<?>> tileRegistryEvent) {
			connectorTile = BlockEntityType.Builder.create(TileEntityInventoryConnector::new, connector).build(null);
			((IRegistryEntry<BlockEntityType<?>>) connectorTile).setRegistryName("ts.inventory_connector.tile");
			terminalTile = BlockEntityType.Builder.create(TileEntityStorageTerminal::new, terminal).build(null);
			((IRegistryEntry<BlockEntityType<?>>) terminalTile).setRegistryName("ts.storage_terminal.tile");
			openCrateTile = BlockEntityType.Builder.create(TileEntityOpenCrate::new, openCrate).build(null);
			((IRegistryEntry<BlockEntityType<?>>) openCrateTile).setRegistryName("ts.open_crate.tile");
			paintedTile = BlockEntityType.Builder.create(TileEntityPainted::new, paintedTrim).build(null);//invCableFramed
			((IRegistryEntry<BlockEntityType<?>>) paintedTile).setRegistryName("ts.painted.tile");
			invCableConnectorTile = BlockEntityType.Builder.create(TileEntityInventoryCableConnector::new, invCableConnector).build(null);
			((IRegistryEntry<BlockEntityType<?>>) invCableConnectorTile).setRegistryName("ts.inventory_cable_connector.tile");
			invProxyTile = BlockEntityType.Builder.create(TileEntityInventoryProxy::new, invProxy).build(null);
			((IRegistryEntry<BlockEntityType<?>>) invProxyTile).setRegistryName("ts.inventory_proxy.tile");
			craftingTerminalTile = BlockEntityType.Builder.create(TileEntityCraftingTerminal::new, craftingTerminal).build(null);
			((IRegistryEntry<BlockEntityType<?>>) craftingTerminalTile).setRegistryName("ts.crafting_terminal.tile");
			invHopperBasicTile = BlockEntityType.Builder.create(TileEntityInventoryHopperBasic::new, invHopperBasic).build(null);
			((IRegistryEntry<BlockEntityType<?>>) invHopperBasicTile).setRegistryName("ts.inventoty_hopper_basic.tile");
			tileRegistryEvent.getRegistry().register(connectorTile);
			tileRegistryEvent.getRegistry().register(terminalTile);
			tileRegistryEvent.getRegistry().register(openCrateTile);
			tileRegistryEvent.getRegistry().register(paintedTile);
			tileRegistryEvent.getRegistry().register(invCableConnectorTile);
			tileRegistryEvent.getRegistry().register(invProxyTile);
			tileRegistryEvent.getRegistry().register(craftingTerminalTile);
			tileRegistryEvent.getRegistry().register(invHopperBasicTile);
		}

		@SubscribeEvent
		public static void onContainerRegistry(final Register<ScreenHandlerType<?>> containerRegistryEvent) {
			storageTerminal = ContainerRegistry.create(ContainerStorageTerminal::new);
			((IRegistryEntry<ScreenHandlerType<?>>) storageTerminal).setRegistryName("ts.storage_terminal.container");
			craftingTerminalCont = ContainerRegistry.create(ContainerCraftingTerminal::new);
			((IRegistryEntry<ScreenHandlerType<?>>) craftingTerminalCont).setRegistryName("ts.crafting_terminal.container");
			containerRegistryEvent.getRegistry().register(storageTerminal);
			containerRegistryEvent.getRegistry().register(craftingTerminalCont);
		}
	}

	@Override
	public void onInitialize() {
	}
}
