package com.tom.storagemod.inventory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.tom.storagemod.Config;
import com.tom.storagemod.block.entity.IInventoryConnector;
import com.tom.storagemod.inventory.filter.ItemPredicate;
import com.tom.storagemod.util.Priority;
import com.tom.storagemod.util.Priority.IPriority;
import com.tom.storagemod.util.WorldStates;

public abstract class MultiInventoryAccess implements IInventoryAccess {
	protected List<IInventoryAccess> connected = new ArrayList<>();
	protected MultiChangeTracker tracker = new MultiChangeTracker();

	public MultiInventoryAccess() {
		WorldStates.trackers.put(getPlatformHandler(), tracker());
	}

	public void build(IInventoryConnector self, Collection<IInventoryConnector> connectors) {
		connected.clear();
		Queue<IInventoryConnector> q = new ArrayDeque<>();
		q.add(self);
		q.addAll(connectors);
		Set<IInventoryConnector> all = new HashSet<>();
		while (!q.isEmpty()) {
			IInventoryConnector ic = q.poll();
			if (all.add(ic)) {
				q.addAll(ic.getConnectedConnectors());
			}
		}
		var map = all.stream().flatMap(c -> c.getConnectedInventories().stream()).
				collect(Collectors.groupingBy(IPriority.GETTER, () -> new EnumMap<>(Priority.class), Collectors.toList()));
		Set<IInventoryAccess> allRoots = new HashSet<>();
		allRoots.add(this);
		for (int i = Priority.VALUES.length - 1; i >= 0; i--) {
			for (IInventoryAccess a : map.getOrDefault(Priority.VALUES[i], Collections.emptyList())) {
				IInventoryAccess root = a.getRootHandler();
				if (root instanceof MultiInventoryAccess)continue;//skip
				if (allRoots.add(root))
					connected.add(a);
			}
		}
		refresh();
	}

	@Override
	public ItemStack pullMatchingStack(ItemStack st, long max) {
		ItemStack res = ItemStack.EMPTY;
		int c = 0;
		for (int i = 0;i<connected.size();i++) {
			ItemStack p = connected.get(i).pullMatchingStack(st, max - c);
			if (p.isEmpty())continue;
			if (res.isEmpty())res = p;
			c += p.getCount();
			if (c >= max)break;
		}
		res.setCount(c);
		return res;
	}

	@Override
	public ItemStack pushStack(ItemStack stack) {
		for (int i = 0;i<connected.size();i++) {
			stack = connected.get(i).pushStack(stack);
			if (stack.isEmpty())return ItemStack.EMPTY;
		}
		return stack;
	}

	@Override
	public IInventoryChangeTracker tracker() {
		return tracker;
	}

	@Override
	public int getFreeSlotCount() {
		int c = 0;
		for (int i = 0;i<connected.size();i++) {
			c += connected.get(i).getFreeSlotCount();
		}
		return c;
	}

	@Override
	public int getSlotCount() {
		int c = 0;
		for (int i = 0;i<connected.size();i++) {
			c += connected.get(i).getSlotCount();
		}
		return c;
	}

	public void clear() {
		connected.clear();
	}

	public int getInventoryCount() {
		return connected.size();
	}

	protected void refresh() {
	}

	@Override
	public IInventoryAccess getRootHandler() {
		return this;
	}

	private static class ItemList extends ArrayList<StoredItemStack> {
		private static final long serialVersionUID = 6690277901361998268L;
	}

	public static class TrackerInfo {
		private int id;
		private IMultiThreadedTracker<Object, Object> mt;
		private Object prep, result;
		private long tracker;

		public TrackerInfo(int id, IMultiThreadedTracker<?, ?> mt, Object prep) {
			this.id = id;
			this.mt = (IMultiThreadedTracker<Object, Object>) mt;
			this.prep = prep;
		}

		public void run() {
			result = mt.processOffThread(prep);
		}

		public void finish(Level level) {
			tracker = mt.finishOffThreadProcess(level, result);
		}
	}

	protected class MultiChangeTracker implements IInventoryChangeTracker {
		private long lastUpdate, lastChange;
		private long[] trackers = new long[0];
		private ItemList[] items = new ItemList[0];

