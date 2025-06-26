package com.tom.storagemod.menu;

import java.util.Map.Entry;
import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.tom.storagemod.Content;
import com.tom.storagemod.block.entity.InventoryCableConnectorBlockEntity;
import com.tom.storagemod.inventory.RemoteConnections;
import com.tom.storagemod.inventory.RemoteConnections.Channel;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.util.DataSlots;
import com.tom.storagemod.util.IDataReceiver;

public class InventoryLinkMenu extends AbstractContainerMenu implements IDataReceiver {
	private InventoryCableConnectorBlockEntity te;
	private Inventory pinv;
	private boolean sentList;
	public int beaconLvl;

	public InventoryLinkMenu(int id, Inventory playerInv) {
		this(id, playerInv, null);
	}

	public InventoryLinkMenu(int id, Inventory playerInv, InventoryCableConnectorBlockEntity tile) {
		super(Content.inventoryLink.get(), id);
		this.te = tile;
		this.pinv = playerInv;
		addDataSlot(DataSlots.create(v -> beaconLvl = v, () -> te != null ? te.getBeaconLevel() : 0));
	}

	@Override
	public void receive(ValueInput tag) {
		if(pinv.player.isSpectator() || te == null)return;
		tag.read("id", UUIDUtil.CODEC).ifPresentOrElse(id -> {
			if(tag.getBooleanOr("select", false)) {
				var c = RemoteConnections.get(pinv.player.level()).getChannel(id);
				if (c != null && c.canAccess(pinv.player))
					te.setChannel(id);
			}
			tag.read("p", Codec.BOOL).ifPresentOrElse(p -> {
				RemoteConnections.get(pinv.player.level()).editChannel(id, p, pinv.player.getUUID());
			}, () -> {
				RemoteConnections.get(pinv.player.level()).removeChannel(id, pinv.player.getUUID());
			});
		}, () -> {
			UUID chn = RemoteConnections.get(pinv.player.level()).makeChannel(tag.getStringOr("d", "No Name"), tag.getBooleanOr("p", false), pinv.player);
			te.setChannel(chn);
		});
		sentList = false;
	}

	@Override
	public boolean stillValid(Player p_38874_) {
		return te != null ? te.stillValid(p_38874_) : true;
	}

	@Override
	public void broadcastChanges() {
		if(te == null)return;
		if(!sentList) {
			CompoundTag mainTag = new CompoundTag();
			UUID chn = te.getChannel();
			if(chn != null)
				mainTag.store("selected", UUIDUtil.CODEC, chn);

			ListTag list = new ListTag();
			RemoteConnections.get(pinv.player.level()).streamChannels(pinv.player).map(LinkChannel::create).forEach(c -> {
				CompoundTag t = new CompoundTag();
				c.saveToClient(t);
				list.add(t);
			});
			mainTag.put("list", list);

			NetworkHandler.sendTo((ServerPlayer) pinv.player, mainTag);
			sentList = true;
		}
		super.broadcastChanges();
	}

	@Override
	public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
		return ItemStack.EMPTY;
	}

	public static record LinkChannel(UUID id, String displayName, boolean publicChannel, UUID owner, String ownerName) {

		public static final MapCodec<LinkChannel> CODEC = RecordCodecBuilder.mapCodec(
				b -> b.group(
						UUIDUtil.CODEC.optionalFieldOf("id", null).forGetter(LinkChannel::id),
						Codec.STRING.fieldOf("d").forGetter(LinkChannel::displayName),
						Codec.BOOL.fieldOf("p").forGetter(LinkChannel::publicChannel),
						UUIDUtil.CODEC.optionalFieldOf("o", null).forGetter(LinkChannel::owner),
						Codec.STRING.optionalFieldOf("on", null).forGetter(LinkChannel::ownerName)
						)
				.apply(b, LinkChannel::new)
				);

		public static LinkChannel create(Entry<UUID, Channel> e) {
			Channel c = e.getValue();
			return new LinkChannel(e.getKey(), c.displayName, c.publicChannel, c.owner, c.ownerName);
		}

		public LinkChannel(boolean isPublic, String name) {
			this(null, name, isPublic, null, null);
		}

		public void saveToClient(CompoundTag t) {
			t.store("id", UUIDUtil.CODEC, id);
			t.putString("d", displayName);
			t.putBoolean("p", publicChannel);
			t.store("o", UUIDUtil.CODEC, owner);
			t.putString("on", ownerName == null ? "" : ownerName);
		}

		public void saveToServer(CompoundTag t) {
			t.putBoolean("p", publicChannel);
			t.putString("d", displayName);
		}
	}
}
