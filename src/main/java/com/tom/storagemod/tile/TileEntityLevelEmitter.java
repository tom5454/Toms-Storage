package com.tom.storagemod.tile;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.TickerUtil.TickableServer;
import com.tom.storagemod.block.BlockInventoryCableConnector;
import com.tom.storagemod.block.BlockLevelEmitter;
import com.tom.storagemod.block.IInventoryCable;
import com.tom.storagemod.gui.ContainerLevelEmitter;

public class TileEntityLevelEmitter extends BlockEntity implements TickableServer, MenuProvider {
	private ItemStack filter = ItemStack.EMPTY;
	private int count;
	private LazyOptional<IItemHandler> top;
	private boolean lessThan;

	public TileEntityLevelEmitter(BlockPos pos, BlockState state) {
		super(StorageMod.levelEmitterTile.get(), pos, state);
	}

	@Override
	public void updateServer() {
		if(level.getGameTime() % 20 == 1) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(BlockInventoryCableConnector.FACING);
			Stack<BlockPos> toCheck = new Stack<>();
			Set<BlockPos> checkedBlocks = new HashSet<>();
			checkedBlocks.add(worldPosition);
			BlockPos up = worldPosition.relative(facing.getOpposite());
			state = level.getBlockState(up);
			if(state.getBlock() instanceof IInventoryCable) {
				top = null;
				toCheck.add(up);
				while(!toCheck.isEmpty()) {
					BlockPos cp = toCheck.pop();
					if(!checkedBlocks.contains(cp)) {
						checkedBlocks.add(cp);
						if(level.isLoaded(cp)) {
							state = level.getBlockState(cp);
							if(state.getBlock() == StorageMod.connector.get()) {
								BlockEntity te = level.getBlockEntity(cp);
								if(te instanceof TileEntityInventoryConnector) {
									top = ((TileEntityInventoryConnector) te).getInventory();
								}
								break;
							}
							if(state.getBlock() instanceof IInventoryCable) {
								toCheck.addAll(((IInventoryCable)state.getBlock()).next(level, state, cp));
							}
						}
						if(checkedBlocks.size() > Config.invConnectorMax)break;
					}
				}
			} else {
				if(top == null || !top.isPresent()) {
					BlockEntity te = level.getBlockEntity(up);
					if(te != null) {
						top = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
					}
				}
			}
		}
		if(!level.isClientSide && level.getGameTime() % 10 == 2 && top != null) {
			BlockState state = level.getBlockState(worldPosition);
			boolean p = state.getValue(BlockLevelEmitter.POWERED);
			boolean currState = false;
			IItemHandler top = this.top.orElse(EmptyHandler.INSTANCE);
			if(!filter.isEmpty()) {
				int counter = 0;
				for (int i = 0; i < top.getSlots(); i++) {
					ItemStack inSlot = top.getStackInSlot(i);
					if(!ItemStack.isSame(inSlot, getFilter()) || !ItemStack.tagMatches(inSlot, getFilter())) {
						continue;
					}
					counter += inSlot.getCount();
				}
				if(lessThan) {
					currState = counter < count;
				} else {
					currState = counter > count;
				}
			} else {
				currState = false;
			}
			if(currState != p) {
				level.setBlock(worldPosition, state.setValue(BlockLevelEmitter.POWERED, Boolean.valueOf(currState)), 3);

				Direction direction = state.getValue(BlockLevelEmitter.FACING);
				BlockPos blockpos = worldPosition.relative(direction);
				if (!ForgeEventFactory.onNeighborNotify(level, worldPosition, level.getBlockState(worldPosition), EnumSet.of(direction), false).isCanceled()) {
					level.neighborChanged(blockpos, state.getBlock(), worldPosition);
					level.updateNeighborsAtExceptFromFacing(blockpos, state.getBlock(), direction.getOpposite());
				}
			}
		}
	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		compound.put("Filter", getFilter().save(new CompoundTag()));
		compound.putInt("Count", count);
		compound.putBoolean("lessThan", lessThan);
	}

	@Override
	public void load(CompoundTag nbtIn) {
		super.load(nbtIn);
		setFilter(ItemStack.of(nbtIn.getCompound("Filter")));
		count = nbtIn.getInt("Count");
		lessThan = nbtIn.getBoolean("lessThan");
	}

	public void setFilter(ItemStack filter) {
		this.filter = filter;
	}

	public ItemStack getFilter() {
		return filter;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public void setLessThan(boolean lessThan) {
		this.lessThan = lessThan;
	}

	public boolean isLessThan() {
		return lessThan;
	}

	@Override
	public AbstractContainerMenu createMenu(int p_createMenu_1_, Inventory p_createMenu_2_, Player p_createMenu_3_) {
		return new ContainerLevelEmitter(p_createMenu_1_, p_createMenu_2_, this);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("ts.level_emitter");
	}

	public boolean stillValid(Player p_59619_) {
		if (this.level.getBlockEntity(this.worldPosition) != this) {
			return false;
		} else {
			return !(p_59619_.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) > 64.0D);
		}
	}
}
