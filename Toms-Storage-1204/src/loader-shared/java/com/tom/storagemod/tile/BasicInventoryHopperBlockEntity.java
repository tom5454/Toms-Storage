package com.tom.storagemod.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.BasicInventoryHopperBlock;
import com.tom.storagemod.block.InventoryCableConnectorBlock;
import com.tom.storagemod.item.IItemFilter;
import com.tom.storagemod.util.BlockFace;
import com.tom.storagemod.util.ItemPredicate;

public class BasicInventoryHopperBlockEntity extends AbstractInventoryHopperBlockEntity {
	private ItemStack filter = ItemStack.EMPTY;
	private ItemPredicate filterPred;
	private int cooldown, lastItemSlot = -1;
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

		if(lastItemSlot != -1 && lastItemSlot < top.getSlots()) {
			if(hasFilter) {
				ItemStack inSlot = top.getStackInSlot(lastItemSlot);
				if(!filterPred.test(inSlot)) {
					lastItemSlot = -1;
				}
			} else {
				ItemStack inSlot = top.getStackInSlot(lastItemSlot);
				if(inSlot.isEmpty()) {
					lastItemSlot = -1;
				}
			}
		}
		if(lastItemSlot == -1) {
			for (int i = 0; i < top.getSlots(); i++) {
				if(hasFilter) {
					ItemStack inSlot = top.getStackInSlot(i);
					if(!filterPred.test(inSlot)) {
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
				ItemStack is = ItemHandlerHelper.insertItemStacked(bottom, extractItem, true);
				if(is.isEmpty()) {
					is = ItemHandlerHelper.insertItemStacked(bottom, top.extractItem(lastItemSlot, 1, false), false);
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
		this.filter = ItemStack.of(nbtIn.getCompound("Filter"));
	}

	public void setFilter(ItemStack filter) {
		this.filter = filter;
		if(this.filter.isEmpty())filterPred = null;
		else if(this.filter.getItem() instanceof IItemFilter i) {
			filterPred = i.createFilter(BlockFace.touching(level, worldPosition, getBlockState().getValue(InventoryCableConnectorBlock.FACING)), filter);
		} else {
			filterPred = s -> ItemStack.isSameItemSameTags(s, filter);
		}
		setChanged();
	}

	public ItemStack getFilter() {
		return filter;
	}
}
