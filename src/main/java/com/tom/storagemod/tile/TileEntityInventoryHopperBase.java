package com.tom.storagemod.tile;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.TickerUtil.TickableServer;
import com.tom.storagemod.block.BlockInventoryCableConnector;
import com.tom.storagemod.block.IInventoryCable;
import com.tom.storagemod.util.InventoryWrapper;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class TileEntityInventoryHopperBase extends BlockEntity implements TickableServer {
	protected boolean topNet, bottomNet;
	protected InventoryWrapper top;
	protected InventoryWrapper bottom;

	public TileEntityInventoryHopperBase(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public void updateServer() {
		if (!world.isClient && world.getTime() % 20 == 1) {
			Stack<BlockPos> inventoryCableStack = new Stack<>();
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(BlockInventoryCableConnector.FACING);
			Stack<BlockPos> toCheck = new Stack<>();
			Set<BlockPos> checkedBlocks = new HashSet<>();
			checkedBlocks.add(pos);
			BlockPos up = pos.offset(facing.getOpposite());
			BlockPos down = pos.offset(facing);
			state = world.getBlockState(up);
			if (state.getBlock() instanceof IInventoryCable) {
				top = null;
				topNet = true;
				toCheck.add(up);
				while (!toCheck.isEmpty()) {
					BlockPos cp = toCheck.pop();
					// Try find connector from cache
					BlockPos connectorBlockPos = CablePathCache.tryGet(cp);
					if (connectorBlockPos != null) {
						BlockEntity te = world.getBlockEntity(connectorBlockPos);
						if (te instanceof TileEntityInventoryConnector) {
							top = ((TileEntityInventoryConnector) te).getInventory();
							toCheck.clear();
							break;
						}
					}

					if (!checkedBlocks.contains(cp)) {
						checkedBlocks.add(cp);
						if (world.canSetBlock(cp)) {
							state = world.getBlockState(cp);
							if (state.getBlock() == StorageMod.connector) {
								BlockEntity te = world.getBlockEntity(cp);
								if (te instanceof TileEntityInventoryConnector) {
									top = ((TileEntityInventoryConnector) te).getInventory();
									for (BlockPos pos : inventoryCableStack) {
										CablePathCache.Put(pos, cp);
									}
								}
								break;
							}
							if (state.getBlock() instanceof IInventoryCable) {
								inventoryCableStack.add(cp);
								toCheck.addAll(((IInventoryCable) state.getBlock()).next(world, state, cp));
							}
						}
						if (checkedBlocks.size() > StorageMod.CONFIG.invConnectorMaxCables)
							break;
					}
				}
			} else {
				topNet = false;
				Inventory inv = HopperBlockEntity.getInventoryAt(world, up);
				if (inv != null) {
					top = new InventoryWrapper(inv, facing);
				} else {
					top = null;
				}
			}
			state = world.getBlockState(down);
			if (state.getBlock() instanceof IInventoryCable) {
				toCheck.add(down);
				bottom = null;
				bottomNet = true;
				while (!toCheck.isEmpty()) {
					BlockPos cp = toCheck.pop();
					BlockPos connecorBlockPos = CablePathCache.tryGet(cp);
					if (connecorBlockPos != null) {
						BlockEntity te = world.getBlockEntity(connecorBlockPos);
						if (te instanceof TileEntityInventoryConnector) {
							bottom = ((TileEntityInventoryConnector) te).getInventory();
							toCheck.clear();
							break;
						}
					}
					if (!checkedBlocks.contains(cp)) {
						checkedBlocks.add(cp);
						if (world.canSetBlock(cp)) {
							state = world.getBlockState(cp);
							if (state.getBlock() == StorageMod.connector) {
								BlockEntity te = world.getBlockEntity(cp);
								if (te instanceof TileEntityInventoryConnector) {
									bottom = ((TileEntityInventoryConnector) te).getInventory();
									for (BlockPos pos : inventoryCableStack) {
										CablePathCache.Put(pos, cp);
									}
								}
								break;
							}
							if (state.getBlock() instanceof IInventoryCable) {
								inventoryCableStack.add(cp);
								toCheck.addAll(((IInventoryCable) state.getBlock()).next(world, state, cp));
							}
						}
						if (checkedBlocks.size() > StorageMod.CONFIG.invConnectorMaxCables)
							break;
					}
				}
			} else {
				bottomNet = false;
				Inventory inv = HopperBlockEntity.getInventoryAt(world, down);
				if (inv != null) {
					bottom = new InventoryWrapper(inv, facing.getOpposite());
				} else {
					bottom = null;
				}
			}
		}
		if (!world.isClient && (topNet || bottomNet) && top != null && bottom != null) {
			update();
		}
	}

	protected abstract void update();
}
