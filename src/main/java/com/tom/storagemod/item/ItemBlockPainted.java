package com.tom.storagemod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ItemBlockPainted extends BlockItem {

	public ItemBlockPainted(Block block, Item.Properties p) {
		super(block, p);
		setRegistryName(block.getRegistryName());
	}
	public ItemBlockPainted(Block block) {
		this(block, new Item.Properties());
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
