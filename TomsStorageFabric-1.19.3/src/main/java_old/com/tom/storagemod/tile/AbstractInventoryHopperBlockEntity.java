package com.tom.storagemod.tile;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.TickerUtil.TickableServer;
import com.tom.storagemod.block.IInventoryCable;
import com.tom.storagemod.block.InventoryCableConnectorBlock;

public abstract class AbstractInventoryHopperBlockEntity extends BlockEntity implements TickableServer {
	protected boolean topNet, bottomNet;
	protected Storage<ItemVariant> top;
	protected Storage<ItemVariant> bottom;
	public AbstractInventoryHopperBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public void updateServer() {
		if(!world.isClient && world.getTime() % 20 == 1) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(InventoryCableConnectorBlock.FACING);
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
						if(world.canSetBlock(cp)) {
							state = world.getBlockState(cp);
							if(state.getBlock() == StorageMod.connector) {
								BlockEntity te = world.getBlockEntity(cp);
								if(te instanceof InventoryConnectorBlockEntity) {
									top = ((InventoryConnectorBlockEntity) te).getInventory();
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
				top = ItemStorage.SIDED.find(world, up, facing);
				if(top == null) {
					Inventory inv = HopperBlockEntity.getInventoryAt(world, up);
					if(inv != null)top = InventoryStorage.of(inv, facing);
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
						if(world.canSetBlock(cp)) {
							state = world.getBlockState(cp);
							if(state.getBlock() == StorageMod.connector) {
								BlockEntity te = world.getBlockEntity(cp);
								if(te instanceof InventoryConnectorBlockEntity) {
									bottom = ((InventoryConnectorBlockEntity) te).getInventory();
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
				bottom = ItemStorage.SIDED.find(world, down, facing.getOpposite());
				if(bottom == null) {
					Inventory inv = HopperBlockEntity.getInventoryAt(world, down);
					if(inv != null)bottom = InventoryStorage.of(inv, facing.getOpposite());
				}
			}
		}
		if(!world.isClient && (topNet || bottomNet) && top != null && bottom != null) {
			update();
		}
	}

	protected abstract void update();
}
