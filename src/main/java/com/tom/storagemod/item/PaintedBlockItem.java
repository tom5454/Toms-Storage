package com.tom.storagemod.item;

import java.util.function.Function;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.StorageMod;

public class PaintedBlockItem extends BlockItem {

	public static Function<Block, Item> makeHidden() {
		return b -> new PaintedBlockItem(b, new Item.Properties());
	}

	public static Function<Block, Item> make() {
		return b -> new PaintedBlockItem(b, new Item.Properties().tab(StorageMod.STORAGE_MOD_TAB));
	}

	private PaintedBlockItem(Block block, Item.Properties p) {
		super(block, p);
	}

	@Override
	public Component getName(ItemStack is) {
		MutableComponent tc = (MutableComponent) super.getName(is);
		if(is.hasTag() && is.getTag().getCompound("BlockEntityTag").contains("block")) {
			BlockState st = NbtUtils.readBlockState(is.getTag().getCompound("BlockEntityTag").getCompound("block"));
			tc.append(" (");
			tc.append(st.getBlock().getName().withStyle(ChatFormatting.GREEN));
			tc.append(")");
		}
		return tc;
	}

}
