package com.tom.storagemod.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
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

import com.tom.storagemod.Content;

public class PaintedBlockEntity extends BlockEntity {
	private BlockState blockState;

	public PaintedBlockEntity(BlockPos pos, BlockState state) {
		super(Content.paintedTile.get(), pos, state);
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
	public void load(CompoundTag compound, HolderLookup.Provider provider) {
		super.load(compound, provider);
		blockState = NbtUtils.readBlockState(provider.lookupOrThrow(Registries.BLOCK), compound.getCompound("block"));
		markDirtyClient();
	}

	@Override
	public void saveAdditional(CompoundTag compound, HolderLookup.Provider provider) {
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
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		return saveWithFullMetadata(provider);
	}

	public BlockState getPaintedBlockState() {
		return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
	}

	@Override
	public void applyComponents(DataComponentMap dataComponentMap) {
		super.applyComponents(dataComponentMap);
		blockState = dataComponentMap.get(Content.paintComponent.get());
	}

	@Override
	public void collectComponents(DataComponentMap.Builder builder) {
		super.collectComponents(builder);
		if (this.blockState != null) {
			builder.set(Content.paintComponent.get(), blockState);
		}
	}

	@Override
	public void removeComponentsFromTag(CompoundTag compoundTag) {
		super.removeComponentsFromTag(compoundTag);
		compoundTag.remove("block");
	}
}
