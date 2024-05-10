package com.tom.storagemod.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class NetworkHandler {

	public static void sendDataToServer(CompoundTag tag) {
		ClientPlayNetworking.send(new DataPacket(tag));
	}

	public static void sendTo(ServerPlayer player, CompoundTag tag) {
		ServerPlayNetworking.send(player, new DataPacket(tag));
	}

	public static void openTerminal() {
		ClientPlayNetworking.send(new OpenTerminalPacket());
	}
}
