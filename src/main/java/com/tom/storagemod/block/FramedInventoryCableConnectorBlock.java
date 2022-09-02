package com.tom.storagemod.block;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.tile.PaintedBlockEntity;

public class FramedInventoryCableConnectorBlock extends InventoryCableConnectorBlock implements IPaintable {

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		tooltip.add(Text.translatable("tooltip.toms_storage.paintable"));
		StorageModClient.tooltip("inventory_cable_connector", tooltip);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return VoxelShapes.fullCube();
	}

	@Override
	public boolean paint(World world, BlockPos pos, BlockState to) {
		BlockState old = world.getBlockState(pos);
		BlockEntity te = world.getBlockEntity(pos);
		NbtCompound tag = te.createNbt();
		world.setBlockState(pos, StorageMod.invCableConnectorPainted.getDefaultState().
				with(FACING, old.get(FACING))
				.with(DOWN, old.get(DOWN))
				.with(UP, old.get(UP))
				.with(NORTH, old.get(NORTH))
				.with(EAST, old.get(EAST))
				.with(SOUTH, old.get(SOUTH))
				.with(WEST, old.get(WEST)), 2);
		te = world.getBlockEntity(pos);
		te.readNbt(tag);
		if(te != null && te instanceof PaintedBlockEntity)
			return ((PaintedBlockEntity)te).setPaintedBlockState(to);
		return false;
	}

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		return new ItemStack(StorageMod.invCableConnectorFramed);
	}
}
