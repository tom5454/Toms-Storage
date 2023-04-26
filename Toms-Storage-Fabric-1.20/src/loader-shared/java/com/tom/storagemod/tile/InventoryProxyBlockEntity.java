package com.tom.storagemod.tile;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;

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
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.InventoryProxyBlock;
import com.tom.storagemod.block.InventoryProxyBlock.DirectionWithNull;
import com.tom.storagemod.platform.PlatformStorage;
import com.tom.storagemod.util.IProxy;
import com.tom.storagemod.util.InventoryWrapper;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public class InventoryProxyBlockEntity extends PaintedBlockEntity implements TickableServer, SidedStorageBlockEntity, Container, PlatformStorage, IProxy {
	private Storage<ItemVariant> pointedAtSt;

	public InventoryProxyBlockEntity(BlockPos pos, BlockState state) {
		super(Content.invProxyTile.get(), pos, state);
	}

	@Override
	public void updateServer() {
		if(level.getGameTime() % 20 == 18) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(InventoryProxyBlock.FACING);
			DirectionWithNull filter = state.getValue(InventoryProxyBlock.FILTER_FACING);
			BlockEntity te = level.getBlockEntity(worldPosition.relative(facing));
			if(te != null && !(te instanceof InventoryProxyBlockEntity)) {
				pointedAtSt = ItemStorage.SIDED.find(level, worldPosition.relative(facing), facing.getOpposite());
				Container inv = HopperBlockEntity.getContainerAt(level, worldPosition.relative(facing));
				if(inv != null) {
					pointedAt = new InventoryWrapper(inv, facing.getOpposite());
				} else {
					pointedAt = null;
				}
			} else {
				pointedAtSt = null;
				pointedAt = null;
			}
			//Old function
			ignoreCount = false;
			globalCountLimit = 64;
			if(filter != DirectionWithNull.NULL) {
				te = level.getBlockEntity(worldPosition.relative(filter.getDir()));
				if(te != null && !(te instanceof InventoryProxyBlockEntity)) {
					Container inv = getInventoryAt(level, worldPosition.relative(filter.getDir()));
					if(inv != null) {
						this.filter = new InventoryWrapper(inv, filter.getDir().getOpposite());
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
						}
					}
				}
			} else {
				this.filter = null;
			}
			if (this.filter != null) {
				this.pointedAtSt = InventoryStorage.of(this, Direction.DOWN);
			}
			this.level.updateNeighbourForOutputSignal(this.worldPosition, getBlockState().getBlock());
		}
	}

	@Override
	public Storage<ItemVariant> get() {
		return pointedAtSt;
	}

	@Override
	public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if(pointedAtSt == null)return 0L;
		return pointedAtSt.insert(resource, maxAmount, transaction);
	}

	@Override
	public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
		if(pointedAtSt == null)return 0L;
		return pointedAtSt.extract(resource, maxAmount, transaction);
	}

	@Override
	public Iterator<StorageView<ItemVariant>> iterator() {
		if(pointedAtSt == null)return Collections.emptyIterator();
		return pointedAtSt.iterator();
	}

	@Override
	public @Nullable StorageView<ItemVariant> exactView(ItemVariant resource) {
		if(pointedAtSt == null)return null;
		return pointedAtSt.exactView(resource);
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(Direction side) {
		BlockState state = level.getBlockState(worldPosition);
		Direction facing = state.getValue(InventoryProxyBlock.FACING);
		return side == facing ? null : this;
	}

	//Old function
	private InventoryWrapper pointedAt;
	private InventoryWrapper filter;
	private boolean ignoreCount;
	private int globalCountLimit = 64;

	private int parseInt(String value, int def) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return def;
		}
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
	public void clearContent() {
	}

	@Override
	public int getContainerSize() {
		return call(InventoryWrapper::size, i -> true, 0);
	}

	@Override
	public boolean isEmpty() {
		return call(InventoryWrapper::isEmpty, i -> true, true);
	}

	@Override
	public ItemStack getItem(int paramInt) {
		return call(i -> i.getStack(paramInt), i -> checkFilter(i, paramInt, null), ItemStack.EMPTY);
	}

	@Override
	public ItemStack removeItem(int paramInt1, int paramInt2) {
		return call(i -> i.removeStack(paramInt1, paramInt2), i -> checkFilter(i, paramInt1, null), ItemStack.EMPTY);
	}

	@Override
	public ItemStack removeItemNoUpdate(int paramInt) {
		return call(i -> i.removeStack(paramInt), i -> checkFilter(i, paramInt, null), ItemStack.EMPTY);
	}

	@Override
	public void setItem(int paramInt, ItemStack paramItemStack) {
		call(i -> {
			i.setStack(paramInt, paramItemStack);
			return Unit.INSTANCE;
		}, i -> true, Unit.INSTANCE);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return call(i -> i.isValid(slot, stack, null), i -> checkFilter(i, slot, stack), false);
	}

	private boolean checkFilter(InventoryWrapper w, int slot, ItemStack stack) {
		if(filter != null) {
			if(filter.size() > slot) {
				ItemStack fstack = filter.getStack(slot);
				if(fstack.isEmpty())return true;
				if(stack == null) {
					stack = w.getStack(slot);
					if(ItemStack.isSame(stack, fstack) && ItemStack.tagMatches(stack, fstack)) {
						return true;
					}
					return false;
				}
				if(ItemStack.isSame(stack, fstack) && ItemStack.tagMatches(stack, fstack)) {
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
	public boolean stillValid(Player paramPlayerEntity) {
		return false;
	}

	@Override
	public int getMaxStackSize() {
		return filter == null ? call(InventoryWrapper::getMaxCountPerStack, i -> true, 0) : globalCountLimit;
	}

	public int getComparatorOutput() {
		return call(inventory -> {
			if(filter == null) {
				return AbstractContainerMenu.getRedstoneSignalFromContainer(inventory.getInventory());
			}
			int i = 0;
			float f = 0.0F;

			int fsize = filter.size();

			for (int j = 0; j < inventory.size(); j++) {
				ItemStack itemStack = inventory.getStack(j);
				ItemStack fstack = fsize > j ? filter.getStack(j) : ItemStack.EMPTY;

				if (!itemStack.isEmpty()) {
					if(fstack.isEmpty() || (ItemStack.isSame(itemStack, fstack) && ItemStack.tagMatches(itemStack, fstack))) {
						f += itemStack.getCount() / Math.min((ignoreCount || fstack.isEmpty() ? globalCountLimit : fstack.getCount()), itemStack.getMaxStackSize());
						i++;
					}
				}
			}

			f /= inventory.size();
			return Mth.floor(f * 14.0F) + ((i > 0) ? 1 : 0);
		}, i -> true, 0);
	}

	public static Container getInventoryAt(Level world, BlockPos blockPos) {
		Container inventory = null;
		BlockState blockState = world.getBlockState(blockPos);
		Block block = blockState.getBlock();
		if (block instanceof WorldlyContainerHolder) {
			inventory = ((WorldlyContainerHolder) block).getContainer(blockState, world, blockPos);
		} else if (blockState.hasBlockEntity()) {
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			if (blockEntity instanceof Container) {
				inventory = (Container) blockEntity;
				if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
					inventory = ChestBlock.getContainer((ChestBlock) block, blockState, world, blockPos, true);
				}
			}
		}

		return inventory;
	}
}
