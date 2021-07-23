package com.tom.storagemod.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.TickerUtil.TickableServer;

public class TileEntityOpenCrate extends BlockEntity implements TickableServer, Container {
	private List<ItemEntity> items = new ArrayList<>();
	private LazyOptional<IItemHandlerModifiable> chestHandler;

	public TileEntityOpenCrate(BlockPos pos, BlockState state) {
		super(StorageMod.openCrateTile, pos, state);
	}

	@Override
	public void updateServer() {
		if(level.getGameTime() % 5 == 0){
			BlockState state = level.getBlockState(worldPosition);
			Direction f = state.getValue(BlockStateProperties.FACING);
			BlockPos p = worldPosition.relative(f);
			items = level.getEntitiesOfClass(ItemEntity.class, new AABB(p));
		}
	}

	@Override
	public int getContainerSize() {
		return items.size() + 1;
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	@Override
	public ItemStack getItem(int index) {
		if(items.size() > index){
			ItemEntity ent = items.get(index);
			if(ent.isAlive())return ent.getItem();
			else return ItemStack.EMPTY;
		}else return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		if(items.size() > index){
			ItemEntity ent = items.get(index);
			if(ent.isAlive()){
				ItemStack s = ent.getItem().split(count);
				if(ent.getItem().isEmpty())ent.remove(RemovalReason.KILLED);
				return s;
			}
			else return ItemStack.EMPTY;
		}else return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		if(items.size() > index){
			ItemEntity ent = items.get(index);
			if(ent.isAlive()){
				ItemStack s = ent.getItem();
				ent.remove(RemovalReason.KILLED);
				return s;
			}
			else return ItemStack.EMPTY;
		}else return ItemStack.EMPTY;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		BlockState state = level.getBlockState(worldPosition);
		Direction f = Direction.UP;
		if(state.getBlock() == StorageMod.openCrate)f = state.getValue(BlockStateProperties.FACING);
		BlockPos p = worldPosition.relative(f);
		ItemEntity entityitem = new ItemEntity(level, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, stack);
		entityitem.setDefaultPickUpDelay();
		entityitem.setDeltaMovement(Vec3.ZERO);
		level.addFreshEntity(entityitem);
		items.add(entityitem);
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public void startOpen(Player player) {
	}

	@Override
	public void stopOpen(Player player) {
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return index == items.size();
	}

	@Override
	public void clearContent() {
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (!this.remove && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (this.chestHandler == null)
				this.chestHandler = LazyOptional.of(this::createHandler);
			return this.chestHandler.cast();
		}
		return super.getCapability(cap, side);
	}

	private IItemHandlerModifiable createHandler() {
		return new InvWrapper(this);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (chestHandler != null)
			chestHandler.invalidate();
	}
}
