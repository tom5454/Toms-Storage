package com.tom.storagemod.block;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.tom.storagemod.proxy.ClientProxy;
import com.tom.storagemod.tile.TileEntityCraftingTerminal;

public class CraftingTerminal extends StorageTerminalBase {

	public CraftingTerminal() {
		super();
		setRegistryName("ts.crafting_terminal");
	}

	@Override
	public TileEntity newBlockEntity(IBlockReader worldIn) {
		return new TileEntityCraftingTerminal();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		ClientProxy.tooltip("crafting_terminal", tooltip);
	}
}
