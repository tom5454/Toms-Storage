package com.tom.storagemod.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;

import com.tom.storagemod.platform.SavedDataFactory;

public class RemoteConnections extends SavedData {
	private static final String CONNECTIONS_TAG = "connections";
	public static final String CHANNEL_ID = "id";
	public static final String OWNER_ID = "owner";
	public static final String PUBLIC_TAG = "public";
	public static final String DISPLAY_NAME = "name";
	private static final String ID = "toms_storage_rc";
	private static final SavedDataFactory<RemoteConnections> FACTORY = new SavedDataFactory<>(RemoteConnections::new, RemoteConnections::new, ID);
	private Map<UUID, Channel> connections = new HashMap<>();

	private RemoteConnections() {
	}

	private RemoteConnections(CompoundTag tag) {
		load(tag.getList(CONNECTIONS_TAG, Tag.TAG_COMPOUND), connections);
	}

	public static void load(ListTag list, Map<UUID, Channel> connections) {
		for (int i = 0; i < list.size(); i++) {
			CompoundTag t = list.getCompound(i);
			UUID channel = t.getUUID(CHANNEL_ID);
			connections.put(channel, new Channel(t));
		}
	}

	public static RemoteConnections get(Level world) {
		ServerLevel sw = (ServerLevel) world;
		return FACTORY.get(sw.getServer().overworld().getDataStorage());
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		ListTag list = new ListTag();
		connections.forEach((k, v) -> {
			CompoundTag t = new CompoundTag();
			t.putUUID(CHANNEL_ID, k);
			v.save(t);
			list.add(t);
		});
		tag.put(CONNECTIONS_TAG, list);
		return tag;
	}

	public static class Channel {
		public Set<DimPos> connectors = new HashSet<>();
		public UUID owner;
		public boolean publicChannel;
		public String displayName;

		public Channel(UUID owner, boolean publicChannel, String displayName) {
			this.owner = owner;
			this.publicChannel = publicChannel;
			this.displayName = displayName;
		}

		private Channel(CompoundTag t) {
			this(t.getUUID(OWNER_ID), t.getBoolean(PUBLIC_TAG), t.getString(DISPLAY_NAME));
		}

		public static Channel fromTag(CompoundTag t) {
			return new Channel(null, t.getBoolean(PUBLIC_TAG), t.getString(DISPLAY_NAME));
		}

		public void register(ServerLevel world, BlockPos blockPos) {
			DimPos pos = new DimPos(world, blockPos);
			connectors.add(pos);
		}

		public IItemHandler findOthers(ServerLevel world, BlockPos blockPos, int lvl) {
			DimPos pos = new DimPos(world, blockPos);
			connectors.add(pos);
			Iterator<DimPos> posItr = connectors.iterator();
			MultiItemHandler handler = new MultiItemHandler();
			while (posItr.hasNext()) {
				DimPos dimPos = posItr.next();
				if(!dimPos.equals(pos)) {
					BlockEntity te = dimPos.getTileEntity(world);
					if(te instanceof IInventoryLink link) {
						LazyOptional<IItemHandler> h = link.getInventoryFrom(world, lvl);
						if(h != null)
							handler.add(h);
					} else {
						posItr.remove();
					}
				}
			}
			handler.refresh();
			return handler;
		}

		public void save(CompoundTag t) {
			t.putUUID(OWNER_ID, owner);
			t.putBoolean(PUBLIC_TAG, publicChannel);
			t.putString(DISPLAY_NAME, displayName);
		}

		public void saveNet(CompoundTag t) {
			t.putBoolean(PUBLIC_TAG, publicChannel);
			t.putString(DISPLAY_NAME, displayName);
		}
	}

	public static class DimPos {
		public int x, y, z;
		public ResourceKey<Level> dim;

		public DimPos(Level world, BlockPos pos) {
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
			this.dim = world.dimension();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dim == null) ? 0 : dim.location().hashCode());
			result = prime * result + x;
			result = prime * result + y;
			result = prime * result + z;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			DimPos other = (DimPos) obj;
			if (dim == null) {
				if (other.dim != null) return false;
			} else if (!dim.equals(other.dim)) return false;
			if (x != other.x) return false;
			if (y != other.y) return false;
			if (z != other.z) return false;
			return true;
		}

		public BlockEntity getTileEntity(ServerLevel world) {
			Level dim = world.getServer().getLevel(this.dim);
			BlockPos pos = new BlockPos(x, y, z);
			if(!dim.isLoaded(pos))return null;
			return dim.getBlockEntity(pos);
		}
	}

	public Channel getChannel(UUID connection) {
		return connections.get(connection);
	}

	public UUID makeChannel(CompoundTag t, UUID owner) {
		UUID id = UUID.randomUUID();
		String name = t.getString(DISPLAY_NAME);
		if(name.length() > 50)name = "Channel " + System.currentTimeMillis();
		connections.put(id, new Channel(owner, t.getBoolean(PUBLIC_TAG), name));
		setDirty();
		return id;
	}

	public void removeChannel(UUID id, UUID player) {
		Channel c = connections.get(id);
		if(c != null && c.owner.equals(player)) {
			connections.remove(id);
			setDirty();
		}
	}

	public ListTag listChannels(Player pl) {
		ListTag list = new ListTag();
		for (Entry<UUID, Channel> e : connections.entrySet()) {
			if(e.getValue().publicChannel || e.getValue().owner.equals(pl.getUUID())) {
				CompoundTag t = new CompoundTag();
				t.putUUID(CHANNEL_ID, e.getKey());
				e.getValue().save(t);
				list.add(t);
			}
		}
		return list;
	}

	public void editChannel(UUID id, boolean pub, UUID player) {
		Channel c = connections.get(id);
		if(c != null && c.owner.equals(player)) {
			c.publicChannel = pub;
			setDirty();
		}
	}

	public void invalidateCache(UUID id) {
		Channel c = connections.get(id);
		if(c != null)c.connectors.clear();
	}
}
