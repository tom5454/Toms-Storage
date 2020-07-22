package com.tom.storagemod.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import com.tom.fabriclibs.ext.IItem;
import com.tom.fabriclibs.ext.IRegistered;

public class ItemBlockPainted extends BlockItem implements IItem {

	public ItemBlockPainted(Block block, Item.Settings p) {
		super(block, p);
		setRegistryName(((IRegistered) block).getRegistryName());
	}
	public ItemBlockPainted(Block block) {
		this(block, new Item.Settings());
	}

	@Override
	public Text getName(ItemStack is) {
		Text tcS = super.getName(is);
		MutableText tc = (MutableText) tcS;
		if(is.hasTag() && is.getTag().getCompound("BlockEntityTag").contains("block")) {
			BlockState st = NbtHelper.toBlockState(is.getTag().getCompound("BlockEntityTag").getCompound("block"));
			tc.append(" (");
			tc.append(st.getBlock().getName().formatted(Formatting.GREEN));
			tc.append(")");
		}
		return tc;
	}
}
