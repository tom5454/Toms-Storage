package com.tom.storagemod.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.InventoryCableConnectorBlock;
import com.tom.storagemod.gui.InventoryConnectorFilterMenu;
import com.tom.storagemod.item.IItemFilter;
import com.tom.storagemod.util.BlockFace;
import com.tom.storagemod.util.FilteredInventoryHandler;
import com.tom.storagemod.util.ItemFilterHandler;
import com.tom.storagemod.util.Priority;

public class FilteredInventoryCableConnectorBlockEntity extends AbstractInventoryCableConnectorBlockEntity implements MenuProvider {
	private SimpleContainer filter = new SimpleContainer(9) {

		@Override
		public boolean stillValid(Player p_59619_) {
			return FilteredInventoryCableConnectorBlockEntity.this.stillValid(p_59619_);
		}
	};
	private boolean allowList = true, keepLastInSlot;
	private Priority priority = Priority.NORMAL;

	public FilteredInventoryCableConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(Content.invCableConnectorFilteredTile.get(), pos, state);
	}

	@Override
	protected IItemHandler applyFilter(IItemHandler h) {
		ItemFilterHandler fh = new ItemFilterHandler(filter, BlockFace.touching(level, worldPosition, getBlockState().getValue(InventoryCableConnectorBlock.FACING)));
		return new FilteredInventoryHandler(h, s -> fh.test(s) == allowList, keepLastInSlot);
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
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
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

	public boolean stillValid(Player p_59619_) {
		if (this.level.getBlockEntity(this.worldPosition) != this) {
			return false;
		} else {
			return !(p_59619_.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) > 64.0D);
		}
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
