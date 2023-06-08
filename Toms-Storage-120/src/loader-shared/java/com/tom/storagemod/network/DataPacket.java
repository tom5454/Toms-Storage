package com.tom.storagemod.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class DataPacket {
	public CompoundTag tag;

	public DataPacket(CompoundTag tag) {
		this.tag = tag;
	}

	public DataPacket(FriendlyByteBuf pb) {
		tag = pb.readAnySizeNbt();
	}

	public void toBytes(FriendlyByteBuf pb) {
		pb.writeNbt(tag);
	}
}
