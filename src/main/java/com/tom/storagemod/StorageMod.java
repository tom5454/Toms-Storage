package com.tom.storagemod;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.tom.storagemod.block.BlockInventoryCable;
import com.tom.storagemod.block.BlockInventoryCableConnector;
import com.tom.storagemod.block.BlockInventoryCableConnectorFiltered;
import com.tom.storagemod.block.BlockInventoryCableConnectorFramed;
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
import com.tom.storagemod.gui.ContainerInventoryLink;
import com.tom.storagemod.gui.ContainerLevelEmitter;
import com.tom.storagemod.gui.ContainerStorageTerminal;
import com.tom.storagemod.item.ItemAdvWirelessTerminal;
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

	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, modid);
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, modid);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, modid);
	private static final DeferredRegister<MenuType<?>> MENU_TYPE = DeferredRegister.create(ForgeRegistries.CONTAINERS, modid);

	public static RegistryObject<InventoryConnector> connector = blockWithItem("ts.inventory_connector", InventoryConnector::new);
	public static RegistryObject<StorageTerminal> terminal = blockWithItem("ts.storage_terminal", StorageTerminal::new);
	public static RegistryObject<BlockTrim> inventoryTrim = blockWithItem("ts.trim", BlockTrim::new);
	public static RegistryObject<BlockOpenCrate> openCrate = blockWithItem("ts.open_crate", BlockOpenCrate::new);
	public static RegistryObject<BlockPaintedTrim> paintedTrim = blockWithItem("ts.painted_trim", BlockPaintedTrim::new, ItemBlockPainted.makeHidden());
	public static RegistryObject<BlockInventoryCable> invCable = blockWithItem("ts.inventory_cable", BlockInventoryCable::new);
	public static RegistryObject<BlockInventoryCableFramed> invCableFramed = blockWithItem("ts.inventory_cable_framed", BlockInventoryCableFramed::new, ItemBlockPainted.make());
	public static RegistryObject<BlockInventoryCableConnector> invCableConnector = blockWithItem("ts.inventory_cable_connector", BlockInventoryCableConnector::new);
	public static RegistryObject<BlockInventoryCableConnectorFiltered> invCableConnectorFiltered = blockWithItem("ts.inventory_cable_connector_filtered", BlockInventoryCableConnectorFiltered::new);
	public static RegistryObject<BlockInventoryCableConnectorFramed> invCableConnectorFramed = blockWithItem("ts.inventory_cable_connector_framed", BlockInventoryCableConnectorFramed::new, ItemBlockPainted.make());
	public static RegistryObject<BlockInventoryProxy> invProxy = blockWithItem("ts.inventory_proxy", BlockInventoryProxy::new, ItemBlockPainted.make());
	public static RegistryObject<CraftingTerminal> craftingTerminal = blockWithItem("ts.crafting_terminal", CraftingTerminal::new);
	public static RegistryObject<BlockInventoryHopperBasic> invHopperBasic = blockWithItem("ts.inventory_hopper_basic", BlockInventoryHopperBasic::new);
	public static RegistryObject<BlockLevelEmitter> levelEmitter = blockWithItem("ts.level_emitter", BlockLevelEmitter::new);

	public static RegistryObject<ItemPaintKit> paintingKit = ITEMS.register("ts.paint_kit", ItemPaintKit::new);
	public static RegistryObject<ItemWirelessTerminal> wirelessTerminal = ITEMS.register("ts.wireless_terminal", ItemWirelessTerminal::new);
	public static RegistryObject<ItemAdvWirelessTerminal> advWirelessTerminal = ITEMS.register("ts.adv_wireless_terminal", ItemAdvWirelessTerminal::new);

	public static RegistryObject<BlockEntityType<TileEntityInventoryConnector>> connectorTile = blockEntity("ts.inventory_connector.tile", TileEntityInventoryConnector::new, connector);
	public static RegistryObject<BlockEntityType<TileEntityStorageTerminal>> terminalTile = blockEntity("ts.storage_terminal.tile", TileEntityStorageTerminal::new, terminal);
	public static RegistryObject<BlockEntityType<TileEntityOpenCrate>> openCrateTile = blockEntity("ts.open_crate.tile", TileEntityOpenCrate::new, openCrate);
	public static RegistryObject<BlockEntityType<TileEntityPainted>> paintedTile = blockEntity("ts.painted.tile", TileEntityPainted::new, paintedTrim, invCableFramed);
	public static RegistryObject<BlockEntityType<TileEntityInventoryCableConnector>> invCableConnectorTile = blockEntity("ts.inventory_cable_connector.tile", TileEntityInventoryCableConnector::new, invCableConnector, invCableConnectorFramed);
	public static RegistryObject<BlockEntityType<TileEntityInventoryCableConnectorFiltered>> invCableConnectorFilteredTile = blockEntity("ts.inventory_cable_connector_filtered.tile", TileEntityInventoryCableConnectorFiltered::new, invCableConnectorFiltered);
	public static RegistryObject<BlockEntityType<TileEntityInventoryProxy>> invProxyTile = blockEntity("ts.inventory_proxy.tile", TileEntityInventoryProxy::new, invProxy);
	public static RegistryObject<BlockEntityType<TileEntityCraftingTerminal>> craftingTerminalTile = blockEntity("ts.crafting_terminal.tile", TileEntityCraftingTerminal::new, craftingTerminal);
	public static RegistryObject<BlockEntityType<TileEntityInventoryHopperBasic>> invHopperBasicTile = blockEntity("ts.inventory_hopper_basic.tile", TileEntityInventoryHopperBasic::new, invHopperBasic);
	public static RegistryObject<BlockEntityType<TileEntityLevelEmitter>> levelEmitterTile = blockEntity("ts.level_emitter.tile", TileEntityLevelEmitter::new, levelEmitter);

	public static RegistryObject<MenuType<ContainerStorageTerminal>> storageTerminal = menu("ts.storage_terminal.container", ContainerStorageTerminal::new);
	public static RegistryObject<MenuType<ContainerCraftingTerminal>> craftingTerminalCont = menu("ts.crafting_terminal.container", ContainerCraftingTerminal::new);
	public static RegistryObject<MenuType<ContainerFiltered>> filteredConatiner = menu("ts.filtered.container", ContainerFiltered::new);
	public static RegistryObject<MenuType<ContainerLevelEmitter>> levelEmitterConatiner = menu("ts.level_emitter.container", ContainerLevelEmitter::new);
	public static RegistryObject<MenuType<ContainerInventoryLink>> inventoryLink = menu("ts.inventory_link.container", ContainerInventoryLink::new);

	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();

	public StorageMod() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the doClientStuff method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.commonSpec);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.serverSpec);
		FMLJavaModLoadingContext.get().getModEventBus().register(Config.class);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);

		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
		BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
		BLOCK_ENTITY.register(FMLJavaModLoadingContext.get().getModEventBus());
		MENU_TYPE.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	private static <B extends Block> RegistryObject<B> blockWithItem(String name, Supplier<B> create) {
		RegistryObject<B> re = BLOCKS.register(name, create);
		ITEMS.register(name, () -> new BlockItem(re.get(), new Item.Properties().tab(STORAGE_MOD_TAB)));
		return re;
	}

	private static <B extends Block, I extends Item> RegistryObject<B> blockWithItem(String name, Supplier<B> create, Function<Block, I> createItem) {
		RegistryObject<B> re = BLOCKS.register(name, create);
		ITEMS.register(name, () -> createItem.apply(re.get()));
		return re;
	}

	@SafeVarargs
	private static <BE extends BlockEntity> RegistryObject<BlockEntityType<BE>> blockEntity(String name, BlockEntitySupplier<? extends BE> create, RegistryObject<? extends Block>... blocks) {
		return BLOCK_ENTITY.register(name, () -> {
			return BlockEntityType.Builder.<BE>of(create, Arrays.stream(blocks).map(RegistryObject::get).toArray(Block[]::new)).build(null);
		});
	}

	private static <M extends AbstractContainerMenu> RegistryObject<MenuType<M>> menu(String name, MenuSupplier<M> create) {
		return MENU_TYPE.register(name, () -> new MenuType<>(create));
	}

	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("Tom's Storage Setup starting");
		proxy.setup();
		NetworkHandler.init();
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		proxy.clientSetup();
	}

	public static final CreativeModeTab STORAGE_MOD_TAB = new CreativeModeTab("toms_storage.tab") {

		@Override
		@OnlyIn(Dist.CLIENT)
		public ItemStack makeIcon() {
			return new ItemStack(terminal.get());
		}
	};
}
