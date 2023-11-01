package com.tom.storagemod.network;

import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.item.WirelessTerminal;
import com.tom.storagemod.util.IDataReceiver;
import com.tom.storagemod.util.PlayerInvUtil;

public class NetworkHandler {
	public static final SimpleChannel INSTANCE = ChannelBuilder
			.named(new ResourceLocation(StorageMod.modid, "main"))
			.networkProtocolVersion(2)
			.simpleChannel().

			messageBuilder(DataPacket.class)
			.decoder(DataPacket::new)
			.encoder(DataPacket::toBytes)
			.consumerMainThread(NetworkHandler::handleData)
			.add().

			messageBuilder(OpenTerminalPacket.class)
			.decoder(b -> new OpenTerminalPacket())
			.encoder((a, b) -> {})
			.consumerMainThread(NetworkHandler::handleData)
			.add();

	public static void init() {
		StorageMod.LOGGER.info("Initilaized Network Handler");
	}

	public static void handleData(DataPacket packet, CustomPayloadEvent.Context ctx) {
		if(ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
			ServerPlayer sender = ctx.getSender();
			if(sender.containerMenu instanceof IDataReceiver) {
				((IDataReceiver)sender.containerMenu).receive(packet.tag);
			}
		} else if(ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
			if(Minecraft.getInstance().screen instanceof IDataReceiver) {
				((IDataReceiver)Minecraft.getInstance().screen).receive(packet.tag);
			}
		}
		ctx.setPacketHandled(true);
	}

	public static void handleData(OpenTerminalPacket packet, CustomPayloadEvent.Context ctx) {
		ServerPlayer sender = ctx.getSender();
		ItemStack t = PlayerInvUtil.findItem(sender, i -> i.getItem() instanceof WirelessTerminal e && e.canOpen(i), ItemStack.EMPTY, Function.identity());
		if(!t.isEmpty())
			((WirelessTerminal)t.getItem()).open(sender, t);
	}

	public static void sendDataToServer(CompoundTag tag) {
		INSTANCE.send(new DataPacket(tag), PacketDistributor.SERVER.noArg());
	}

	public static void sendTo(ServerPlayer pl, CompoundTag tag) {
		INSTANCE.send(new DataPacket(tag), PacketDistributor.PLAYER.with(pl));
	}

	public static void openTerminal() {
		INSTANCE.send(new OpenTerminalPacket(), PacketDistributor.SERVER.noArg());
	}
}
