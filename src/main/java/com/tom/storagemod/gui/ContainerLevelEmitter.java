package com.tom.storagemod.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.network.IDataReceiver;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.tile.TileEntityLevelEmitter;

public class ContainerLevelEmitter extends Container implements IDataReceiver {
	private final IInventory inv;
	private TileEntityLevelEmitter te;
	private PlayerInventory pinv;

	public ContainerLevelEmitter(int p_i50087_1_, PlayerInventory p_i50087_2_) {
		this(p_i50087_1_, p_i50087_2_, null);
	}

	public ContainerLevelEmitter(int p_i50088_1_, PlayerInventory p_i50088_2_, TileEntityLevelEmitter te) {
		super(StorageMod.levelEmitterConatiner, p_i50088_1_);
		this.inv = te == null ? new Inventory(1) : new IInventory() {

			@Override
			public void clearContent() {
			}

			@Override
			public void setItem(int index, ItemStack stack) {
				te.setFilter(stack);
			}

			@Override
			public ItemStack removeItemNoUpdate(int index) {
				return ItemStack.EMPTY;
			}

			@Override
			public void setChanged() {
			}

			@Override
			public boolean stillValid(PlayerEntity player) {
				return false;
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public ItemStack getItem(int index) {
				return te.getFilter();
			}

			@Override
			public int getContainerSize() {
				return 1;
			}

			@Override
			public ItemStack removeItem(int index, int count) {
				return ItemStack.EMPTY;
			}
		};
		inv.startOpen(p_i50088_2_.player);
		this.te = te;
		this.pinv = p_i50088_2_;

		this.addSlot(new SlotPhantom(inv, 0, 43, 38));

		for(int k = 0; k < 3; ++k) {
			for(int i1 = 0; i1 < 9; ++i1) {
				this.addSlot(new Slot(p_i50088_2_, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
			}
		}

		for(int l = 0; l < 9; ++l) {
			this.addSlot(new Slot(p_i50088_2_, l, 8 + l * 18, 142));
		}

	}

	/**
	 * Determines whether supplied player can use this container
	 */
	@Override
	public boolean stillValid(PlayerEntity playerIn) {
		return true;
	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
	 * inventory and the other inventory(s).
	 */
	@Override
	public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			if (index < 1) {
			} else {
				ItemStack is = slot.getItem().copy();
				is.setCount(1);
				Slot sl = this.slots.get(0);
				if(!ItemStack.isSame(sl.getItem(), is)) {
					if(sl.getItem().isEmpty()) {
						sl.set(is);
					}
				}
			}
		}

		return ItemStack.EMPTY;
	}

	/**
	 * Called when the container is closed.
	 */
	@Override
	public void removed(PlayerEntity playerIn) {
		super.removed(playerIn);
		this.inv.stopOpen(playerIn);
	}

	@Override
	public ItemStack clicked(int slotId, int dragType, ClickType click, PlayerEntity player) {
		Slot slot = slotId > -1 && slotId < slots.size() ? slots.get(slotId) : null;
		if (slot instanceof SlotPhantom) {
			ItemStack s = player.inventory.getCarried().copy();
			if(!s.isEmpty())s.setCount(1);
			slot.set(s);
			return player.inventory.getCarried();
		}
		return super.clicked(slotId, dragType, click, player);
	}

	private int lastCount = 0;
	private boolean lessThan = false;

	@Override
	public void receive(CompoundNBT tag) {
		if(pinv.player.isSpectator() || te == null)return;
		int count = tag.getInt("count");
		boolean lt = tag.getBoolean("lessThan");
		te.setCount(count);
		te.setLessThan(lt);
	}

	@Override
	public void broadcastChanges() {
		if(te == null)return;
		if(lastCount != te.getCount() || lessThan != te.isLessThan()) {
			CompoundNBT mainTag = new CompoundNBT();
			mainTag.putInt("count", te.getCount());
			mainTag.putBoolean("lessThan", te.isLessThan());
			lastCount = te.getCount();
			lessThan = te.isLessThan();
			NetworkHandler.sendTo((ServerPlayerEntity) pinv.player, mainTag);
		}
		super.broadcastChanges();
	}
}
