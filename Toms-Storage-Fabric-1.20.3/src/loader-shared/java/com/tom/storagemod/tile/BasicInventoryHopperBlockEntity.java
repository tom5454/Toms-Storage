package com.tom.storagemod.tile;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.base.Predicates;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.BasicInventoryHopperBlock;
import com.tom.storagemod.block.InventoryCableConnectorBlock;
import com.tom.storagemod.item.IItemFilter;
import com.tom.storagemod.util.BlockFace;
import com.tom.storagemod.util.ItemPredicate;

public class BasicInventoryHopperBlockEntity extends AbstractInventoryHopperBlockEntity {
	private ItemStack filter = ItemStack.EMPTY;
	private ItemPredicate filterPred;
	private int cooldown;
	public BasicInventoryHopperBlockEntity(BlockPos pos, BlockState state) {
		super(Content.invHopperBasicTile.get(), pos, state);
	}

	@Override
	protected void update() {
		if(!filter.isEmpty() && filterPred == null)setFilter(filter);//update predicate
		boolean hasFilter = filterPred != null;
		if(topNet && !hasFilter)return;
		if(cooldown > 0) {
			cooldown--;
			return;
		}
		if(!this.getBlockState().getValue(BasicInventoryHopperBlock.ENABLED))return;

		try (Transaction tr = Transaction.openOuter()) {
			if(StorageUtil.move(top, bottom, hasFilter ? filterPred : Predicates.alwaysTrue(), 1, tr) == 1) {
				tr.commit();
				cooldown = 10;
			} else {
				cooldown = 5;
			}
		}
	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		compound.put("Filter", getFilter().save(new CompoundTag()));
	}

	@Override
	public void load(CompoundTag nbtIn) {
		super.load(nbtIn);
		this.filter = ItemStack.of(nbtIn.getCompound("Filter"));
	}

	public void setFilter(ItemStack filter) {
		this.filter = filter;
		if(this.filter.isEmpty())filterPred = null;
		else if(this.filter.getItem() instanceof IItemFilter i) {
			filterPred = i.createFilter(BlockFace.touching(level, worldPosition, getBlockState().getValue(InventoryCableConnectorBlock.FACING)), filter);
		} else {
			ItemVariant iv = ItemVariant.of(filter);
			filterPred = iv::equals;
		}
		setChanged();
	}

	public ItemStack getFilter() {
		return filter;
	}
}
