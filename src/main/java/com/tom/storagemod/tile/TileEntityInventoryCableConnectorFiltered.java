package com.tom.storagemod.tile;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.gui.ContainerFiltered;
import com.tom.storagemod.util.FilteredInventoryWrapper;
import com.tom.storagemod.util.InventoryWrapper;

public class TileEntityInventoryCableConnectorFiltered extends TileEntityInventoryCableConnectorBase implements NamedScreenHandlerFactory {
	private SimpleInventory filter = new SimpleInventory(9);

	public TileEntityInventoryCableConnectorFiltered() {
		super(StorageMod.invCableConnectorFilteredTile);
	}

	@Override
	public <R> R call(Function<InventoryWrapper, R> func, Predicate<InventoryWrapper> accessCheck, R def) {
		return super.call(func, accessCheck, def);
	}

	@Override
	protected InventoryWrapper applyFilter() {
		InventoryWrapper w = super.applyFilter();
		if(w == null)return null;
		return new FilteredInventoryWrapper(w, filter);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		tag.put("filter", filter.toNbtList());
		return super.writeNbt(tag);
	}

	@Override
	public void fromTag(BlockState state, NbtCompound tag) {
		super.fromTag(state, tag);
		filter.readNbtList(tag.getList("filter", 10));
	}

	@Override
	public ScreenHandler createMenu(int arg0, PlayerInventory arg1, PlayerEntity arg2) {
		return new ContainerFiltered(arg0, arg1, filter);
	}

	@Override
	public Text getDisplayName() {
		return new TranslatableText("ts.connector_filtered");
	}
}
