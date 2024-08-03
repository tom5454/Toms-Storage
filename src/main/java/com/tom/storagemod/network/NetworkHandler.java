package com.tom.storagemod.network;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.item.WirelessTerminal;
import com.tom.storagemod.util.PlayerInvUtil;

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
		INSTANCE.registerMessage(1, OpenTerminalPacket.class, (a, b) -> {}, b -> new OpenTerminalPacket(), NetworkHandler::handleData);
		StorageMod.LOGGER.info("Initilaized Network Handler");
	}

	public static void handleData(DataPacket packet, Supplier<NetworkEvent.Context> ctx) {
		if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
			ctx.get().enqueueWork(() -> {
				ServerPlayerEntity sender = ctx.get().getSender();
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
			ServerPlayerEntity sender = ctx.get().getSender();

			ItemStack t = PlayerInvUtil.findItem(sender, i -> i.getItem() instanceof WirelessTerminal && ((WirelessTerminal) i.getItem()).canOpen(i), ItemStack.EMPTY, Function.identity());
			if(!t.isEmpty())
				((WirelessTerminal)t.getItem()).open(sender, t);
		});
	}

	public static void sendDataToServer(CompoundNBT tag) {
		INSTANCE.sendToServer(new DataPacket(tag));
	}

	public static void sendTo(ServerPlayerEntity pl, CompoundNBT tag) {
		INSTANCE.send(PacketDistributor.PLAYER.with(() -> pl), new DataPacket(tag));
	}

	public static void openTerminal() {
		INSTANCE.sendToServer(new OpenTerminalPacket());
	}
}
