package com.tom.storagemod.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.block.IInventoryCable;
import com.tom.storagemod.util.WorldStates;

public class InventoryCableNetwork {
	private final Level level;
	private Map<BlockPos, CableCache> caches = new HashMap<>();

	public InventoryCableNetwork(Level level) {
		this.level = level;
	}

	public List<BlockPos> getNetworkNodes(BlockPos from) {
		CableCache cache = caches.get(from);
		if (cache != null)return cache.attached;
		Set<BlockPos> checked = new HashSet<>();
		Stack<BlockPos> next = new Stack<>();
		Set<BlockPos> cables = new HashSet<>();
		List<BlockPos> attached = new ArrayList<>();
		next.add(from);
		while (!next.isEmpty()) {
			BlockPos p = next.pop();
			if (checked.contains(p))continue;
			checked.add(p);
			CableCache cc = caches.get(p);
			if (cc != null) {
				cables.addAll(cc.cables);
				attached.addAll(cc.attached);
				checked.addAll(cc.cables);
				continue;
			}
			if (!level.isLoaded(p))continue;
			BlockState st = level.getBlockState(p);
			if (st.getBlock() instanceof IInventoryCable c) {
				if (c.isFunctionalNode())attached.add(p);
				else cables.add(p);
				next.addAll(c.nextScan(level, st, p));
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

	public static InventoryCableNetwork getNetwork(Level level) {
		return WorldStates.cableNetworks.computeIfAbsent(level, InventoryCableNetwork::new);
	}

	private static record CableCache(Set<BlockPos> cables, List<BlockPos> attached) {}
}
