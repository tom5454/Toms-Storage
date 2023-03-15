package com.tom.storagemod;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;

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
import com.tom.storagemod.gui.InventoryConnectorFilterMenu;
import com.tom.storagemod.gui.InventoryLinkMenu;
import com.tom.storagemod.gui.ItemFilterMenu;
import com.tom.storagemod.gui.LevelEmitterMenu;
import com.tom.storagemod.gui.StorageTerminalMenu;
import com.tom.storagemod.gui.TagItemFilterMenu;
import com.tom.storagemod.item.AdvWirelessTerminalItem;
import com.tom.storagemod.item.FilterItem;
import com.tom.storagemod.item.PaintKitItem;
import com.tom.storagemod.item.PaintedBlockItem;
import com.tom.storagemod.item.PolyFilterItem;
import com.tom.storagemod.item.TagFilterItem;
import com.tom.storagemod.item.WirelessTerminalItem;
import com.tom.storagemod.platform.Platform;
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
import com.tom.storagemod.util.GameObject;
import com.tom.storagemod.util.GameObject.GameObjectBlockEntity;

public class Content {
	public static GameObject<InventoryConnectorBlock> connector = blockWithItem("ts.inventory_connector", InventoryConnectorBlock::new);
	public static GameObject<StorageTerminalBlock> terminal = blockWithItem("ts.storage_terminal", StorageTerminalBlock::new);
	public static GameObject<TrimBlock> inventoryTrim = blockWithItem("ts.trim", TrimBlock::new);
	public static GameObject<OpenCrateBlock> openCrate = blockWithItem("ts.open_crate", OpenCrateBlock::new);
	public static GameObject<PaintedTrimBlock> paintedTrim = blockWithItem("ts.painted_trim", PaintedTrimBlock::new, PaintedBlockItem.makeHidden());
	public static GameObject<InventoryCableBlock> invCable = blockWithItem("ts.inventory_cable", InventoryCableBlock::new);
	public static GameObject<FramedInventoryCableBlock> invCableFramed = blockWithItem("ts.inventory_cable_framed", FramedInventoryCableBlock::new, PaintedBlockItem.make());
	public static GameObject<InventoryCableConnectorBlock> invCableConnector = blockWithItem("ts.inventory_cable_connector", InventoryCableConnectorBlock::new);
	public static GameObject<FilteredInventoryCableConnectorBlock> invCableConnectorFiltered = blockWithItem("ts.inventory_cable_connector_filtered", FilteredInventoryCableConnectorBlock::new);
	public static GameObject<FramedInventoryCableConnectorBlock> invCableConnectorFramed = blockWithItem("ts.inventory_cable_connector_framed", FramedInventoryCableConnectorBlock::new, PaintedBlockItem.make());
	public static GameObject<InventoryProxyBlock> invProxy = blockWithItem("ts.inventory_proxy", InventoryProxyBlock::new, PaintedBlockItem.make());
	public static GameObject<CraftingTerminalBlock> craftingTerminal = blockWithItem("ts.crafting_terminal", CraftingTerminalBlock::new);
	public static GameObject<BasicInventoryHopperBlock> invHopperBasic = blockWithItem("ts.inventory_hopper_basic", BasicInventoryHopperBlock::new);
	public static GameObject<LevelEmitterBlock> levelEmitter = blockWithItem("ts.level_emitter", LevelEmitterBlock::new);

	public static GameObject<PaintKitItem> paintingKit = item("ts.paint_kit", PaintKitItem::new);
	public static GameObject<WirelessTerminalItem> wirelessTerminal = item("ts.wireless_terminal", WirelessTerminalItem::new);
	public static GameObject<AdvWirelessTerminalItem> advWirelessTerminal = item("ts.adv_wireless_terminal", AdvWirelessTerminalItem::new);
	public static GameObject<FilterItem> itemFilter = item("ts.item_filter", FilterItem::new);
	public static GameObject<PolyFilterItem> polyItemFliter = item("ts.polymorphic_item_filter", PolyFilterItem::new);
	public static GameObject<TagFilterItem> tagItemFliter = item("ts.tag_item_filter", TagFilterItem::new);

