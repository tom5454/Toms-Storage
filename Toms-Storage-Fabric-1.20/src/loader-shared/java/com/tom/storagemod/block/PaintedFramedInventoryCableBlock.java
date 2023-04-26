package com.tom.storagemod.block;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

import com.tom.storagemod.Content;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.tile.PaintedBlockEntity;

public class PaintedFramedInventoryCableBlock extends BaseEntityBlock implements IInventoryCable, IPaintable {

	public PaintedFramedInventoryCableBlock() {
		super(Block.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(2).noOcclusion());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip,
			TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.toms_storage.paintable"));
		StorageModClient.tooltip("inventory_cable", tooltip);
	}

	@Override
	public List<BlockPos> next(Level world, BlockState state, BlockPos pos) {
		List<BlockPos> next = new ArrayList<>();
		for (Direction d : Direction.values()) {
			next.add(pos.relative(d));
		}
		return next;
	}

	@Override
	public boolean paint(Level world, BlockPos pos, BlockState to) {
		world.setBlock(pos, StorageMod.invCablePainted.get().defaultBlockState(), 2);
		BlockEntity te = world.getBlockEntity(pos);
		if(te != null && te instanceof PaintedBlockEntity)
			return ((PaintedBlockEntity)te).setPaintedBlockState(to);
		return false;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PaintedBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_149645_1_) {
		return RenderShape.MODEL;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter world, BlockPos pos) {
		return false;
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
		return new ItemStack(Content.invCableFramed.get());
	}
}
