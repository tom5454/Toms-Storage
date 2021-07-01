package com.tom.storagemod.tile;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import com.tom.storagemod.StorageMod;

public class TileEntityPainted extends TileEntity {
	public static final ModelProperty<Supplier<BlockState>> FACADE_STATE = new ModelProperty<>();
	private BlockState blockState;

	public TileEntityPainted() {
		super(StorageMod.paintedTile);
	}

	public TileEntityPainted(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
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
	public void load(BlockState st, @Nonnull CompoundNBT compound) {
		super.load(st, compound);
		blockState = NBTUtil.readBlockState(compound.getCompound("block"));
		markDirtyClient();
	}

	@Nonnull
	@Override
	public CompoundNBT save(@Nonnull CompoundNBT compound) {
		if (blockState != null) {
			compound.put("block", NBTUtil.writeBlockState(blockState));
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
	public CompoundNBT getUpdateTag() {
		CompoundNBT updateTag = super.getUpdateTag();
		save(updateTag);
		return updateTag;
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbtTag = new CompoundNBT();
		save(nbtTag);
		return new SUpdateTileEntityPacket(getBlockPos(), 1, nbtTag);
	}

	public BlockState getPaintedBlockState() {
		return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
		BlockState old = getPaintedBlockState();
		CompoundNBT tagCompound = packet.getTag();
		super.onDataPacket(net, packet);
		load(level.getBlockState(worldPosition), tagCompound);

		if (level != null && level.isClientSide) {
			// If needed send a render update.
			if (! getPaintedBlockState().equals(old)) {
				level.blockEntityChanged(getBlockPos(), this.getTileEntity());
			}
		}
	}
}
