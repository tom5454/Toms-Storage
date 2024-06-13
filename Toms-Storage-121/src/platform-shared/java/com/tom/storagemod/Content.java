package com.tom.storagemod;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.block.BasicInventoryHopperBlock;
import com.tom.storagemod.block.CraftingTerminalBlock;
import com.tom.storagemod.block.FilingCabinetBlock;
import com.tom.storagemod.block.FramedInventoryCableBlock;
import com.tom.storagemod.block.InventoryCableBlock;
import com.tom.storagemod.block.InventoryConnectorBlock;
import com.tom.storagemod.block.InventoryInterfaceBlock;
import com.tom.storagemod.block.LevelEmitterBlock;
import com.tom.storagemod.block.OpenCrateBlock;
import com.tom.storagemod.block.PaintedTrimBlock;
import com.tom.storagemod.block.StorageTerminalBlock;
import com.tom.storagemod.block.TrimBlock;
import com.tom.storagemod.block.entity.BasicInventoryHopperBlockEntity;
import com.tom.storagemod.block.entity.CraftingTerminalBlockEntity;
import com.tom.storagemod.block.entity.FilingCabinetBlockEntity;
import com.tom.storagemod.block.entity.InventoryConnectorBlockEntity;
import com.tom.storagemod.block.entity.InventoryInterfaceBlockEntity;
import com.tom.storagemod.block.entity.LevelEmitterBlockEntity;
import com.tom.storagemod.block.entity.OpenCrateBlockEntity;
import com.tom.storagemod.block.entity.PaintedBlockEntity;
import com.tom.storagemod.block.entity.StorageTerminalBlockEntity;
import com.tom.storagemod.components.ConfiguratorComponent;
import com.tom.storagemod.components.SimpleItemFilterComponent;
import com.tom.storagemod.components.TagFilterComponent;
import com.tom.storagemod.components.WorldPos;
import com.tom.storagemod.item.AdvWirelessTerminalItem;
import com.tom.storagemod.item.FilterItem;
import com.tom.storagemod.item.InventoryConfiguratorItem;
import com.tom.storagemod.item.PaintKitItem;
import com.tom.storagemod.item.PaintedBlockItem;
import com.tom.storagemod.item.PolyFilterItem;
import com.tom.storagemod.item.TagFilterItem;
import com.tom.storagemod.item.WirelessTerminalItem;
import com.tom.storagemod.menu.CraftingTerminalMenu;
import com.tom.storagemod.menu.FilingCabinetMenu;
import com.tom.storagemod.menu.InventoryConfiguratorMenu;
import com.tom.storagemod.menu.ItemFilterMenu;
import com.tom.storagemod.menu.LevelEmitterMenu;
import com.tom.storagemod.menu.StorageTerminalMenu;
import com.tom.storagemod.menu.TagItemFilterMenu;
import com.tom.storagemod.platform.GameObject;
import com.tom.storagemod.platform.GameObject.GameObjectBlockEntity;
import com.tom.storagemod.platform.Platform;

