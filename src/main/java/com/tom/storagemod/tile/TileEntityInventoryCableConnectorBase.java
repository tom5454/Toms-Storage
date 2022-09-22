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
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.TickerUtil.TickableServer;
import com.tom.storagemod.block.BlockInventoryCableConnector;
import com.tom.storagemod.block.IInventoryCable;
import com.tom.storagemod.tile.TileEntityInventoryConnector.LinkedInv;
import com.tom.storagemod.util.IProxy;

public class TileEntityInventoryCableConnectorBase extends TileEntityPainted implements TickableServer, SidedStorageBlockEntity, Storage<ItemVariant>, IProxy {
	protected TileEntityInventoryConnector master;
	protected Storage<ItemVariant> pointedAt, masterW;
	protected LinkedInv linv;

	public TileEntityInventoryCableConnectorBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void updateServer() {
		if(world.getTime() % 20 == 19) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(BlockInventoryCableConnector.FACING);
			Stack<BlockPos> toCheck = new Stack<>();
			Set<BlockPos> checkedBlocks = new HashSet<>();
			checkedBlocks.add(pos);
			toCheck.addAll(((IInventoryCable)state.getBlock()).next(world, state, pos));
			if(master != null)master.unLink(linv);
			master = null;
			linv = new LinkedInv();
			masterW = null;
			while(!toCheck.isEmpty()) {
				BlockPos cp = toCheck.pop();
				if(!checkedBlocks.contains(cp)) {
					checkedBlocks.add(cp);
					if(world.canSetBlock(cp)) {
						state = world.getBlockState(cp);
						if(state.getBlock() == StorageMod.connector) {
							BlockEntity te = world.getBlockEntity(cp);
							if(te instanceof TileEntityInventoryConnector) {
								master = (TileEntityInventoryConnector) te;
								linv.time = world.getTime();
								linv.handler = this::applyFilter;
								master.addLinked(linv);
								masterW = master.getInventory();
							}
							break;
						}
						if(state.getBlock() instanceof IInventoryCable) {
							toCheck.addAll(((IInventoryCable)state.getBlock()).next(world, state, cp));
						}
					}
					if(checkedBlocks.size() > StorageMod.CONFIG.invConnectorMaxCables)break;
				}
			}
			pointedAt = getPointedAt(pos.offset(facing), facing);
		}
	}

	protected Storage<ItemVariant> getPointedAt(BlockPos pos, Direction facing) {
		Storage<ItemVariant> itemHandler = ItemStorage.SIDED.find(world, pos, facing.getOpposite());
		if(itemHandler == null) {
			Inventory inv = HopperBlockEntity.getInventoryAt(world, pos);
			if(inv != null)itemHandler = InventoryStorage.of(inv, facing.getOpposite());
		}
		return itemHandler;
	}

	protected Storage<ItemVariant> applyFilter() {
		return pointedAt;
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
	public Iterator<? extends StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		if(masterW == null)return Collections.emptyIterator();
		return masterW.iterator(transaction);
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(Direction side) {
		return this;
	}
}
