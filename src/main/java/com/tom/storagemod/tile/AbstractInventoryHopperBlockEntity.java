package com.tom.storagemod.tile;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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
		if(!level.isClientSide && level.getGameTime() % 20 == 1) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(InventoryCableConnectorBlock.FACING);
			Stack<BlockPos> toCheck = new Stack<>();
			Set<BlockPos> checkedBlocks = new HashSet<>();
			checkedBlocks.add(worldPosition);
			BlockPos up = worldPosition.relative(facing.getOpposite());
			BlockPos down = worldPosition.relative(facing);
			state = level.getBlockState(up);
			if(state.getBlock() instanceof IInventoryCable) {
				top = null;
				topNet = true;
				toCheck.add(up);
				while(!toCheck.isEmpty()) {
					BlockPos cp = toCheck.pop();
					if(!checkedBlocks.contains(cp)) {
						checkedBlocks.add(cp);
						if(level.isLoaded(cp)) {
							state = level.getBlockState(cp);
							if(state.getBlock() == StorageMod.connector) {
								BlockEntity te = level.getBlockEntity(cp);
								if(te instanceof InventoryConnectorBlockEntity) {
									top = ((InventoryConnectorBlockEntity) te).getInventory();
								}
								break;
							}
							if(state.getBlock() instanceof IInventoryCable) {
								toCheck.addAll(((IInventoryCable)state.getBlock()).next(level, state, cp));
							}
						}
						if(checkedBlocks.size() > StorageMod.CONFIG.invConnectorMaxCables)break;
					}
				}
			} else {
				topNet = false;
				top = ItemStorage.SIDED.find(level, up, facing);
				if(top == null) {
					Container inv = HopperBlockEntity.getContainerAt(level, up);
					if(inv != null)top = InventoryStorage.of(inv, facing);
				}
			}
			state = level.getBlockState(down);
			if(state.getBlock() instanceof IInventoryCable) {
				toCheck.add(down);
				bottom = null;
				bottomNet = true;
				while(!toCheck.isEmpty()) {
					BlockPos cp = toCheck.pop();
					if(!checkedBlocks.contains(cp)) {
						checkedBlocks.add(cp);
						if(level.isLoaded(cp)) {
							state = level.getBlockState(cp);
							if(state.getBlock() == StorageMod.connector) {
								BlockEntity te = level.getBlockEntity(cp);
								if(te instanceof InventoryConnectorBlockEntity) {
									bottom = ((InventoryConnectorBlockEntity) te).getInventory();
								}
								break;
							}
							if(state.getBlock() instanceof IInventoryCable) {
								toCheck.addAll(((IInventoryCable)state.getBlock()).next(level, state, cp));
							}
						}
						if(checkedBlocks.size() > StorageMod.CONFIG.invConnectorMaxCables)break;
					}
				}
			} else {
				bottomNet = false;
				bottom = ItemStorage.SIDED.find(level, down, facing.getOpposite());
				if(bottom == null) {
					Container inv = HopperBlockEntity.getContainerAt(level, down);
					if(inv != null)bottom = InventoryStorage.of(inv, facing.getOpposite());
				}
			}
		}
		if(!level.isClientSide && (topNet || bottomNet) && top != null && bottom != null) {
			update();
		}
	}

	protected abstract void update();
}
