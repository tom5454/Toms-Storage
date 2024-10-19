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

import com.tom.storagemod.StorageTags;
import com.tom.storagemod.inventory.filter.IFilter;
import com.tom.storagemod.inventory.filter.ItemFilter;
import com.tom.storagemod.inventory.filter.ItemPredicate;
import com.tom.storagemod.item.IItemFilter;
import com.tom.storagemod.util.BlockFaceReference;
import com.tom.storagemod.util.Priority;

public class BlockFilter implements IFilter {
	private BlockPos pos;
	private Direction side;
	private Set<BlockPos> connected;
	public SimpleContainer filter = new SimpleContainer(1);
	private boolean skip, keepLast;
	private Priority priority;
	private ItemFilter itemFilter = ItemFilter.TRUE;
	private boolean filterNeedsUpdate = true;
	private boolean multiblockFilled;

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
		multiblockFilled = true;
	}

	public void dropContents(LevelAccessor level, BlockPos pos2) {
		if (level instanceof Level lvl) {
			filter.removeAllItems().forEach(f -> Block.popResource(lvl, pos2, f));
		}
	}

	public IInventoryAccess wrap(Level level, IInventoryAccess acc) {
		ItemStack filter = this.filter.getItem(0);
		if (filterNeedsUpdate || !itemFilter.configMatch(filter)) {
			if (filter.getItem() instanceof IItemFilter f) {
				itemFilter = f.createFilter(new BlockFaceReference(level, pos, side), filter);
			} else
				itemFilter = ItemFilter.TRUE;
			filterNeedsUpdate = false;
		}
		if (acc instanceof PlatformFilteredInventoryAccess f) {
			MultiFilter mf = new MultiFilter(f.getFilter(), this);
			return new PlatformFilteredInventoryAccess(f.getActualInventory(), mf);
		}
		return new PlatformFilteredInventoryAccess(acc, this);
	}

	public boolean skip() {
		return skip;
	}

	public static BlockFilter findBlockFilterAt(Level level, BlockPos pos) {
		return BlockPos.betweenClosedStream(new AABB(pos).inflate(8)).
				map(p -> getFilterAt(level, p)).
				filter(p -> p != null && p.getConnectedBlocks().contains(pos)).findFirst().
				orElseGet(() -> {
					if (level.getBlockState(pos).is(StorageTags.INV_CONFIG_SKIP))return null;
					return getOrCreateFilterAt(level, pos);
				});
	}

	public static BlockFilter getFilterAt(Level level, BlockPos pos) {
		return PlatformInventoryAccess.getBlockFilterAt(level, pos, false);
	}

	public static BlockFilter getOrCreateFilterAt(Level level, BlockPos pos) {
		var f = PlatformInventoryAccess.getBlockFilterAt(level, pos, true);
		if (f != null)f.fillMultiblock(level);
		return f;
	}

	@Override
	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	@Override
	public ItemPredicate getItemPred() {
		return itemFilter;
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

	@Override
	public boolean isKeepLast() {
		return keepLast;
	}

	public void setKeepLast(boolean keepLast) {
		this.keepLast = keepLast;
	}

	public void markFilterDirty() {
		filterNeedsUpdate = true;
	}

	private void fillMultiblock(Level level) {
		if (multiblockFilled)return;
		VanillaMultiblockInventories.checkChest(level, pos, level.getBlockState(pos), p -> addConnected(level, p));
		multiblockFilled = true;
	}

	@Override
	public String toString() {
		return "BlockFilter@" + pos;
	}
}
