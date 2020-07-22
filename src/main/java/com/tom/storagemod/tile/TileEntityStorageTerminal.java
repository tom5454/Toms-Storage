package com.tom.storagemod.tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.block.StorageTerminalBase;
import com.tom.storagemod.block.StorageTerminalBase.TerminalPos;
import com.tom.storagemod.gui.ContainerStorageTerminal;
import com.tom.storagemod.item.ItemWirelessTerminal;

public class TileEntityStorageTerminal extends BlockEntity implements NamedScreenHandlerFactory, Tickable {
	private IItemHandler itemHandler;
	private Map<StoredItemStack, StoredItemStack> items = new HashMap<>();
	private int sort;
	private String lastSearch = "";
	public TileEntityStorageTerminal() {
		super(StorageMod.terminalTile);
	}

	public TileEntityStorageTerminal(BlockEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	@Override
	public ScreenHandler createMenu(int id, PlayerInventory plInv, PlayerEntity arg2) {
		return new ContainerStorageTerminal(id, plInv, this);
	}

	@Override
	public Text getDisplayName() {
		return new TranslatableText("ts.storage_terminal");
	}

	public List<StoredItemStack> getStacks() {
		return new ArrayList<>(items.values());
	}

	public StoredItemStack pullStack(StoredItemStack stack, long max) {
		ItemStack st = stack.getStack();
		StoredItemStack ret = null;
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			ItemStack s = itemHandler.getStackInSlot(i);
			if(ItemStack.areItemsEqual(s, st) && ItemStack.areTagsEqual(s, st)) {
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

	public ItemStack pushStack(ItemStack itemstack) {
		StoredItemStack is = pushStack(new StoredItemStack(itemstack));
		return is == null ? ItemStack.EMPTY : is.getActualStack();
	}

	public void pushOrDrop(ItemStack st) {
		if(st.isEmpty())return;
		StoredItemStack st0 = pushStack(new StoredItemStack(st));
		if(st0 != null) {
			ItemScatterer.spawn(world, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, st0.getActualStack());
		}
	}

	@Override
	public void tick() {
		if(!world.isClient) {
			BlockState st = world.getBlockState(pos);
			Direction d = st.get(StorageTerminalBase.FACING);
			TerminalPos p = st.get(StorageTerminalBase.TERMINAL_POS);
			if(p == TerminalPos.UP)d = Direction.UP;
			if(p == TerminalPos.DOWN)d = Direction.DOWN;
			items.clear();
			Inventory inv = HopperBlockEntity.getInventoryAt(world, pos.offset(d));
			if(inv != null) {
				itemHandler = InvWrapper.wrap(inv, d.getOpposite());
				IntStream.range(0, itemHandler.getSlots()).mapToObj(itemHandler::getStackInSlot).filter(s -> !s.isEmpty()).
				map(StoredItemStack::new).forEach(s -> items.merge(s, s,
						(a, b) -> new StoredItemStack(a.getStack(), a.getQuantity() + b.getQuantity())));
			}
		}
	}

	public boolean canInteractWith(PlayerEntity player) {
		if(world.getBlockEntity(pos) != this)return false;
		double dist = ItemWirelessTerminal.isPlayerHolding(player) ? StorageMod.CONFIG.wirelessRange*2*StorageMod.CONFIG.wirelessRange*2 : 64;
		return !(player.squaredDistanceTo(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) > dist);
	}

	public int getSorting() {
		return sort;
	}

	public void setSorting(int newC) {
		sort = newC;
	}

	@Override
	public CompoundTag toTag(CompoundTag compound) {
		compound.putInt("sort", sort);
		return super.toTag(compound);
	}

	@Override
	public void fromTag(BlockState st, CompoundTag compound) {
		sort = compound.getInt("sort");
		super.fromTag(st, compound);
	}

	public String getLastSearch() {
		return lastSearch;
	}

	public void setLastSearch(String string) {
		lastSearch = string;
	}
}
