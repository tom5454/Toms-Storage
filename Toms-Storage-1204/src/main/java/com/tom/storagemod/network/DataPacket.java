package com.tom.storagemod.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.FriendlyByteBuf;

public class DataPacket {
	public CompoundTag tag;

	public DataPacket(CompoundTag tag) {
		this.tag = tag;
	}

	public DataPacket(FriendlyByteBuf pb) {
		tag = (CompoundTag) pb.readNbt(NbtAccounter.unlimitedHeap());
	}

	public void toBytes(FriendlyByteBuf pb) {
		pb.writeNbt(tag);
	}
}
