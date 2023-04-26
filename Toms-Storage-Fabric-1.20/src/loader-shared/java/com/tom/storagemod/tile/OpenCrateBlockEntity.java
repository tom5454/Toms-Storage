package com.tom.storagemod.tile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.tom.storagemod.Content;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public class OpenCrateBlockEntity extends BlockEntity implements SidedStorageBlockEntity, TickableServer {
	private List<ItemEntity> items = new ArrayList<>();
	private Handler handler = new Handler();

	public OpenCrateBlockEntity(BlockPos pos, BlockState state) {
		super(Content.openCrateTile.get(), pos, state);
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
	public Storage<ItemVariant> getItemStorage(Direction side) {
		return handler;
	}

	private class State {
		private ItemStack spawn;
		private ItemEntity entity;
		private int remove;
	}

	private class Handler extends SnapshotParticipant<List<State>> implements Storage<ItemVariant> {
		private List<State> state = new ArrayList<>();

		@Override
		protected List<State> createSnapshot() {
			return new ArrayList<>(state);
		}

		@Override
		protected void readSnapshot(List<State> snapshot) {
			this.state = new ArrayList<>(snapshot);
		}

		@Override
		protected void onFinalCommit() {
			if(!state.isEmpty()) {
				state.forEach(s -> {
					if(s.spawn != null) {
						BlockState state = level.getBlockState(worldPosition);
						Direction f = Direction.UP;
						if(state.getBlock() == Content.openCrate.get())f = state.getValue(BlockStateProperties.FACING);
						BlockPos p = worldPosition.relative(f);
						ItemEntity entityitem = new ItemEntity(level, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, s.spawn);
						entityitem.setDefaultPickUpDelay();
						entityitem.setDeltaMovement(Vec3.ZERO);
						level.addFreshEntity(entityitem);
						items.add(entityitem);
					} else if(s.entity != null) {
						s.entity.getItem().split(s.remove);
						if(s.entity.getItem().isEmpty())s.entity.discard();
					}
				});
			}
			state = new ArrayList<>();
		}

		@Override
		public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (maxAmount < 1) return 0;
			updateSnapshots(transaction);
			State state = new State();
			state.spawn = resource.toStack((int) maxAmount);
			this.state.add(state);
			return maxAmount;
		}

		@Override
		public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
			if (maxAmount < 1)return 0;
			long ext = 0;
			for (ItemEntity e : new ArrayList<>(items)) {
				if(resource.matches(e.getItem()) && e.isAlive()) {
					ext += extract0(e, (int) (maxAmount - ext), transaction);
					if(ext == maxAmount)break;
				}
			}
			return ext;
		}

		private long extract0(ItemEntity ent, int maxAmount, TransactionContext transaction) {
			if (maxAmount < 1)return 0;
			if (ent == null)return 0;
			updateSnapshots(transaction);
			State state = new State();
			state.entity = ent;
			state.remove = Math.min(maxAmount, ent.getItem().getCount());
			this.state.add(state);
			return state.remove;
		}

		@Override
		public Iterator<StorageView<ItemVariant>> iterator() {
			return items.stream().map(item -> (StorageView<ItemVariant>) new StorageView<ItemVariant>() {

				@Override
				public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
					if(!item.isAlive())return 0L;
					return extract0(item, (int) maxAmount, transaction);
				}

				@Override
				public boolean isResourceBlank() {
					return item.getItem().isEmpty();
				}

				@Override
				public ItemVariant getResource() {
					return ItemVariant.of(item.getItem());
				}

				@Override
				public long getAmount() {
					return item.getItem().getCount();
				}

				@Override
				public long getCapacity() {
					return 64;
				}
			}).iterator();
		}
	}
}
