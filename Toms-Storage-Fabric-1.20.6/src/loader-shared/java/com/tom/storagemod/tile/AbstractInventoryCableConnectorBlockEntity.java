package com.tom.storagemod.tile;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Content;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.IInventoryCable;
import com.tom.storagemod.block.InventoryCableConnectorBlock;
import com.tom.storagemod.tile.InventoryConnectorBlockEntity.LinkedInv;
import com.tom.storagemod.util.IProxy;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public class AbstractInventoryCableConnectorBlockEntity extends PaintedBlockEntity implements TickableServer, SidedStorageBlockEntity, Storage<ItemVariant>, IProxy {
	protected InventoryConnectorBlockEntity master;
	protected Storage<ItemVariant> pointedAt, masterW;
	protected LinkedInv linv;

	public AbstractInventoryCableConnectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void updateServer() {
		if(level.getGameTime() % 20 == 19) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(InventoryCableConnectorBlock.FACING);
			Stack<BlockPos> toCheck = new Stack<>();
			Set<BlockPos> checkedBlocks = new HashSet<>();
			checkedBlocks.add(worldPosition);
			toCheck.addAll(((IInventoryCable)state.getBlock()).next(level, state, worldPosition));
			if(master != null)master.unLink(linv);
			master = null;
			linv = new LinkedInv();
			initLinv();
			masterW = null;
			while(!toCheck.isEmpty()) {
				BlockPos cp = toCheck.pop();
				if(!checkedBlocks.contains(cp)) {
					checkedBlocks.add(cp);
					if(level.isLoaded(cp)) {
						state = level.getBlockState(cp);
						if(state.getBlock() == Content.connector.get()) {
							BlockEntity te = level.getBlockEntity(cp);
							if(te instanceof InventoryConnectorBlockEntity) {
								master = (InventoryConnectorBlockEntity) te;
								linv.time = level.getGameTime();
								linv.handler = this::getPointedAtHandler;
								master.addLinked(linv);
								masterW = applyFilter(master.getInventory());
							}
							break;
						}
						if(state.getBlock() instanceof IInventoryCable) {
							toCheck.addAll(((IInventoryCable)state.getBlock()).next(level, state, cp));
						}
					}
					if(checkedBlocks.size() > StorageMod.CONFIG.invConnectorMaxCables)break;
				}
			}
			pointedAt = getPointedAt(worldPosition.relative(facing), facing);
		}
	}

	protected void initLinv() {
	}

	protected Storage<ItemVariant> getPointedAt(BlockPos pos, Direction facing) {
		Storage<ItemVariant> itemHandler = ItemStorage.SIDED.find(level, pos, facing.getOpposite());
		if(itemHandler == null) {
			Container inv = HopperBlockEntity.getContainerAt(level, pos);
			if(inv != null)itemHandler = InventoryStorage.of(inv, facing.getOpposite());
		}
		return itemHandler;
	}

	private Storage<ItemVariant> getPointedAtHandler() {
		return pointedAt == null ? null : applyFilter(pointedAt);
	}

	protected Storage<ItemVariant> applyFilter(Storage<ItemVariant> st) {
		return st;
	}

	@Override
	public Storage<ItemVariant> get() {
		return masterW;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if(masterW == null)return 0L;
		return masterW.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if(masterW == null)return 0L;
		return masterW.extract(resource, maxAmount, transaction);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		if(masterW == null)return Collections.emptyIterator();
		return masterW.iterator();
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(Direction side) {
		return this;
	}
}
