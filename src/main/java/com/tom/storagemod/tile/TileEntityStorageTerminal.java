package com.tom.storagemod.tile;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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
import com.tom.storagemod.item.WirelessTerminal;
import com.tom.storagemod.util.PlayerInvUtil;

public class TileEntityStorageTerminal extends TileEntity implements INamedContainerProvider, ITickableTileEntity {
	private IItemHandler itemHandler;
	private Map<StoredItemStack, Long> items = new HashMap<>();
	private int sort;
	private String lastSearch = "";
	private boolean updateItems;
	private int beaconLevel;

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

	public Map<StoredItemStack, Long> getStacks() {
		updateItems = true;
		return items;
	}

	public StoredItemStack pullStack(StoredItemStack stack, long max) {
		if(stack != null && itemHandler != null && max > 0) {
			ItemStack st = stack.getStack();
			StoredItemStack ret = null;
			for (int i = 0; i < itemHandler.getSlots(); i++) {
				ItemStack s = itemHandler.getStackInSlot(i);
				if(ItemStack.isSame(s, st) && ItemStack.tagMatches(s, st)) {
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
			InventoryHelper.dropItemStack(level, worldPosition.getX() + .5f, worldPosition.getY() + .5f, worldPosition.getZ() + .5f, st0.getActualStack());
		}
	}

	@Override
	public void tick() {
		if(!level.isClientSide) {
			if (updateItems) {
				BlockState st = level.getBlockState(worldPosition);
				Direction d = st.getValue(StorageTerminalBase.FACING);
				TerminalPos p = st.getValue(StorageTerminalBase.TERMINAL_POS);
				if(p == TerminalPos.UP)d = Direction.UP;
				if(p == TerminalPos.DOWN)d = Direction.DOWN;
				TileEntity invTile = level.getBlockEntity(worldPosition.relative(d));
				items.clear();
				if(invTile != null) {
					LazyOptional<IItemHandler> lih = invTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite());
					itemHandler = lih.orElse(null);
					if(itemHandler != null) {
						IntStream.range(0, itemHandler.getSlots()).mapToObj(itemHandler::getStackInSlot).filter(s -> !s.isEmpty()).
						map(StoredItemStack::new).forEach(s -> items.merge(s, s.getQuantity(), (a, b) -> a + b));
					}
				}
				updateItems = false;
			}
			if(level.getGameTime() % 40 == 5) {
				beaconLevel = BlockPos.betweenClosedStream(new AxisAlignedBB(worldPosition).inflate(8)).mapToInt(p -> {
					if(level.isLoaded(p)) {
						BlockState st = level.getBlockState(p);
						if(st.is(Blocks.BEACON)) {
							return calcBeaconLevel(p.getX(), p.getY(), p.getZ());
						}
					}
					return 0;
				}).max().orElse(0);
			}
		}
	}

	public boolean canInteractWith(PlayerEntity player) {
		if(level.getBlockEntity(worldPosition) != this)return false;
		int termReach = PlayerInvUtil.findItem(player, i -> i.getItem() instanceof WirelessTerminal, 0, i -> ((WirelessTerminal)i.getItem()).getRange(player, i));
		if(Config.wirelessTermBeaconLvl != -1 && beaconLevel >= Config.wirelessTermBeaconLvl && termReach > 0) {
			if(Config.wirelessTermBeaconLvlDim != -1 && beaconLevel >= Config.wirelessTermBeaconLvlDim)return true;
			else return player.level == level;
		}
		int d = Math.max(termReach, 4);
		return player.level == level && !(player.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) > d*2*d*2);
	}

	public int getSorting() {
		return sort;
	}

	public void setSorting(int newC) {
		sort = newC;
	}

	@Override
	public CompoundNBT save(CompoundNBT compound) {
		compound.putInt("sort", sort);
		return super.save(compound);
	}

	@Override
	public void load(BlockState st, CompoundNBT compound) {
		sort = compound.getInt("sort");
		super.load(st, compound);
	}

	public String getLastSearch() {
		return lastSearch;
	}

	public void setLastSearch(String string) {
		lastSearch = string;
	}

	private int calcBeaconLevel(int x, int y, int z) {
		int i = 0;

		TileEntity ent = level.getBlockEntity(new BlockPos(x, y, z));
		if(ent instanceof BeaconTileEntity) {
			BeaconTileEntity b = (BeaconTileEntity) ent;
			if(b.getLevels() == 0)return 0;

			for(int j = 1; j <= 4; i = j++) {
				int k = y - j;
				if (k < 0) {
					break;
				}

				boolean flag = true;

				for(int l = x - j; l <= x + j && flag; ++l) {
					for(int i1 = z - j; i1 <= z + j; ++i1) {
						if (!level.getBlockState(new BlockPos(l, k, i1)).is(BlockTags.BEACON_BASE_BLOCKS)) {
							flag = false;
							break;
						}
					}
				}

				if (!flag) {
					break;
				}
			}
		}
		return i;
	}
}
