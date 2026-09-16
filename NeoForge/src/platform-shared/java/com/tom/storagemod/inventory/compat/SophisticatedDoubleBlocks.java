package com.tom.storagemod.inventory.compat;

public class SophisticatedDoubleBlocks {
	public static void register() {
		//MultiblockInventoryAPI.EVENT.register(SophisticatedDoubleBlocks::checkGroup);
	}

	/*public static void checkGroup(Level level, BlockPos p, BlockState state, Consumer<BlockPos> extra) {
		for (BlockPos pos : StoragePositionGroups.getGroup(level, p).memberPositions()) {
			if (!pos.equals(p))
				extra.accept(pos);
		}
	}*/
}
