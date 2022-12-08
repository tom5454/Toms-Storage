package com.tom.storagemod.rei;

import java.util.List;

import net.minecraft.nbt.CompoundTag;

import com.tom.storagemod.StoredItemStack;

public interface IREIAutoFillTerminal {
	void sendMessage(CompoundTag compound);
	List<StoredItemStack> getStoredItems();
}
