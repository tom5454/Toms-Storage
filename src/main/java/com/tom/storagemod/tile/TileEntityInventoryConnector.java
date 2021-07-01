package com.tom.storagemod.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.ITrim;

public class TileEntityInventoryConnector extends TileEntity implements ITickableTileEntity {
	private List<LazyOptional<IItemHandler>> handlers = new ArrayList<>();
	private List<LinkedInv> linkedInvs = new ArrayList<>();
	private LazyOptional<IItemHandler> invHandler;
	private int[] invSizes = new int[0];
	private int invSize;

	public TileEntityInventoryConnector() {
		super(StorageMod.connectorTile);
	}

	@Override
	public void tick() {
		long time = level.getGameTime();
		if(!level.isClientSide && time % 20 == 0) {
			Stack<BlockPos> toCheck = new Stack<>();
			Set<BlockPos> checkedBlocks = new HashSet<>();
			toCheck.add(worldPosition);
			checkedBlocks.add(worldPosition);
			handlers.clear();
			Set<LinkedInv> toRM = new HashSet<>();
			for (LinkedInv inv : linkedInvs) {
				if(inv.time + 40 < time) {
					toRM.add(inv);
					continue;
				}
				handlers.add(inv.handler.get());
			}
			linkedInvs.removeAll(toRM);
			Collections.sort(linkedInvs);
			while(!toCheck.isEmpty()) {
				BlockPos cp = toCheck.pop();
				for (Direction d : Direction.values()) {
					BlockPos p = cp.relative(d);
					if(!checkedBlocks.contains(p) && p.distSqr(worldPosition) < Config.invRange) {
						checkedBlocks.add(p);
						BlockState state = level.getBlockState(p);
						if(state.getBlock() instanceof ITrim) {
							toCheck.add(p);
						} else {
							TileEntity te = level.getBlockEntity(p);
							if (te instanceof TileEntityInventoryConnector || te instanceof TileEntityInventoryProxy || te instanceof TileEntityInventoryCableConnector) {
								continue;
							} else if(te != null && !Config.onlyTrims) {
								LazyOptional<IItemHandler> inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite());
								if(te instanceof ChestTileEntity) {//Check for double chests
									Block block = state.getBlock();
									if(block instanceof ChestBlock) {
										ChestType type = state.getValue(ChestBlock.TYPE);
										if (type != ChestType.SINGLE) {
											BlockPos opos = p.relative(ChestBlock.getConnectedDirection(state));
											BlockState ostate = this.getLevel().getBlockState(opos);
											if (state.getBlock() == ostate.getBlock()) {
												ChestType otype = ostate.getValue(ChestBlock.TYPE);
												if (otype != ChestType.SINGLE && type != otype && state.getValue(ChestBlock.FACING) == ostate.getValue(ChestBlock.FACING)) {
													toCheck.add(opos);
													checkedBlocks.add(opos);
												}
											}
										}
									}
								}
								if(inv.isPresent()) {
									IItemHandler ihr = IProxy.resolve(inv.orElse(null));
									if(ihr instanceof InvHandler) {
										InvHandler ih = (InvHandler) ihr;
										if(checkHandlers(ih, 0)) {
											if(!handlers.contains(InfoHandler.INSTANCE))handlers.add(InfoHandler.INSTANCE);
											continue;
										}
									}
									toCheck.add(p);
									handlers.add(inv);
								}
							}
						}
					}
				}
			}
			if(invSizes.length != handlers.size())invSizes = new int[handlers.size()];
			invSize = 0;
			for (int i = 0; i < invSizes.length; i++) {
				IItemHandler ih = handlers.get(i).orElse(null);
				if(ih == null)invSizes[i] = 0;
				else {
					int s = ih.getSlots();
					invSizes[i] = s;
					invSize += s;
				}
			}
		}
	}
	private boolean checkHandlers(InvHandler ih, int depth) {
		if(depth > 3)return true;
		for (LazyOptional<IItemHandler> lo : ih.getHandlers()) {
			IItemHandler ihr = IProxy.resolve(lo.orElse(null));
			if(ihr instanceof InvHandler) {
				if(checkHandlers((InvHandler) ihr, depth+1))return true;
			}
		}
		return false;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (!this.remove && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return getInventory().cast();
		}
		return super.getCapability(cap, side);
	}

	public LazyOptional<IItemHandler> getInventory() {
		if (this.invHandler == null)
			this.invHandler = LazyOptional.of(InvHandler::new);
		return this.invHandler;
	}

	private class InvHandler implements IItemHandler {
		private boolean calling;

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			if(calling)return false;
			calling = true;
			for (int i = 0; i < invSizes.length; i++) {
				if(slot >= invSizes[i])slot -= invSizes[i];
				else {
					boolean r = handlers.get(i).orElse(EmptyHandler.INSTANCE).isItemValid(slot, stack);
					calling = false;
					return r;
				}
			}
			calling = false;
			return false;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if(calling)return stack;
			calling = true;
			for (int i = 0; i < invSizes.length; i++) {
				if(slot >= invSizes[i])slot -= invSizes[i];
				else {
					ItemStack s = handlers.get(i).orElse(EmptyHandler.INSTANCE).insertItem(slot, stack, simulate);
					calling = false;
					return s;
				}
			}
			calling = false;
			return stack;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			if(calling)return ItemStack.EMPTY;
			calling = true;
			for (int i = 0; i < invSizes.length; i++) {
				if(slot >= invSizes[i])slot -= invSizes[i];
				else {
					ItemStack s = handlers.get(i).orElse(EmptyHandler.INSTANCE).getStackInSlot(slot);
					calling = false;
					return s;
				}
			}
			calling = false;
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlots() {
			return invSize;
		}

		@Override
		public int getSlotLimit(int slot) {
			if(calling)return 0;
			calling = true;
			for (int i = 0; i < invSizes.length; i++) {
				if(slot >= invSizes[i])slot -= invSizes[i];
				else {
					int r = handlers.get(i).orElse(EmptyHandler.INSTANCE).getSlotLimit(slot);
					calling = false;
					return r;
				}
			}
			calling = false;
			return 0;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if(calling)return ItemStack.EMPTY;
			calling = true;
			for (int i = 0; i < invSizes.length; i++) {
				if(slot >= invSizes[i])slot -= invSizes[i];
				else {
					ItemStack s = handlers.get(i).orElse(EmptyHandler.INSTANCE).extractItem(slot, amount, simulate);
					calling = false;
					return s;
				}
			}
			calling = false;
			return ItemStack.EMPTY;
		}

		public List<LazyOptional<IItemHandler>> getHandlers() {
			return handlers;
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (invHandler != null)
			invHandler.invalidate();
	}

	public void addLinked(LinkedInv inv) {
		linkedInvs.add(inv);
	}

	public static class LinkedInv implements Comparable<LinkedInv> {
		public Supplier<LazyOptional<IItemHandler>> handler;
		public long time;
		public int priority;

		@Override
		public int compareTo(LinkedInv o) {
			return Integer.compare(priority, o.priority);
		}
	}

	public void unLink(LinkedInv linv) {
		linkedInvs.remove(linv);
	}

	public int getFreeSlotCount() {
		return getInventory().lazyMap(inv -> {
			int empty = 0;
			for(int i = 0;i<invSize;i++) {
				if(inv.getStackInSlot(i).isEmpty())empty++;
			}
			return empty;
		}).orElse(0);
	}

	public int getInvSize() {
		return invSize;
	}
}
