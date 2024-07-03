package com.tom.storagemod.block.entity;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import com.tom.storagemod.Content;
import com.tom.storagemod.platform.PlatformBlockEntity;

public class PaintedBlockEntity extends PlatformBlockEntity {
	public static final ModelProperty<Supplier<BlockState>> FACADE_STATE = new ModelProperty<>();
	private BlockState blockState;

	public PaintedBlockEntity(BlockPos pos, BlockState state) {
		super(Content.paintedBE.get(), pos, state);
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
	public void loadAdditional(CompoundTag compound, HolderLookup.Provider provider) {
		super.loadAdditional(compound, provider);
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
			requestModelDataUpdate();
			getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);
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
	protected void applyImplicitComponents(DataComponentInput dataComponentInput) {
		super.applyImplicitComponents(dataComponentInput);
		blockState = dataComponentInput.get(Content.paintComponent.get());
	}

	@Override
	protected void collectImplicitComponents(Builder builder) {
		super.collectImplicitComponents(builder);
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