		private boolean multithreadProcessing(Level level) {
			int size = connected.size();
			List<TrackerInfo> infos = new ArrayList<>(size);
			boolean ch = false;
			for (int i = 0;i<connected.size();i++) {
				IInventoryChangeTracker tr = connected.get(i).tracker();
				if (tr instanceof IMultiThreadedTracker mt) {
					Object prep = mt.prepForOffThread(level);
					if (prep != null) {
						infos.add(new TrackerInfo(i, mt, prep));
						continue;
					}
				}
				long v = tr.getChangeTracker(level);
				if (v != trackers[i]) {
					ch |= true;
					items[i] = null;
					trackers[i] = v;
				}
			}
			infos.parallelStream().unordered().peek(TrackerInfo::run).toArray();
			for (TrackerInfo tr : infos) {
				tr.finish(level);

				int i = tr.id;
				long v = tr.tracker;
				if (v != trackers[i]) {
					ch |= true;
					items[i] = null;
					trackers[i] = v;
				}
			}
			return ch;
		}

		@Override
		public long getChangeTracker(Level level) {
			if (lastUpdate != level.getGameTime()) {
				if (trackers.length != connected.size()) {
					trackers = new long[connected.size()];
					items = new ItemList[connected.size()];
					for (int i = 0;i<connected.size();i++) {
						IInventoryChangeTracker tr = connected.get(i).tracker();
						trackers[i] = tr.getChangeTracker(level);
					}
					lastChange = System.nanoTime();
					return lastChange;
				}
				boolean ch = false;
				if (Config.get().runMultithreaded) {
					ch = multithreadProcessing(level);
				} else {
					for (int i = 0;i<connected.size();i++) {
						IInventoryAccess ia = connected.get(i);
						long v = ia.tracker().getChangeTracker(level);
						if (v != trackers[i]) {
							ch |= true;
							items[i] = null;
							trackers[i] = v;
						}
					}
				}
				if (ch)lastChange = System.nanoTime();
				lastUpdate = level.getGameTime();
			}
			return lastChange;
		}

		@Override
		public Stream<StoredItemStack> streamWrappedStacks(boolean parallel) {
			var str = IntStream.range(0, items.length).mapToObj(i -> {
				if (items[i] == null) {
					items[i] = connected.get(i).tracker().streamWrappedStacks(false).collect(Collectors.toCollection(ItemList::new));
				}
				return items[i];
			});
			if (parallel)return str.parallel().flatMap(List::parallelStream).unordered();
			else return str.flatMap(List::stream);
		}

		@Override
		public long countItems(StoredItemStack filter) {
			long c = 0;
			for (int i = 0;i<connected.size();i++) {
				IInventoryAccess ia = connected.get(i);
				c += ia.tracker().countItems(filter);
			}
			return c;
		}

		@Override
		public InventorySlot findSlot(ItemPredicate filter, boolean findEmpty) {
			for (int i = 0;i<connected.size();i++) {
				IInventoryAccess ia = connected.get(i);
				InventorySlot is = ia.tracker().findSlot(filter, findEmpty);
				if (is != null)return is;
			}
			return null;
		}

		@Override
		public InventorySlot findSlotDest(StoredItemStack forStack) {
			for (int i = 0;i<connected.size();i++) {
				IInventoryAccess ia = connected.get(i);
				InventorySlot is = ia.tracker().findSlotDest(forStack);
				if (is != null)return is;
			}
			return null;
		}

		@Override
		public InventorySlot findSlotAfter(InventorySlot slot, ItemPredicate filter, boolean findEmpty, boolean loop) {
			for (int i = 0;i<connected.size();i++) {
				IInventoryAccess ia = connected.get(i);
				InventorySlot is = ia.tracker().findSlotAfter(slot, filter, findEmpty, loop);
				if (is != null)return is;
			}
			return null;
		}

		@Override
		public InventorySlot findSlotDestAfter(InventorySlot slot, StoredItemStack forStack, boolean loop) {
			for (int i = 0;i<connected.size();i++) {
				IInventoryAccess ia = connected.get(i);
				InventorySlot is = ia.tracker().findSlotDestAfter(slot, forStack, loop);
				if (is != null)return is;
			}
			return null;
		}
	}

	public Collection<IInventoryAccess> getConnected() {
		return connected;
	}
}
