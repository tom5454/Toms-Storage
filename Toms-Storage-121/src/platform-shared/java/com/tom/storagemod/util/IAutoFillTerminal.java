package com.tom.storagemod.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.nbt.CompoundTag;

import com.tom.storagemod.inventory.StoredItemStack;

public interface IAutoFillTerminal {
	public static List<ISearchHandler> updateSearch = new ArrayList<>();

	void sendMessage(CompoundTag compound);
	List<StoredItemStack> getStoredItems();

	static boolean hasSync() {
		return !updateSearch.isEmpty();
	}

	static void sync(String searchString) {
		updateSearch.forEach(c -> c.setSearch(searchString));
	}

	static String getHandlerName() {
		return updateSearch.stream().map(ISearchHandler::getName).collect(Collectors.joining(", "));
	}

	static String getHandlerNameOr(String text) {
		String t = getHandlerName();
		return t.isEmpty() ? text : t;
	}

	public interface ISearchHandler {
		void setSearch(String set);
		String getName();
		String getSearch();
	}
}