public class Content {
	public static final GameObject<InventoryConnectorBlock> connector = blockWithItem("inventory_connector", InventoryConnectorBlock::new);
	public static final GameObject<StorageTerminalBlock> terminal = blockWithItem("storage_terminal", StorageTerminalBlock::new);
	public static final GameObject<CraftingTerminalBlock> craftingTerminal = blockWithItem("crafting_terminal", CraftingTerminalBlock::new);
	public static final GameObject<OpenCrateBlock> openCrate = blockWithItem("open_crate", OpenCrateBlock::new);
	public static final GameObject<TrimBlock> inventoryTrim = blockWithItem("trim", TrimBlock::new);
	public static final GameObject<PaintedTrimBlock> paintedTrim = blockWithItemNoTab("painted_trim", PaintedTrimBlock::new, PaintedBlockItem.makeHidden());
	public static final GameObject<LevelEmitterBlock> levelEmitter = blockWithItem("level_emitter", LevelEmitterBlock::new);
	public static final GameObject<InventoryCableBlock> invCable = blockWithItem("inventory_cable", InventoryCableBlock::new);
	public static final GameObject<FramedInventoryCableBlock> invCableFramed = blockWithItem("inventory_cable_framed", FramedInventoryCableBlock::new, PaintedBlockItem.make());
	public static final GameObject<BasicInventoryHopperBlock> basicInvHopper = blockWithItem("basic_inventory_hopper", BasicInventoryHopperBlock::new);
	public static final GameObject<InventoryInterfaceBlock> invInterface = blockWithItem("inventory_interface", InventoryInterfaceBlock::new);
	public static final GameObject<FilingCabinetBlock> filingCabinet = blockWithItem("filing_cabinet", FilingCabinetBlock::new);

	public static final GameObject<PaintKitItem> paintingKit = item("paint_kit", PaintKitItem::new);
	public static final GameObject<WirelessTerminalItem> wirelessTerminal = item("wireless_terminal", WirelessTerminalItem::new);
	public static final GameObject<AdvWirelessTerminalItem> advWirelessTerminal = item("adv_wireless_terminal", AdvWirelessTerminalItem::new);
	public static final GameObject<FilterItem> itemFilter = item("item_filter", FilterItem::new);
	public static final GameObject<PolyFilterItem> polyItemFilter = item("polymorphic_item_filter", PolyFilterItem::new);
	public static final GameObject<TagFilterItem> tagItemFilter = item("tag_item_filter", TagFilterItem::new);
	public static final GameObject<InventoryConfiguratorItem> invConfig = item("inventory_configurator", InventoryConfiguratorItem::new);

	public static final GameObjectBlockEntity<InventoryConnectorBlockEntity> connectorBE = blockEntity("inventory_connector", InventoryConnectorBlockEntity::new, connector);
	public static final GameObjectBlockEntity<OpenCrateBlockEntity> openCrateBE = blockEntity("open_crate", OpenCrateBlockEntity::new, openCrate);
	public static final GameObjectBlockEntity<PaintedBlockEntity> paintedBE = blockEntity("painted", PaintedBlockEntity::new, Content.paintedTrim, Content.invCableFramed);
	public static final GameObjectBlockEntity<StorageTerminalBlockEntity> terminalBE = blockEntity("storage_terminal", StorageTerminalBlockEntity::new, terminal);
	public static final GameObjectBlockEntity<CraftingTerminalBlockEntity> craftingTerminalBE = blockEntity("crafting_terminal", CraftingTerminalBlockEntity::new, craftingTerminal);
	public static final GameObjectBlockEntity<LevelEmitterBlockEntity> levelEmitterBE = blockEntity("level_emitter", LevelEmitterBlockEntity::new, levelEmitter);
	public static final GameObjectBlockEntity<BasicInventoryHopperBlockEntity> basicInvHopperBE = blockEntity("basic_inventory_hopper", BasicInventoryHopperBlockEntity::new, basicInvHopper);
	public static final GameObjectBlockEntity<InventoryInterfaceBlockEntity> invInterfaceBE = blockEntity("inventory_interface", InventoryInterfaceBlockEntity::new, invInterface);
	public static final GameObjectBlockEntity<FilingCabinetBlockEntity> filingCabinetBE = blockEntity("filing_cabinet", FilingCabinetBlockEntity::new, filingCabinet);

