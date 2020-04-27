package com.tom.storagemod.block;

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

import com.tom.storagemod.StorageMod;

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
		if(world.getGameTime() % 20 == 0) {
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
					if(!checkedBlocks.contains(p) && p.distanceSq(pos) < 256) {
						checkedBlocks.add(p);
						TileEntity te = world.getTileEntity(p);
						if (te instanceof TileEntityInventoryConnector) {
							continue;
						} else if(te != null) {
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
								toCheck.add(p);
								handlers.add(inv);
							}
						} else {
							BlockState state = world.getBlockState(p);
							if(state.getBlock() == StorageMod.inventoryTrim) {
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

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (!this.removed && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (this.invHandler == null)
				this.invHandler = LazyOptional.of(this::createHandler);
			return this.invHandler.cast();
		}
		return super.getCapability(cap, side);
	}

	private IItemHandler createHandler() {
		return new IItemHandler() {

			@Override
			public boolean isItemValid(int slot, ItemStack stack) {
				for (int i = 0; i < invSizes.length; i++) {
					if(slot >= invSizes[i])slot -= invSizes[i];
					else return handlers.get(i).orElse(EmptyHandler.INSTANCE).isItemValid(slot, stack);
				}
				return false;
			}

			@Override
			public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
				for (int i = 0; i < invSizes.length; i++) {
					if(slot >= invSizes[i])slot -= invSizes[i];
					else return handlers.get(i).orElse(EmptyHandler.INSTANCE).insertItem(slot, stack, simulate);
				}
				return stack;
			}

			@Override
			public ItemStack getStackInSlot(int slot) {
				for (int i = 0; i < invSizes.length; i++) {
					if(slot >= invSizes[i])slot -= invSizes[i];
					else return handlers.get(i).orElse(EmptyHandler.INSTANCE).getStackInSlot(slot);
				}
				return ItemStack.EMPTY;
			}

			@Override
			public int getSlots() {
				return invSize;
			}

			@Override
			public int getSlotLimit(int slot) {
				for (int i = 0; i < invSizes.length; i++) {
					if(slot >= invSizes[i])slot -= invSizes[i];
					else return handlers.get(i).orElse(EmptyHandler.INSTANCE).getSlotLimit(slot);
				}
				return 0;
			}

			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				for (int i = 0; i < invSizes.length; i++) {
					if(slot >= invSizes[i])slot -= invSizes[i];
					else return handlers.get(i).orElse(EmptyHandler.INSTANCE).extractItem(slot, amount, simulate);
				}
				return ItemStack.EMPTY;
			}
		};
	}

	@Override
	public void remove() {
		super.remove();
		if (invHandler != null)
			invHandler.invalidate();
	}
}
