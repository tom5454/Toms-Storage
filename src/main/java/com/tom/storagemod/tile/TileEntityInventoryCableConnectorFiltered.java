package com.tom.storagemod.tile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.gui.ContainerFiltered;

public class TileEntityInventoryCableConnectorFiltered extends TileEntityInventoryCableConnectorBase implements INamedContainerProvider {
	private Inventory filter = new Inventory(9);

	public TileEntityInventoryCableConnectorFiltered() {
		super(StorageMod.invCableConnectorFilteredTile);
	}

	@Override
	protected LazyOptional<IItemHandler> applyFilter() {
		if(pointedAt == null)return LazyOptional.empty();
		else return pointedAt.lazyMap(h -> new FilteredInventoryHandler(h, filter));
	}

	@Override
	protected LazyOptional<IItemHandler> getCapability() {
		return super.getCapability().lazyMap(h -> new FilteredInventoryHandler(h, filter));
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.put("filter", filter.write());
		return super.write(tag);
	}

	@Override
	public void read(BlockState state, CompoundNBT tag) {
		super.read(state, tag);
		filter.read(tag.getList("filter", 10));
	}

	@Override
	public Container createMenu(int arg0, PlayerInventory arg1, PlayerEntity arg2) {
		return new ContainerFiltered(arg0, arg1, filter);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("ts.connector_filtered");
	}
}
