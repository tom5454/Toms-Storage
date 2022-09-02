package com.tom.storagemod.tile;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.TickerUtil.TickableServer;
import com.tom.storagemod.block.IInventoryCable;
import com.tom.storagemod.block.InventoryCableConnectorBlock;
import com.tom.storagemod.block.LevelEmitterBlock;
import com.tom.storagemod.gui.LevelEmitterMenu;

public class LevelEmitterBlockEntity extends BlockEntity implements TickableServer, NamedScreenHandlerFactory {
	private ItemStack filter = ItemStack.EMPTY;
	private int count;
	private Storage<ItemVariant> top;
	private boolean lessThan;

	public LevelEmitterBlockEntity(BlockPos pos, BlockState state) {
		super(StorageMod.levelEmitterTile, pos, state);
	}

	@Override
	public void updateServer() {
		if(world.getTime() % 20 == 1) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(InventoryCableConnectorBlock.FACING);
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
						if(world.canSetBlock(cp)) {
							state = world.getBlockState(cp);
							if(state.getBlock() == StorageMod.connector) {
								BlockEntity te = world.getBlockEntity(cp);
								if(te instanceof InventoryConnectorBlockEntity) {
									top = ((InventoryConnectorBlockEntity) te).getInventory();
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
				top = ItemStorage.SIDED.find(world, up, state, world.getBlockEntity(up), facing);
			}
		}
		if(world.getTime() % 10 == 2 && top != null) {
			BlockState state = world.getBlockState(pos);
			boolean p = state.get(LevelEmitterBlock.POWERED);
			boolean currState = false;
			if(!filter.isEmpty()) {
				long counter = top.simulateExtract(ItemVariant.of(filter), count + 1, null);

				if(lessThan) {
					currState = counter < count;
				} else {
					currState = counter > count;
				}
			} else {
				currState = false;
			}
			if(currState != p) {
				world.setBlockState(pos, state.with(LevelEmitterBlock.POWERED, Boolean.valueOf(currState)), 3);

				Direction direction = state.get(LevelEmitterBlock.FACING);
				BlockPos blockPos = pos.offset(direction.getOpposite());

				world.updateNeighbor(blockPos, state.getBlock(), pos);
				world.updateNeighborsExcept(blockPos, state.getBlock(), direction);
			}
		}
	}

	@Override
	public void writeNbt(NbtCompound compound) {
		compound.put("Filter", getFilter().writeNbt(new NbtCompound()));
		compound.putInt("Count", count);
		compound.putBoolean("lessThan", lessThan);
	}

	@Override
	public void readNbt(NbtCompound nbtIn) {
		super.readNbt(nbtIn);
		filter = ItemStack.fromNbt(nbtIn.getCompound("Filter"));
		count = nbtIn.getInt("Count");
		lessThan = nbtIn.getBoolean("lessThan");
	}

	public void setFilter(ItemStack filter) {
		this.filter = filter;
		markDirty();
	}

	public ItemStack getFilter() {
		return filter;
	}

	public void setCount(int count) {
		this.count = count;
		markDirty();
	}

	public int getCount() {
		return count;
	}

	public void setLessThan(boolean lessThan) {
		this.lessThan = lessThan;
		markDirty();
	}

	public boolean isLessThan() {
		return lessThan;
	}

	@Override
	public ScreenHandler createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
		return new LevelEmitterMenu(p_createMenu_1_, p_createMenu_2_, this);
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable("ts.level_emitter");
	}

	public boolean stillValid(PlayerEntity p_59619_) {
		if (this.world.getBlockEntity(this.pos) != this) {
			return false;
		} else {
			return !(p_59619_.squaredDistanceTo(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) > 64.0D);
		}
	}
}
