package com.tom.storagemod.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;

import com.tom.storagemod.StorageMod;

public class TileEntityOpenCrate extends TileEntity implements ITickableTileEntity, IInventory {
	private List<ItemEntity> items = new ArrayList<>();
	private LazyOptional<IItemHandlerModifiable> chestHandler;

	public TileEntityOpenCrate() {
		super(StorageMod.openCrateTile);
	}

	@Override
	public void tick() {
		if(level.getGameTime() % 5 == 0){
			BlockState state = level.getBlockState(worldPosition);
			Direction f = state.getValue(BlockStateProperties.FACING);
			BlockPos p = worldPosition.relative(f);
			items = level.getEntitiesOfClass(ItemEntity.class, new AxisAlignedBB(p));
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
				if(ent.getItem().isEmpty())ent.remove();
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
				ent.remove();
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
		entityitem.setDeltaMovement(Vector3d.ZERO);
		level.addFreshEntity(entityitem);
		items.add(entityitem);
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return true;
	}

	@Override
	public void startOpen(PlayerEntity player) {
	}

	@Override
	public void stopOpen(PlayerEntity player) {
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
