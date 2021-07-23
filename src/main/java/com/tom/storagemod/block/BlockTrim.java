package com.tom.storagemod.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.proxy.ClientProxy;

public class BlockTrim extends Block implements ITrim, IPaintable {

	public BlockTrim() {
		super(Block.Properties.of(Material.WOOD).strength(3).harvestTool(ToolType.AXE));
		setRegistryName("ts.trim");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip,
			TooltipFlag flagIn) {
		tooltip.add(new TranslatableComponent("tooltip.toms_storage.paintable"));
		ClientProxy.tooltip("trim", tooltip);
	}

	@Override
	public boolean paint(Level world, BlockPos pos, BlockState to) {
		world.setBlockAndUpdate(pos, StorageMod.paintedTrim.defaultBlockState());
		return StorageMod.paintedTrim.paint(world, pos, to);
	}
}
