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
import net.minecraft.util.math.Vec3d;

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
		if(world.getGameTime() % 5 == 0){
			BlockState state = world.getBlockState(pos);
			Direction f = state.get(BlockStateProperties.FACING);
			BlockPos p = pos.offset(f);
			items = world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(p));
		}
	}

	@Override
	public int getSizeInventory() {
		return items.size() + 1;
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		if(items.size() > index){
			ItemEntity ent = items.get(index);
			if(ent.isAlive())return ent.getItem();
			else return ItemStack.EMPTY;
		}else return ItemStack.EMPTY;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
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
	public ItemStack removeStackFromSlot(int index) {
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
	public void setInventorySlotContents(int index, ItemStack stack) {
		BlockState state = world.getBlockState(pos);
		Direction f = Direction.UP;
		if(state.getBlock() == StorageMod.openCrate)f = state.get(BlockStateProperties.FACING);
		BlockPos p = pos.offset(f);
		ItemEntity entityitem = new ItemEntity(world, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, stack);
		entityitem.setDefaultPickupDelay();
		entityitem.setMotion(Vec3d.ZERO);
		world.addEntity(entityitem);
		items.add(entityitem);
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return true;
	}

	@Override
	public void openInventory(PlayerEntity player) {
	}

	@Override
	public void closeInventory(PlayerEntity player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return index == items.size();
	}

	@Override
	public void clear() {
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (!this.removed && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
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
	public void remove() {
		super.remove();
		if (chestHandler != null)
			chestHandler.invalidate();
	}
}
