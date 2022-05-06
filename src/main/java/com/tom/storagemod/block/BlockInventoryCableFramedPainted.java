package com.tom.storagemod.block;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;
import com.tom.storagemod.tile.TileEntityPainted;

public class BlockInventoryCableFramedPainted extends BlockWithEntity implements IInventoryCable, IPaintable {

	public BlockInventoryCableFramedPainted() {
		super(Block.Settings.of(Material.WOOD).strength(2).nonOpaque());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, BlockView worldIn, List<Text> tooltip,
			TooltipContext flagIn) {
		tooltip.add(Text.translatable("tooltip.toms_storage.paintable"));
		StorageModClient.tooltip("inventory_cable", tooltip);
	}

	@Override
	public List<BlockPos> next(World world, BlockState state, BlockPos pos) {
		List<BlockPos> next = new ArrayList<>();
		for (Direction d : Direction.values()) {
			next.add(pos.offset(d));
		}
		return next;
	}

	@Override
	public boolean paint(World world, BlockPos pos, BlockState to) {
		world.setBlockState(pos, StorageMod.invCablePainted.getDefaultState(), 2);
		BlockEntity te = world.getBlockEntity(pos);
		if(te != null && te instanceof TileEntityPainted)
			return ((TileEntityPainted)te).setPaintedBlockState(to);
		return false;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityPainted(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
		return false;
	}

	@Override
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		return new ItemStack(StorageMod.invCableFramed);
	}
}
