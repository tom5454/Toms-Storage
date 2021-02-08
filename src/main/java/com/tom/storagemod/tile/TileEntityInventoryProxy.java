package com.tom.storagemod.tile;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.TickerUtil.TickableServer;
import com.tom.storagemod.block.BlockInventoryProxy;
import com.tom.storagemod.block.BlockInventoryProxy.DirectionWithNull;

public class TileEntityInventoryProxy extends TileEntityPainted implements TickableServer, Inventory, IProxy {
	private InventoryWrapper pointedAt;
	private InventoryWrapper filter;
	private boolean ignoreCount;
	private int globalCountLimit = 64;
	public TileEntityInventoryProxy(BlockPos pos, BlockState state) {
		super(StorageMod.invProxyTile, pos, state);
	}

	private boolean calling;
	public <R> R call(Function<InventoryWrapper, R> func, Predicate<InventoryWrapper> accessCheck, R def) {
		if(calling)return def;
		calling = true;
		if(pointedAt != null && accessCheck.test(pointedAt)) {
			R r = func.apply(pointedAt);
			calling = false;
			return r;
		}
		calling = false;
		return def;
	}

	@Override
	public void updateServer() {
		if(world.getTime() % 20 == 18) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(BlockInventoryProxy.FACING);
			DirectionWithNull filter = state.get(BlockInventoryProxy.FILTER_FACING);
			BlockEntity te = world.getBlockEntity(pos.offset(facing));
			if(te != null && !(te instanceof TileEntityInventoryProxy)) {
				if(te instanceof Inventory) {
					pointedAt = new InventoryWrapper((Inventory) te, facing.getOpposite());
				} else {
					pointedAt = null;
				}
			} else {
				pointedAt = null;
			}
			ignoreCount = false;
			globalCountLimit = 64;
			if(filter != DirectionWithNull.NULL) {
				te = world.getBlockEntity(pos.offset(filter.getDir()));
				if(te != null && !(te instanceof TileEntityInventoryProxy)) {
					if(te instanceof Inventory) {
						this.filter = new InventoryWrapper((Inventory) te, filter.getDir().getOpposite());
						String[] sp = this.filter.toString().split(",");
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
						this.filter = null;
					}
				}
			} else {
				this.filter = null;
			}
			markDirty();
		}
	}

	private int parseInt(String value, int def) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return def;
		}
	}

	@Override
	public void clear() {
	}

	@Override
	public Inventory get() {
		if(pointedAt != null) {
			return pointedAt.getInventory();
		}
		return null;
	}

	@Override
	public int size() {
		return call(InventoryWrapper::size, i -> true, 0);
	}

	@Override
	public boolean isEmpty() {
		return call(InventoryWrapper::isEmpty, i -> true, true);
	}

	@Override
	public ItemStack getStack(int paramInt) {
		return call(i -> i.getStack(paramInt), i -> checkFilter(i, paramInt, null), ItemStack.EMPTY);
	}

	@Override
	public ItemStack removeStack(int paramInt1, int paramInt2) {
		return call(i -> i.removeStack(paramInt1, paramInt2), i -> checkFilter(i, paramInt1, null), ItemStack.EMPTY);
	}

	@Override
	public ItemStack removeStack(int paramInt) {
		return call(i -> i.removeStack(paramInt), i -> checkFilter(i, paramInt, null), ItemStack.EMPTY);
	}

	@Override
	public void setStack(int paramInt, ItemStack paramItemStack) {
		call(i -> {
			i.setStack(paramInt, paramItemStack);
			return Unit.INSTANCE;
		}, i -> true, Unit.INSTANCE);
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return call(i -> i.isValid(slot, stack, null), i -> checkFilter(i, slot, stack), false);
	}

	private boolean checkFilter(InventoryWrapper w, int slot, ItemStack stack) {
		if(filter != null) {
			if(filter.size() > slot) {
				ItemStack fstack = filter.getStack(slot);
				if(fstack.isEmpty())return true;
				if(stack == null) {
					stack = w.getStack(slot);
					if(ItemStack.areItemsEqual(stack, fstack) && ItemStack.areTagsEqual(stack, fstack)) {
						return true;
					}
					return false;
				}
				if(ItemStack.areItemsEqual(stack, fstack) && ItemStack.areTagsEqual(stack, fstack)) {
					if(ignoreCount)return true;
					int count = w.getStack(slot).getCount();
					if(count < fstack.getCount())
						return true;
				}
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean canPlayerUse(PlayerEntity paramPlayerEntity) {
		return false;
	}

	@Override
	public int getMaxCountPerStack() {
		return filter == null ? 64 : globalCountLimit;
	}

	public int getComparatorOutput() {
		return call(inventory -> {
			if(filter == null) {
				return ScreenHandler.calculateComparatorOutput(inventory.getInventory());
			}
			int i = 0;
			float f = 0.0F;

			int fsize = filter.size();

			for (int j = 0; j < inventory.size(); j++) {
				ItemStack itemStack = inventory.getStack(j);
				ItemStack fstack = fsize > j ? filter.getStack(j) : ItemStack.EMPTY;

				if (!itemStack.isEmpty()) {
					if(fstack.isEmpty() || (ItemStack.areItemsEqual(itemStack, fstack) && ItemStack.areTagsEqual(itemStack, fstack))) {
						f += itemStack.getCount() / Math.min((ignoreCount || fstack.isEmpty() ? globalCountLimit : fstack.getCount()), itemStack.getMaxCount());
						i++;
					}
				}
			}

			f /= inventory.size();
			return MathHelper.floor(f * 14.0F) + ((i > 0) ? 1 : 0);
		}, i -> true, 0);
	}
}
