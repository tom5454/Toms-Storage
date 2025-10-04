package com.tom.storagemod.block.entity;

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

import com.tom.storagemod.Content;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public class OpenCrateBlockEntity extends BlockEntity implements TickableServer, Container {
	private List<ItemEntity> items = new ArrayList<>();

	public OpenCrateBlockEntity(BlockPos pos, BlockState state) {
		super(Content.openCrateBE.get(), pos, state);
	}

	@Override
	public void updateServer() {
		if(level.getGameTime() % 5 == Math.abs(worldPosition.hashCode()) % 5){
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
		if(state.getBlock() == Content.openCrate.get())f = state.getValue(BlockStateProperties.FACING);
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
	public boolean canPlaceItem(int index, ItemStack stack) {
		return index == items.size();
	}

	@Override
	public void clearContent() {
	}
}
