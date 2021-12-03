package com.tom.storagemod.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.gui.ContainerFiltered;

public class TileEntityInventoryCableConnectorFiltered extends TileEntityInventoryCableConnectorBase implements MenuProvider {
	private SimpleContainer filter = new SimpleContainer(9);

	public TileEntityInventoryCableConnectorFiltered(BlockPos pos, BlockState state) {
		super(StorageMod.invCableConnectorFilteredTile, pos, state);
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
	public void saveAdditional(CompoundTag tag) {
		tag.put("filter", filter.createTag());
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		filter.fromTag(tag.getList("filter", 10));
	}

	@Override
	public AbstractContainerMenu createMenu(int arg0, Inventory arg1, Player arg2) {
		return new ContainerFiltered(arg0, arg1, filter);
	}

	@Override
	public Component getDisplayName() {
		return new TranslatableComponent("ts.connector_filtered");
	}
}
