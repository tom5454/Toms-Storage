package com.tom.storagemod.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class RemoteConnections extends PersistentState {
	private static final String CONNECTIONS_TAG = "connections";
	public static final String CHANNEL_ID = "id";
	public static final String OWNER_ID = "owner";
	public static final String PUBLIC_TAG = "public";
	public static final String DISPLAY_NAME = "name";
	private static final String ID = "toms_storage_rc";
	private Map<UUID, Channel> connections = new HashMap<>();

	private RemoteConnections() {
	}

	private RemoteConnections(NbtCompound tag) {
		load(tag.getList(CONNECTIONS_TAG, NbtElement.COMPOUND_TYPE), connections);
	}

	public static void load(NbtList list, Map<UUID, Channel> connections) {
		for (int i = 0; i < list.size(); i++) {
			NbtCompound t = list.getCompound(i);
			UUID channel = t.getUuid(CHANNEL_ID);
			connections.put(channel, new Channel(t));
		}
	}

	public static RemoteConnections get(World world) {
		ServerWorld sw = (ServerWorld) world;
		return sw.getServer().getOverworld().getPersistentStateManager().getOrCreate(RemoteConnections::new, RemoteConnections::new, ID);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		NbtList list = new NbtList();
		connections.forEach((k, v) -> {
			NbtCompound t = new NbtCompound();
			t.putUuid(CHANNEL_ID, k);
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

		private Channel(NbtCompound t) {
			this(t.getUuid(OWNER_ID), t.getBoolean(PUBLIC_TAG), t.getString(DISPLAY_NAME));
		}

		public static Channel fromTag(NbtCompound t) {
			return new Channel(null, t.getBoolean(PUBLIC_TAG), t.getString(DISPLAY_NAME));
		}

		public void register(ServerWorld world, BlockPos blockPos) {
			DimPos pos = new DimPos(world, blockPos);
			connectors.add(pos);
		}

		public Storage<ItemVariant> findOthers(ServerWorld world, BlockPos blockPos, int lvl) {
			DimPos pos = new DimPos(world, blockPos);
			connectors.add(pos);
			Iterator<DimPos> posItr = connectors.iterator();
			MergedStorage handler = new MergedStorage();
			while (posItr.hasNext()) {
				DimPos dimPos = posItr.next();
				if(!dimPos.equals(pos)) {
					BlockEntity te = dimPos.getTileEntity(world);
					if(te instanceof IInventoryLink link) {
						Storage<ItemVariant> h = link.getInventoryFrom(world, lvl);
						if(h != null)
							handler.add(h);
					} else {
						posItr.remove();
					}
				}
			}
			//handler.refresh();
			return handler;
		}

		public void save(NbtCompound t) {
			t.putUuid(OWNER_ID, owner);
			t.putBoolean(PUBLIC_TAG, publicChannel);
			t.putString(DISPLAY_NAME, displayName);
		}

		public void saveNet(NbtCompound t) {
			t.putBoolean(PUBLIC_TAG, publicChannel);
			t.putString(DISPLAY_NAME, displayName);
		}
	}

	public static class DimPos {
		public int x, y, z;
		public RegistryKey<World> dim;

		public DimPos(World world, BlockPos pos) {
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
			this.dim = world.getRegistryKey();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((dim == null) ? 0 : dim.getValue().hashCode());
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

		public BlockEntity getTileEntity(ServerWorld world) {
			World dim = world.getServer().getWorld(this.dim);
			BlockPos pos = new BlockPos(x, y, z);
			if(!dim.canSetBlock(pos))return null;
			return dim.getBlockEntity(pos);
		}
	}

	public Channel getChannel(UUID connection) {
		return connections.get(connection);
	}

	public UUID makeChannel(NbtCompound t, UUID owner) {
		UUID id = UUID.randomUUID();
		String name = t.getString(DISPLAY_NAME);
		if(name.length() > 50)name = "Channel " + System.currentTimeMillis();
		connections.put(id, new Channel(owner, t.getBoolean(PUBLIC_TAG), name));
		markDirty();
		return id;
	}

	public void removeChannel(UUID id, UUID player) {
		Channel c = connections.get(id);
		if(c != null && c.owner.equals(player)) {
			connections.remove(id);
			markDirty();
		}
	}

	public NbtList listChannels(PlayerEntity pl) {
		NbtList list = new NbtList();
		for (Entry<UUID, Channel> e : connections.entrySet()) {
			if(e.getValue().publicChannel || e.getValue().owner.equals(pl.getUuid())) {
				NbtCompound t = new NbtCompound();
				t.putUuid(CHANNEL_ID, e.getKey());
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
			markDirty();
		}
	}

	public void invalidateCache(UUID id) {
		Channel c = connections.get(id);
		if(c != null)c.connectors.clear();
	}
}
