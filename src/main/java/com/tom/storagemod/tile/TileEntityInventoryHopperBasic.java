package com.tom.storagemod.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.EmptyHandler;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.BlockInventoryHopperBasic;

public class TileEntityInventoryHopperBasic extends TileEntityInventoryHopperBase {
	private ItemStack filter = ItemStack.EMPTY;
	private int cooldown, lastItemSlot = -1;
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
		if(!this.getBlockState().getValue(BlockInventoryHopperBasic.ENABLED))return;
		boolean hasFilter = !getFilter().isEmpty();
		IItemHandler top = this.top.orElse(EmptyHandler.INSTANCE);
		IItemHandler bot = this.bottom.orElse(EmptyHandler.INSTANCE);

		if(lastItemSlot != -1 && lastItemSlot < top.getSlots()) {
			if(hasFilter) {
				ItemStack inSlot = top.getStackInSlot(lastItemSlot);
				if(!ItemStack.isSame(inSlot, getFilter()) || !ItemStack.tagMatches(inSlot, getFilter())) {
					lastItemSlot = -1;
				}
			} else {
				ItemStack inSlot = top.getStackInSlot(lastItemSlot);
				if(inSlot.isEmpty()) {
					lastItemSlot = -1;
				}
			}
		} else lastItemSlot = -1;
		if(lastItemSlot == -1) {
			for (int i = 0; i < top.getSlots(); i++) {
				if(hasFilter) {
					ItemStack inSlot = top.getStackInSlot(i);
					if(!ItemStack.isSame(inSlot, getFilter()) || !ItemStack.tagMatches(inSlot, getFilter())) {
						continue;
					}
				}
				ItemStack extractItem = top.extractItem(i, 1, true);
				if (!extractItem.isEmpty()) {
					lastItemSlot = i;
					break;
				}
			}
			cooldown = 10;
		}
		if(lastItemSlot != -1) {
			ItemStack extractItem = top.extractItem(lastItemSlot, 1, true);
			if (!extractItem.isEmpty()) {
				ItemStack is = ItemHandlerHelper.insertItemStacked(bot, extractItem, true);
				if(is.isEmpty()) {
					is = ItemHandlerHelper.insertItemStacked(bot, top.extractItem(lastItemSlot, 1, false), false);
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
	}

	public ItemStack getFilter() {
		return filter;
	}
}
