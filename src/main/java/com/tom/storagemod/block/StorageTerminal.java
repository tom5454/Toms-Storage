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
import com.tom.storagemod.tile.TileEntityStorageTerminal;

public class StorageTerminal extends StorageTerminalBase {

	public StorageTerminal() {
		super();
		setRegistryName("ts.storage_terminal");
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new TileEntityStorageTerminal();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		ClientProxy.tooltip("storage_terminal", tooltip);
	}
}
