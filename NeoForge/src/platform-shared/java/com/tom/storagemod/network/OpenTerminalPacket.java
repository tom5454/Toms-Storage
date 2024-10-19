package com.tom.storagemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.tom.storagemod.StorageMod;

public class OpenTerminalPacket implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<OpenTerminalPacket> ID = new CustomPacketPayload.Type<>(ResourceLocation.tryBuild(StorageMod.modid, "open_terminal"));
	public static final StreamCodec<FriendlyByteBuf, OpenTerminalPacket> STREAM_CODEC = CustomPacketPayload.codec((a, b) -> {}, OpenTerminalPacket::new);

	public OpenTerminalPacket() {}
	private OpenTerminalPacket(FriendlyByteBuf buffer) {}

	@Override
	public CustomPacketPayload.Type<OpenTerminalPacket> type() {
		return ID;
	}
}