	public static final GameObject<MenuType<StorageTerminalMenu>> storageTerminalMenu = menu("storage_terminal", StorageTerminalMenu::new);
	public static final GameObject<MenuType<CraftingTerminalMenu>> craftingTerminalMenu = menu("crafting_terminal", CraftingTerminalMenu::new);
	public static final GameObject<MenuType<LevelEmitterMenu>> levelEmitterMenu = menu("level_emitter", LevelEmitterMenu::new);
	//public static GameObject<MenuType<InventoryLinkMenu>> inventoryLink = menu("inventory_link", InventoryLinkMenu::new);
	public static final GameObject<MenuType<ItemFilterMenu>> itemFilterMenu = menu("item_filter", ItemFilterMenu::new);
	public static final GameObject<MenuType<TagItemFilterMenu>> tagItemFilterMenu = menu("tag_item_filter", TagItemFilterMenu::new);
	public static final GameObject<MenuType<InventoryConfiguratorMenu>> invConfigMenu = menu("inventory_configurator", InventoryConfiguratorMenu::new);
	public static final GameObject<MenuType<FilingCabinetMenu>> filingCabinetMenu = menu("filing_cabinet", FilingCabinetMenu::new);

	public static final GameObject<DataComponentType<BlockState>> paintComponent = Platform.DATA_COMPONENT_TYPES.register("paint_state", () -> DataComponentType.<BlockState>builder().persistent(BlockState.CODEC).build());
	public static final GameObject<DataComponentType<WorldPos>> boundPosComponent = Platform.DATA_COMPONENT_TYPES.register("bound_pos", () -> DataComponentType.<WorldPos>builder().persistent(WorldPos.CODEC).build());
	public static final GameObject<DataComponentType<SimpleItemFilterComponent>> simpleItemFilterComponent = Platform.DATA_COMPONENT_TYPES.register("simple_item_filter", () -> DataComponentType.<SimpleItemFilterComponent>builder().persistent(SimpleItemFilterComponent.CODEC).build());
	public static final GameObject<DataComponentType<TagFilterComponent>> tagFilterComponent = Platform.DATA_COMPONENT_TYPES.register("tag_filter", () -> DataComponentType.<TagFilterComponent>builder().persistent(TagFilterComponent.CODEC).build());
	public static final GameObject<DataComponentType<ConfiguratorComponent>> configuratorComponent = Platform.DATA_COMPONENT_TYPES.register("configurator", () -> DataComponentType.<ConfiguratorComponent>builder().persistent(ConfiguratorComponent.CODEC).build());

	private static <B extends Block> GameObject<B> blockWithItem(String name, Supplier<B> create) {
		return blockWithItem(name, create, b -> new BlockItem(b, new Properties()));
	}

	private static <B extends Block, I extends Item> GameObject<B> blockWithItem(String name, Supplier<B> create, Function<Block, I> createItem) {
		GameObject<B> re = Platform.BLOCKS.register(name, create);
		item(name, () -> createItem.apply(re.get()));
		return re;
	}

	private static <B extends Block, I extends Item> GameObject<B> blockWithItemNoTab(String name, Supplier<B> create, Function<Block, I> createItem) {
		GameObject<B> re = Platform.BLOCKS.register(name, create);
		itemNoTab(name, () -> createItem.apply(re.get()));
		return re;
	}

	private static <I extends Item> GameObject<I> item(String name, Supplier<I> fact) {
		return Platform.ITEMS.register(name, () -> Platform.addItemToTab(fact.get()));
	}

	private static <I extends Item> GameObject<I> itemNoTab(String name, Supplier<I> fact) {
		return Platform.ITEMS.register(name, fact);
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	private static <BE extends BlockEntity> GameObjectBlockEntity<BE> blockEntity(String name, BlockEntitySupplier<? extends BE> create, GameObject<? extends Block>... blocks) {
		return (GameObjectBlockEntity<BE>) Platform.BLOCK_ENTITY.registerBE(name, create, blocks);
	}

	private static <M extends AbstractContainerMenu> GameObject<MenuType<M>> menu(String name, MenuSupplier<M> create) {
		return Platform.MENU_TYPE.register(name, () -> new MenuType<>(create, FeatureFlags.VANILLA_SET));
	}

	public static void init() {}
}
