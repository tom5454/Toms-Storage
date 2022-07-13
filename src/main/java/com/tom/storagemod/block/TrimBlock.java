package com.tom.storagemod.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StorageModClient;

public class TrimBlock extends Block implements ITrim, IPaintable {

	public TrimBlock() {
		super(Block.Properties.of(Material.WOOD).strength(3));
	}

	@Override
	public void appendHoverText(ItemStack stack, BlockGetter worldIn, List<Component> tooltip,
			TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.toms_storage.paintable"));
		StorageModClient.tooltip("trim", tooltip);
	}

	@Override
	public boolean paint(Level world, BlockPos pos, BlockState to) {
		world.setBlockAndUpdate(pos, StorageMod.paintedTrim.get().defaultBlockState());
		return StorageMod.paintedTrim.get().paint(world, pos, to);
	}
}
