package com.tom.storagemod.tile;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.BlockInventoryCableConnector;
import com.tom.storagemod.block.IInventoryCable;

public abstract class TileEntityInventoryHopperBase extends TileEntity implements ITickableTileEntity {
	protected boolean topNet, bottomNet;
	protected LazyOptional<IItemHandler> top;
	protected LazyOptional<IItemHandler> bottom;
	public TileEntityInventoryHopperBase(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public void tick() {
		if(!world.isRemote && world.getGameTime() % 20 == 1) {
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
						if(world.isBlockLoaded(cp)) {
							state = world.getBlockState(cp);
							if(state.getBlock() == StorageMod.connector) {
								TileEntity te = world.getTileEntity(cp);
								if(te instanceof TileEntityInventoryConnector) {
									top = ((TileEntityInventoryConnector) te).getInventory();
								}
								break;
							}
							if(state.getBlock() instanceof IInventoryCable) {
								toCheck.addAll(((IInventoryCable)state.getBlock()).next(world, state, cp));
							}
						}
						if(checkedBlocks.size() > Config.invConnectorMax)break;
					}
				}
			} else {
				topNet = false;
				if(top == null || !top.isPresent()) {
					TileEntity te = world.getTileEntity(up);
					if(te != null) {
						top = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
					}
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
						if(world.isBlockLoaded(cp)) {
							state = world.getBlockState(cp);
							if(state.getBlock() == StorageMod.connector) {
								TileEntity te = world.getTileEntity(cp);
								if(te instanceof TileEntityInventoryConnector) {
									bottom = ((TileEntityInventoryConnector) te).getInventory();
								}
								break;
							}
							if(state.getBlock() instanceof IInventoryCable) {
								toCheck.addAll(((IInventoryCable)state.getBlock()).next(world, state, cp));
							}
						}
						if(checkedBlocks.size() > Config.invConnectorMax)break;
					}
				}
			} else {
				bottomNet = false;
				if(bottom == null || !bottom.isPresent()) {
					TileEntity te = world.getTileEntity(down);
					if(te != null) {
						bottom = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
					}
				}
			}
		}
		if(!world.isRemote && (topNet || bottomNet) && top != null && top.isPresent() && bottom != null && bottom.isPresent()) {
			update();
		}
	}

	protected abstract void update();
}
