package com.tom.storagemod.inventory;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.tom.storagemod.block.entity.IInventoryConnector;
import com.tom.storagemod.inventory.PlatformInventoryAccess.BlockInventoryAccess;
import com.tom.storagemod.util.IValidInfo;

public class NetworkInventory {
	private BlockInventoryAccess block = new BlockInventoryAccess();
	private WeakReference<IInventoryConnector> networkAccess;
	private long lastUpdate;

	public void onLoad(Level level, BlockPos worldPosition, Direction side, IValidInfo v) {
		block.onLoad(level, worldPosition, side, v);
	}

	public void scanNetwork(Level level, BlockPos worldPosition) {
		networkAccess = null;
		Collection<BlockPos> netBlocks = InventoryCableNetwork.getNetwork(level).getNetworkNodes(worldPosition);

		for (BlockPos p : netBlocks) {
			if (!level.isLoaded(p))continue;

			BlockEntity be = level.getBlockEntity(p);
			if (be instanceof IInventoryConnector te && te.hasConnectedInventories()) {
				networkAccess = new WeakReference<>(te);
				break;
			}
		}
	}

	public IInventoryAccess getAccess(Level level, BlockPos worldPosition) {
		if (block.exists()) {
			if (block.get() instanceof PlatformItemHandler h) {
				Set<IProxy> dejaVu = Collections.newSetFromMap(new IdentityHashMap<>());
				return h.getRootHandler(dejaVu);
			}
			return block;
		}
		if (level.getGameTime() - lastUpdate > 10) {
			scanNetwork(level, worldPosition);
			lastUpdate = level.getGameTime();
		}
		if (networkAccess != null) {
			IInventoryConnector net = networkAccess.get();
			if (net != null && net.hasConnectedInventories())return net.getMergedHandler();
		}
		return block;
	}

	public boolean isNetwork() {
		return !block.exists() && networkAccess != null;
	}

	public boolean isValid() {
		return block.exists() || networkAccess != null;
	}
}
