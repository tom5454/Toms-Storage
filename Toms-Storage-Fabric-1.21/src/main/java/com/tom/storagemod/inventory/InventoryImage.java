package com.tom.storagemod.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public class InventoryImage {
	private final long version;
	private List<FabricStack> stacks = new ArrayList<>();
	public static record FabricStack(StorageView<ItemVariant> view, StorageView<ItemVariant> uv, ItemVariant item, long count) {}

	public InventoryImage(long version) {
		this.version = version;
	}

	public long getVersion() {
		return version;
	}

	public void addStack(StorageView<ItemVariant> view) {
		stacks.add(new FabricStack(view, view.getUnderlyingView(), view.getResource(), view.getAmount()));
	}

	public static record InvState(long change, long version, Map<StorageView<ItemVariant>, StoredItemStack> mod) {}

	public List<FabricStack> getStacks() {
		return stacks;
	}
}


