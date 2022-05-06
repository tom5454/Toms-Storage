package com.tom.storagemod.gui;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;

import com.tom.storagemod.NetworkHandler;
import com.tom.storagemod.NetworkHandler.IDataReceiver;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.tile.TileEntityInventoryCableConnector;
import com.tom.storagemod.util.RemoteConnections;

public class ContainerInventoryLink extends ScreenHandler implements IDataReceiver {
	private TileEntityInventoryCableConnector te;
	private PlayerInventory pinv;
	private boolean sentList;

	public ContainerInventoryLink(int id, PlayerInventory playerInv) {
		this(id, playerInv, null);
	}

	public ContainerInventoryLink(int id, PlayerInventory playerInv, TileEntityInventoryCableConnector tile) {
		super(StorageMod.inventoryLink, id);
		this.te = tile;
		this.pinv = playerInv;
	}

	@Override
	public void receive(NbtCompound tag) {
		if(pinv.player.isSpectator() || te == null)return;
		if(tag.contains("remote")) {
			te.setRemote(tag.getBoolean("remote"));
			sentList = false;
			return;
		}
		UUID id = null;
		if(tag.contains("id"))id = tag.getUuid("id");
		if(id == null) {
			UUID chn = RemoteConnections.get(pinv.player.world).makeChannel(tag, pinv.player.getUuid());
			te.setChannel(chn);
		} else if(tag.getBoolean("select")) {
			te.setChannel(id);
		} else if(tag.contains(RemoteConnections.PUBLIC_TAG)) {
			RemoteConnections.get(pinv.player.world).editChannel(id, tag.getBoolean(RemoteConnections.PUBLIC_TAG), pinv.player.getUuid());
		} else {
			RemoteConnections.get(pinv.player.world).removeChannel(id, pinv.player.getUuid());
		}
		sentList = false;
	}

	@Override
	public boolean canUse(PlayerEntity p_38874_) {
		return te != null ? te.stillValid(p_38874_) : true;
	}

	@Override
	public void sendContentUpdates() {
		if(te == null)return;
		if(!sentList) {
			NbtCompound mainTag = new NbtCompound();
			UUID chn = te.getChannel();
			if(chn != null)
				mainTag.putUuid("selected", chn);

			mainTag.put("list", RemoteConnections.get(pinv.player.world).listChannels(pinv.player));
			mainTag.putInt("lvl", te.getBeaconLevel());
			mainTag.putBoolean("remote", te.isRemote());

			NetworkHandler.sendTo(pinv.player, mainTag);
			sentList = true;
		}
		super.sendContentUpdates();
	}

	@Override
	public ItemStack transferSlot(PlayerEntity player, int index) {
		return ItemStack.EMPTY;
	}
}
