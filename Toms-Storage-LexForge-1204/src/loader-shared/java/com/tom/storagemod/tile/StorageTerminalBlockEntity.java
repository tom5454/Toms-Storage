package com.tom.storagemod.tile;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import com.tom.storagemod.Config;
import com.tom.storagemod.Content;
import com.tom.storagemod.block.AbstractStorageTerminalBlock;
import com.tom.storagemod.block.AbstractStorageTerminalBlock.TerminalPos;
import com.tom.storagemod.gui.StorageTerminalMenu;
import com.tom.storagemod.item.WirelessTerminal;
import com.tom.storagemod.util.PlayerInvUtil;
import com.tom.storagemod.util.StoredItemStack;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public class StorageTerminalBlockEntity extends BlockEntity implements MenuProvider, TickableServer {
	private IItemHandler itemHandler;
	private Map<StoredItemStack, Long> items = new HashMap<>();
	private int sort;
	private String lastSearch = "";
	private boolean updateItems;
	private int beaconLevel;

	public StorageTerminalBlockEntity(BlockPos pos, BlockState state) {
		super(Content.terminalTile.get(), pos, state);
	}

	public StorageTerminalBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory plInv, Player arg2) {
		return new StorageTerminalMenu(id, plInv, this);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("ts.storage_terminal");
	}

	public Map<StoredItemStack, Long> getStacks() {
		updateItems = true;
		return items;
	}

	public StoredItemStack pullStack(StoredItemStack stack, long max) {
		if(stack != null && itemHandler != null && max > 0) {
			ItemStack st = stack.getStack();
			StoredItemStack ret = null;
			for (int i = itemHandler.getSlots() - 1; i >= 0; i--) {
				ItemStack s = itemHandler.getStackInSlot(i);
				if(ItemStack.isSameItemSameTags(s, st)) {
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
			Containers.dropItemStack(level, worldPosition.getX() + .5f, worldPosition.getY() + .5f, worldPosition.getZ() + .5f, st0.getActualStack());
		}
	}

	@Override
	public void updateServer() {
		if(updateItems) {
			BlockState st = level.getBlockState(worldPosition);
			Direction d = st.getValue(AbstractStorageTerminalBlock.FACING);
			TerminalPos p = st.getValue(AbstractStorageTerminalBlock.TERMINAL_POS);
			if(p == TerminalPos.UP)d = Direction.UP;
			if(p == TerminalPos.DOWN)d = Direction.DOWN;
			BlockEntity invTile = level.getBlockEntity(worldPosition.relative(d));
			items.clear();
			if(invTile != null) {
				LazyOptional<IItemHandler> lih = invTile.getCapability(ForgeCapabilities.ITEM_HANDLER, d.getOpposite());
				itemHandler = lih.orElse(null);
				if(itemHandler != null) {
					IntStream.range(0, itemHandler.getSlots()).mapToObj(itemHandler::getStackInSlot).filter(s -> !s.isEmpty()).
					map(StoredItemStack::new).forEach(s -> items.merge(s, s.getQuantity(), (a, b) -> a + b));
				}
			}
			updateItems = false;
		}
		if(level.getGameTime() % 40 == 5) {
			beaconLevel = BlockPos.betweenClosedStream(new AABB(worldPosition).inflate(8)).mapToInt(p -> {
				if(level.isLoaded(p)) {
					BlockState st = level.getBlockState(p);
					if(st.is(Blocks.BEACON)) {
						return InventoryCableConnectorBlockEntity.calcBeaconLevel(level, p.getX(), p.getY(), p.getZ());
					}
				}
				return 0;
			}).max().orElse(0);
		}
	}

	public boolean canInteractWith(Player player) {
		if(level.getBlockEntity(worldPosition) != this)return false;
		int d = 4;
		int termReach = PlayerInvUtil.findItem(player, i -> i.getItem() instanceof WirelessTerminal, 0, i -> ((WirelessTerminal)i.getItem()).getRange(player, i));
		if(Config.get().wirelessTermBeaconLvl != -1 && beaconLevel >= Config.get().wirelessTermBeaconLvl && termReach > 0) {
			if(Config.get().wirelessTermBeaconLvlDim != -1 && beaconLevel >= Config.get().wirelessTermBeaconLvlDim)return true;
			else return player.level() == level;
		}
		d = Math.max(d, termReach);
		return player.level() == level && !(player.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) > d*2*d*2);
	}

	public int getSorting() {
		return sort;
	}

	public void setSorting(int newC) {
		sort = newC;
	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		compound.putInt("sort", sort);
	}

	@Override
	public void load(CompoundTag compound) {
		sort = compound.getInt("sort");
		super.load(compound);
	}

	public String getLastSearch() {
		return lastSearch;
	}

	public void setLastSearch(String string) {
		lastSearch = string;
	}

	public int getBeaconLevel() {
		return beaconLevel;
	}
}
