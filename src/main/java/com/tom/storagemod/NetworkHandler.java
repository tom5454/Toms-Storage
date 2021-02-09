package com.tom.storagemod;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import io.netty.buffer.Unpooled;

public class NetworkHandler {
	public static final Identifier DATA_S2C = new Identifier(StorageMod.modid, "data_packet_s2c");
	public static final Identifier DATA_C2S = new Identifier(StorageMod.modid, "data_packet_c2s");

	public static interface IDataReceiver {
		void receive(CompoundTag tag);
	}

	public static void sendToServer(CompoundTag tag) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeCompoundTag(tag);
		ClientPlayNetworking.send(DATA_C2S, buf);
	}

	public static void sendTo(PlayerEntity player, CompoundTag tag) {
		PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeCompoundTag(tag);
		ServerPlayNetworking.send((ServerPlayerEntity) player, DATA_S2C, buf);
	}
}
