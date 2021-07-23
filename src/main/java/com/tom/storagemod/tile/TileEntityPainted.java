package com.tom.storagemod.tile;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import com.tom.storagemod.StorageMod;

public class TileEntityPainted extends BlockEntity {
	public static final ModelProperty<Supplier<BlockState>> FACADE_STATE = new ModelProperty<>();
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
	public IModelData getModelData() {
		return new ModelDataMap.Builder().withInitial(FACADE_STATE, this::getPaintedBlockState).build();
	}

	@Override
	public void load(@Nonnull CompoundTag compound) {
		super.load(compound);
		blockState = NbtUtils.readBlockState(compound.getCompound("block"));
		markDirtyClient();
	}

	@Nonnull
	@Override
	public CompoundTag save(@Nonnull CompoundTag compound) {
		if (blockState != null) {
			compound.put("block", NbtUtils.writeBlockState(blockState));
		}
		return super.save(compound);
	}

	private void markDirtyClient() {
		setChanged();
		if (getLevel() != null) {
			BlockState state = getLevel().getBlockState(getBlockPos());
			getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);
		}
	}

	@Nonnull
	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag updateTag = super.getUpdateTag();
		save(updateTag);
		return updateTag;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		CompoundTag nbtTag = new CompoundTag();
		save(nbtTag);
		return new ClientboundBlockEntityDataPacket(getBlockPos(), 1, nbtTag);
	}

	public BlockState getPaintedBlockState() {
		return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
		BlockState old = getPaintedBlockState();
		CompoundTag tagCompound = packet.getTag();
		super.onDataPacket(net, packet);
		load(tagCompound);

		if (level != null && level.isClientSide) {
			// If needed send a render update.
			if (! getPaintedBlockState().equals(old)) {
				level.blockEntityChanged(getBlockPos());
			}
			requestModelDataUpdate();
		}
	}
}
