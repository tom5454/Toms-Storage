package com.tom.storagemod.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityInventoryProxy;
import com.tom.storagemod.tile.TileEntityPainted;

public class BlockInventoryProxy extends ContainerBlock implements IPaintable {
	public static final DirectionProperty FACING = BlockStateProperties.FACING;

	public BlockInventoryProxy() {
		super(Block.Properties.create(Material.WOOD).hardnessAndResistance(3).harvestTool(ToolType.AXE));
		setRegistryName("ts.inventory_proxy");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		tooltip.add(new TranslationTextComponent("tooltip.toms_storage.paintable"));
		ClientProxy.tooltip("inventory_proxy", tooltip);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new TileEntityInventoryProxy();
	}
	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getFace().getOpposite());
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockRenderType getRenderType(BlockState p_149645_1_) {
		return BlockRenderType.MODEL;
	}

	@Override
	public boolean paint(World world, BlockPos pos, BlockState to) {
		TileEntity te = world.getTileEntity(pos);
		if(te != null && te instanceof TileEntityPainted)
			return ((TileEntityPainted)te).setPaintedBlockState(to);
		return false;
	}
}
