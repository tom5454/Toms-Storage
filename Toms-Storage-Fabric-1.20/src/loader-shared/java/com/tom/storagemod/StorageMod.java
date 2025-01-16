package com.tom.storagemod;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.tom.storagemod.block.FramedInventoryCableConnectorBlock;
import com.tom.storagemod.block.InventoryProxyBlock;
import com.tom.storagemod.block.PaintedFramedInventoryCableBlock;
import com.tom.storagemod.item.WirelessTerminal;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.platform.Platform;
import com.tom.storagemod.util.GameObject;
import com.tom.storagemod.util.IDataReceiver;
import com.tom.storagemod.util.PlayerInvUtil;

import io.netty.buffer.ByteBufOutputStream;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class StorageMod implements ModInitializer {
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();

	public static final String modid = "toms_storage";

	public static GameObject<PaintedFramedInventoryCableBlock> invCablePainted;
	public static GameObject<FramedInventoryCableConnectorBlock> invCableConnectorPainted;
	public static GameObject<InventoryProxyBlock> invProxyPainted;

	public static final Gson gson = new GsonBuilder().create();
	public static ConfigHolder<Config> configHolder = AutoConfig.register(Config.class, GsonConfigSerializer::new);
	private static Config LOADED_CONFIG = configHolder.getConfig();
	public static Config CONFIG = new Config();

	public static Set<Block> multiblockInvs;
	public static Set<Block> blockedBlocks;

	public StorageMod() {
	}

	public static ResourceLocation id(String id) {
		return new ResourceLocation(modid, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onInitialize() {
		LOGGER.info("Tom's Storage Setup starting");
		Content.init();

		invCablePainted = Platform.BLOCKS.register("ts.inventory_cable_painted", PaintedFramedInventoryCableBlock::new);
		invCableConnectorPainted = Platform.BLOCKS.register("ts.inventory_cable_connector_painted", FramedInventoryCableConnectorBlock::new);
		invProxyPainted = Platform.BLOCKS.register("ts.inventory_proxy_painted", InventoryProxyBlock::new);

		Content.paintedTile.addBlocks(invCablePainted);
		Content.invCableConnectorTile.addBlocks(invCableConnectorPainted);
		Content.invProxyTile.addBlocks(invProxyPainted);
		Platform.BLOCK_ENTITY.register();

		ServerPlayNetworking.registerGlobalReceiver(NetworkHandler.DATA_C2S, (s, p, h, buf, rp) -> {
			CompoundTag tag = Platform.readNbtTag(buf);
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
			multiblockInvs = null;
			blockedBlocks = null;
		});

		StorageTags.init();

		configHolder.registerSaveListener((a, b) -> {
			multiblockInvs = null;
			blockedBlocks = null;
			return InteractionResult.PASS;
		});
	}
}
