package com.tom.storagemod.inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;

import com.tom.storagemod.components.WorldPos;

public class RemoteConnections extends SavedData {
	private static final String CONNECTIONS_TAG = "connections";
	public static final String CHANNEL_ID = "id";
	public static final String OWNER_ID = "owner";
	public static final String PUBLIC_TAG = "public";
	public static final String DISPLAY_NAME = "name";
	private static final String ID = "toms_storage_rc";
	private static final SavedData.Factory<RemoteConnections> FACTORY = new SavedData.Factory<>(RemoteConnections::new, RemoteConnections::new, DataFixTypes.LEVEL);
	public static final String OWNER_NAME = "owner_name";
	private Map<UUID, Channel> connections = new HashMap<>();

	private RemoteConnections() {
	}

	private RemoteConnections(CompoundTag tag, HolderLookup.Provider provider) {
		var list = tag.getList(CONNECTIONS_TAG, Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag t = list.getCompound(i);
			UUID channel = t.getUUID(CHANNEL_ID);
			connections.put(channel, new Channel(t));
		}
	}

	public static RemoteConnections get(Level world) {
		ServerLevel sw = (ServerLevel) world;
		return sw.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, ID);
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
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
		public Set<WorldPos> connectors = new HashSet<>();
		public UUID owner;
		public String ownerName;
		public boolean publicChannel;
		public String displayName;

		public Channel(UUID owner, String ownerName, boolean publicChannel, String displayName) {
			this.owner = owner;
			this.ownerName = ownerName;
			this.publicChannel = publicChannel;
			this.displayName = displayName;
		}

		private Channel(CompoundTag t) {
			this(t.getUUID(OWNER_ID), t.getString(OWNER_NAME), t.getBoolean(PUBLIC_TAG), t.getString(DISPLAY_NAME));
		}

		public void register(ServerLevel world, BlockPos blockPos) {
			WorldPos pos = new WorldPos(world.dimension(), blockPos);
			connectors.add(pos);
		}

		public Set<IInventoryLink> findOthers(ServerLevel world, BlockPos blockPos, int lvl) {
			WorldPos pos = new WorldPos(world.dimension(), blockPos);
			connectors.add(pos);
			Set<IInventoryLink> found = new HashSet<>();
			Iterator<WorldPos> posItr = connectors.iterator();
			while (posItr.hasNext()) {
				WorldPos dimPos = posItr.next();
				if(!dimPos.equals(pos)) {
					BlockEntity te = dimPos.getBlockEntity(world);
					if(te instanceof IInventoryLink link) {
						if (link.isAccessibleFrom(world, blockPos, lvl))
							found.add(link);
					} else {
						posItr.remove();
					}
				}
			}
			return found;
		}

		public void save(CompoundTag t) {
			t.putUUID(OWNER_ID, owner);
			t.putBoolean(PUBLIC_TAG, publicChannel);
			t.putString(DISPLAY_NAME, displayName);
			t.putString(OWNER_NAME, ownerName);
		}

		public boolean canAccess(Player player) {
			return publicChannel || owner.equals(player.getUUID());
		}
	}

	public Channel getChannel(UUID connection) {
		return connections.get(connection);
	}

	public UUID makeChannel(String name, boolean isPublic, Player owner) {
		UUID id = UUID.randomUUID();
		if(name.isEmpty() || name.length() > 50)name = "Channel " + System.currentTimeMillis();
		connections.put(id, new Channel(owner.getGameProfile().getId(), owner.getGameProfile().getName(), isPublic, name));
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

	public Stream<Entry<UUID, Channel>> streamChannels(Player player) {
		return connections.entrySet().stream().filter(c -> c.getValue().canAccess(player));
	}
}
