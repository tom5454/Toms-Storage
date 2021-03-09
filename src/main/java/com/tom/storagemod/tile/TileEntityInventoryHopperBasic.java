package com.tom.storagemod.tile;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.EmptyHandler;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.BlockInventoryHopperBasic;

public class TileEntityInventoryHopperBasic extends TileEntityInventoryHopperBase {
	private ItemStack filter = ItemStack.EMPTY;
	private int cooldown;
	public TileEntityInventoryHopperBasic() {
		super(StorageMod.invHopperBasicTile);
	}

	@Override
	protected void update() {
		if(topNet && getFilter().isEmpty())return;
		if(cooldown > 0) {
			cooldown--;
			return;
		}
		if(!this.getBlockState().get(BlockInventoryHopperBasic.ENABLED))return;
		boolean hasFilter = !getFilter().isEmpty();
		IItemHandler top = this.top.orElse(EmptyHandler.INSTANCE);
		IItemHandler bot = this.bottom.orElse(EmptyHandler.INSTANCE);
		for (int i = 0; i < top.getSlots(); i++) {
			if(hasFilter) {
				ItemStack inSlot = top.getStackInSlot(i);
				if(!ItemStack.areItemsEqual(inSlot, getFilter()) || !ItemStack.areItemStackTagsEqual(inSlot, getFilter())) {
					continue;
				}
			}
			ItemStack extractItem = top.extractItem(i, 1, true);
			if (!extractItem.isEmpty()) {
				ItemStack is = ItemHandlerHelper.insertItemStacked(bot, extractItem, true);
				if(is.isEmpty()) {
					is = ItemHandlerHelper.insertItemStacked(bot, top.extractItem(i, 1, false), false);
					cooldown = 10;
					if(!is.isEmpty()) {
						//Never?
					}
					return;
				}
			}
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Filter", getFilter().write(new CompoundNBT()));
		return super.write(compound);
	}

	@Override
	public void read(BlockState stateIn, CompoundNBT nbtIn) {
		super.read(stateIn, nbtIn);
		setFilter(ItemStack.read(nbtIn.getCompound("Filter")));
	}

	public void setFilter(ItemStack filter) {
		this.filter = filter;
	}

	public ItemStack getFilter() {
		return filter;
	}
}
