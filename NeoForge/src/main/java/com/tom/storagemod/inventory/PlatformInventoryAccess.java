package com.tom.storagemod.inventory;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

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
		public ResourceHandler<ItemResource> get() {
			return EmptyResourceHandler.instance();
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
		public IInventoryAccess getRootHandler(Set<IProxy> dejaVu) {
			return this;
		}
	};

	public static class BlockInventoryAccess implements PlatformInventoryAccess {
		private boolean valid;
		private BlockCapabilityCache<ResourceHandler<ItemResource>, @Nullable Direction> itemCache;
		private IInventoryChangeTracker.Delegate tracker = new IInventoryChangeTracker.Delegate();

		public void onLoad(Level level, BlockPos worldPosition, Direction side, IValidInfo isValid) {
			valid = true;
			itemCache = BlockCapabilityCache.create(Capabilities.Item.BLOCK, (ServerLevel) level, worldPosition, side, () -> valid && isValid.isObjectValid(), this::onInvalid);
		}

		protected void onInvalid() {
		}

		@Override
		public IInventoryChangeTracker tracker() {
			@Nullable ResourceHandler<ItemResource> itemHandler = itemCache.getCapability();
			if (itemHandler != null)
				tracker.setDelegate(WorldStates.getTracker(itemHandler));
			else
				tracker.setDelegate(InventoryChangeTracker.NULL);
			return tracker;
		}

		@Override
		public @Nullable ResourceHandler<ItemResource> get() {
			return itemCache == null || !valid ? null : itemCache.getCapability();
		}

		public static boolean hasInventoryAt(Level level, BlockPos pos, BlockState state, Direction direction) {
			return level.getCapability(Capabilities.Item.BLOCK, pos, state, null, direction) != null;
		}

		@Override
		public void markInvalid() {
			valid = false;
		}

		public boolean exists() {
			return itemCache.getCapability() != null;
		}

		@Override
		public IInventoryAccess getRootHandler(Set<IProxy> dejaVu) {
			if (get() instanceof IProxy p) {
				if (dejaVu.add(p)) {
					return p.getRootHandler(dejaVu);
				} else {
					return this;
				}
			}
			return this;
		}

		@Override
		public String toString() {
			return "BlockInventoryAccess at " + itemCache.pos();
		}
	}

	@Override
	ResourceHandler<ItemResource> get();

	@Override
	public default int getFreeSlotCount() {
		ResourceHandler<ItemResource> itemHandler = get();
		if (itemHandler == null)return 0;
		int empty = 0;
		for(int i = 0;i<itemHandler.size();i++) {
			if(itemHandler.getResource(i).isEmpty())empty++;
		}
		return empty;
	}

	@Override
	public default int getSlotCount() {
		ResourceHandler<ItemResource> itemHandler = get();
		if (itemHandler == null)return 0;
		return itemHandler.size();
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
