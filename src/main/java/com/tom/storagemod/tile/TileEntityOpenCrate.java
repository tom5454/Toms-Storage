package com.tom.storagemod.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import com.tom.storagemod.StorageMod;

public class TileEntityOpenCrate extends BlockEntity implements Tickable, Inventory {
	private List<ItemEntity> items = new ArrayList<>();

	public TileEntityOpenCrate() {
		super(StorageMod.openCrateTile);
	}

	@Override
	public void tick() {
		if(world.getTime() % 5 == 0){
			BlockState state = world.getBlockState(pos);
			Direction f = state.get(Properties.FACING);
			BlockPos p = pos.offset(f);
			items = world.getNonSpectatingEntities(ItemEntity.class, new Box(p));
		}
	}

	@Override
	public int size() {
		return items.size() + 1;
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	@Override
	public ItemStack getStack(int index) {
		if(items.size() > index){
			ItemEntity ent = items.get(index);
			if(ent.isAlive())return ent.getStack();
			else return ItemStack.EMPTY;
		}else return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStack(int index, int count) {
		if(items.size() > index){
			ItemEntity ent = items.get(index);
			if(ent.isAlive()){
				ItemStack s = ent.getStack().split(count);
				if(ent.getStack().isEmpty())ent.remove();
				return s;
			}
			else return ItemStack.EMPTY;
		}else return ItemStack.EMPTY;
	}

	@Override
	public ItemStack removeStack(int index) {
		if(items.size() > index){
			ItemEntity ent = items.get(index);
			if(ent.isAlive()){
				ItemStack s = ent.getStack();
				ent.remove();
				return s;
			}
			else return ItemStack.EMPTY;
		}else return ItemStack.EMPTY;
	}

	@Override
	public void setStack(int index, ItemStack stack) {
		BlockState state = world.getBlockState(pos);
		Direction f = Direction.UP;
		if(state.getBlock() == StorageMod.openCrate)f = state.get(Properties.FACING);
		BlockPos p = pos.offset(f);
		ItemEntity entityitem = new ItemEntity(world, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, stack);
		entityitem.setToDefaultPickupDelay();
		entityitem.setVelocity(Vec3d.ZERO);
		world.spawnEntity(entityitem);
		items.add(entityitem);
	}

	@Override
	public boolean isValid(int index, ItemStack stack) {
		return index == items.size();
	}

	@Override
	public void clear() {
	}

	@Override
	public boolean canPlayerUse(PlayerEntity paramPlayerEntity) {
		return true;
	}
}
