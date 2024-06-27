package com.tom.storagemod.block.entity;

import java.lang.ref.WeakReference;
import java.util.Collection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Content;
import com.tom.storagemod.inventory.IInventoryAccess;
import com.tom.storagemod.inventory.IInventoryAccess.IInventory;
import com.tom.storagemod.inventory.IInventoryConnectorReference;
import com.tom.storagemod.inventory.InventoryCableNetwork;
import com.tom.storagemod.inventory.PlatformInventoryAccess;
import com.tom.storagemod.platform.PlatformBlockEntity;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public class InventoryInterfaceBlockEntity extends PlatformBlockEntity implements TickableServer, IInventory, IInventoryConnectorReference {
	private WeakReference<IInventoryConnector> networkAccess;

	public InventoryInterfaceBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
		super(Content.invInterfaceBE.get(), p_155229_, p_155230_);
	}

	@Override
	public void updateServer() {
		long time = level.getGameTime();
		if(time % 20 == worldPosition.hashCode() % 20) {
			networkAccess = null;
			Collection<BlockPos> netBlocks = InventoryCableNetwork.getNetwork(level).getNetworkNodes(worldPosition);
			for (BlockPos p : netBlocks) {
				if (!level.isLoaded(p))continue;

				BlockEntity be = level.getBlockEntity(p);
				if (be instanceof IInventoryConnector te) {
					networkAccess = new WeakReference<>(te);
					break;
				}
			}
		}
	}

	@Override
	public IInventoryAccess getInventoryAccess() {
		if (networkAccess != null) {
			IInventoryConnector net = networkAccess.get();
			if (net != null && net.isValid())return net.getMergedHandler();
		}
		return PlatformInventoryAccess.EMPTY;
	}

	@Override
	public IInventoryConnector getConnectorRef() {
		if (networkAccess != null) {
			IInventoryConnector net = networkAccess.get();
			if (net != null && net.isValid())return net;
		}
		return null;
	}
}
