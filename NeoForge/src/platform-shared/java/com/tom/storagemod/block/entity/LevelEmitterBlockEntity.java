package com.tom.storagemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.LevelEmitterBlock;
import com.tom.storagemod.inventory.IInventoryAccess.IInventoryChangeTracker;
import com.tom.storagemod.inventory.NetworkInventory;
import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.menu.LevelEmitterMenu;
import com.tom.storagemod.platform.Platform;
import com.tom.storagemod.platform.PlatformBlockEntity;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public class LevelEmitterBlockEntity extends PlatformBlockEntity implements TickableServer, MenuProvider {
	private StoredItemStack filter;
	private int count = 1;
	private NetworkInventory topCache = new NetworkInventory();
	private boolean lessThan;
	private long changeTracker;

	public LevelEmitterBlockEntity(BlockPos pos, BlockState state) {
		super(Content.levelEmitterBE.get(), pos, state);
	}

	@Override
	public void updateServer() {
		if (level.getGameTime() % 10 != Math.abs(worldPosition.hashCode()) % 10)return;
		BlockState state = level.getBlockState(worldPosition);
		boolean p = state.getValue(LevelEmitterBlock.POWERED);
		boolean currState = false;
		if(filter != null) {
			IInventoryChangeTracker tr = topCache.getAccess(level, worldPosition).tracker();
			long ct = tr.getChangeTracker(level);
			if (ct != changeTracker) {
				changeTracker = ct;
				long counter = tr.countItems(getFilter());
				if(lessThan) {
					currState = counter < count;
				} else {
					currState = counter > count;
				}
			} else return;
		} else {
			currState = false;
		}
		if(currState != p) {
			level.setBlock(worldPosition, state.setValue(LevelEmitterBlock.POWERED, Boolean.valueOf(currState)), 3);

			Direction direction = state.getValue(LevelEmitterBlock.FACING);
			BlockPos blockpos = worldPosition.relative(direction);
			if (Platform.notifyBlocks(level, worldPosition, direction)) {
				final Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(level, direction,
						direction.getAxis().isHorizontal() ? Direction.UP : direction);
				level.updateNeighborsAt(blockpos, state.getBlock(), orientation);
				level.updateNeighborsAt(blockpos.relative(direction), state.getBlock(), orientation);
			}
		}
	}

	@Override
	public void saveAdditional(ValueOutput compound) {
		super.saveAdditional(compound);
		StoredItemStack f = getFilter();
		if (f != null) {
			ItemStack is = f.getStack();
			if (!is.isEmpty())
				compound.store("Filter", ItemStack.CODEC, is);
		}
		compound.putInt("Count", count);
		compound.putBoolean("lessThan", lessThan);
	}

	@Override
	public void loadAdditional(ValueInput nbtIn) {
		super.loadAdditional(nbtIn);
		setFilter(nbtIn.read("Filter", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
		count = nbtIn.getIntOr("Count", 0);
		lessThan = nbtIn.getBooleanOr("lessThan", false);
	}

	public void setFilter(ItemStack filter) {
		this.filter = new StoredItemStack(filter);
		changeTracker = 0L;
	}

	public StoredItemStack getFilter() {
		return filter;
	}

	public void setCount(int count) {
		this.count = count;
		changeTracker = 0L;
	}

	public int getCount() {
		return count;
	}

	public void setLessThan(boolean lessThan) {
		this.lessThan = lessThan;
		changeTracker = 0L;
	}

	public boolean isLessThan() {
		return lessThan;
	}

	@Override
	public AbstractContainerMenu createMenu(int p_createMenu_1_, Inventory p_createMenu_2_, Player p_createMenu_3_) {
		return new LevelEmitterMenu(p_createMenu_1_, p_createMenu_2_, this);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("menu.toms_storage.level_emitter");
	}

	public boolean stillValid(Player p_59619_) {
		if (this.level.getBlockEntity(this.worldPosition) != this) {
			return false;
		} else {
			return !(p_59619_.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) > 64.0D);
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (!level.isClientSide()) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(BlockStateProperties.FACING);
			topCache.onLoad(level, worldPosition.relative(facing.getOpposite()), facing, this);
		}
	}
}
