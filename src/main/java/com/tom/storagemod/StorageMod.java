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
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.tom.storagemod.block.BasicInventoryHopperBlock;
import com.tom.storagemod.block.CraftingTerminalBlock;
import com.tom.storagemod.block.FilteredInventoryCableConnectorBlock;
import com.tom.storagemod.block.FramedInventoryCableBlock;
import com.tom.storagemod.block.FramedInventoryCableConnectorBlock;
import com.tom.storagemod.block.InventoryCableBlock;
import com.tom.storagemod.block.InventoryCableConnectorBlock;
import com.tom.storagemod.block.InventoryConnectorBlock;
import com.tom.storagemod.block.InventoryProxyBlock;
import com.tom.storagemod.block.LevelEmitterBlock;
import com.tom.storagemod.block.OpenCrateBlock;
import com.tom.storagemod.block.PaintedTrimBlock;
import com.tom.storagemod.block.StorageTerminalBlock;
import com.tom.storagemod.block.TrimBlock;
import com.tom.storagemod.gui.CraftingTerminalMenu;
import com.tom.storagemod.gui.FilteredMenu;
import com.tom.storagemod.gui.InventoryLinkMenu;
import com.tom.storagemod.gui.LevelEmitterMenu;
import com.tom.storagemod.gui.StorageTerminalMenu;
import com.tom.storagemod.item.AdvWirelessTerminalItem;
import com.tom.storagemod.item.PaintKitItem;
import com.tom.storagemod.item.PaintedBlockItem;
import com.tom.storagemod.item.WirelessTerminalItem;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.tile.BasicInventoryHopperBlockEntity;
import com.tom.storagemod.tile.CraftingTerminalBlockEntity;
import com.tom.storagemod.tile.FilteredInventoryCableConnectorBlockEntity;
import com.tom.storagemod.tile.InventoryCableConnectorBlockEntity;
import com.tom.storagemod.tile.InventoryConnectorBlockEntity;
import com.tom.storagemod.tile.InventoryProxyBlockEntity;
import com.tom.storagemod.tile.LevelEmitterBlockEntity;
import com.tom.storagemod.tile.OpenCrateBlockEntity;
import com.tom.storagemod.tile.PaintedBlockEntity;
import com.tom.storagemod.tile.StorageTerminalBlockEntity;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(StorageMod.modid)
public class StorageMod {
	public static final String modid = "toms_storage";

	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, modid);
	private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, modid);
	private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, modid);
	private static final DeferredRegister<MenuType<?>> MENU_TYPE = DeferredRegister.create(ForgeRegistries.MENU_TYPES, modid);

	public static RegistryObject<InventoryConnectorBlock> connector = blockWithItem("ts.inventory_connector", InventoryConnectorBlock::new);
	public static RegistryObject<StorageTerminalBlock> terminal = blockWithItem("ts.storage_terminal", StorageTerminalBlock::new);
	public static RegistryObject<TrimBlock> inventoryTrim = blockWithItem("ts.trim", TrimBlock::new);
	public static RegistryObject<OpenCrateBlock> openCrate = blockWithItem("ts.open_crate", OpenCrateBlock::new);
	public static RegistryObject<PaintedTrimBlock> paintedTrim = blockWithItem("ts.painted_trim", PaintedTrimBlock::new, PaintedBlockItem.makeHidden());
	public static RegistryObject<InventoryCableBlock> invCable = blockWithItem("ts.inventory_cable", InventoryCableBlock::new);
	public static RegistryObject<FramedInventoryCableBlock> invCableFramed = blockWithItem("ts.inventory_cable_framed", FramedInventoryCableBlock::new, PaintedBlockItem.make());
	public static RegistryObject<InventoryCableConnectorBlock> invCableConnector = blockWithItem("ts.inventory_cable_connector", InventoryCableConnectorBlock::new);
	public static RegistryObject<FilteredInventoryCableConnectorBlock> invCableConnectorFiltered = blockWithItem("ts.inventory_cable_connector_filtered", FilteredInventoryCableConnectorBlock::new);
	public static RegistryObject<FramedInventoryCableConnectorBlock> invCableConnectorFramed = blockWithItem("ts.inventory_cable_connector_framed", FramedInventoryCableConnectorBlock::new, PaintedBlockItem.make());
	public static RegistryObject<InventoryProxyBlock> invProxy = blockWithItem("ts.inventory_proxy", InventoryProxyBlock::new, PaintedBlockItem.make());
	public static RegistryObject<CraftingTerminalBlock> craftingTerminal = blockWithItem("ts.crafting_terminal", CraftingTerminalBlock::new);
	public static RegistryObject<BasicInventoryHopperBlock> invHopperBasic = blockWithItem("ts.inventory_hopper_basic", BasicInventoryHopperBlock::new);
	public static RegistryObject<LevelEmitterBlock> levelEmitter = blockWithItem("ts.level_emitter", LevelEmitterBlock::new);

	public static RegistryObject<PaintKitItem> paintingKit = ITEMS.register("ts.paint_kit", PaintKitItem::new);
	public static RegistryObject<WirelessTerminalItem> wirelessTerminal = ITEMS.register("ts.wireless_terminal", WirelessTerminalItem::new);
	public static RegistryObject<AdvWirelessTerminalItem> advWirelessTerminal = ITEMS.register("ts.adv_wireless_terminal", AdvWirelessTerminalItem::new);

	public static RegistryObject<BlockEntityType<InventoryConnectorBlockEntity>> connectorTile = blockEntity("ts.inventory_connector.tile", InventoryConnectorBlockEntity::new, connector);
	public static RegistryObject<BlockEntityType<StorageTerminalBlockEntity>> terminalTile = blockEntity("ts.storage_terminal.tile", StorageTerminalBlockEntity::new, terminal);
	public static RegistryObject<BlockEntityType<OpenCrateBlockEntity>> openCrateTile = blockEntity("ts.open_crate.tile", OpenCrateBlockEntity::new, openCrate);
	public static RegistryObject<BlockEntityType<PaintedBlockEntity>> paintedTile = blockEntity("ts.painted.tile", PaintedBlockEntity::new, paintedTrim, invCableFramed);
	public static RegistryObject<BlockEntityType<InventoryCableConnectorBlockEntity>> invCableConnectorTile = blockEntity("ts.inventory_cable_connector.tile", InventoryCableConnectorBlockEntity::new, invCableConnector, invCableConnectorFramed);
	public static RegistryObject<BlockEntityType<FilteredInventoryCableConnectorBlockEntity>> invCableConnectorFilteredTile = blockEntity("ts.inventory_cable_connector_filtered.tile", FilteredInventoryCableConnectorBlockEntity::new, invCableConnectorFiltered);
	public static RegistryObject<BlockEntityType<InventoryProxyBlockEntity>> invProxyTile = blockEntity("ts.inventory_proxy.tile", InventoryProxyBlockEntity::new, invProxy);
	public static RegistryObject<BlockEntityType<CraftingTerminalBlockEntity>> craftingTerminalTile = blockEntity("ts.crafting_terminal.tile", CraftingTerminalBlockEntity::new, craftingTerminal);
	public static RegistryObject<BlockEntityType<BasicInventoryHopperBlockEntity>> invHopperBasicTile = blockEntity("ts.inventory_hopper_basic.tile", BasicInventoryHopperBlockEntity::new, invHopperBasic);
	public static RegistryObject<BlockEntityType<LevelEmitterBlockEntity>> levelEmitterTile = blockEntity("ts.level_emitter.tile", LevelEmitterBlockEntity::new, levelEmitter);

	public static RegistryObject<MenuType<StorageTerminalMenu>> storageTerminal = menu("ts.storage_terminal.container", StorageTerminalMenu::new);
	public static RegistryObject<MenuType<CraftingTerminalMenu>> craftingTerminalCont = menu("ts.crafting_terminal.container", CraftingTerminalMenu::new);
	public static RegistryObject<MenuType<FilteredMenu>> filteredConatiner = menu("ts.filtered.container", FilteredMenu::new);
	public static RegistryObject<MenuType<LevelEmitterMenu>> levelEmitterConatiner = menu("ts.level_emitter.container", LevelEmitterMenu::new);
	public static RegistryObject<MenuType<InventoryLinkMenu>> inventoryLink = menu("ts.inventory_link.container", InventoryLinkMenu::new);

	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();

	public StorageMod() {
		// Register the setup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		// Register the doClientStuff method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerColors);

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
		NetworkHandler.init();
	}

	private void doClientStuff(final FMLClientSetupEvent event) {
		StorageModClient.clientSetup();
	}

	private void registerColors(RegisterColorHandlersEvent.Block event) {
		StorageModClient.registerColors(event);
	}

	public static final CreativeModeTab STORAGE_MOD_TAB = new CreativeModeTab("toms_storage.tab") {

		@Override
		@OnlyIn(Dist.CLIENT)
		public ItemStack makeIcon() {
			return new ItemStack(terminal.get());
		}
	};
}
