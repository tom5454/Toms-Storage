package com.tom.storagemod;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.tom.storagemod.api.MultiblockInventoryAPI;
import com.tom.storagemod.inventory.PlatformItemHandler;
import com.tom.storagemod.inventory.VanillaMultiblockInventories;
import com.tom.storagemod.item.ILeftClickListener;
import com.tom.storagemod.item.WirelessTerminal;
import com.tom.storagemod.network.DataPacket;
import com.tom.storagemod.network.OpenTerminalPacket;
import com.tom.storagemod.platform.Platform;
import com.tom.storagemod.platform.PlatformItem;
import com.tom.storagemod.util.IDataReceiver;
import com.tom.storagemod.util.PlayerInvUtil;
import com.tom.storagemod.util.TickerUtil.OnLoadListener;

import io.netty.buffer.ByteBufOutputStream;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class StorageMod implements ModInitializer {
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();

	public static final String modid = "toms_storage";

	public static final Gson gson = new GsonBuilder().create();
	public static ConfigHolder<Config> configHolder = AutoConfig.register(Config.class, GsonConfigSerializer::new);
	private static Config LOADED_CONFIG = configHolder.getConfig();
	public static Config CONFIG = new Config();

	public static boolean polymorph;
	public static Set<Block> blockedBlocks;

	public StorageMod() {
	}

	public static ResourceLocation id(String id) {
		return ResourceLocation.tryBuild(modid, id);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Tom's Storage Setup starting");
		Content.init();

		Platform.DATA_COMPONENT_TYPES.runRegistration();
		Platform.BLOCKS.runRegistration();
		Platform.ITEMS.runRegistration();
		Platform.BLOCK_ENTITY.runRegistration();
		Platform.MENU_TYPE.runRegistration();

		PayloadTypeRegistry.playS2C().register(DataPacket.ID, DataPacket.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(DataPacket.ID, DataPacket.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(DataPacket.ID, (p, c) -> {
			if(c.player().containerMenu instanceof IDataReceiver d) {
				d.receive(p.tag());
			}
		});

		PayloadTypeRegistry.playC2S().register(OpenTerminalPacket.ID, OpenTerminalPacket.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(OpenTerminalPacket.ID, (p, c) -> {
			ItemStack t = PlayerInvUtil.findItem(c.player(), i -> i.getItem() instanceof WirelessTerminal e && e.canOpen(i), ItemStack.EMPTY, Function.identity());
			if(!t.isEmpty())
				((WirelessTerminal)t.getItem()).open(c.player(), t);
		});

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
			blockedBlocks = null;
		});

		ItemStorage.SIDED.registerForBlockEntity((be, side) -> PlatformItemHandler.of(be), Content.connectorBE.get());
		ItemStorage.SIDED.registerForBlockEntity((be, side) -> PlatformItemHandler.of(be), Content.invInterfaceBE.get());
		ItemStorage.SIDED.registerForBlockEntity((be, side) -> InventoryStorage.of(be.getInv(), side), Content.filingCabinetBE.get());
		ItemStorage.SIDED.registerForBlockEntity((be, side) -> PlatformItemHandler.of(be), Content.invProxyBE.get());

		ResourceLocation at = ResourceLocation.tryBuild(modid, "left_click_item_on_block");
		ResourceLocation rl = ResourceLocation.tryBuild(modid, "use_item_first");
		AttackBlockCallback.EVENT.register(at, (player, world, hand, pos, direction) -> {
			ItemStack is = player.getItemInHand(hand);
			if (is.getItem() instanceof ILeftClickListener l)
				if (l.onLeftClick(is, pos, player))
					return InteractionResult.SUCCESS;
			return InteractionResult.PASS;
		});
		AttackBlockCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, at);

		UseBlockCallback.EVENT.register(rl, (player, world, hand, hitResult) -> {
			BlockPos pos = hitResult.getBlockPos();
			ItemStack is = player.getItemInHand(hand);
			if (is.getItem() instanceof PlatformItem l)
				return l.onRightClick(player, is, pos, hand);
			return InteractionResult.PASS;
		});
		UseBlockCallback.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, rl);

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
			if (entity != null) {
				StorageModComponents.BLOCK_FILTER.get(entity).remove(world, pos);
			}
		});

		ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, w) -> {
			if (be instanceof OnLoadListener l)
				w.getServer().tell(new TickTask(0, () -> {
					l.onLoad();
				}));
		});

		polymorph = FabricLoader.getInstance().isModLoaded("polymorph");

		configHolder.registerSaveListener((a, b) -> {
			blockedBlocks = null;
			return InteractionResult.PASS;
		});

		MultiblockInventoryAPI.EVENT.register(VanillaMultiblockInventories::checkChest);
		//See build.gradle
		/*if (FabricLoader.getInstance().isModLoaded("sophisticatedcore")) {
			SophisticatedDoubleBlocks.register();
		}*/
	}
}
