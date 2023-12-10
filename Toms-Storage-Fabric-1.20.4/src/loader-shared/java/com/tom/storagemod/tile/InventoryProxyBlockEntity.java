package com.tom.storagemod.tile;

import java.util.Collections;
import java.util.Iterator;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.InventoryProxyBlock;
import com.tom.storagemod.util.IProxy;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public class InventoryProxyBlockEntity extends PaintedBlockEntity implements TickableServer, SidedStorageBlockEntity, Storage<ItemVariant>, IProxy {
	private Storage<ItemVariant> pointedAtSt;

	public InventoryProxyBlockEntity(BlockPos pos, BlockState state) {
		super(Content.invProxyTile.get(), pos, state);
	}

	@Override
	public void updateServer() {
		if(level.getGameTime() % 20 == 18) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(InventoryProxyBlock.FACING);
			BlockEntity te = level.getBlockEntity(worldPosition.relative(facing));
			if(te != null && !(te instanceof InventoryProxyBlockEntity)) {
				pointedAtSt = ItemStorage.SIDED.find(level, worldPosition.relative(facing), facing.getOpposite());
			} else {
				pointedAtSt = null;
			}
		}
	}

	@Override
	public Storage<ItemVariant> get() {
		return pointedAtSt;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if(pointedAtSt == null)return 0L;
		return pointedAtSt.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if(pointedAtSt == null)return 0L;
		return pointedAtSt.extract(resource, maxAmount, transaction);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		if(pointedAtSt == null)return Collections.emptyIterator();
		return pointedAtSt.iterator();
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(Direction side) {
		BlockState state = level.getBlockState(worldPosition);
		Direction facing = state.getValue(InventoryProxyBlock.FACING);
		return side == facing ? null : this;
	}
}
