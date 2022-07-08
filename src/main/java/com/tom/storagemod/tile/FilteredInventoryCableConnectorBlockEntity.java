package com.tom.storagemod.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.gui.FilteredMenu;
import com.tom.storagemod.util.FilteredInventoryHandler;

public class FilteredInventoryCableConnectorBlockEntity extends AbstractInventoryCableConnectorBlockEntity implements MenuProvider {
	private SimpleContainer filter = new SimpleContainer(9) {

		@Override
		public boolean stillValid(Player p_59619_) {
			return FilteredInventoryCableConnectorBlockEntity.this.stillValid(p_59619_);
		}
	};

	public FilteredInventoryCableConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(StorageMod.invCableConnectorFilteredTile.get(), pos, state);
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
		return new FilteredMenu(arg0, arg1, filter);
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
}
