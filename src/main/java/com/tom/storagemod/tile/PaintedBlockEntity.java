package com.tom.storagemod.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.StorageMod;

public class PaintedBlockEntity extends BlockEntity {
	private BlockState blockState;

	public PaintedBlockEntity(BlockPos pos, BlockState state) {
		super(StorageMod.paintedTile, pos, state);
	}

	public PaintedBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	public boolean setPaintedBlockState(BlockState blockState) {
		BlockState old = getPaintedBlockState();
		this.blockState = blockState;
		boolean changed = !getPaintedBlockState().equals(old);
		if(changed)markDirtyClient();
		return changed;
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		blockState = NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), compound.getCompound("block"));
		markDirtyClient();
	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		if (blockState != null) {
			compound.put("block", NbtUtils.writeBlockState(blockState));
		}
	}

	private void markDirtyClient() {
		setChanged();
		if (getLevel() != null) {
			BlockState state = getLevel().getBlockState(getBlockPos());
			getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);

			if(!level.isClientSide) {
				ServerLevel world = (ServerLevel) getLevel();
				world.getChunkSource().chunkMap.getPlayers(new ChunkPos(getBlockPos()), false).forEach(player -> {
					player.connection.send(getUpdatePacket());
				});
			}
			//sync();
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return saveWithFullMetadata();
	}

	public BlockState getPaintedBlockState() {
		return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
	}
}
