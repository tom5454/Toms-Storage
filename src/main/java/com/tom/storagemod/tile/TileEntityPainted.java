package com.tom.storagemod.tile;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import com.tom.storagemod.StorageMod;

public class TileEntityPainted extends BlockEntity implements BlockEntityClientSerializable {
	//public static final ModelProperty<Supplier<BlockState>> FACADE_STATE = new ModelProperty<>();
	private BlockState blockState;

	public TileEntityPainted(BlockPos pos, BlockState state) {
		super(StorageMod.paintedTile, pos, state);
	}

	public TileEntityPainted(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
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
	public void readNbt(CompoundTag compound) {
		super.readNbt(compound);
		blockState = NbtHelper.toBlockState(compound.getCompound("block"));
		markDirtyClient();
	}

	@Override
	public CompoundTag writeNbt(CompoundTag compound) {
		if (blockState != null) {
			compound.put("block", NbtHelper.fromBlockState(blockState));
		}
		return super.writeNbt(compound);
	}

	private void markDirtyClient() {
		markDirty();
		if (getWorld() != null) {
			BlockState state = getWorld().getBlockState(getPos());
			getWorld().updateListeners(getPos(), state, state, 3);

			ServerWorld world = (ServerWorld) getWorld();
			world.getChunkManager().threadedAnvilChunkStorage.getPlayersWatchingChunk(new ChunkPos(getPos()), false).forEach(player -> {
				player.networkHandler.sendPacket(toUpdatePacket());
			});

			sync();
		}
	}

	@Override
	public BlockEntityUpdateS2CPacket toUpdatePacket() {
		CompoundTag nbtTag = new CompoundTag();
		writeNbt(nbtTag);
		return new BlockEntityUpdateS2CPacket(getPos(), 127, nbtTag);
	}

	public BlockState getPaintedBlockState() {
		return blockState == null ? Blocks.AIR.getDefaultState() : blockState;
	}

	@Override
	public void fromClientTag(CompoundTag tag) {
		BlockState old = getPaintedBlockState();
		blockState = NbtHelper.toBlockState(tag.getCompound("block"));
		if (world != null && world.isClient) {
			// If needed send a render update.
			if (! getPaintedBlockState().equals(old)) {
				world.markDirty(getPos());
				BlockState st = world.getBlockState(pos);
				world.updateListeners(pos, st, st, 3);
			}
		}
	}

	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		return writeNbt(tag);
	}
}
