package com.tom.storagemod.tile;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.BlockInventoryCableConnector;
import com.tom.storagemod.block.BlockLevelEmitter;
import com.tom.storagemod.block.IInventoryCable;
import com.tom.storagemod.gui.ContainerLevelEmitter;
import com.tom.storagemod.util.EmptyHandler;
import com.tom.storagemod.util.IItemHandler;
import com.tom.storagemod.util.InventoryWrapper;

public class TileEntityLevelEmitter extends BlockEntity implements Tickable, NamedScreenHandlerFactory {
	private ItemStack filter = ItemStack.EMPTY;
	private int count;
	private InventoryWrapper top;
	private boolean lessThan;

	public TileEntityLevelEmitter() {
		super(StorageMod.levelEmitterTile);
	}

	@Override
	public void tick() {
		if(!world.isClient && world.getTime() % 20 == 1) {
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
						if(world.isChunkLoaded(cp)) {
							state = world.getBlockState(cp);
							if(state.getBlock() == StorageMod.connector) {
								BlockEntity te = world.getBlockEntity(cp);
								if(te instanceof TileEntityInventoryConnector) {
									top = ((TileEntityInventoryConnector) te).getInventory();
								}
								break;
							}
							if(state.getBlock() instanceof IInventoryCable) {
								toCheck.addAll(((IInventoryCable)state.getBlock()).next(world, state, cp));
							}
						}
						if(checkedBlocks.size() > StorageMod.CONFIG.invConnectorMaxCables)break;
					}
				}
			} else {
				BlockEntity te = world.getBlockEntity(up);
				if(te instanceof Inventory) {
					top = new InventoryWrapper((Inventory) te, facing);
				} else {
					top = null;
				}
			}
		}
		if(!world.isClient && world.getTime() % 10 == 2 && top != null) {
			BlockState state = world.getBlockState(pos);
			boolean p = state.get(BlockLevelEmitter.POWERED);
			boolean currState = false;
			IItemHandler top = this.top == null ? EmptyHandler.INSTANCE : this.top.wrap();
			if(!filter.isEmpty()) {
				int counter = 0;
				for (int i = 0; i < top.getSlots(); i++) {
					ItemStack inSlot = top.getStackInSlot(i);
					if(!ItemStack.areItemsEqual(inSlot, getFilter()) || !ItemStack.areTagsEqual(inSlot, getFilter())) {
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
				BlockPos blockPos = pos.offset(direction.getOpposite());

				world.updateNeighbor(blockPos, state.getBlock(), pos);
				world.updateNeighborsExcept(blockPos, state.getBlock(), direction);
			}
		}
	}

	@Override
	public NbtCompound writeNbt(NbtCompound compound) {
		compound.put("Filter", getFilter().writeNbt(new NbtCompound()));
		compound.putInt("Count", count);
		compound.putBoolean("lessThan", lessThan);
		return super.writeNbt(compound);
	}

	@Override
	public void fromTag(BlockState stateIn, NbtCompound nbtIn) {
		super.fromTag(stateIn, nbtIn);
		setFilter(ItemStack.fromNbt(nbtIn.getCompound("Filter")));
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
	public ScreenHandler createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
		return new ContainerLevelEmitter(p_createMenu_1_, p_createMenu_2_, this);
	}

	@Override
	public Text getDisplayName() {
		return new TranslatableText("ts.level_emitter");
	}
}
