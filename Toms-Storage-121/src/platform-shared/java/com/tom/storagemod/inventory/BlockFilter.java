package com.tom.storagemod.inventory;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import com.tom.storagemod.inventory.filter.ItemPredicate;
import com.tom.storagemod.item.IItemFilter;
import com.tom.storagemod.util.BlockFace;
import com.tom.storagemod.util.Priority;

public class BlockFilter {
	private BlockPos pos;
	private Direction side;
	private Set<BlockPos> connected;
	public SimpleContainer filter = new SimpleContainer(1);
	private boolean skip, keepLast;
	private Priority priority;
	private ItemPredicate itemPred = ItemPredicate.TRUE;
	private boolean filterNeedsUpdate = true;

	public BlockFilter(BlockPos pos) {
		this.pos = pos;
		side = Direction.DOWN;
		priority = Priority.NORMAL;
		connected = new HashSet<>();
		connected.add(pos);
		filter.addListener(__ -> markFilterDirty());
	}

	public Set<BlockPos> getConnectedBlocks() {
		return connected;
	}

	public BlockPos getMainPos() {
		return pos;
	}

	public Direction getSide() {
		return side;
	}

	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		ListTag conn = new ListTag();
		connected.forEach(e -> {
			CompoundTag t = new CompoundTag();
			t.putInt("x", e.getX() - pos.getX());
			t.putInt("y", e.getY() - pos.getY());
			t.putInt("z", e.getZ() - pos.getZ());
			conn.add(t);
		});
		tag.put("connected", conn);
		tag.putBoolean("skip", skip);
		tag.putString("side", side.getSerializedName());
		if (!filter.getItem(0).isEmpty())
			tag.put("filter", filter.getItem(0).save(provider));
		tag.putInt("priority", priority.ordinal());
		tag.putBoolean("keepLast", keepLast);
		return tag;
	}

	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		ListTag conn = tag.getList("connected", Tag.TAG_COMPOUND);
		for (int i = 0;i<conn.size();i++) {
			CompoundTag t = conn.getCompound(i);
			int x = t.getInt("x") + pos.getX();
			int y = t.getInt("y") + pos.getY();
			int z = t.getInt("z") + pos.getZ();
			connected.add(new BlockPos(x, y, z));
		}
		skip = tag.getBoolean("skip");
		side = Direction.byName(tag.getString("side"));
		filter.setItem(0, ItemStack.parseOptional(provider, tag.getCompound("filter")));
		priority = Priority.VALUES[Math.abs(tag.getInt("priority")) % Priority.VALUES.length];
		keepLast = tag.getBoolean("keepLast");
	}

	public void dropContents(LevelAccessor level, BlockPos pos2) {
		if (level instanceof Level lvl) {
			filter.removeAllItems().forEach(f -> Block.popResource(lvl, pos2, f));
		}
	}

	public IInventoryAccess wrap(Level level, IInventoryAccess acc) {
		ItemStack filter = this.filter.getItem(0);
		if (filterNeedsUpdate || !itemPred.configMatch(filter)) {
			if (filter.getItem() instanceof IItemFilter f) {
				itemPred = f.createFilter(new BlockFace(level, pos, side), filter);
			} else
				itemPred = ItemPredicate.TRUE;
			filterNeedsUpdate = false;
		}
		return new PlatformFilteredInventoryAccess(acc, this);
	}

	public boolean skip() {
		return skip;
	}

	public static BlockFilter findBlockFilterAt(Level level, BlockPos pos) {
		return BlockPos.betweenClosedStream(new AABB(pos).inflate(8)).
				map(p -> PlatformInventoryAccess.getBlockFilterAt(level, p, false)).
				filter(p -> p != null && p.getConnectedBlocks().contains(pos)).findFirst().
				orElseGet(() -> PlatformInventoryAccess.getBlockFilterAt(level, pos, true));
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public ItemPredicate getItemPred() {
		return itemPred;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	public void setSide(Direction side) {
		this.side = side;
	}

	public void addConnected(Level level, BlockPos pos) {
		if (!this.pos.equals(pos))
			PlatformInventoryAccess.removeBlockFilterAt(level, pos);
		connected.add(pos.immutable());
	}

	public boolean isKeepLast() {
		return keepLast;
	}

	public void setKeepLast(boolean keepLast) {
		this.keepLast = keepLast;
	}

	public void markFilterDirty() {
		filterNeedsUpdate = true;
	}
}
