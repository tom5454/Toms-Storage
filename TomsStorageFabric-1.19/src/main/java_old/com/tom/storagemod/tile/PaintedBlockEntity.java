package com.tom.storagemod.tile;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

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
	public void readNbt(NbtCompound compound) {
		super.readNbt(compound);
		blockState = NbtHelper.toBlockState(compound.getCompound("block"));
		markDirtyClient();
	}

	@Override
	public void writeNbt(NbtCompound compound) {
		if (blockState != null) {
			compound.put("block", NbtHelper.fromBlockState(blockState));
		}
	}

	private void markDirtyClient() {
		markDirty();
		if (getWorld() != null) {
			BlockState state = getWorld().getBlockState(getPos());
			getWorld().updateListeners(getPos(), state, state, 3);

			if(!world.isClient) {
				ServerWorld world = (ServerWorld) getWorld();
				world.getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(new ChunkPos(getPos()), false).forEach(player -> {
					player.networkHandler.sendPacket(toUpdatePacket());
				});
			}
			//sync();
		}
	}

	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbtWithIdentifyingData();
	}

	public BlockState getPaintedBlockState() {
		return blockState == null ? Blocks.AIR.getDefaultState() : blockState;
	}
}
