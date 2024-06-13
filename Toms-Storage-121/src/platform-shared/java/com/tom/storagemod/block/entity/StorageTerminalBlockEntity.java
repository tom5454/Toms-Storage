package com.tom.storagemod.block.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import com.tom.storagemod.Config;
import com.tom.storagemod.Content;
import com.tom.storagemod.block.AbstractStorageTerminalBlock;
import com.tom.storagemod.block.AbstractStorageTerminalBlock.TerminalPos;
import com.tom.storagemod.inventory.IInventoryAccess;
import com.tom.storagemod.inventory.IInventoryAccess.IInventoryChangeTracker;
import com.tom.storagemod.inventory.NetworkInventory;
import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.item.WirelessTerminal;
import com.tom.storagemod.menu.StorageTerminalMenu;
import com.tom.storagemod.platform.PlatformBlockEntity;
import com.tom.storagemod.util.PlayerInvUtil;
import com.tom.storagemod.util.TermRangeCalc;
import com.tom.storagemod.util.TickerUtil.TickableServer;
import com.tom.storagemod.util.Util;

public class StorageTerminalBlockEntity extends PlatformBlockEntity implements MenuProvider, TickableServer {
	private NetworkInventory itemCache = new NetworkInventory();
	private Map<StoredItemStack, StoredItemStack> items = new HashMap<>();
	private int sort;
	private String lastSearch = "";
	private boolean updateItems;
	private int changeCount;
	private int beaconLevel;
	private long changeTracker;

	public StorageTerminalBlockEntity(BlockPos pos, BlockState state) {
		super(Content.terminalBE.get(), pos, state);
	}

	public StorageTerminalBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (!level.isClientSide) {
			BlockState st = level.getBlockState(worldPosition);
			Direction d = st.getValue(AbstractStorageTerminalBlock.FACING);
			TerminalPos p = st.getValue(AbstractStorageTerminalBlock.TERMINAL_POS);
			if(p == TerminalPos.UP)d = Direction.UP;
			if(p == TerminalPos.DOWN)d = Direction.DOWN;
			itemCache.onLoad(level, worldPosition.relative(d), d.getOpposite(), this);
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory plInv, Player arg2) {
		return new StorageTerminalMenu(id, plInv, this);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("menu.toms_storage.storage_terminal");
	}

	public Map<StoredItemStack, StoredItemStack> getStacks() {
		updateItems = true;
		return items;
	}

	public StoredItemStack pullStack(StoredItemStack stack, long max) {
		if (stack == null)return null;
		IInventoryAccess ii = itemCache.getAccess(level, worldPosition);
		ItemStack ex = ii.pullMatchingStack(stack.getStack(), max);
		if (ex.isEmpty())return null;
		return new StoredItemStack(ex);
	}

	public StoredItemStack pushStack(StoredItemStack stack) {
		if (stack == null)return null;
		IInventoryAccess ii = itemCache.getAccess(level, worldPosition);
		ItemStack r = ii.pushStack(stack.getActualStack());
		if (r.isEmpty())return null;
		return new StoredItemStack(r);
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
			IInventoryAccess ii = itemCache.getAccess(level, worldPosition);
			IInventoryChangeTracker tr = ii.tracker();
			long ct = tr.getChangeTracker(level);
			if (changeTracker != ct) {
				changeTracker = ct;

				if (Config.get().runMultithreaded) {
					items = tr.streamWrappedStacks(true).collect(Collectors.groupingByConcurrent(Function.identity(), Util.reducingWithCopy(null, StoredItemStack::merge, StoredItemStack::new)));
				} else {
					items = new HashMap<>();
					tr.streamWrappedStacks(false).forEach(s -> items.merge(s, s, StoredItemStack::merge));
					items.replaceAll((k, v) -> new StoredItemStack(v));
				}
				changeCount++;
				var c = new StoredItemStack(new ItemStack(Items.BARRIER), changeCount);
				items.put(c, c);
			}
			updateItems = false;
		}
		if(level.getGameTime() % 40 == 5) {
			beaconLevel = BlockPos.betweenClosedStream(new AABB(worldPosition).inflate(8)).mapToInt(p -> {
				if(level.isLoaded(p)) {
					BlockState st = level.getBlockState(p);
					if(st.is(Blocks.BEACON)) {
						return TermRangeCalc.calcBeaconLevel(level, p.getX(), p.getY(), p.getZ());
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
			if(Config.get().wirelessTermBeaconLvlCrossDim != -1 && beaconLevel >= Config.get().wirelessTermBeaconLvlCrossDim)return true;
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
	public void saveAdditional(CompoundTag compound, HolderLookup.Provider provider) {
		super.saveAdditional(compound, provider);
		compound.putInt("sort", sort);
	}

	@Override
	public void loadAdditional(CompoundTag compound, HolderLookup.Provider provider) {
		sort = compound.getInt("sort");
		super.loadAdditional(compound, provider);
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

	public int getChangeCount() {
		return changeCount;
	}
}
