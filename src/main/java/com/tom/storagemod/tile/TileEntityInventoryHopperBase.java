package com.tom.storagemod.tile;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.BlockInventoryCableConnector;
import com.tom.storagemod.block.IInventoryCable;

public abstract class TileEntityInventoryHopperBase extends BlockEntity implements Tickable {
	protected boolean topNet, bottomNet;
	protected InventoryWrapper top;
	protected InventoryWrapper bottom;
	public TileEntityInventoryHopperBase(BlockEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void tick() {
		if(!world.isClient && world.getTime() % 20 == 1) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(BlockInventoryCableConnector.FACING);
			Stack<BlockPos> toCheck = new Stack<>();
			Set<BlockPos> checkedBlocks = new HashSet<>();
			checkedBlocks.add(pos);
			BlockPos up = pos.offset(facing.getOpposite());
			BlockPos down = pos.offset(facing);
			state = world.getBlockState(up);
			if(state.getBlock() instanceof IInventoryCable) {
				top = null;
				topNet = true;
				toCheck.add(up);
				while(!toCheck.isEmpty()) {
					BlockPos cp = toCheck.pop();
					if(!checkedBlocks.contains(cp)) {
						checkedBlocks.add(cp);
						if(world.isChunkLoaded(cp)) {
							state = world.getBlockState(cp);
							if(state.getBlock() == StorageMod.connector) {
								BlockEntity te = world.getBlockEntity(cp);
								if(te instanceof TileEntityInventoryConnector) {
									top = ((TileEntityInventoryConnector) te).getInventory();
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
			} else {
				topNet = false;
				BlockEntity te = world.getBlockEntity(up);
				if(te instanceof Inventory) {
					top = new InventoryWrapper((Inventory) te, facing);
				} else {
					top = null;
				}
			}
			state = world.getBlockState(down);
			if(state.getBlock() instanceof IInventoryCable) {
				toCheck.add(down);
				bottom = null;
				bottomNet = true;
				while(!toCheck.isEmpty()) {
					BlockPos cp = toCheck.pop();
					if(!checkedBlocks.contains(cp)) {
						checkedBlocks.add(cp);
						if(world.isChunkLoaded(cp)) {
							state = world.getBlockState(cp);
							if(state.getBlock() == StorageMod.connector) {
								BlockEntity te = world.getBlockEntity(cp);
								if(te instanceof TileEntityInventoryConnector) {
									bottom = ((TileEntityInventoryConnector) te).getInventory();
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
			} else {
				bottomNet = false;
				BlockEntity te = world.getBlockEntity(down);
				if(te instanceof Inventory) {
					bottom = new InventoryWrapper((Inventory) te, facing.getOpposite());
				} else {
					bottom = null;
				}
			}
		}
		if(!world.isClient && (topNet || bottomNet) && top != null && bottom != null) {
			update();
		}
	}

	protected abstract void update();
}
