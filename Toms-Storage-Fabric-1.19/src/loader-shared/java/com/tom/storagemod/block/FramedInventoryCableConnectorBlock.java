package com.tom.storagemod.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.tile.PaintedBlockEntity;

public class FramedInventoryCableConnectorBlock extends InventoryCableConnectorBlock implements IPaintable {

	public FramedInventoryCableConnectorBlock() {
		super(false);
	}

	@Override
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip,
			TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.toms_storage.paintable"));
		StorageModClient.tooltip("inventory_cable_connector", tooltip);
	}

	@Override
	public boolean paint(Level world, BlockPos pos, BlockState to) {
		BlockState old = world.getBlockState(pos);
		BlockEntity te = world.getBlockEntity(pos);
		CompoundTag tag = te.saveWithoutMetadata();
		world.setBlock(pos, StorageMod.invCableConnectorPainted.get().defaultBlockState().
				setValue(FACING, old.getValue(FACING))
				.setValue(DOWN, old.getValue(DOWN))
				.setValue(UP, old.getValue(UP))
				.setValue(NORTH, old.getValue(NORTH))
				.setValue(EAST, old.getValue(EAST))
				.setValue(SOUTH, old.getValue(SOUTH))
				.setValue(WEST, old.getValue(WEST)), 2);
		te = world.getBlockEntity(pos);
		te.load(tag);
		if(te != null && te instanceof PaintedBlockEntity)
			return ((PaintedBlockEntity)te).setPaintedBlockState(to);
		return false;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}
}