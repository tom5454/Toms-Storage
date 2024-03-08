package com.tom.storagemod.tile;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.InventoryCableConnectorBlock;
import com.tom.storagemod.gui.InventoryConnectorFilterMenu;
import com.tom.storagemod.item.IItemFilter;
import com.tom.storagemod.util.BlockFace;
import com.tom.storagemod.util.FilteredStorage;
import com.tom.storagemod.util.ItemFilterHandler;
import com.tom.storagemod.util.Priority;

public class FilteredInventoryCableConnectorBlockEntity extends AbstractInventoryCableConnectorBlockEntity implements MenuProvider, ContainerListener {
	private SimpleContainer filter = new SimpleContainer(9);
	private boolean allowList = true, keepLastInSlot;
	private Priority priority = Priority.NORMAL;

	public FilteredInventoryCableConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(Content.invCableConnectorFilteredTile.get(), pos, state);
		filter.addListener(this);
	}

	@Override
	protected Storage<ItemVariant> applyFilter(Storage<ItemVariant> st) {
		Storage<ItemVariant> w = super.applyFilter(st);
		if(w == null)return null;
		ItemFilterHandler fh = new ItemFilterHandler(filter.items, BlockFace.touching(level, worldPosition, getBlockState().getValue(InventoryCableConnectorBlock.FACING)));
		return new FilteredStorage(w, s -> fh.test(s) == allowList, keepLastInSlot);
	}

	@Override
	public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		tag.put("filter", filter.createTag(provider));
		tag.putBoolean("allowList", allowList);
		tag.putInt("priority", priority.ordinal());
		tag.putBoolean("keepLast", keepLastInSlot);
	}

	@Override
	public void load(CompoundTag tag, HolderLookup.Provider provider) {
		super.load(tag, provider);
		filter.fromTag(tag.getList("filter", 10), provider);
		if(tag.contains("allowList"))allowList = tag.getBoolean("allowList");
		else allowList = true;
		if(tag.contains("priority"))priority = Priority.VALUES[Math.abs(tag.getInt("priority")) % Priority.VALUES.length];
		else priority = Priority.NORMAL;
		keepLastInSlot = tag.getBoolean("keepLast");
	}

	@Override
	public AbstractContainerMenu createMenu(int arg0, Inventory arg1, Player arg2) {
		return new InventoryConnectorFilterMenu(arg0, arg1, this);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("ts.connector_filtered");
	}

	@Override
	public void containerChanged(Container sender) {
		setChanged();
	}

	public void dropFilters() {
		for(int i = 0;i<filter.getContainerSize();i++) {
			ItemStack is = filter.getItem(i);
			if(is.getItem() instanceof IItemFilter) {
				Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), is);
			}
		}
	}

	public SimpleContainer getFilter() {
		return filter;
	}

	public void setAllowList(boolean b) {
		allowList = b;
		setChanged();
	}

	public boolean isAllowList() {
		return allowList;
	}

	public void setPriority(Priority p) {
		priority = p;
		setChanged();
	}

	public Priority getPriority() {
		return priority;
	}

	public void setKeepLastInSlot(boolean keepLastInSlot) {
		this.keepLastInSlot = keepLastInSlot;
	}

	public boolean isKeepLastInSlot() {
		return keepLastInSlot;
	}

	@Override
	protected void initLinv() {
		linv.priority = priority;
	}
}
