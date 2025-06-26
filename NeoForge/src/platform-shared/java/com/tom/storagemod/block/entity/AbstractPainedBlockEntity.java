package com.tom.storagemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.tom.storagemod.Content;
import com.tom.storagemod.platform.PlatformBlockEntity;

public abstract class AbstractPainedBlockEntity extends PlatformBlockEntity {
	protected BlockState blockState;

	public AbstractPainedBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
		super(p_155228_, p_155229_, p_155230_);
	}

	public boolean setPaintedBlockState(BlockState blockState) {
		BlockState old = getPaintedBlockState();
		this.blockState = blockState;
		boolean changed = !getPaintedBlockState().equals(old);
		if(changed)markDirtyClient();
		return changed;
	}

	@Override
	public void loadAdditional(ValueInput compound) {
		super.loadAdditional(compound);
		blockState = compound.read("block", BlockState.CODEC).orElse(null);
		markDirtyClient();
	}

	@Override
	public void saveAdditional(ValueOutput compound) {
		super.saveAdditional(compound);
		compound.storeNullable("block", BlockState.CODEC, blockState);
	}

	protected abstract void markDirtyClient();

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
	protected void applyImplicitComponents(DataComponentGetter data) {
		super.applyImplicitComponents(data);
		blockState = data.get(Content.paintComponent.get());
	}

	@Override
	protected void collectImplicitComponents(Builder builder) {
		super.collectImplicitComponents(builder);
		if (this.blockState != null) {
			builder.set(Content.paintComponent.get(), blockState);
		}
	}

	@Override
	public void removeComponentsFromTag(ValueOutput compoundTag) {
		super.removeComponentsFromTag(compoundTag);
		compoundTag.discard("block");
	}
}
