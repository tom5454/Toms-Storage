package com.tom.storagemod.tile;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tickable;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.BlockInventoryCableConnector;
import com.tom.storagemod.block.IInventoryCable;
import com.tom.storagemod.tile.TileEntityInventoryConnector.LinkedInv;
import com.tom.storagemod.util.IProxy;
import com.tom.storagemod.util.InventoryWrapper;

public class TileEntityInventoryCableConnectorBase extends BlockEntity implements Tickable, Inventory, IProxy {
	protected TileEntityInventoryConnector master;
	protected InventoryWrapper pointedAt, masterW;
	protected LinkedInv linv;

	public TileEntityInventoryCableConnectorBase(BlockEntityType<?> type) {
		super(type);
	}

	@Override
	public void tick() {
		if(!world.isClient && world.getTime() % 20 == 19) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(BlockInventoryCableConnector.FACING);
			Stack<BlockPos> toCheck = new Stack<>();
			Set<BlockPos> checkedBlocks = new HashSet<>();
			checkedBlocks.add(pos);
			toCheck.addAll(((IInventoryCable)state.getBlock()).next(world, state, pos));
			if(master != null)master.unLink(linv);
			master = null;
			linv = new LinkedInv();
			masterW = null;
			while(!toCheck.isEmpty()) {
				BlockPos cp = toCheck.pop();
				if(!checkedBlocks.contains(cp)) {
					checkedBlocks.add(cp);
					if(world.isChunkLoaded(cp)) {
						state = world.getBlockState(cp);
						if(state.getBlock() == StorageMod.connector) {
							BlockEntity te = world.getBlockEntity(cp);
							if(te instanceof TileEntityInventoryConnector) {
								master = (TileEntityInventoryConnector) te;
								linv.time = world.getTime();
								linv.handler = this::applyFilter;
								master.addLinked(linv);
								masterW = new InventoryWrapper(master, facing);
							}
							break;
						}
						if(state.getBlock() instanceof IInventoryCable) {
							toCheck.addAll(((IInventoryCable)state.getBlock()).next(world, state, cp));
						}
					}
					if(checkedBlocks.size() > StorageMod.CONFIG.invConnectorMaxCables)break;
				}
			}
			BlockPos p = pos.offset(facing);
			Inventory inv = HopperBlockEntity.getInventoryAt(world, p);
			if(inv != null) {
				pointedAt = new InventoryWrapper(inv, facing.getOpposite());
			} else {
				pointedAt = null;
			}
		}
	}

	protected InventoryWrapper applyFilter() {
		return pointedAt;
	}

	private boolean calling;
	public <R> R call(Function<InventoryWrapper, R> func, Predicate<InventoryWrapper> accessCheck, R def) {
		if(calling)return def;
		calling = true;
		if(masterW != null && accessCheck.test(masterW)) {
			R r = func.apply(masterW);
			calling = false;
			return r;
		}
		calling = false;
		return def;
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
	public void clear() {
	}

	@Override
	public ItemStack getStack(int paramInt) {
		return call(i -> i.getStack(paramInt), i -> true, ItemStack.EMPTY);
	}

	@Override
	public ItemStack removeStack(int paramInt1, int paramInt2) {
		return call(i -> i.removeStack(paramInt1, paramInt2), i -> true, ItemStack.EMPTY);
	}

	@Override
	public ItemStack removeStack(int paramInt) {
		return call(i -> i.removeStack(paramInt), i -> true, ItemStack.EMPTY);
	}

	@Override
	public void setStack(int paramInt, ItemStack paramItemStack) {
		call(i -> {
			i.setStack(paramInt, paramItemStack);
			return Unit.INSTANCE;
		}, i -> true, Unit.INSTANCE);
	}

	@Override
	public boolean canPlayerUse(PlayerEntity paramPlayerEntity) {
		return false;
	}

	@Override
	public Inventory get() {
		return masterW != null ? masterW.getInventory() : null;
	}

	@Override
	public boolean isValid(int slot, ItemStack stack) {
		return call(i -> i.isValid(slot, stack, false), i -> true, false);
	}
}
