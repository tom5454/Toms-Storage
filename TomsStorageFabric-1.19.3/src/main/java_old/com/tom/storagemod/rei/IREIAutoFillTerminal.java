package com.tom.storagemod.rei;

import java.util.List;

import net.minecraft.nbt.NbtCompound;

import com.tom.storagemod.StoredItemStack;

public interface IREIAutoFillTerminal {
	void sendMessage(NbtCompound compound);
	List<StoredItemStack> getStoredItems();
}
