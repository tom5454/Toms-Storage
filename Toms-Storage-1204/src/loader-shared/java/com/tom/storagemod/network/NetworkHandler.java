package com.tom.storagemod.network;

import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.item.WirelessTerminal;
import com.tom.storagemod.util.IDataReceiver;
import com.tom.storagemod.util.PlayerInvUtil;

public class NetworkHandler {
	@SubscribeEvent
	public static void register(final RegisterPayloadHandlerEvent event) {
		final IPayloadRegistrar registrar = event.registrar(StorageMod.modid);

		registrar.play(DataPacket.ID, DataPacket::new, handler -> handler
				.client(NetworkHandler::handleDataClient)
				.server(NetworkHandler::handleDataServer));

		registrar.play(OpenTerminalPacket.ID, OpenTerminalPacket::new, handler -> handler
				.server(NetworkHandler::handleTermServer));
	}

	public static void handleDataServer(DataPacket packet, PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
			ServerPlayer sender = (ServerPlayer) context.player().orElseThrow();
			if(sender.containerMenu instanceof IDataReceiver) {
				((IDataReceiver)sender.containerMenu).receive(packet.tag());
			}
		});
	}

	public static void handleDataClient(DataPacket packet, PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
			if(Minecraft.getInstance().screen instanceof IDataReceiver) {
				((IDataReceiver)Minecraft.getInstance().screen).receive(packet.tag());
			}
		});
	}

	public static void handleTermServer(OpenTerminalPacket packet, PlayPayloadContext context) {
		context.workHandler().submitAsync(() -> {
			ServerPlayer sender = (ServerPlayer) context.player().orElseThrow();
			ItemStack t = PlayerInvUtil.findItem(sender, i -> i.getItem() instanceof WirelessTerminal e && e.canOpen(i), ItemStack.EMPTY, Function.identity());
			if(!t.isEmpty())
				((WirelessTerminal)t.getItem()).open(sender, t);
		});
	}

	public static void sendDataToServer(CompoundTag tag) {
		PacketDistributor.SERVER.noArg().send(new DataPacket(tag));
	}

	public static void sendTo(ServerPlayer pl, CompoundTag tag) {
		PacketDistributor.PLAYER.with(pl).send(new DataPacket(tag));
	}

	public static void openTerminal() {
		PacketDistributor.SERVER.noArg().send(new OpenTerminalPacket());
	}
}
