package com.tom.storagemod.tile;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.BlockInventoryCableConnector;
import com.tom.storagemod.block.BlockLevelEmitter;
import com.tom.storagemod.block.IInventoryCable;
import com.tom.storagemod.gui.ContainerLevelEmitter;

public class TileEntityLevelEmitter extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
	private ItemStack filter = ItemStack.EMPTY;
	private int count;
	private LazyOptional<IItemHandler> top;
	private boolean lessThan;

	public TileEntityLevelEmitter() {
		super(StorageMod.levelEmitterTile);
	}

	@Override
	public void tick() {
		if(!world.isRemote && world.getGameTime() % 20 == 1) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(BlockInventoryCableConnector.FACING);
			Stack<BlockPos> toCheck = new Stack<>();
			Set<BlockPos> checkedBlocks = new HashSet<>();
			checkedBlocks.add(pos);
			BlockPos up = pos.offset(facing.getOpposite());
			state = world.getBlockState(up);
			if(state.getBlock() instanceof IInventoryCable) {
				top = null;
				toCheck.add(up);
				while(!toCheck.isEmpty()) {
					BlockPos cp = toCheck.pop();
					if(!checkedBlocks.contains(cp)) {
						checkedBlocks.add(cp);
						if(world.isBlockLoaded(cp)) {
							state = world.getBlockState(cp);
							if(state.getBlock() == StorageMod.connector) {
								TileEntity te = world.getTileEntity(cp);
								if(te instanceof TileEntityInventoryConnector) {
									top = ((TileEntityInventoryConnector) te).getInventory();
								}
								break;
							}
							if(state.getBlock() instanceof IInventoryCable) {
								toCheck.addAll(((IInventoryCable)state.getBlock()).next(world, state, cp));
							}
						}
						if(checkedBlocks.size() > Config.invConnectorMax)break;
					}
				}
			} else {
				if(top == null || !top.isPresent()) {
					TileEntity te = world.getTileEntity(up);
					if(te != null) {
						top = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
					}
				}
			}
		}
		if(!world.isRemote && world.getGameTime() % 10 == 2 && top != null) {
			BlockState state = world.getBlockState(pos);
			boolean p = state.get(BlockLevelEmitter.POWERED);
			boolean currState = false;
			IItemHandler top = this.top.orElse(EmptyHandler.INSTANCE);
			if(!filter.isEmpty()) {
				int counter = 0;
				for (int i = 0; i < top.getSlots(); i++) {
					ItemStack inSlot = top.getStackInSlot(i);
					if(!ItemStack.areItemsEqual(inSlot, getFilter()) || !ItemStack.areItemStackTagsEqual(inSlot, getFilter())) {
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
				world.setBlockState(pos, state.with(BlockLevelEmitter.POWERED, Boolean.valueOf(currState)), 3);

				Direction direction = state.get(BlockLevelEmitter.FACING);
				BlockPos blockpos = pos.offset(direction);
				if (!ForgeEventFactory.onNeighborNotify(world, pos, world.getBlockState(pos), EnumSet.of(direction), false).isCanceled()) {
					world.neighborChanged(blockpos, state.getBlock(), pos);
					world.notifyNeighborsOfStateExcept(blockpos, state.getBlock(), direction.getOpposite());
				}
			}
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("Filter", getFilter().write(new CompoundNBT()));
		compound.putInt("Count", count);
		compound.putBoolean("lessThan", lessThan);
		return super.write(compound);
	}

	@Override
	public void read(BlockState stateIn, CompoundNBT nbtIn) {
		super.read(stateIn, nbtIn);
		setFilter(ItemStack.read(nbtIn.getCompound("Filter")));
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
	public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
		return new ContainerLevelEmitter(p_createMenu_1_, p_createMenu_2_, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent("ts.level_emitter");
	}
}
