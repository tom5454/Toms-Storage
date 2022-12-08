package com.tom.storagemod;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.tom.storagemod.NetworkHandler.IDataReceiver;
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
import com.tom.storagemod.block.PaintedFramedInventoryCableBlock;
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
import com.tom.storagemod.item.WirelessTerminal;
import com.tom.storagemod.item.WirelessTerminalItem;
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
import com.tom.storagemod.util.PlayerInvUtil;

import io.netty.buffer.ByteBufOutputStream;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class StorageMod implements ModInitializer {
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();

	public static final String modid = "toms_storage";
	public static InventoryConnectorBlock connector;
	public static StorageTerminalBlock terminal;
	public static TrimBlock inventoryTrim;
	public static OpenCrateBlock openCrate;
	public static PaintedTrimBlock paintedTrim;
	public static InventoryCableBlock invCable;
	public static FramedInventoryCableBlock invCableFramed;
	public static PaintedFramedInventoryCableBlock invCablePainted;
	public static InventoryCableConnectorBlock invCableConnector;
	public static FilteredInventoryCableConnectorBlock invCableConnectorFiltered;
	public static FramedInventoryCableConnectorBlock invCableConnectorFramed, invCableConnectorPainted;
	public static InventoryProxyBlock invProxy, invProxyPainted;
	public static CraftingTerminalBlock craftingTerminal;
	public static BasicInventoryHopperBlock invHopperBasic;
	public static LevelEmitterBlock levelEmitter;

	public static PaintKitItem paintingKit;
	public static WirelessTerminalItem wirelessTerminal;
	public static AdvWirelessTerminalItem advWirelessTerminal;

	public static BlockEntityType<InventoryConnectorBlockEntity> connectorTile;
	public static BlockEntityType<StorageTerminalBlockEntity> terminalTile;
	public static BlockEntityType<OpenCrateBlockEntity> openCrateTile;
	public static BlockEntityType<PaintedBlockEntity> paintedTile;
	public static BlockEntityType<InventoryCableConnectorBlockEntity> invCableConnectorTile;
	public static BlockEntityType<FilteredInventoryCableConnectorBlockEntity> invCableConnectorFilteredTile;
	public static BlockEntityType<InventoryProxyBlockEntity> invProxyTile;
	public static BlockEntityType<CraftingTerminalBlockEntity> craftingTerminalTile;
	public static BlockEntityType<BasicInventoryHopperBlockEntity> invHopperBasicTile;
	public static BlockEntityType<LevelEmitterBlockEntity> levelEmitterTile;

	public static MenuType<StorageTerminalMenu> storageTerminal;
	public static MenuType<CraftingTerminalMenu> craftingTerminalCont;
	public static MenuType<FilteredMenu> filteredConatiner;
	public static MenuType<LevelEmitterMenu> levelEmitterConatiner;
	public static MenuType<InventoryLinkMenu> inventoryLink;

	public static final Gson gson = new GsonBuilder().create();
	public static ConfigHolder<Config> configHolder = AutoConfig.register(Config.class, GsonConfigSerializer::new);
	private static Config LOADED_CONFIG = configHolder.getConfig();
	public static Config CONFIG = new Config();

	public static Set<Block> multiblockInvs;

	public StorageMod() {
	}

	public static final CreativeModeTab STORAGE_MOD_TAB = FabricItemGroupBuilder.build(id("tab"), () -> new ItemStack(terminal));


	public static ResourceLocation id(String id) {
		return new ResourceLocation(modid, id);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Tom's Storage Setup starting");
		connector = new InventoryConnectorBlock();
		terminal = new StorageTerminalBlock();
		openCrate = new OpenCrateBlock();
		inventoryTrim = new TrimBlock();
		paintedTrim = new PaintedTrimBlock();
		invCable = new InventoryCableBlock();
		invCableFramed = new FramedInventoryCableBlock();
		invCablePainted = new PaintedFramedInventoryCableBlock();
		invCableConnector = new InventoryCableConnectorBlock();
		invCableConnectorFiltered = new FilteredInventoryCableConnectorBlock();
		invCableConnectorFramed = new FramedInventoryCableConnectorBlock();
		invCableConnectorPainted = new FramedInventoryCableConnectorBlock();
		invProxy = new InventoryProxyBlock();
		craftingTerminal = new CraftingTerminalBlock();
		invHopperBasic = new BasicInventoryHopperBlock();
		levelEmitter = new LevelEmitterBlock();
		invProxyPainted = new InventoryProxyBlock();

		paintingKit = new PaintKitItem();
		wirelessTerminal = new WirelessTerminalItem();
		advWirelessTerminal = new AdvWirelessTerminalItem();

		connectorTile = FabricBlockEntityTypeBuilder.create(InventoryConnectorBlockEntity::new, connector).build(null);
		terminalTile = FabricBlockEntityTypeBuilder.create(StorageTerminalBlockEntity::new, terminal).build(null);
		openCrateTile = FabricBlockEntityTypeBuilder.create(OpenCrateBlockEntity::new, openCrate).build(null);
		paintedTile = FabricBlockEntityTypeBuilder.create(PaintedBlockEntity::new, paintedTrim, invCableFramed, invCablePainted).build(null);
		invCableConnectorTile = FabricBlockEntityTypeBuilder.create(InventoryCableConnectorBlockEntity::new, invCableConnector, invCableConnectorFramed, invCableConnectorPainted).build(null);
		invCableConnectorFilteredTile = FabricBlockEntityTypeBuilder.create(FilteredInventoryCableConnectorBlockEntity::new, invCableConnectorFiltered).build(null);
		invProxyTile = FabricBlockEntityTypeBuilder.create(InventoryProxyBlockEntity::new, invProxy, invProxyPainted).build(null);
		craftingTerminalTile = FabricBlockEntityTypeBuilder.create(CraftingTerminalBlockEntity::new, craftingTerminal).build(null);
		invHopperBasicTile = FabricBlockEntityTypeBuilder.create(BasicInventoryHopperBlockEntity::new, invHopperBasic).build(null);
		levelEmitterTile = FabricBlockEntityTypeBuilder.create(LevelEmitterBlockEntity::new, levelEmitter).build(null);

		storageTerminal = registerSimple(id("ts.storage_terminal.container"), StorageTerminalMenu::new);
		craftingTerminalCont = registerSimple(id("ts.crafting_terminal.container"), CraftingTerminalMenu::new);
		filteredConatiner = registerSimple(id("ts.filtered.container"), FilteredMenu::new);
		levelEmitterConatiner = registerSimple(id("ts.level_emitter.container"), LevelEmitterMenu::new);
		inventoryLink = registerSimple(id("ts.inventory_link.container"), InventoryLinkMenu::new);

		Registry.register(Registry.BLOCK, id("ts.inventory_connector"), connector);
		Registry.register(Registry.BLOCK, id("ts.storage_terminal"), terminal);
		Registry.register(Registry.BLOCK, id("ts.open_crate"), openCrate);
		Registry.register(Registry.BLOCK, id("ts.trim"), inventoryTrim);
		Registry.register(Registry.BLOCK, id("ts.painted_trim"), paintedTrim);
		Registry.register(Registry.BLOCK, id("ts.inventory_cable"), invCable);
		Registry.register(Registry.BLOCK, id("ts.inventory_cable_framed"), invCableFramed);
		Registry.register(Registry.BLOCK, id("ts.inventory_cable_painted"), invCablePainted);
		Registry.register(Registry.BLOCK, id("ts.inventory_cable_connector"), invCableConnector);
		Registry.register(Registry.BLOCK, id("ts.inventory_cable_connector_filtered"), invCableConnectorFiltered);
		Registry.register(Registry.BLOCK, id("ts.inventory_proxy"), invProxy);
		Registry.register(Registry.BLOCK, id("ts.crafting_terminal"), craftingTerminal);
		Registry.register(Registry.BLOCK, id("ts.inventory_hopper_basic"), invHopperBasic);
		Registry.register(Registry.BLOCK, id("ts.level_emitter"), levelEmitter);
		Registry.register(Registry.BLOCK, id("ts.inventory_cable_connector_framed"), invCableConnectorFramed);
		Registry.register(Registry.BLOCK, id("ts.inventory_cable_connector_painted"), invCableConnectorPainted);
		Registry.register(Registry.BLOCK, id("ts.inventory_proxy_painted"), invProxyPainted);

		Registry.register(Registry.ITEM, id("ts.paint_kit"), paintingKit);
		Registry.register(Registry.ITEM, id("ts.wireless_terminal"), wirelessTerminal);
		Registry.register(Registry.ITEM, id("ts.adv_wireless_terminal"), advWirelessTerminal);

		Registry.register(Registry.BLOCK_ENTITY_TYPE, id("ts.inventory_connector.tile"), connectorTile);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, id("ts.storage_terminal.tile"), terminalTile);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, id("ts.open_crate.tile"), openCrateTile);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, id("ts.painted.tile"), paintedTile);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, id("ts.inventory_cable_connector.tile"), invCableConnectorTile);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, id("ts.inventory_cable_connector_filtered.tile"), invCableConnectorFilteredTile);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, id("ts.inventory_proxy.tile"), invProxyTile);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, id("ts.crafting_terminal.tile"), craftingTerminalTile);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, id("ts.inventoty_hopper_basic.tile"), invHopperBasicTile);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, id("ts.level_emitter.tile"), levelEmitterTile);

		registerItemForBlock(connector);
		registerItemForBlock(terminal);
		registerItemForBlock(openCrate);
		registerItemForBlock(inventoryTrim);
		Registry.register(Registry.ITEM, Registry.BLOCK.getKey(paintedTrim), new PaintedBlockItem(paintedTrim));
		registerItemForBlock(invCable);
		Registry.register(Registry.ITEM, Registry.BLOCK.getKey(invCableFramed), new PaintedBlockItem(invCableFramed, new Item.Properties().tab(STORAGE_MOD_TAB)));
		registerItemForBlock(invCableConnector);
		registerItemForBlock(invCableConnectorFiltered);
		registerItemForBlock(invProxy);
		registerItemForBlock(craftingTerminal);
		registerItemForBlock(invHopperBasic);
		registerItemForBlock(levelEmitter);
		registerItemForBlock(invCableConnectorFramed);

		ServerPlayNetworking.registerGlobalReceiver(NetworkHandler.DATA_C2S, (s, p, h, buf, rp) -> {
			CompoundTag tag = buf.readAnySizeNbt();
			s.submit(() -> {
				if(p.containerMenu instanceof IDataReceiver) {
					((IDataReceiver)p.containerMenu).receive(tag);
				}
			});
		});
		ServerPlayNetworking.registerGlobalReceiver(NetworkHandler.OPEN_TERMINAL_C2S, (s, p, h, buf, rp) -> s.submit(() -> {
			ItemStack t = PlayerInvUtil.findItem(p, i -> i.getItem() instanceof WirelessTerminal e && e.canOpen(i), ItemStack.EMPTY, Function.identity());
			if(!t.isEmpty())
				((WirelessTerminal)t.getItem()).open(p, t);
		}));

		ServerLoginNetworking.registerGlobalReceiver(id("config"), (server, handler, understood, buf, sync, respSender) -> {
		});

		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, sync) -> {
			FriendlyByteBuf packet = PacketByteBufs.create();
			try (OutputStreamWriter writer = new OutputStreamWriter(new ByteBufOutputStream(packet))){
				gson.toJson(LOADED_CONFIG, writer);
			} catch (IOException e) {
				LOGGER.warn("Error sending config sync", e);
			}
			sender.sendPacket(id("config"), packet);
		});

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			CONFIG = LOADED_CONFIG;
		});

		StorageTags.init();

		configHolder.registerSaveListener((a, b) -> {
			multiblockInvs = null;
			return InteractionResult.PASS;
		});
	}

	private static <T extends AbstractContainerMenu> MenuType<T> registerSimple(ResourceLocation id, MenuType.MenuSupplier<T> factory) {
		MenuType<T> type = new MenuType<>(factory);
		return Registry.register(Registry.MENU, id, type);
	}

	private static void registerItemForBlock(Block block) {
		Registry.register(Registry.ITEM, Registry.BLOCK.getKey(block), new BlockItem(block, new Item.Properties().tab(STORAGE_MOD_TAB)));
	}
}
