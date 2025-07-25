package com.tom.storagemod.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;

import com.tom.storagemod.block.entity.BlockFilterAttachment;
import com.tom.storagemod.platform.Platform;
import com.tom.storagemod.util.IValidInfo;
import com.tom.storagemod.util.WorldStates;

public interface PlatformInventoryAccess extends IInventoryAccess {
	public static final PlatformInventoryAccess EMPTY = new PlatformInventoryAccess() {

		@Override
		public IInventoryChangeTracker tracker() {
			return InventoryChangeTracker.NULL;
		}

		@Override
		public IItemHandler get() {
			return EmptyItemHandler.INSTANCE;
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

		@Override
		public IInventoryAccess getRootHandler(java.util.Set<IInventoryAccess> visited) {
			return this;
		}
	};

	public static class BlockInventoryAccess implements PlatformInventoryAccess {
		private boolean valid;
		private BlockCapabilityCache<IItemHandler, Direction> itemCache;
		private IInventoryChangeTracker.Delegate tracker = new IInventoryChangeTracker.Delegate();

		public void onLoad(Level level, BlockPos worldPosition, Direction side, IValidInfo isValid) {
			valid = true;
			itemCache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, (ServerLevel) level, worldPosition, side, () -> valid && isValid.isObjectValid(), this::onInvalid);
		}

		protected void onInvalid() {
		}

		@Override
		public IInventoryChangeTracker tracker() {
			IItemHandler itemHandler = itemCache.getCapability();
			if (itemHandler != null)
				tracker.setDelegate(WorldStates.getTracker(itemHandler));
			else
				tracker.setDelegate(InventoryChangeTracker.NULL);
			return tracker;
		}

		@Override
		public IItemHandler get() {
			return itemCache == null || !valid ? null : itemCache.getCapability();
		}

		public static boolean hasInventoryAt(Level level, BlockPos pos, BlockState state, Direction direction) {
			return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, state, null, direction) != null;
		}

		@Override
		public void markInvalid() {
			valid = false;
		}

		public boolean exists() {
			return itemCache.getCapability() != null;
		}

		@Override
		public IInventoryAccess getRootHandler() {
			return getRootHandler(new java.util.HashSet<>());
		}

		public IInventoryAccess getRootHandler(java.util.Set<IInventoryAccess> visited) {
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

		@Override
		public String toString() {
			return "BlockInventoryAccess at " + itemCache.pos();
		}
	}

	@Override
	IItemHandler get();

	@Override
	public default int getFreeSlotCount() {
		IItemHandler itemHandler = get();
		if (itemHandler == null)return 0;
		int empty = 0;
		for(int i = 0;i<itemHandler.getSlots();i++) {
			if(itemHandler.getStackInSlot(i).isEmpty())empty++;
		}
		return empty;
	}

	@Override
	public default int getSlotCount() {
		IItemHandler itemHandler = get();
		if (itemHandler == null)return 0;
		return itemHandler.getSlots();
	}

	default IInventoryAccess getRootHandler(java.util.Set<IInventoryAccess> visited) {
		return getRootHandler(); // fallback to normal implementation
	}

	public static BlockFilter getBlockFilterAt(Level level, BlockPos p, boolean make) {
		BlockEntity be = level.getBlockEntity(p);
		if (be == null || (!make && !be.hasData(Platform.BLOCK_FILTER)))return null;
		return be.getData(Platform.BLOCK_FILTER).getFilter();
	}

	public static void removeBlockFilterAt(Level level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		if (be == null || !be.hasData(Platform.BLOCK_FILTER))return;
		BlockFilterAttachment f = be.getData(Platform.BLOCK_FILTER);
		f.getFilter().dropContents(level, pos);
		be.removeData(Platform.BLOCK_FILTER);
	}
}
