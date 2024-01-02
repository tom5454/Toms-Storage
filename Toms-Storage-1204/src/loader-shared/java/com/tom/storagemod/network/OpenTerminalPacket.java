package com.tom.storagemod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.tom.storagemod.StorageMod;

public class OpenTerminalPacket implements CustomPacketPayload {
	public static final ResourceLocation ID = new ResourceLocation(StorageMod.modid, "open_terminal");

	public OpenTerminalPacket() {}
	public OpenTerminalPacket(FriendlyByteBuf buffer) {}

	@Override
	public void write(FriendlyByteBuf p_294947_) {
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
