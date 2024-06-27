package com.tom.storagemod.inventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.block.IInventoryCable;
import com.tom.storagemod.util.BlockFace;
import com.tom.storagemod.util.WorldStates;

public class InventoryCableNetwork {
	private final Level level;
	private Map<BlockPos, CableCache> caches = new HashMap<>();

	public InventoryCableNetwork(Level level) {
		this.level = level;
	}

	public Collection<BlockPos> getNetworkNodes(BlockPos from) {
		CableCache cache = caches.get(from);
		if (cache != null)return cache.attached;
		Set<BlockPos> checked = new HashSet<>();
		Stack<BlockFace> next = new Stack<>();
		Set<BlockPos> cables = new HashSet<>();
		Set<BlockPos> attached = new HashSet<>();
		next.add(new BlockFace(from, Direction.DOWN));
		while (!next.isEmpty()) {
			BlockFace p = next.pop();
			if (checked.contains(p.pos()))continue;
			checked.add(p.pos());
			if (!level.isLoaded(p.pos()))continue;
			BlockState st = level.getBlockState(p.pos());
			if (st.getBlock() instanceof IInventoryCable c && c.canConnectFrom(st, p.from())) {
				CableCache cc = caches.get(p.pos());
				if (cc != null) {
					cables.addAll(cc.cables);
					attached.addAll(cc.attached);
					checked.addAll(cc.cables);
					continue;
				}
				if (c.isFunctionalNode())attached.add(p.pos());
				else cables.add(p.pos());
				next.addAll(c.nextScan(level, st, p.pos()));
			}
		}
		CableCache cc = new CableCache(cables, attached);
		cables.forEach(p -> caches.put(p, cc));
		return cc.attached;
	}

	public void markNodeInvalid(BlockPos pos) {
		CableCache cc = caches.get(pos);
		if (cc != null)cc.cables.forEach(caches::remove);
	}

	public void markNodeInvalid(BlockFace pos) {
		markNodeInvalid(pos.pos());
	}

	public static InventoryCableNetwork getNetwork(Level level) {
		return WorldStates.cableNetworks.computeIfAbsent(level, InventoryCableNetwork::new);
	}

	private static record CableCache(Set<BlockPos> cables, Set<BlockPos> attached) {}
}
