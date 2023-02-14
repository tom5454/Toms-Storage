package com.tom.storagemod.tile;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import com.tom.storagemod.Content;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.AbstractStorageTerminalBlock;
import com.tom.storagemod.block.AbstractStorageTerminalBlock.TerminalPos;
import com.tom.storagemod.gui.StorageTerminalMenu;
import com.tom.storagemod.item.WirelessTerminal;
import com.tom.storagemod.util.PlayerInvUtil;
import com.tom.storagemod.util.StoredItemStack;
import com.tom.storagemod.util.TickerUtil.TickableServer;
import com.tom.storagemod.util.Util;

public class StorageTerminalBlockEntity extends BlockEntity implements MenuProvider, TickableServer {
	private Storage<ItemVariant> itemHandler;
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
		ItemStack st = stack.getStack();
		if(itemHandler == null)return null;

		try (Transaction transaction = Transaction.openOuter()) {
			long ext = itemHandler.extract(ItemVariant.of(stack.getStack()), max, transaction);
			if(ext > 0) {
				transaction.commit();
				return new StoredItemStack(st, ext);
			}
		}
		return null;
	}

	public StoredItemStack pushStack(StoredItemStack stack) {
		if(stack != null && itemHandler != null) {
			try (Transaction transaction = Transaction.openOuter()) {
				long ins = itemHandler.insert(ItemVariant.of(stack.getStack()), stack.getQuantity(), transaction);
				if(ins == 0)return stack;
				transaction.commit();
				if(ins == stack.getQuantity())return null;
				return new StoredItemStack(stack.getStack(), stack.getQuantity() - ins);
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
			items.clear();
			itemHandler = ItemStorage.SIDED.find(level, worldPosition.relative(d), d.getOpposite());
			if(itemHandler == null) {
				Container inv = HopperBlockEntity.getContainerAt(level, worldPosition.relative(d));
				if(inv != null)itemHandler = InventoryStorage.of(inv, d.getOpposite());
			}
			if(itemHandler != null) {
				Util.stream(itemHandler.iterator()).
				filter(s -> !s.isResourceBlank()).distinct().map(s -> new StoredItemStack(s.getResource().toStack(), s.getAmount())).
				forEach(s -> items.merge(s, s.getQuantity(), (a, b) -> a + b));
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
		if(beaconLevel >= StorageMod.CONFIG.wirelessTermBeaconLvl && termReach > 0) {
			if(beaconLevel >= StorageMod.CONFIG.wirelessTermBeaconLvlDim)return true;
			else return player.getLevel() == level;
		}
		d = Math.max(d, termReach);
		return player.getLevel() == level && !(player.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) > d*2*d*2);
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
