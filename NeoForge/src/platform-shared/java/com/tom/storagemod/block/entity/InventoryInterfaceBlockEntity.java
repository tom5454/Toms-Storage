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
	private WeakReference<IInventoryConnector> connectorAccess;
	private boolean hasConnector;

	public InventoryInterfaceBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
		super(Content.invInterfaceBE.get(), p_155229_, p_155230_);
	}

	@Override
	public void updateServer() {
		long time = level.getGameTime();
		if(time % 20 == Math.abs(worldPosition.hashCode()) % 20) {
			if (hasConnector) {
				hasConnector = false;
			} else {
				connectorAccess = null;
			}
			if (connectorAccess != null) {
				networkAccess = connectorAccess;
			} else {
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
		}
	}

	@Override
	public IInventoryAccess getInventoryAccess() {
		if (networkAccess != null) {
			IInventoryConnector net = networkAccess.get();
			if (net != null && net.hasConnectedInventories())return net.getMergedHandler();
		}
		return PlatformInventoryAccess.EMPTY;
	}

	@Override
	public IInventoryConnector getConnectorRef() {
		if (networkAccess != null) {
			IInventoryConnector net = networkAccess.get();
			if (net != null && net.hasConnectedInventories())return net;
		}
		return null;
	}

	public void setConnectorAccess(IInventoryConnector connectorAccess) {
		this.connectorAccess = new WeakReference<>(connectorAccess);
		this.hasConnector = true;
	}
}
