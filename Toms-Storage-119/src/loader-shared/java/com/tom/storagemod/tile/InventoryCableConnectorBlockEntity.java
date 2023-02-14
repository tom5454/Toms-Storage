package com.tom.storagemod.tile;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import com.tom.storagemod.Config;
import com.tom.storagemod.Content;
import com.tom.storagemod.block.InventoryCableConnectorBlock;
import com.tom.storagemod.gui.InventoryLinkMenu;
import com.tom.storagemod.util.IInventoryLink;
import com.tom.storagemod.util.RemoteConnections;
import com.tom.storagemod.util.RemoteConnections.Channel;

public class InventoryCableConnectorBlockEntity extends AbstractInventoryCableConnectorBlockEntity implements MenuProvider, IInventoryLink {
	private static final String CHANNEL_TAG = "channel";
	private static final String REMOTE_TAG = "remote";
	private UUID channel = null;
	private int beaconLevel = -1;
	private boolean remote;

	public InventoryCableConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(Content.invCableConnectorTile.get(), pos, state);
	}

	@Override
	public void updateServer() {
		super.updateServer();
		if(!level.isClientSide && level.getGameTime() % 20 == 18) {
			BlockState state = level.getBlockState(worldPosition);
			Direction facing = state.getValue(InventoryCableConnectorBlock.FACING);
			BlockPos pos = worldPosition.relative(facing);
			BlockState st = level.getBlockState(pos);
			if(st.is(Blocks.BEACON)) {
				beaconLevel = calcBeaconLevel(level, pos.getX(), pos.getY(), pos.getZ());
			} else {
				beaconLevel = -1;
			}
		}
	}

	@Override
	protected LazyOptional<IItemHandler> getPointedAt(BlockPos pos, Direction facing) {
		if(beaconLevel >= 0) {
			if(channel != null && beaconLevel > 0) {
				Channel chn = RemoteConnections.get(level).getChannel(channel);
				if(chn != null) {
					if(!remote) {
						IItemHandler h = chn.findOthers((ServerLevel) level, worldPosition, beaconLevel);
						return LazyOptional.of(() -> h);
					} else {
						chn.register((ServerLevel) level, worldPosition);
					}
				}
			}
			return null;
		}
		return super.getPointedAt(pos, facing);
	}

	public static int calcBeaconLevel(Level world, int x, int y, int z) {
		int i = 0;

		BlockEntity ent = world.getBlockEntity(new BlockPos(x, y, z));
		if(ent instanceof BeaconBlockEntity b) {
			if(b.getBeamSections().isEmpty())return 0;

			for(int j = 1; j <= 4; i = j++) {
				int k = y - j;
				if (k < world.getMinBuildHeight()) {
					break;
				}

				boolean flag = true;

				for(int l = x - j; l <= x + j && flag; ++l) {
					for(int i1 = z - j; i1 <= z + j; ++i1) {
						if (!world.getBlockState(new BlockPos(l, k, i1)).is(BlockTags.BEACON_BASE_BLOCKS)) {
							flag = false;
							break;
						}
					}
				}

				if (!flag) {
					break;
				}
			}
		}
		return i;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory plInv, Player arg2) {
		return new InventoryLinkMenu(id, plInv, this);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("ts.inventory_connector");
	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		if(channel != null) {
			compound.putUUID(CHANNEL_TAG, channel);
		}
		compound.putBoolean(REMOTE_TAG, remote);
	}

	@Override
	public void load(CompoundTag nbtIn) {
		super.load(nbtIn);
		if(nbtIn.contains(CHANNEL_TAG)) {
			channel = nbtIn.getUUID(CHANNEL_TAG);
		} else {
			channel = null;
		}
		remote = nbtIn.getBoolean(REMOTE_TAG);
	}

	@Override
	public LazyOptional<IItemHandler> getInventoryFrom(Level fromWorld, int fromLevel) {
		if(!remote || beaconLevel < Config.get().invLinkBeaconLvl)return null;
		if(beaconLevel >= Config.get().invLinkBeaconLvlDim || fromLevel >= Config.get().invLinkBeaconLvlDim)return getCapability();
		if(fromWorld.dimension().equals(level.dimension()))return getCapability();
		return null;
	}

	public boolean stillValid(Player p_59619_) {
		if(channel != null) {
			Channel chn = RemoteConnections.get(level).getChannel(channel);
			if(chn != null && !chn.publicChannel && !chn.owner.equals(p_59619_.getUUID()))
				return false;
		}
		if (this.level.getBlockEntity(this.worldPosition) != this || beaconLevel < 0) {
			return false;
		} else {
			return !(p_59619_.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) > 64.0D);
		}
	}

	@Override
	public UUID getChannel() {
		return channel;
	}

	public void setChannel(UUID channel) {
		RemoteConnections.get(level).invalidateCache(this.channel);
		this.channel = channel;
		setChanged();
	}

	public int getBeaconLevel() {
		return beaconLevel;
	}

	public boolean isRemote() {
		return remote;
	}

	public void setRemote(boolean remote) {
		this.remote = remote;
		setChanged();
	}
}
