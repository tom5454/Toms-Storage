package com.tom.storagemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Content;

public class PaintedBlockEntity extends AbstractPainedBlockEntity {

	public PaintedBlockEntity(BlockPos pos, BlockState state) {
		super(Content.paintedBE.get(), pos, state);
	}

	public PaintedBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	protected void markDirtyClient() {
		setChanged();
		if (getLevel() != null) {
			BlockState state = getLevel().getBlockState(getBlockPos());
			getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);

			if(!level.isClientSide() && level instanceof ServerLevel world) {
				world.getChunkSource().chunkMap.getPlayers(new ChunkPos(getBlockPos()), false).forEach(player -> {
					player.connection.send(getUpdatePacket());
				});
			}
			//sync();
		}
	}
}
