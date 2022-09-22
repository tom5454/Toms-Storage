package com.tom.storagemod.tile;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.BlockInventoryHopperBasic;

public class TileEntityInventoryHopperBasic extends TileEntityInventoryHopperBase {
	private ItemStack filter = ItemStack.EMPTY;
	private int cooldown;
	public TileEntityInventoryHopperBasic(BlockPos pos, BlockState state) {
		super(StorageMod.invHopperBasicTile, pos, state);
	}

	@Override
	protected void update() {
		if(topNet && getFilter().isEmpty())return;
		if(cooldown > 0) {
			cooldown--;
			return;
		}
		if(!this.getCachedState().get(BlockInventoryHopperBasic.ENABLED))return;

		try (Transaction tr = Transaction.openOuter()) {
			ItemVariant iv = getFilter().isEmpty() ? null : ItemVariant.of(getFilter());
			if(StorageUtil.move(top, bottom, i -> {
				if(iv == null)return true;
				return iv.equals(i);
			}, 1, tr) == 1) {
				tr.commit();
				cooldown = 10;
			} else {
				cooldown = 5;
			}
		}
	}

	@Override
	public void writeNbt(NbtCompound compound) {
		compound.put("Filter", getFilter().writeNbt(new NbtCompound()));
	}

	@Override
	public void readNbt(NbtCompound nbtIn) {
		super.readNbt(nbtIn);
		setFilter(ItemStack.fromNbt(nbtIn.getCompound("Filter")));
	}

	public void setFilter(ItemStack filter) {
		this.filter = filter;
	}

	public ItemStack getFilter() {
		return filter;
	}
}
