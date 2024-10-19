package com.tom.storagemod.inventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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

	private static record BlockFilterState(BlockPos pos, List<BlockPos> connected, boolean skip, Direction side, ItemStack filter, Priority priority, boolean keepLast) {}

	private static final Codec<BlockFilterState> STATE_CODEC = RecordCodecBuilder.<BlockFilterState>mapCodec(b -> {
		return b.group(
				BlockPos.CODEC.fieldOf("pos").forGetter(BlockFilterState::pos),
				Codec.list(BlockPos.CODEC).fieldOf("connected").forGetter(BlockFilterState::connected),
				Codec.BOOL.fieldOf("skip").forGetter(BlockFilterState::skip),
				Direction.CODEC.fieldOf("side").forGetter(BlockFilterState::side),
				ItemStack.OPTIONAL_CODEC.fieldOf("filter").forGetter(BlockFilterState::filter),
				Priority.CODEC.fieldOf("priority").forGetter(BlockFilterState::priority),
				Codec.BOOL.fieldOf("keep_last").forGetter(BlockFilterState::keepLast)
				).apply(b, BlockFilterState::new);
	}).codec();
	public static final Codec<BlockFilter> CODEC = STATE_CODEC.xmap(BlockFilter::new, BlockFilter::storeState);

	public BlockFilter(BlockPos pos) {
		this.pos = pos;
		side = Direction.DOWN;
		priority = Priority.NORMAL;
		connected = new HashSet<>();
		connected.add(pos);
		filter.addListener(__ -> markFilterDirty());
	}

	public BlockFilter(BlockFilterState state) {
		this.pos = state.pos();
		this.connected = new HashSet<>(state.connected());
		this.skip = state.skip();
		this.side = state.side();
		filter.setItem(0, state.filter());
		this.priority = state.priority();
		this.keepLast = state.keepLast();
		multiblockFilled = true;

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

	public BlockFilterState storeState() {
		return new BlockFilterState(pos, new ArrayList<>(connected), skip, side, filter.getItem(0).copy(), priority, keepLast);
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
