package com.tom.storagemod.block;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;

public class BlockTrim extends Block implements ITrim, IPaintable {

	public BlockTrim() {
		super(Block.Settings.of(Material.WOOD).strength(3));
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, BlockView world, List<Text> tooltip, TooltipContext options) {
		tooltip.add(new TranslatableText("tooltip.toms_storage.paintable"));
		StorageModClient.tooltip("trim", tooltip);
	}

	@Override
	public boolean paint(World world, BlockPos pos, BlockState to) {
		world.setBlockState(pos, StorageMod.paintedTrim.getDefaultState());
		return StorageMod.paintedTrim.paint(world, pos, to);
	}
}
