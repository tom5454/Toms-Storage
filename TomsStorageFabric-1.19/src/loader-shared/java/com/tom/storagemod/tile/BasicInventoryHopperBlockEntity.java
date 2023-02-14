package com.tom.storagemod.tile;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.BasicInventoryHopperBlock;

public class BasicInventoryHopperBlockEntity extends AbstractInventoryHopperBlockEntity {
	private ItemStack filter = ItemStack.EMPTY;
	private int cooldown;
	public BasicInventoryHopperBlockEntity(BlockPos pos, BlockState state) {
		super(Content.invHopperBasicTile.get(), pos, state);
	}

	@Override
	protected void update() {
		if(topNet && getFilter().isEmpty())return;
		if(cooldown > 0) {
			cooldown--;
			return;
		}
		if(!this.getBlockState().getValue(BasicInventoryHopperBlock.ENABLED))return;

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
	public void saveAdditional(CompoundTag compound) {
		compound.put("Filter", getFilter().save(new CompoundTag()));
	}

	@Override
	public void load(CompoundTag nbtIn) {
		super.load(nbtIn);
		setFilter(ItemStack.of(nbtIn.getCompound("Filter")));
	}

	public void setFilter(ItemStack filter) {
		this.filter = filter;
		setChanged();
	}

	public ItemStack getFilter() {
		return filter;
	}
}
