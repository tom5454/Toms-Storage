package com.tom.storagemod.block.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Config;
import com.tom.storagemod.Content;
import com.tom.storagemod.block.InventoryCableConnectorBlock;
import com.tom.storagemod.inventory.BlockFilter;
import com.tom.storagemod.inventory.IInventoryAccess;
import com.tom.storagemod.inventory.IInventoryConnectorReference;
import com.tom.storagemod.inventory.IInventoryLink;
import com.tom.storagemod.inventory.InventoryCableNetwork;
import com.tom.storagemod.inventory.MultiInventoryAccess;
import com.tom.storagemod.inventory.PlatformInventoryAccess;
import com.tom.storagemod.inventory.PlatformInventoryAccess.BlockInventoryAccess;
import com.tom.storagemod.inventory.PlatformMultiInventoryAccess;
import com.tom.storagemod.inventory.RemoteConnections;
import com.tom.storagemod.inventory.RemoteConnections.Channel;
import com.tom.storagemod.menu.InventoryLinkMenu;
import com.tom.storagemod.util.BeaconLevelCalc;
import com.tom.storagemod.util.TickerUtil.TickableServer;

public class InventoryCableConnectorBlockEntity extends PaintedBlockEntity implements MenuProvider, TickableServer, IInventoryConnector, IInventoryLink {
	private static final String CHANNEL_TAG = "channel";
	private BlockInventoryAccess block = new BlockInventoryAccess();
	private MultiInventoryAccess mergedHandler = new PlatformMultiInventoryAccess();
	private Set<IInventoryConnector> linkedConnectors = new HashSet<>();
	private Collection<IInventoryAccess> filteredMerge = Collections.emptyList();
	private IInventoryAccess self = block;
	private UUID channel = null;
	private int beaconLevel = -1;

	public InventoryCableConnectorBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
		super(Content.cableConnectorBE.get(), p_155229_, p_155230_);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		if (!level.isClientSide) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(InventoryCableConnectorBlock.FACING);
			block.onLoad(level, worldPosition.relative(facing), facing.getOpposite(), this);
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
		return new InventoryLinkMenu(p_39954_, p_39955_, this);
	}

	@Override
	public void updateServer() {
		long time = level.getGameTime();
		if(time % 20 == Math.abs(worldPosition.hashCode()) % 20) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(InventoryCableConnectorBlock.FACING);
			BlockPos pos = worldPosition.relative(facing);
			BlockState st = level.getBlockState(pos);
			mergedHandler.clear();
			linkedConnectors.clear();
			beaconLevel = -1;
			detectCableNetwork();
			if (st.is(Blocks.BEACON)) {
				beaconLevel = BeaconLevelCalc.calcBeaconLevel(level, pos.getX(), pos.getY(), pos.getZ());
				self = mergedHandler;
				if (channel != null) {
					Channel chn = RemoteConnections.get(level).getChannel(channel);
					if(chn != null) {
						chn.register((ServerLevel) level, worldPosition);
						Set<IInventoryLink> links = chn.findOthers((ServerLevel) level, worldPosition, beaconLevel);
						links.forEach(l -> linkedConnectors.add(l.getConnector()));
					}
				}
				mergedHandler.build(this, linkedConnectors);
			} else {
				BlockEntity be = level.getBlockEntity(pos);
				if (be instanceof IInventoryConnectorReference ref) {
					var inv = ref.getConnectorRef();
					if (inv != null) {
						self = mergedHandler;
						mergedHandler.build(inv, Collections.emptyList());

						BlockFilter f = BlockFilter.getFilterAt(level, worldPosition);
						if (f != null) {
							List<IInventoryAccess> invs = new ArrayList<>();
							for (IInventoryAccess a : mergedHandler.getConnected()) {
								invs.add(f.wrap(level, a));
							}
							filteredMerge = invs;
							mergedHandler.build(this, Collections.emptyList());
						} else filteredMerge = mergedHandler.getConnected();
					} else {
						self = PlatformInventoryAccess.EMPTY;
					}
				} else {
					BlockFilter f = BlockFilter.getFilterAt(level, worldPosition);
					if (f != null)self = f.wrap(level, block);
					else self = block;
				}
			}
		}
	}

	private void detectCableNetwork() {
		Collection<BlockPos> netBlocks = InventoryCableNetwork.getNetwork(level).getNetworkNodes(worldPosition);

		for (BlockPos p : netBlocks) {
			if (!level.isLoaded(p))continue;

			BlockEntity be = level.getBlockEntity(p);
			if (be == this)continue;
			if (be instanceof IInventoryConnector te)
				linkedConnectors.add(te);
		}
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("menu.toms_storage.inventory_connector");
	}

	public boolean stillValid(Player player) {
		if(channel != null) {
			Channel chn = RemoteConnections.get(level).getChannel(channel);
			if(chn != null && !chn.publicChannel && !chn.owner.equals(player.getUUID()))
				return false;
		}
		if (this.level.getBlockEntity(this.worldPosition) != this || beaconLevel < 0) {
			return false;
		} else {
			return !(player.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) > 64.0D);
		}
	}

	@Override
	public IInventoryAccess getMergedHandler() {
		return self;
	}

	@Override
	public Collection<IInventoryAccess> getConnectedInventories() {
		return beaconLevel >= 0 ? Collections.emptyList() : self == mergedHandler ? filteredMerge : Collections.singleton(self);
	}

	@Override
	public boolean hasConnectedInventories() {
		return !isRemoved() && beaconLevel >= 0;
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
		super.loadAdditional(nbt, provider);
		if(nbt.contains(CHANNEL_TAG)) {
			channel = nbt.getUUID(CHANNEL_TAG);
		} else {
			channel = null;
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider provider) {
		super.saveAdditional(nbt, provider);
		if(channel != null) {
			nbt.putUUID(CHANNEL_TAG, channel);
		}
	}

	@Override
	public IInventoryConnector getConnector() {
		return this;
	}

	@Override
	public boolean isAccessibleFrom(ServerLevel world, BlockPos blockPos, int level) {
		int cLocalLvl = Config.get().invLinkBeaconLvl;
		int cSDLvl = Config.get().invLinkBeaconLvlSameDim;
		int cCDLvl = Config.get().invLinkBeaconLvlCrossDim;
		int lvl = Math.max(beaconLevel, level);
		int lvlM = Math.min(beaconLevel, level);
		if (cLocalLvl == -1)return false;
		if (cCDLvl != -1 && lvl >= cCDLvl && (lvlM > 0 || cCDLvl == 0))return true;
		if (!this.level.dimension().equals(world.dimension()))return false;
		if (cSDLvl != -1 && lvl >= cSDLvl && (lvlM > 0 || cSDLvl == 0))return true;
		int range = Config.get().invLinkBeaconRange;
		range *= range;
		return blockPos.distSqr(worldPosition) < range;
	}

	@Override
	public UUID getChannel() {
		return channel;
	}

	public void setChannel(UUID chn) {
		this.channel = chn;
		setChanged();
	}

	public int getBeaconLevel() {
		return beaconLevel;
	}

	@Override
	public Collection<IInventoryConnector> getConnectedConnectors() {
		return linkedConnectors;
	}

	public boolean hasBeacon() {
		BlockState state = level.getBlockState(worldPosition);
		Direction facing = state.getValue(InventoryCableConnectorBlock.FACING);
		BlockPos pos = worldPosition.relative(facing);
		BlockState st = level.getBlockState(pos);
		return st.is(Blocks.BEACON);
	}
}
