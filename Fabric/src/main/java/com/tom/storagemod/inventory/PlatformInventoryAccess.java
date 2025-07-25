package com.tom.storagemod.inventory;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.StorageModComponents;
import com.tom.storagemod.util.IValidInfo;
import com.tom.storagemod.util.WorldStates;

public interface PlatformInventoryAccess extends IInventoryAccess {
	public static final PlatformInventoryAccess EMPTY = new PlatformInventoryAccess() {

		@Override
		public IInventoryChangeTracker tracker() {
			return InventoryChangeTracker.NULL;
		}

		@Override
		public Storage<ItemVariant> get() {
			return Storage.empty();
		}

		@Override
		public ItemStack pullMatchingStack(ItemStack st, long max) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack pushStack(ItemStack stack) {
			return stack;
		}

		@Override
		public int getFreeSlotCount() {
			return 0;
		}

		@Override
		public IInventoryAccess getRootHandler() {
			return this;
		}
	};

	public static class BlockInventoryAccess implements PlatformInventoryAccess {
		private BlockApiCache<Storage<ItemVariant>, Direction> cache;
		private Direction d;
		private IInventoryChangeTracker.Delegate tracker = new IInventoryChangeTracker.Delegate();
		private long lastCheck;
		private Storage<ItemVariant> storageCache;

		@Override
		public Storage<ItemVariant> get() {
			if (cache == null)return null;
			if (cache.getWorld().getGameTime() == lastCheck) {
				return storageCache;
			}
			Storage<ItemVariant> sv = cache != null ? cache.find(d) : null;
			lastCheck = cache.getWorld().getGameTime();
			storageCache = sv;
			return sv;
		}

		@Override
		public IInventoryChangeTracker tracker() {
			var itemHandler = get();
			if (itemHandler != null)
				tracker.setDelegate(WorldStates.getTracker(itemHandler));
			else
				tracker.setDelegate(InventoryChangeTracker.NULL);
			return tracker;
		}

		public void onLoad(Level level, BlockPos p, Direction d, IValidInfo inventoryConnectorBlockEntity) {
			cache = BlockApiCache.create(ItemStorage.SIDED, (ServerLevel) level, p);
			this.d = d;
		}

		public static boolean hasInventoryAt(Level level, BlockPos pos, BlockState state, Direction direction) {
			return ItemStorage.SIDED.find(level, pos, state, null, direction) != null;
		}

		public boolean exists() {
			return get() != null;
		}

		@Override
		public void markInvalid() {
			cache = null;
			storageCache = null;
		}

		@Override
		public IInventoryAccess getRootHandler() {
			return getRootHandler(new java.util.HashSet<>());
		}

		private IInventoryAccess getRootHandler(java.util.Set<IInventoryAccess> visited) {
			if (!visited.add(this)) return this; // cycle detected
			var g = get();
			if (g instanceof IProxy p) {
				if (p instanceof PlatformProxyInventoryAccess proxy) {
					return proxy.getRootHandler(visited);
				}
				return p.getRootHandler(); // fallback, but may recurse
			}
			return this;
		}

		protected void onInvalid() {
		}
	}

	@Override
	Storage<ItemVariant> get();

	@Override
	default int getFreeSlotCount() {
		Storage<ItemVariant> h = get();
		if (h == null)return 0;
		int em = 0;
		for (StorageView<ItemVariant> slot : h) {
			if (slot.isResourceBlank())em++;
		}
		return em;
	}

	@Override
	default int getSlotCount() {
		Storage<ItemVariant> h = get();
		if (h == null)return 0;
		int cnt = 0;
		for (StorageView<ItemVariant> slot : h)cnt++;
		return cnt;
	}

	public static BlockFilter getBlockFilterAt(Level level, BlockPos pos, boolean make) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be == null)return null;
		return StorageModComponents.BLOCK_FILTER.get(be).getFilter(make);
	}

	public static void removeBlockFilterAt(Level level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be == null)return;
		StorageModComponents.BLOCK_FILTER.get(be).remove(level, pos);
	}
}
