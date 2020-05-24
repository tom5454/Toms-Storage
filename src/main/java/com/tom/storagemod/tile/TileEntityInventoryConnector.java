package com.tom.storagemod.tile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

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
	private LazyOptional<IItemHandler> invHandler;
	private int[] invSizes = new int[0];
	private int invSize;

	public TileEntityInventoryConnector() {
		super(StorageMod.connectorTile);
	}

	@Override
	public void tick() {
		if(!world.isRemote && world.getGameTime() % 20 == 0) {
			Stack<BlockPos> toCheck = new Stack<>();
			Set<BlockPos> checkedBlocks = new HashSet<>();
			//Set<List<ItemStack>> equalCheck = new HashSet<>();
			toCheck.add(pos);
			checkedBlocks.add(pos);
			handlers.clear();
			//System.out.println("Start checking invs");
			while(!toCheck.isEmpty()) {
				BlockPos cp = toCheck.pop();
				for (Direction d : Direction.values()) {
					BlockPos p = cp.offset(d);
					if(!checkedBlocks.contains(p) && p.distanceSq(pos) < Config.invRange) {
						checkedBlocks.add(p);
						TileEntity te = world.getTileEntity(p);
						if (te instanceof TileEntityInventoryConnector || te instanceof TileEntityInventoryProxy) {
							continue;
						} else if(te != null && !Config.onlyTrims) {
							LazyOptional<IItemHandler> inv = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite());
							if(te instanceof ChestTileEntity) {//Check for double chests
								BlockState state = world.getBlockState(p);
								Block block = state.getBlock();
								if(block instanceof ChestBlock) {
									ChestType type = state.get(ChestBlock.TYPE);
									if (type != ChestType.SINGLE) {
										BlockPos opos = p.offset(ChestBlock.getDirectionToAttached(state));
										BlockState ostate = this.getWorld().getBlockState(opos);
										if (state.getBlock() == ostate.getBlock()) {
											ChestType otype = ostate.get(ChestBlock.TYPE);
											if (otype != ChestType.SINGLE && type != otype && state.get(ChestBlock.FACING) == ostate.get(ChestBlock.FACING)) {
												toCheck.add(opos);
												checkedBlocks.add(opos);
											}
										}
									}
								}
							}
							if(inv.isPresent()) {
								//System.out.println("Checking pos: " + p + " " + inv.orElse(null));
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
						} else {
							BlockState state = world.getBlockState(p);
							if(state.getBlock() instanceof ITrim) {
								toCheck.add(p);
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
		if (!this.removed && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (this.invHandler == null)
				this.invHandler = LazyOptional.of(InvHandler::new);
			return this.invHandler.cast();
		}
		return super.getCapability(cap, side);
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

		public TileEntityInventoryConnector getThis() {
			return TileEntityInventoryConnector.this;
		}

		public List<LazyOptional<IItemHandler>> getHandlers() {
			return handlers;
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (invHandler != null)
			invHandler.invalidate();
	}
}
