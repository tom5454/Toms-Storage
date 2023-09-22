package com.tom.storagemod.network;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.item.WirelessTerminal;
import com.tom.storagemod.util.IDataReceiver;
import com.tom.storagemod.util.PlayerInvUtil;

public class NetworkHandler {
	private static final String PROTOCOL_VERSION = "2";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(StorageMod.modid, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
			);
	public static void init() {
		INSTANCE.registerMessage(0, DataPacket.class, DataPacket::toBytes, DataPacket::new, NetworkHandler::handleData);
		INSTANCE.registerMessage(1, OpenTerminalPacket.class, (a, b) -> {}, b -> new OpenTerminalPacket(), NetworkHandler::handleData);
		StorageMod.LOGGER.info("Initilaized Network Handler");
	}

	public static void handleData(DataPacket packet, Supplier<NetworkEvent.Context> ctx) {
		if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
			ctx.get().enqueueWork(() -> {
				ServerPlayer sender = ctx.get().getSender();
				if(sender.containerMenu instanceof IDataReceiver) {
					((IDataReceiver)sender.containerMenu).receive(packet.tag);
				}
			});
		} else if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
			ctx.get().enqueueWork(() -> {
				if(Minecraft.getInstance().screen instanceof IDataReceiver) {
					((IDataReceiver)Minecraft.getInstance().screen).receive(packet.tag);
				}
			});
		}
		ctx.get().setPacketHandled(true);
	}

	public static void handleData(OpenTerminalPacket packet, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();
			ItemStack t = PlayerInvUtil.findItem(sender, i -> i.getItem() instanceof WirelessTerminal e && e.canOpen(i), ItemStack.EMPTY, Function.identity());
			if(!t.isEmpty())
				((WirelessTerminal)t.getItem()).open(sender, t);
		});
	}

	public static void sendDataToServer(CompoundTag tag) {
		INSTANCE.sendToServer(new DataPacket(tag));
	}

	public static void sendTo(ServerPlayer pl, CompoundTag tag) {
		INSTANCE.send(PacketDistributor.PLAYER.with(() -> pl), new DataPacket(tag));
	}

	public static void openTerminal() {
		INSTANCE.sendToServer(new OpenTerminalPacket());
	}
}
