package com.tom.storagemod.network;

import net.minecraft.nbt.CompoundNBT;

public interface IDataReceiver {
	void receive(CompoundNBT tag);
}
