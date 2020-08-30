package com.tom.storagemod.tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.block.StorageTerminalBase;
import com.tom.storagemod.block.StorageTerminalBase.TerminalPos;
import com.tom.storagemod.gui.ContainerStorageTerminal;
import com.tom.storagemod.item.ItemWirelessTerminal;

public class TileEntityStorageTerminal extends TileEntity implements INamedContainerProvider, ITickableTileEntity {
	private IItemHandler itemHandler;
	private Map<StoredItemStack, StoredItemStack> items = new HashMap<>();
	private int sort;
	private String lastSearch = "";
	public TileEntityStorageTerminal() {
		super(StorageMod.terminalTile);
	}

	public TileEntityStorageTerminal(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public Container createMenu(int id, PlayerInventory plInv, PlayerEntity arg2) {
		return new ContainerStorageTerminal(id, plInv, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("ts.storage_terminal");
	}

	public List<StoredItemStack> getStacks() {
		return new ArrayList<>(items.values());
	}

	public StoredItemStack pullStack(StoredItemStack stack, long max) {
		if(stack != null && itemHandler != null && max > 0) {
			ItemStack st = stack.getStack();
			StoredItemStack ret = null;
			for (int i = 0; i < itemHandler.getSlots(); i++) {
				ItemStack s = itemHandler.getStackInSlot(i);
				if(ItemStack.areItemsEqual(s, st) && ItemStack.areItemStackTagsEqual(s, st)) {
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
		return null;
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

	public ItemStack pushStack(ItemStack itemstack) {
		StoredItemStack is = pushStack(new StoredItemStack(itemstack));
		return is == null ? ItemStack.EMPTY : is.getActualStack();
	}

	public void pushOrDrop(ItemStack st) {
		if(st.isEmpty())return;
		StoredItemStack st0 = pushStack(new StoredItemStack(st));
		if(st0 != null) {
			InventoryHelper.spawnItemStack(world, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, st0.getActualStack());
		}
	}

	@Override
	public void tick() {
		if(!world.isRemote) {
			BlockState st = world.getBlockState(pos);
			Direction d = st.get(StorageTerminalBase.FACING);
			TerminalPos p = st.get(StorageTerminalBase.TERMINAL_POS);
			if(p == TerminalPos.UP)d = Direction.UP;
			if(p == TerminalPos.DOWN)d = Direction.DOWN;
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

	public boolean canInteractWith(PlayerEntity player) {
		if(world.getTileEntity(pos) != this)return false;
		double dist = ItemWirelessTerminal.isPlayerHolding(player) ? Config.wirelessRange*2*Config.wirelessRange*2 : 64;
		return !(player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) > dist);
	}

	public int getSorting() {
		return sort;
	}

	public void setSorting(int newC) {
		sort = newC;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.putInt("sort", sort);
		return super.write(compound);
	}

	@Override
	public void read(CompoundNBT compound) {
		sort = compound.getInt("sort");
		super.read(compound);
	}

	public String getLastSearch() {
		return lastSearch;
	}

	public void setLastSearch(String string) {
		lastSearch = string;
	}
}
