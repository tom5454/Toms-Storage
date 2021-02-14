package com.tom.storagemod.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityPainted;

public class BlockPaintedTrim extends ContainerBlock implements ITrim, IPaintable {

	public BlockPaintedTrim() {
		super(Block.Properties.create(Material.WOOD).hardnessAndResistance(3).harvestTool(ToolType.AXE));
		setRegistryName("ts.painted_trim");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("tooltip.toms_storage.paintable"));
		ClientProxy.tooltip("trim", tooltip);
	}

	@Override
	public boolean paint(World world, BlockPos pos, BlockState to) {
		TileEntity te = world.getTileEntity(pos);
		if(te != null && te instanceof TileEntityPainted)
			return ((TileEntityPainted)te).setPaintedBlockState(to);
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new TileEntityPainted();
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos,
			PlayerEntity player) {
		return new ItemStack(StorageMod.inventoryTrim);
	}
}
