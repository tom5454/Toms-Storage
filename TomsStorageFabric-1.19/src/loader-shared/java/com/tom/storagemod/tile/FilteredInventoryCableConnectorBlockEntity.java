package com.tom.storagemod.tile;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Content;
import com.tom.storagemod.gui.FilteredMenu;
import com.tom.storagemod.util.FilteredStorage;

public class FilteredInventoryCableConnectorBlockEntity extends AbstractInventoryCableConnectorBlockEntity implements MenuProvider, ContainerListener {
	private SimpleContainer filter = new SimpleContainer(9);

	public FilteredInventoryCableConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(Content.invCableConnectorFilteredTile.get(), pos, state);
		filter.addListener(this);
	}

	@Override
	protected Storage<ItemVariant> applyFilter() {
		Storage<ItemVariant> w = super.applyFilter();
		if(w == null)return null;
		return new FilteredStorage(w, filter.items);
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.put("filter", filter.createTag());
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		filter.fromTag(tag.getList("filter", 10));
	}

	@Override
	public AbstractContainerMenu createMenu(int arg0, Inventory arg1, Player arg2) {
		return new FilteredMenu(arg0, arg1, filter);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("ts.connector_filtered");
	}

	@Override
	public void containerChanged(Container sender) {
		setChanged();
	}
}
