package com.tom.storagemod.tile;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import com.tom.storagemod.StorageMod;

public class PaintedBlockEntity extends BlockEntity {
	public static final ModelProperty<Supplier<BlockState>> FACADE_STATE = new ModelProperty<>();
	private BlockState blockState;

	public PaintedBlockEntity(BlockPos pos, BlockState state) {
		super(StorageMod.paintedTile.get(), pos, state);
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
	public ModelData getModelData() {
		return ModelData.builder().with(FACADE_STATE, this::getPaintedBlockState).build();
	}

	@Override
	public void load(@Nonnull CompoundTag compound) {
		super.load(compound);
		blockState = NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), compound.getCompound("block"));
		markDirtyClient();
	}

	@Override
	public void saveAdditional(@Nonnull CompoundTag compound) {
		if (blockState != null) {
			compound.put("block", NbtUtils.writeBlockState(blockState));
		}
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
		return saveWithFullMetadata();
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public BlockState getPaintedBlockState() {
		return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
		BlockState old = getPaintedBlockState();
		super.onDataPacket(net, packet);

		if (level != null && level.isClientSide) {
			// If needed send a render update.
			if (! getPaintedBlockState().equals(old)) {
				level.blockEntityChanged(getBlockPos());
			}
			requestModelDataUpdate();
		}
	}
}
