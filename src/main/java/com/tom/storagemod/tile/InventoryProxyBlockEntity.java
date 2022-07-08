package com.tom.storagemod.tile;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.EmptyHandler;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.TickerUtil.TickableServer;
import com.tom.storagemod.block.InventoryProxyBlock;
import com.tom.storagemod.block.InventoryProxyBlock.DirectionWithNull;
import com.tom.storagemod.util.IProxy;

public class InventoryProxyBlockEntity extends PaintedBlockEntity implements TickableServer {
	private LazyOptional<IItemHandler> invHandler;
	private LazyOptional<IItemHandler> pointedAt;
	private LazyOptional<IItemHandler> filter;
	private boolean ignoreCount;
	private int globalCountLimit = 64;

	public InventoryProxyBlockEntity(BlockPos pos, BlockState state) {
		super(StorageMod.invProxyTile.get(), pos, state);
	}
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (!this.remove && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(InventoryProxyBlock.FACING);
			if(side != facing) {
				if (this.invHandler == null)
					this.invHandler = LazyOptional.of(InvHandler::new);
				return this.invHandler.cast();
			}
		}
		return super.getCapability(cap, side);
	}

	private boolean calling;
	public <R> R call(Function<IItemHandler, R> func, Predicate<IItemHandler> accessCheck, R def) {
		if(calling)return def;
		calling = true;
		if(pointedAt != null) {
			IItemHandler ih = pointedAt.orElse(EmptyHandler.INSTANCE);
			if(accessCheck.test(ih)) {
				R r = func.apply(ih);
				calling = false;
				return r;
			}
		}
		calling = false;
		return def;
	}

	private class InvHandler implements IItemHandler, IProxy {

		@Override
		public int getSlots() {
			return call(IItemHandler::getSlots, i -> true, 0);
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return call(i -> i.getStackInSlot(slot), i -> checkFilter(i, slot, null), ItemStack.EMPTY);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return call(i -> i.insertItem(slot, stack, simulate), i -> checkFilter(i, slot, stack), stack);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return call(i -> i.extractItem(slot, amount, simulate), i -> checkFilter(i, slot, null), ItemStack.EMPTY);
		}

		@Override
		public int getSlotLimit(int slot) {
			return call(i -> Math.min(i.getSlotLimit(slot), getMaxCountPerStack()), i -> true, 0);
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return call(i -> i.isItemValid(slot, stack), i -> checkFilter(i, slot, stack), false);
		}

		@Override
		public IItemHandler get() {
			if(pointedAt != null) {
				return pointedAt.orElse(null);
			}
			return null;
		}
	}

	private int getMaxCountPerStack() {
		return filter == null ? 64 : globalCountLimit;
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (invHandler != null)
			invHandler.invalidate();
	}
	@Override
	public void updateServer() {
		if(level.getGameTime() % 20 == 18) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(InventoryProxyBlock.FACING);
			DirectionWithNull filter = state.getValue(InventoryProxyBlock.FILTER_FACING);
			if(pointedAt == null || !pointedAt.isPresent()) {
				BlockEntity te = level.getBlockEntity(worldPosition.relative(facing));
				if(te != null && !(te instanceof InventoryProxyBlockEntity)) {
					pointedAt = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
				}
			}
			ignoreCount = false;
			globalCountLimit = 64;
			if(filter != DirectionWithNull.NULL) {
				BlockEntity te = level.getBlockEntity(worldPosition.relative(filter.getDir()));
				if(te != null && !(te instanceof InventoryProxyBlockEntity)) {
					this.filter = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
					if(te instanceof MenuProvider) {
						String[] sp = ((MenuProvider)te).getDisplayName().getString().split(",");
						for (String string : sp) {
							String[] sp2 = string.split("=");
							String key = sp2[0];
							String value = sp2.length > 1 ? sp2[1] : "";
							switch (key) {
							case "ignoreSize":
								ignoreCount = true;
								break;
							case "maxCount":
								if(!value.isEmpty())
									globalCountLimit = parseInt(value, 64);
								break;
							default:
								break;
							}
						}
					} else {
						ignoreCount = false;
						globalCountLimit = 64;
					}
				}
			} else {
				this.filter = null;
			}
			this.level.updateNeighbourForOutputSignal(this.worldPosition, getBlockState().getBlock());
		}
	}

	private int parseInt(String value, int def) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return def;
		}
	}

	public int getComparatorOutput() {
		return call(inventory -> {
			if(filter == null || !filter.isPresent()) {
				return ItemHandlerHelper.calcRedstoneFromInventory(inventory);
			}
			int i = 0;
			float f = 0.0F;

			IItemHandler filter = this.filter.orElse(EmptyHandler.INSTANCE);

			int fsize = filter.getSlots();

			for (int j = 0; j < inventory.getSlots(); j++) {
				ItemStack itemStack = inventory.getStackInSlot(j);
				ItemStack fstack = fsize > j ? filter.getStackInSlot(j) : ItemStack.EMPTY;

				if (!itemStack.isEmpty()) {
					if(fstack.isEmpty() || (ItemStack.isSame(itemStack, fstack) && ItemStack.tagMatches(itemStack, fstack))) {
						f += itemStack.getCount() / Math.min((ignoreCount || fstack.isEmpty() ? globalCountLimit : fstack.getCount()), itemStack.getMaxStackSize());
						i++;
					}
				}
			}

			f /= inventory.getSlots();
			return Mth.floor(f * 14.0F) + ((i > 0) ? 1 : 0);
		}, i -> true, 0);
	}

	private boolean checkFilter(IItemHandler w, int slot, ItemStack stack) {
		if(filter != null && filter.isPresent()) {
			IItemHandler ih = this.filter.orElse(EmptyHandler.INSTANCE);
			if(ih.getSlots() > slot) {
				ItemStack fstack = ih.getStackInSlot(slot);
				if(fstack.isEmpty())return true;
				if(stack == null) {
					stack = w.getStackInSlot(slot);
					if(ItemStack.isSame(stack, fstack) && ItemStack.tagMatches(stack, fstack)) {
						return true;
					}
					return false;
				}
				if(ItemStack.isSame(stack, fstack) && ItemStack.tagMatches(stack, fstack)) {
					if(ignoreCount)return true;
					int count = w.getStackInSlot(slot).getCount();
					if(count < fstack.getCount())
						return true;
				}
				return false;
			}
		}
		return true;
	}
}
