package com.tom.storagemod.tile;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.util.math.BlockPos;

public class CablePathCache {

    private static ConcurrentHashMap<BlockPos, BlockPos> Cache = new ConcurrentHashMap<>();

    public static BlockPos tryGet(BlockPos InventoryCablePos) {
        if (Cache.containsKey(InventoryCablePos))
            return Cache.get(InventoryCablePos);
        return null;
    }

    public static void Put(BlockPos InventoryCablePos, BlockPos ConnectorPos) {
        Cache.put(InventoryCablePos, ConnectorPos);
    }

    public static void Clear() {
        Cache.clear();
    }
}
