package com.tom.storagemod.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import com.tom.storagemod.Config;
import com.tom.storagemod.Content;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.ITrim;
import com.tom.storagemod.util.IProxy;
import com.tom.storagemod.util.InfoHandler;
import com.tom.storagemod.util.MultiItemHandler;
import com.tom.storagemod.util.Priority;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public class InventoryConnectorBlockEntity extends BlockEntity implements TickableServer {
	private MultiItemHandler handlers = new MultiItemHandler();
	private List<LinkedInv> linkedInvs = new ArrayList<>();
	private LazyOptional<IItemHandler> invHandler = LazyOptional.of(() -> handlers);

	public InventoryConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(Content.connectorTile.get(), pos, state);
	}

	@Override
	public void updateServer() {
		long time = level.getGameTime();
		if(time % 20 == 0) {
			Stack<BlockPos> toCheck = new Stack<>();
			Set<BlockPos> checkedBlocks = new HashSet<>();
			toCheck.add(worldPosition);
			checkedBlocks.add(worldPosition);
			handlers.clear();
			Set<LinkedInv> toRM = new HashSet<>();
			Collections.sort(linkedInvs);
			for (LinkedInv inv : linkedInvs) {
				if(inv.time + 40 < time) {
					toRM.add(inv);
					continue;
				}
				LazyOptional<IItemHandler> i = inv.handler.get();
				if(i.isPresent()) {
					IItemHandler blockHandler = i.orElse(null);
					IItemHandler ihr = IProxy.resolve(blockHandler);
					if(ihr instanceof MultiItemHandler) {
						MultiItemHandler ih = (MultiItemHandler) ihr;
						if(checkHandlers(ih, 0)) {
							if(!handlers.contains(InfoHandler.INSTANCE))handlers.add(InfoHandler.INSTANCE);
							continue;
						}
					}
					handlers.add(i);
				}
			}
			linkedInvs.removeAll(toRM);
			while(!toCheck.isEmpty()) {
				BlockPos cp = toCheck.pop();
				for (Direction d : Direction.values()) {
					BlockPos p = cp.relative(d);
					if(!checkedBlocks.contains(p) && p.distSqr(worldPosition) < Config.get().invRange) {
						checkedBlocks.add(p);
						BlockState state = level.getBlockState(p);
						if(state.getBlock() instanceof ITrim) {
							toCheck.add(p);
						} else {
							BlockEntity te = level.getBlockEntity(p);
							if (te instanceof InventoryConnectorBlockEntity || te instanceof InventoryProxyBlockEntity || te instanceof AbstractInventoryCableConnectorBlockEntity) {
								continue;
							} else if(te != null && !Config.get().onlyTrims) {
								LazyOptional<IItemHandler> inv = te.getCapability(ForgeCapabilities.ITEM_HANDLER, d.getOpposite());
								if(te instanceof ChestBlockEntity) {//Check for double chests
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
									IItemHandler blockHandler = inv.orElse(null);
									if(blockHandler == null) {
										StorageMod.LOGGER.warn("Broken modded block at " + p + " in " + level.dimension().location().toString() + " block id: " + ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString());
									}
									IItemHandler ihr = IProxy.resolve(blockHandler);
									if(ihr instanceof MultiItemHandler) {
										MultiItemHandler ih = (MultiItemHandler) ihr;
										if(checkHandlers(ih, 0)) {
											if(!handlers.contains(InfoHandler.INSTANCE))handlers.add(InfoHandler.INSTANCE);
											continue;
										}
									}
									toCheck.add(p);
									handlers.add(inv);
								}
								if(Config.get().multiblockInvs.contains(state.getBlock())) {
									skipBlocks(p, checkedBlocks, toCheck, state.getBlock());
								}
							}
						}
					}
				}
			}
			handlers.refresh();
		}
	}

	private void skipBlocks(BlockPos pos, Set<BlockPos> checkedBlocks, Stack<BlockPos> edges, Block block) {
		Stack<BlockPos> toCheck = new Stack<>();
		toCheck.add(pos);
		edges.add(pos);
		while(!toCheck.isEmpty()) {
			BlockPos cp = toCheck.pop();
			for (Direction d : Direction.values()) {
				BlockPos p = cp.relative(d);
				if(!checkedBlocks.contains(p) && p.distSqr(worldPosition) < Config.get().invRange) {
					BlockState state = level.getBlockState(p);
					if(state.getBlock() == block) {
						checkedBlocks.add(p);
						edges.add(p);
						toCheck.add(p);
					}
				}
			}
		}
	}

	private boolean checkHandlers(MultiItemHandler ih, int depth) {
		if(depth > 3)return true;
		for (LazyOptional<IItemHandler> lo : ih.getHandlers()) {
			IItemHandler ihr = IProxy.resolve(lo.orElse(null));
			if(ihr == handlers)return true;
			if(ihr instanceof MultiItemHandler) {
				if(checkHandlers((MultiItemHandler) ihr, depth+1))return true;
			}
		}
		return false;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (!this.remove && cap == ForgeCapabilities.ITEM_HANDLER) {
			return getInventory().cast();
		}
		return super.getCapability(cap, side);
	}

	public LazyOptional<IItemHandler> getInventory() {
		return this.invHandler;
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
		public Priority priority = Priority.NORMAL;

		@Override
		public int compareTo(LinkedInv o) {
			return -priority.compareTo(o.priority);
		}
	}

	public void unLink(LinkedInv linv) {
		linkedInvs.remove(linv);
	}

	private int getFreeSlotCount() {
		return getInventory().lazyMap(inv -> {
			int empty = 0;
			for(int i = 0;i<handlers.getSlots();i++) {
				if(inv.getStackInSlot(i).isEmpty())empty++;
			}
			return empty;
		}).orElse(0);
	}

	public Tuple<Integer, Integer> getUsage() {
		return new Tuple<>(handlers.getSlots(), getFreeSlotCount());
	}
}
