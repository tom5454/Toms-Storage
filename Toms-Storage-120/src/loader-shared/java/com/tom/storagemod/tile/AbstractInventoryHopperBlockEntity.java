package com.tom.storagemod.tile;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import com.tom.storagemod.Config;
import com.tom.storagemod.Content;
import com.tom.storagemod.block.IInventoryCable;
import com.tom.storagemod.block.InventoryCableConnectorBlock;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public abstract class AbstractInventoryHopperBlockEntity extends BlockEntity implements TickableServer {
	protected boolean topNet, bottomNet;
	protected LazyOptional<IItemHandler> top;
	protected LazyOptional<IItemHandler> bottom;
	public AbstractInventoryHopperBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public void updateServer() {
		if(level.getGameTime() % 20 == 1) {
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
							if(state.getBlock() == Content.connector.get()) {
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
						if(checkedBlocks.size() > Config.get().invConnectorMax)break;
					}
				}
			} else {
				topNet = false;
				if(top == null || !top.isPresent()) {
					BlockEntity te = level.getBlockEntity(up);
					if(te != null) {
						top = te.getCapability(ForgeCapabilities.ITEM_HANDLER, facing);
					}
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
							if(state.getBlock() == Content.connector.get()) {
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
						if(checkedBlocks.size() > Config.get().invConnectorMax)break;
					}
				}
			} else {
				bottomNet = false;
				if(bottom == null || !bottom.isPresent()) {
					BlockEntity te = level.getBlockEntity(down);
					if(te != null) {
						bottom = te.getCapability(ForgeCapabilities.ITEM_HANDLER, facing.getOpposite());
					}
				}
			}
		}
		if(!level.isClientSide && (topNet || bottomNet) && top != null && top.isPresent() && bottom != null && bottom.isPresent()) {
			update();
		}
	}

	protected abstract void update();
}
