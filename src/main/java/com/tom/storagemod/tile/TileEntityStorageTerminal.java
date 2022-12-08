package com.tom.storagemod.tile;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.TickerUtil.TickableServer;
import com.tom.storagemod.block.StorageTerminalBase;
import com.tom.storagemod.block.StorageTerminalBase.TerminalPos;
import com.tom.storagemod.gui.ContainerStorageTerminal;
import com.tom.storagemod.item.WirelessTerminal;
import com.tom.storagemod.util.PlayerInvUtil;
import com.tom.storagemod.util.Util;

public class TileEntityStorageTerminal extends BlockEntity implements NamedScreenHandlerFactory, TickableServer {
	private Storage<ItemVariant> itemHandler;
	private Map<StoredItemStack, Long> items = new HashMap<>();
	private int sort;
	private String lastSearch = "";
	private boolean updateItems;
	private int beaconLevel;

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
			itemHandler = ItemStorage.SIDED.find(world, pos.offset(d), d.getOpposite());
			if(itemHandler == null) {
				Inventory inv = HopperBlockEntity.getInventoryAt(world, pos.offset(d));
				if(inv != null)itemHandler = InventoryStorage.of(inv, d.getOpposite());
			}
			if(itemHandler != null) {
				try (Transaction transaction = Transaction.openOuter()) {
					Util.stream(itemHandler.iterator(transaction)).
					filter(s -> !s.isResourceBlank()).distinct().map(s -> new StoredItemStack(s.getResource().toStack(), s.getAmount())).
					forEach(s -> items.merge(s, s.getQuantity(), (a, b) -> a + b));
				}
			}
			updateItems = false;
		}
		if(world.getTime() % 40 == 5) {
			beaconLevel = BlockPos.stream(new Box(pos).expand(8)).mapToInt(p -> {
				if(world.canSetBlock(p)) {
					BlockState st = world.getBlockState(p);
					if(st.isOf(Blocks.BEACON)) {
						return TileEntityInventoryCableConnector.calcBeaconLevel(world, p.getX(), p.getY(), p.getZ());
					}
				}
				return 0;
			}).max().orElse(0);
		}
	}

	public boolean canInteractWith(PlayerEntity player) {
		if(world.getBlockEntity(pos) != this)return false;
		int d = 4;
		int termReach = PlayerInvUtil.findItem(player, i -> i.getItem() instanceof WirelessTerminal, 0, i -> ((WirelessTerminal)i.getItem()).getRange(player, i));
		if(beaconLevel >= StorageMod.CONFIG.wirelessTermBeaconLvl && termReach > 0) {
			if(beaconLevel >= StorageMod.CONFIG.wirelessTermBeaconLvlDim)return true;
			else return player.getWorld() == world;
		}
		d = Math.max(d, termReach);
		return player.getWorld() == world && !(player.squaredDistanceTo(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) > d*2*d*2);
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
