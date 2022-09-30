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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.TickerUtil.TickableServer;

public class OpenCrateBlockEntity extends BlockEntity implements SidedStorageBlockEntity, TickableServer {
	private List<ItemEntity> items = new ArrayList<>();
	private Handler handler = new Handler();

	public OpenCrateBlockEntity(BlockPos pos, BlockState state) {
		super(StorageMod.openCrateTile, pos, state);
	}

	@Override
	public void updateServer() {
		if(world.getTime() % 5 == 0){
			BlockState state = world.getBlockState(pos);
			Direction f = state.get(Properties.FACING);
			BlockPos p = pos.offset(f);
			items = world.getNonSpectatingEntities(ItemEntity.class, new Box(p));
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
						BlockState state = world.getBlockState(pos);
						Direction f = Direction.UP;
						if(state.getBlock() == StorageMod.openCrate)f = state.get(Properties.FACING);
						BlockPos p = pos.offset(f);
						ItemEntity entityitem = new ItemEntity(world, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, s.spawn);
						entityitem.setToDefaultPickupDelay();
						entityitem.setVelocity(Vec3d.ZERO);
						world.spawnEntity(entityitem);
						items.add(entityitem);
					} else if(s.entity != null) {
						s.entity.getStack().split(s.remove);
						if(s.entity.getStack().isEmpty())s.entity.discard();
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
				if(resource.matches(e.getStack())) {
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
			state.remove = Math.min(maxAmount, ent.getStack().getCount());
			this.state.add(state);
			return state.remove;
		}

		@Override
		public Iterator<StorageView<ItemVariant>> iterator() {
			return items.stream().map(item -> (StorageView<ItemVariant>) new StorageView<ItemVariant>() {

				@Override
				public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
					return extract0(item, (int) maxAmount, transaction);
				}

				@Override
				public boolean isResourceBlank() {
					return item.getStack().isEmpty();
				}

				@Override
				public ItemVariant getResource() {
					return ItemVariant.of(item.getStack());
				}

				@Override
				public long getAmount() {
					return item.getStack().getCount();
				}

				@Override
				public long getCapacity() {
					return 64;
				}
			}).iterator();
		}
	}
}
