package com.tom.storagemod;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ItemBlockConnector extends BlockItem {

	public ItemBlockConnector() {
		super(StorageMod.invCableConnector, new Properties());
		setRegistryName(StorageMod.invCableConnector.getRegistryName());
	}

	@Override
	public ITextComponent getDisplayName(ItemStack is) {
		ITextComponent tc = super.getDisplayName(is);
		tc.appendText(" (");
		if(is.hasTag() && is.getTag().getCompound("BlockStateTag").contains("color")) {
			String color = is.getTag().getCompound("BlockStateTag").getString("color");
			tc.appendSibling(new TranslationTextComponent("color.minecraft." + color));
		} else {
			tc.appendSibling(new TranslationTextComponent("color.minecraft.white"));
		}
		tc.appendText(")");
		return tc;
	}
}
