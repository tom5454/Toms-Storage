package com.tom.storagemod.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import com.tom.storagemod.block.IPaintable;

public class ItemBlockPainted extends BlockItem {

	public ItemBlockPainted(Block block, Item.Settings p) {
		super(block, p);
	}
	public ItemBlockPainted(Block block) {
		this(block, new Item.Settings());
	}

	@Override
	protected BlockState getPlacementState(ItemPlacementContext context) {
		ItemStack is = context.getStack();
		Block block = is.hasNbt() && is.getNbt().getCompound("BlockEntityTag").contains("block") ? ((IPaintable)getBlock()).getPaintedBlock() : getBlock();
		BlockState blockState = block.getPlacementState(context);
		return (blockState != null && canPlace(context, blockState)) ? blockState : null;
	}

	@Override
	public Text getName(ItemStack is) {
		Text tcS = super.getName(is);
		MutableText tc = (MutableText) tcS;
		if(is.hasNbt() && is.getNbt().getCompound("BlockEntityTag").contains("block")) {
			BlockState st = NbtHelper.toBlockState(is.getNbt().getCompound("BlockEntityTag").getCompound("block"));
			tc.append(" (");
			tc.append(st.getBlock().getName().formatted(Formatting.GREEN));
			tc.append(")");
		}
		return tc;
	}
}
