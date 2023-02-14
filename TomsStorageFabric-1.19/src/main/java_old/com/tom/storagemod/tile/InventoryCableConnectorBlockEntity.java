package com.tom.storagemod.tile;

import java.util.UUID;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.InventoryCableConnectorBlock;
import com.tom.storagemod.gui.InventoryLinkMenu;
import com.tom.storagemod.util.IInventoryLink;
import com.tom.storagemod.util.RemoteConnections;
import com.tom.storagemod.util.RemoteConnections.Channel;

public class InventoryCableConnectorBlockEntity extends AbstractInventoryCableConnectorBlockEntity implements NamedScreenHandlerFactory, IInventoryLink {
	private static final String CHANNEL_TAG = "channel";
	private static final String REMOTE_TAG = "remote";
	private UUID channel = null;
	private int beaconLevel = -1;
	private boolean remote;

	public InventoryCableConnectorBlockEntity(BlockPos pos, BlockState state) {
		super(StorageMod.invCableConnectorTile, pos, state);
	}

	@Override
	public void updateServer() {
		super.updateServer();
		if(!world.isClient && world.getTime() % 20 == 18) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.get(InventoryCableConnectorBlock.FACING);
			BlockPos pos = this.pos.offset(facing);
			BlockState st = world.getBlockState(pos);
			if(st.isOf(Blocks.BEACON)) {
				beaconLevel = calcBeaconLevel(world, pos.getX(), pos.getY(), pos.getZ());
			} else {
				beaconLevel = -1;
			}
		}
	}

	@Override
	protected Storage<ItemVariant> getPointedAt(BlockPos pos, Direction facing) {
		if(beaconLevel >= 0) {
			if(channel != null && beaconLevel > 0) {
				Channel chn = RemoteConnections.get(world).getChannel(channel);
				if(chn != null) {
					if(!remote) {
						return chn.findOthers((ServerWorld) world, this.pos, beaconLevel);
					} else {
						chn.register((ServerWorld) world, this.pos);
					}
				}
			}
			return null;
		}
		return super.getPointedAt(pos, facing);
	}

	public static int calcBeaconLevel(World world, int x, int y, int z) {
		int i = 0;

		BlockEntity ent = world.getBlockEntity(new BlockPos(x, y, z));
		if(ent instanceof BeaconBlockEntity b) {
			if(b.getBeamSegments().isEmpty())return 0;

			for(int j = 1; j <= 4; i = j++) {
				int k = y - j;
				if (k < world.getBottomY()) {
					break;
				}

				boolean flag = true;

				for(int l = x - j; l <= x + j && flag; ++l) {
					for(int i1 = z - j; i1 <= z + j; ++i1) {
						if (!world.getBlockState(new BlockPos(l, k, i1)).isIn(BlockTags.BEACON_BASE_BLOCKS)) {
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
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new InventoryLinkMenu(syncId, inv, this);
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable("ts.inventory_connector");
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		if(nbt.contains(CHANNEL_TAG)) {
			channel = nbt.getUuid(CHANNEL_TAG);
		} else {
			channel = null;
		}
		remote = nbt.getBoolean(REMOTE_TAG);
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		if(channel != null) {
			nbt.putUuid(CHANNEL_TAG, channel);
		}
		nbt.putBoolean(REMOTE_TAG, remote);
	}

	@Override
	public Storage<ItemVariant> getInventoryFrom(ServerWorld fromWorld, int fromLevel) {
		if(!remote || beaconLevel < StorageMod.CONFIG.invLinkBeaconLvl)return null;
		if(beaconLevel >= StorageMod.CONFIG.invLinkBeaconLvlDim || fromLevel >= StorageMod.CONFIG.invLinkBeaconLvlDim)return this;
		if(fromWorld.getRegistryKey().equals(world.getRegistryKey()))return this;
		return null;
	}

	public boolean stillValid(PlayerEntity p_59619_) {
		if(channel != null) {
			Channel chn = RemoteConnections.get(world).getChannel(channel);
			if(chn != null && !chn.publicChannel && !chn.owner.equals(p_59619_.getUuid()))
				return false;
		}
		if (this.world.getBlockEntity(this.pos) != this || beaconLevel < 0) {
			return false;
		} else {
			return !(p_59619_.squaredDistanceTo(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) > 64.0D);
		}
	}

	@Override
	public UUID getChannel() {
		return channel;
	}

	public void setChannel(UUID channel) {
		RemoteConnections.get(world).invalidateCache(this.channel);
		this.channel = channel;
		markDirty();
	}

	public int getBeaconLevel() {
		return beaconLevel;
	}

	public boolean isRemote() {
		return remote;
	}

	public void setRemote(boolean remote) {
		this.remote = remote;
		markDirty();
	}
}
