package com.tom.storagemod.tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import com.tom.storagemod.Config;
import com.tom.storagemod.Content;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.ITrim;
import com.tom.storagemod.util.IProxy;
import com.tom.storagemod.util.InfoHandler;
import com.tom.storagemod.util.MergedStorage;
import com.tom.storagemod.util.Priority;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public class InventoryConnectorBlockEntity extends BlockEntity implements TickableServer, SidedStorageBlockEntity {
	private MergedStorage handlers = new MergedStorage();
	private List<LinkedInv> linkedInvs = new ArrayList<>();

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
			for (LinkedInv inv : linkedInvs) {
				if(inv.time + 40 < time) {
					toRM.add(inv);
					continue;
				}
				Storage<ItemVariant> w = inv.handler.get();
				if(w != null) {
					Storage<ItemVariant> ihr = IProxy.resolve(w);
					if(ihr instanceof MergedStorage ih) {
						if(checkHandlers(ih, 0)) {
							if(!handlers.getStorages().contains(InfoHandler.INSTANCE))handlers.add(InfoHandler.INSTANCE);
							continue;
						}
					}
					handlers.add(w);
				}
			}
			linkedInvs.removeAll(toRM);
			Collections.sort(linkedInvs);
			int range = StorageMod.CONFIG.invRange * StorageMod.CONFIG.invRange;
			while(!toCheck.isEmpty()) {
				BlockPos cp = toCheck.pop();
				for (Direction d : Direction.values()) {
					BlockPos p = cp.relative(d);
					if(!checkedBlocks.contains(p) && p.distSqr(worldPosition) < range) {
						checkedBlocks.add(p);
						BlockState state = level.getBlockState(p);
						if(state.getBlock() instanceof ITrim) {
							toCheck.add(p);
						} else {
							BlockEntity te = level.getBlockEntity(p);
							if (te instanceof InventoryConnectorBlockEntity || te instanceof InventoryProxyBlockEntity || te instanceof AbstractInventoryCableConnectorBlockEntity) {
								continue;
							} else if(te != null && !StorageMod.CONFIG.onlyTrims) {
								Storage<ItemVariant> inv = ItemStorage.SIDED.find(level, p, state, te, d.getOpposite());
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
								if(inv != null) {
									Storage<ItemVariant> ihr = IProxy.resolve(inv);
									if(ihr instanceof MergedStorage ih) {
										if(checkHandlers(ih, 0)) {
											if(!handlers.getStorages().contains(InfoHandler.INSTANCE))handlers.add(InfoHandler.INSTANCE);
											continue;
										}
									}
									toCheck.add(p);
									handlers.add(inv);
								}

								if(Config.getMultiblockInvs().contains(state.getBlock())) {
									skipBlocks(p, checkedBlocks, toCheck, state.getBlock());
								}
							}
						}
					}
				}
			}
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
				if(!checkedBlocks.contains(p) && p.distSqr(pos) < StorageMod.CONFIG.invRange) {
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

	private boolean checkHandlers(MergedStorage ih, int depth) {
		if(depth > 3 || ih == handlers)return true;
		for (Storage<ItemVariant> lo : ih.getStorages()) {
			Storage<ItemVariant> ihr = IProxy.resolve(lo);
			if(ihr == handlers)return true;
			if(ihr instanceof MergedStorage i) {
				if(checkHandlers(i, depth+1))return true;
			}
		}
		return false;
	}

	public void addLinked(LinkedInv inv) {
		linkedInvs.add(inv);
	}

	public static class LinkedInv implements Comparable<LinkedInv> {
		public Supplier<Storage<ItemVariant>> handler;
		public long time;
		public Priority priority = Priority.NORMAL;

		@Override
		public int compareTo(LinkedInv o) {
			return priority.compareTo(o.priority);
		}
	}

	public void unLink(LinkedInv linv) {
		linkedInvs.remove(linv);
	}

	public Storage<ItemVariant> getInventory() {
		return handlers;
	}

	public Tuple<Integer, Integer> getUsage() {
		int slots = 0;
		int free = 0;
		for (StorageView<ItemVariant> view : handlers) {
			slots++;
			if(view.isResourceBlank())free++;
		}
		return new Tuple<>(slots, free);
	}

	@Override
	public @Nullable Storage<ItemVariant> getItemStorage(Direction side) {
		return handlers;
	}
}
