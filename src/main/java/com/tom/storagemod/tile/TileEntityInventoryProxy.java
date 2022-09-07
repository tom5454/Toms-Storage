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
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.TickerUtil.TickableServer;
import com.tom.storagemod.block.BlockInventoryProxy;
import com.tom.storagemod.block.BlockInventoryProxy.DirectionWithNull;
import com.tom.storagemod.util.IProxy;
import com.tom.storagemod.util.InventoryWrapper;

public class TileEntityInventoryProxy extends TileEntityPainted implements TickableServer, SidedStorageBlockEntity, Inventory, Storage<ItemVariant>, IProxy {
	private Storage<ItemVariant> pointedAtSt;

	public TileEntityInventoryProxy(BlockPos pos, BlockState state) {
		super(StorageMod.invProxyTile, pos, state);
	}

	@Override
	public void updateServer() {
		if(world.getTime() % 20 == 18) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(BlockInventoryProxy.FACING);
			DirectionWithNull filter = state.get(BlockInventoryProxy.FILTER_FACING);
			BlockEntity te = world.getBlockEntity(pos.offset(facing));
			if(te != null && !(te instanceof TileEntityInventoryProxy)) {
				pointedAtSt = ItemStorage.SIDED.find(world, pos.offset(facing), facing.getOpposite());
				Inventory inv = HopperBlockEntity.getInventoryAt(world, pos.offset(facing));
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
				te = world.getBlockEntity(pos.offset(filter.getDir()));
				if(te != null && !(te instanceof TileEntityInventoryProxy)) {
					Inventory inv = getInventoryAt(world, pos.offset(filter.getDir()));
					if(inv != null) {
						this.filter = new InventoryWrapper(inv, filter.getDir().getOpposite());
						if(te instanceof NamedScreenHandlerFactory) {
							String[] sp = ((NamedScreenHandlerFactory)te).getDisplayName().getString().split(",");
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
			this.world.updateComparators(this.pos, getCachedState().getBlock());
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
	public Iterator<? extends StorageView<ItemVariant>> iterator(TransactionContext transaction) {
		if(pointedAtSt == null)return Collections.emptyIterator();
		return pointedAtSt.iterator(transaction);
	}

	@Override
	public @Nullable StorageView<ItemVariant> exactView(TransactionContext transaction, ItemVariant resource) {
		if(pointedAtSt == null)return null;
		return pointedAtSt.exactView(transaction, resource);
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(Direction side) {
		return this;
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
	public void clear() {
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
					if(ItemStack.areItemsEqual(stack, fstack) && ItemStack.areNbtEqual(stack, fstack)) {
						return true;
					}
					return false;
				}
				if(ItemStack.areItemsEqual(stack, fstack) && ItemStack.areNbtEqual(stack, fstack)) {
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
		return filter == null ? call(InventoryWrapper::getMaxCountPerStack, i -> true, 0) : globalCountLimit;
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
					if(fstack.isEmpty() || (ItemStack.areItemsEqual(itemStack, fstack) && ItemStack.areNbtEqual(itemStack, fstack))) {
						f += itemStack.getCount() / Math.min((ignoreCount || fstack.isEmpty() ? globalCountLimit : fstack.getCount()), itemStack.getMaxCount());
						i++;
					}
				}
			}

			f /= inventory.size();
			return MathHelper.floor(f * 14.0F) + ((i > 0) ? 1 : 0);
		}, i -> true, 0);
	}

	public static Inventory getInventoryAt(World world, BlockPos blockPos) {
		Inventory inventory = null;
		BlockState blockState = world.getBlockState(blockPos);
		Block block = blockState.getBlock();
		if (block instanceof InventoryProvider) {
			inventory = ((InventoryProvider) block).getInventory(blockState, world, blockPos);
		} else if (blockState.hasBlockEntity()) {
			BlockEntity blockEntity = world.getBlockEntity(blockPos);
			if (blockEntity instanceof Inventory) {
				inventory = (Inventory) blockEntity;
				if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
					inventory = ChestBlock.getInventory((ChestBlock) block, blockState, world, blockPos, true);
				}
			}
		}

		return inventory;
	}
}