	public static GameObjectBlockEntity<InventoryConnectorBlockEntity> connectorTile = blockEntity("ts.inventory_connector.tile", InventoryConnectorBlockEntity::new, connector);
	public static GameObjectBlockEntity<StorageTerminalBlockEntity> terminalTile = blockEntity("ts.storage_terminal.tile", StorageTerminalBlockEntity::new, terminal);
	public static GameObjectBlockEntity<OpenCrateBlockEntity> openCrateTile = blockEntity("ts.open_crate.tile", OpenCrateBlockEntity::new, openCrate);
	public static GameObjectBlockEntity<PaintedBlockEntity> paintedTile = blockEntity("ts.painted.tile", PaintedBlockEntity::new, Content.paintedTrim, Content.invCableFramed);
	public static GameObjectBlockEntity<InventoryCableConnectorBlockEntity> invCableConnectorTile = blockEntity("ts.inventory_cable_connector.tile", InventoryCableConnectorBlockEntity::new, invCableConnector, invCableConnectorFramed);
	public static GameObjectBlockEntity<FilteredInventoryCableConnectorBlockEntity> invCableConnectorFilteredTile = blockEntity("ts.inventory_cable_connector_filtered.tile", FilteredInventoryCableConnectorBlockEntity::new, invCableConnectorFiltered);
	public static GameObjectBlockEntity<InventoryProxyBlockEntity> invProxyTile = blockEntity("ts.inventory_proxy.tile", InventoryProxyBlockEntity::new, invProxy);
	public static GameObjectBlockEntity<CraftingTerminalBlockEntity> craftingTerminalTile = blockEntity("ts.crafting_terminal.tile", CraftingTerminalBlockEntity::new, craftingTerminal);
	public static GameObjectBlockEntity<BasicInventoryHopperBlockEntity> invHopperBasicTile = blockEntity("ts.inventory_hopper_basic.tile", BasicInventoryHopperBlockEntity::new, invHopperBasic);
	public static GameObjectBlockEntity<LevelEmitterBlockEntity> levelEmitterTile = blockEntity("ts.level_emitter.tile", LevelEmitterBlockEntity::new, levelEmitter);

	public static GameObject<MenuType<StorageTerminalMenu>> storageTerminal = menu("ts.storage_terminal.container", StorageTerminalMenu::new);
	public static GameObject<MenuType<CraftingTerminalMenu>> craftingTerminalCont = menu("ts.crafting_terminal.container", CraftingTerminalMenu::new);
	public static GameObject<MenuType<InventoryConnectorFilterMenu>> invCableConnectorFilteredConatiner = menu("ts.inv_connector_filter.container", InventoryConnectorFilterMenu::new);
	public static GameObject<MenuType<LevelEmitterMenu>> levelEmitterConatiner = menu("ts.level_emitter.container", LevelEmitterMenu::new);
	public static GameObject<MenuType<InventoryLinkMenu>> inventoryLink = menu("ts.inventory_link.container", InventoryLinkMenu::new);
	public static GameObject<MenuType<ItemFilterMenu>> itemFilterConatiner = menu("ts.item_filter.container", ItemFilterMenu::new);
	public static GameObject<MenuType<TagItemFilterMenu>> tagItemFilterConatiner = menu("ts.tag_item_filter.container", TagItemFilterMenu::new);

	private static <B extends Block> GameObject<B> blockWithItem(String name, Supplier<B> create) {
		return blockWithItem(name, create, b -> new BlockItem(b, Platform.itemProp()));
	}

	private static <B extends Block, I extends Item> GameObject<B> blockWithItem(String name, Supplier<B> create, Function<Block, I> createItem) {
		GameObject<B> re = Platform.BLOCKS.register(name, create);
		item(name, () -> createItem.apply(re.get()));
		return re;
	}

	private static <I extends Item> GameObject<I> item(String name, Supplier<I> fact) {
		return Platform.ITEMS.register(name, () -> Platform.registerItem(fact.get()));
	}

	@SuppressWarnings("unchecked")
	@SafeVarargs
	private static <BE extends BlockEntity> GameObjectBlockEntity<BE> blockEntity(String name, BlockEntitySupplier<? extends BE> create, GameObject<? extends Block>... blocks) {
		return (GameObjectBlockEntity<BE>) Platform.BLOCK_ENTITY.registerBE(name, create, blocks);
	}

	private static <M extends AbstractContainerMenu> GameObject<MenuType<M>> menu(String name, MenuSupplier<M> create) {
		return Platform.MENU_TYPE.register(name, () -> Platform.createMenuType(create));
	}

	public static void init() {
	}
}
