package com.tom.storagemod.jei;

import java.util.List;

import net.minecraft.nbt.CompoundTag;

import com.tom.storagemod.StoredItemStack;

public interface IJEIAutoFillTerminal {
	void sendMessage(CompoundTag compound);
	List<StoredItemStack> getStoredItems();
}
