package com.tom.storagemod.tile;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.gui.FilteredMenu;
import com.tom.storagemod.util.FilteredStorage;

public class FilteredInventoryCableConnectorBlockEntity extends AbstractInventoryCableConnectorBlockEntity implements NamedScreenHandlerFactory, InventoryChangedListener {
	private SimpleInventory filter = new SimpleInventory(9);

	public FilteredInventoryCableConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(StorageMod.invCableConnectorFilteredTile, pos, state);
		filter.addListener(this);
	}

	@Override
	protected Storage<ItemVariant> applyFilter() {
		Storage<ItemVariant> w = super.applyFilter();
		if(w == null)return null;
		return new FilteredStorage(w, filter.stacks);
	}

	@Override
	public void writeNbt(NbtCompound tag) {
		super.writeNbt(tag);
		tag.put("filter", filter.toNbtList());
	}

	@Override
	public void readNbt(NbtCompound tag) {
		super.readNbt(tag);
		filter.readNbtList(tag.getList("filter", 10));
	}

	@Override
	public ScreenHandler createMenu(int arg0, PlayerInventory arg1, PlayerEntity arg2) {
		return new FilteredMenu(arg0, arg1, filter);
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable("ts.connector_filtered");
	}

	@Override
	public void onInventoryChanged(Inventory sender) {
		markDirty();
	}
}
