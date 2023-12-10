package com.tom.storagemod.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import com.tom.storagemod.StorageMod;

import io.netty.buffer.Unpooled;

public class NetworkHandler {
	public static final ResourceLocation DATA_S2C = new ResourceLocation(StorageMod.modid, "data_s2c");
	public static final ResourceLocation DATA_C2S = new ResourceLocation(StorageMod.modid, "data_c2s");
	public static final ResourceLocation OPEN_TERMINAL_C2S = new ResourceLocation(StorageMod.modid, "open_term_c2s");

	public static void sendDataToServer(CompoundTag tag) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeNbt(tag);
		ClientPlayNetworking.send(DATA_C2S, buf);
	}

	public static void sendTo(ServerPlayer player, CompoundTag tag) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeNbt(tag);
		ServerPlayNetworking.send(player, DATA_S2C, buf);
	}

	public static void openTerminal() {
		ClientPlayNetworking.send(OPEN_TERMINAL_C2S, new FriendlyByteBuf(Unpooled.buffer()));
	}
}
