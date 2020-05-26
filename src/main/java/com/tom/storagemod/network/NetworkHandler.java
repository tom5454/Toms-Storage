package com.tom.storagemod.network;

import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import com.tom.storagemod.StorageMod;

public class NetworkHandler {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(StorageMod.modid, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
			);
	public static void init() {
		INSTANCE.registerMessage(0, DataPacket.class, DataPacket::toBytes, DataPacket::new, NetworkHandler::handleData);
		StorageMod.LOGGER.info("Initilaized Network Handler");
	}

	public static void handleData(DataPacket packet, Supplier<NetworkEvent.Context> ctx) {
		if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
			ctx.get().enqueueWork(() -> {
				ServerPlayerEntity sender = ctx.get().getSender();
				if(sender.openContainer instanceof IDataReceiver) {
					((IDataReceiver)sender.openContainer).receive(packet.tag);
				}
			});
		} else if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
			ctx.get().enqueueWork(() -> {
				if(Minecraft.getInstance().currentScreen instanceof IDataReceiver) {
					((IDataReceiver)Minecraft.getInstance().currentScreen).receive(packet.tag);
				}
			});
		}
		ctx.get().setPacketHandled(true);
	}

	public static void sendDataToServer(CompoundNBT tag) {
		INSTANCE.sendToServer(new DataPacket(tag));
	}

	public static void sendTo(ServerPlayerEntity pl, CompoundNBT tag) {
		INSTANCE.send(PacketDistributor.PLAYER.with(() -> pl), new DataPacket(tag));
	}
}
