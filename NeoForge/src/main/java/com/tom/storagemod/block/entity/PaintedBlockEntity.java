package com.tom.storagemod.block.entity;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;

import com.tom.storagemod.Content;

public class PaintedBlockEntity extends AbstractPainedBlockEntity {
	public static final ModelProperty<Supplier<BlockState>> FACADE_STATE = new ModelProperty<>();

	public PaintedBlockEntity(BlockPos pos, BlockState state) {
		super(Content.paintedBE.get(), pos, state);
	}

	public PaintedBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public ModelData getModelData() {
		return ModelData.builder().with(FACADE_STATE, this::getPaintedBlockState).build();
	}

	@Override
	protected void markDirtyClient() {
		setChanged();
		if (getLevel() != null) {
			BlockState state = getLevel().getBlockState(getBlockPos());
			requestModelDataUpdate();
			getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);
		}
	}
}
