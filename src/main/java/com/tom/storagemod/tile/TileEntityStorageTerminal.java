package com.tom.storagemod.tile;

import java.util.HashMap;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.TickerUtil.TickableServer;
import com.tom.storagemod.block.StorageTerminalBase;
import com.tom.storagemod.block.StorageTerminalBase.TerminalPos;
import com.tom.storagemod.gui.ContainerStorageTerminal;
import com.tom.storagemod.item.WirelessTerminal;
import com.tom.storagemod.util.IItemHandler;
import com.tom.storagemod.util.InvWrapper;
import com.tom.storagemod.util.ItemHandlerHelper;

public class TileEntityStorageTerminal extends BlockEntity implements NamedScreenHandlerFactory, TickableServer {
	private IItemHandler itemHandler;
	private Map<StoredItemStack, Long> items = new HashMap<>();
	private int sort;
	private String lastSearch = "";
	private boolean updateItems;
	public TileEntityStorageTerminal(BlockPos pos, BlockState state) {
		super(StorageMod.terminalTile, pos, state);
	}

	public TileEntityStorageTerminal(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public ScreenHandler createMenu(int id, PlayerInventory plInv, PlayerEntity arg2) {
		return new ContainerStorageTerminal(id, plInv, this);
	}

	@Override
	public Text getDisplayName() {
		return new TranslatableText("ts.storage_terminal");
	}

	public Map<StoredItemStack, Long> getStacks() {
		updateItems = true;
		return items;
	}

	public StoredItemStack pullStack(StoredItemStack stack, long max) {
		ItemStack st = stack.getStack();
		StoredItemStack ret = null;
		if(itemHandler == null)return null;
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			ItemStack s = itemHandler.getStackInSlot(i);
			if(ItemStack.areItemsEqual(s, st) && ItemStack.areNbtEqual(s, st)) {
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
	public void updateServer() {
		if(updateItems) {
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
				map(StoredItemStack::new).forEach(s -> items.merge(s, s.getQuantity(), (a, b) -> a + b));
			}
			updateItems = false;
		}
	}

	public boolean canInteractWith(PlayerEntity player) {
		if(world.getBlockEntity(pos) != this)return false;
		int d = 4;
		if(player.getMainHandStack().getItem() instanceof WirelessTerminal)d = Math.max(d, ((WirelessTerminal)player.getMainHandStack().getItem()).getRange(player, player.getMainHandStack()));
		if(player.getOffHandStack().getItem() instanceof WirelessTerminal)d = Math.max(d, ((WirelessTerminal)player.getOffHandStack().getItem()).getRange(player, player.getOffHandStack()));
		return !(player.squaredDistanceTo(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) > d*2*d*2);
	}

	public int getSorting() {
		return sort;
	}

	public void setSorting(int newC) {
		sort = newC;
	}

	@Override
	public void writeNbt(NbtCompound compound) {
		compound.putInt("sort", sort);
	}

	@Override
	public void readNbt(NbtCompound compound) {
		sort = compound.getInt("sort");
		super.readNbt(compound);
	}

	public String getLastSearch() {
		return lastSearch;
	}

	public void setLastSearch(String string) {
		lastSearch = string;
	}
}
