package com.tom.storagemod.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

public class ItemBlockPainted extends BlockItem {

	public ItemBlockPainted(Block block, Item.Properties p) {
		super(block, p);
		setRegistryName(block.getRegistryName());
	}
	public ItemBlockPainted(Block block) {
		this(block, new Item.Properties());
	}

	@Override
	public ITextComponent getName(ItemStack is) {
		IFormattableTextComponent tc = (IFormattableTextComponent) super.getName(is);
		if(is.hasTag() && is.getTag().getCompound("BlockEntityTag").contains("block")) {
			BlockState st = NBTUtil.readBlockState(is.getTag().getCompound("BlockEntityTag").getCompound("block"));
			tc.append(" (");
			tc.append(st.getBlock().getName().withStyle(TextFormatting.GREEN));
			tc.append(")");
		}
		return tc;
	}
}
