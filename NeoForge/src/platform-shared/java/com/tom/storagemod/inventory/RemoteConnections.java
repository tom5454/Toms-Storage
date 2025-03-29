package com.tom.storagemod.inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.tom.storagemod.components.WorldPos;

public class RemoteConnections extends SavedData {
	private static final String ID = "toms_storage_rc";

	public static final Codec<RemoteConnections> CODEC = RecordCodecBuilder.<RemoteConnections>mapCodec(b -> {
		return b.group(
				Codec.list(Connection.CODEC).fieldOf("connections").forGetter(RemoteConnections::connections)
				).apply(b, RemoteConnections::new);
	}).codec();

	private static final SavedDataType<RemoteConnections> FACTORY = new SavedDataType<>(ID, RemoteConnections::new, __ -> CODEC, DataFixTypes.LEVEL);
	private Map<UUID, Channel> connections = new HashMap<>();

	private RemoteConnections(SavedData.Context ctx) {
	}

	private RemoteConnections(List<Connection> conns) {
		for (Connection connection : conns) {
			connections.put(connection.channelId(), new Channel(connection));
		}
	}

	private List<Connection> connections() {
		return connections.entrySet().stream().map(Connection::new).toList();
	}

	private static record Connection(UUID channelId, UUID owner, String ownerName, String displayName, boolean isPublic) {

		public static final Codec<Connection> CODEC = RecordCodecBuilder.<Connection>mapCodec(b -> {
			return b.group(
					UUIDUtil.CODEC.fieldOf("id").forGetter(Connection::channelId),
					UUIDUtil.CODEC.fieldOf("owner").forGetter(Connection::owner),
					Codec.STRING.fieldOf("owner_name").forGetter(Connection::ownerName),
					Codec.STRING.fieldOf("name").forGetter(Connection::displayName),
					Codec.BOOL.fieldOf("public").forGetter(Connection::isPublic)
					).apply(b, Connection::new);
		}).codec();

		public Connection(Entry<UUID, Channel> e) {
			this(e.getKey(), e.getValue().owner, e.getValue().ownerName, e.getValue().displayName, e.getValue().publicChannel);
		}
	}

	public static RemoteConnections get(Level world) {
		ServerLevel sw = (ServerLevel) world;
		return sw.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY);
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

		private Channel(Connection t) {
			this(t.owner(), t.ownerName(), t.isPublic(), t.displayName());
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
