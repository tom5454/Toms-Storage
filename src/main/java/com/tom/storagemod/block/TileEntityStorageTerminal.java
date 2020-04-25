package com.tom.storagemod.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.gui.ContainerStorageTerminal;

public class TileEntityStorageTerminal extends TileEntity implements INamedContainerProvider, ITickableTileEntity {
	private IItemHandler itemHandler;
	private Map<StoredItemStack, StoredItemStack> items = new HashMap<>();
	public TileEntityStorageTerminal() {
		super(StorageMod.terminalTile);
	}

	@Override
	public Container createMenu(int id, PlayerInventory plInv, PlayerEntity arg2) {
		return new ContainerStorageTerminal(id, plInv, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("ts.storage_terminal.name");
	}

	public List<StoredItemStack> getStacks() {
		return new ArrayList<>(items.values());
	}

	public StoredItemStack pullStack(StoredItemStack stack, long max) {
		ItemStack st = stack.getStack();
		StoredItemStack ret = null;
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			ItemStack s = itemHandler.getStackInSlot(i);
			if(ItemStack.areItemsEqual(s, st)) {
				ItemStack pulled = itemHandler.extractItem(i, (int) max, false);
				if(!pulled.isEmpty()) {
					if(ret == null)ret = new StoredItemStack(pulled);
					else ret.grow(pulled.getCount());
					max -= pulled.getCount();
					if(max < 1)break;
				}
			}
		}
		return ret;
	}

	public StoredItemStack pushStack(StoredItemStack stack) {
		if(stack != null && itemHandler != null) {
			ItemStack is = ItemHandlerHelper.insertItemStacked(itemHandler, stack.getActualStack(), false);
			if(is.isEmpty())return null;
			else {
				return new StoredItemStack(is);
			}
		}
		return stack;
	}

	@Override
	public void tick() {
		if(!world.isRemote) {
			BlockState st = world.getBlockState(pos);
			Direction d = st.get(StorageTerminal.FACING);
			TileEntity invTile = world.getTileEntity(pos.offset(d));
			items.clear();
			if(invTile != null) {
				LazyOptional<IItemHandler> lih = invTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite());
				itemHandler = lih.orElse(null);
				if(itemHandler != null) {
					IntStream.range(0, itemHandler.getSlots()).mapToObj(itemHandler::getStackInSlot).filter(s -> !s.isEmpty()).
					map(StoredItemStack::new).forEach(s -> items.merge(s, s,
							(a, b) -> new StoredItemStack(a.getStack(), a.getQuantity() + b.getQuantity())));
				}
			}
		}
	}

}
