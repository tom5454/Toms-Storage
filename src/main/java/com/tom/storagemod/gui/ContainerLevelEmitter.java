package com.tom.storagemod.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import com.tom.storagemod.NetworkHandler;
import com.tom.storagemod.NetworkHandler.IDataReceiver;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.tile.TileEntityLevelEmitter;

public class ContainerLevelEmitter extends ScreenHandler implements IDataReceiver {
	private final Inventory inv;
	private TileEntityLevelEmitter te;
	private PlayerInventory pinv;

	public ContainerLevelEmitter(int p_i50087_1_, PlayerInventory p_i50087_2_) {
		this(p_i50087_1_, p_i50087_2_, null);
	}

	public ContainerLevelEmitter(int p_i50088_1_, PlayerInventory p_i50088_2_, TileEntityLevelEmitter te) {
		super(StorageMod.levelEmitterConatiner, p_i50088_1_);
		this.inv = te == null ? new SimpleInventory(1) : new Inventory() {

			@Override
			public void clear() {
			}

			@Override
			public void markDirty() {
			}

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public ItemStack getStack(int index) {
				return te.getFilter();
			}

			@Override
			public int size() {
				return 1;
			}

			@Override
			public ItemStack removeStack(int paramInt1, int paramInt2) {
				return ItemStack.EMPTY;
			}

			@Override
			public ItemStack removeStack(int paramInt) {
				return ItemStack.EMPTY;
			}

			@Override
			public void setStack(int paramInt, ItemStack paramItemStack) {
				te.setFilter(paramItemStack);
			}

			@Override
			public boolean canPlayerUse(PlayerEntity paramPlayerEntity) {
				return false;
			}
		};
		inv.onOpen(p_i50088_2_.player);
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
	public boolean canUse(PlayerEntity playerIn) {
		return te != null ? te.stillValid(playerIn) : true;
	}

	/**
	 * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
	 * inventory and the other inventory(s).
	 */
	@Override
	public ItemStack transferSlot(PlayerEntity playerIn, int index) {
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasStack()) {
			if (index < 9) {
			} else {
				ItemStack is = slot.getStack().copy();
				is.setCount(1);
				Slot sl = this.slots.get(0);
				if(!ItemStack.areItemsEqual(sl.getStack(), is)) {
					if(sl.getStack().isEmpty()) {
						sl.setStack(is);
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
	public void close(PlayerEntity playerIn) {
		super.close(playerIn);
		this.inv.onClose(playerIn);
	}

	@Override
	public void onSlotClick(int slotId, int dragType, SlotActionType click, PlayerEntity player) {
		Slot slot = slotId > -1 && slotId < slots.size() ? slots.get(slotId) : null;
		if (slot instanceof SlotPhantom) {
			ItemStack s = getCursorStack().copy();
			if(!s.isEmpty())s.setCount(1);
			slot.setStack(s);
			return;
		}
		super.onSlotClick(slotId, dragType, click, player);
	}

	private int lastCount = 0;
	private boolean lessThan = false;

	@Override
	public void receive(NbtCompound tag) {
		if(pinv.player.isSpectator() || te == null)return;
		int count = tag.getInt("count");
		boolean lt = tag.getBoolean("lessThan");
		te.setCount(count);
		te.setLessThan(lt);
	}

	@Override
	public void sendContentUpdates() {
		if(te == null)return;
		if(lastCount != te.getCount() || lessThan != te.isLessThan()) {
			NbtCompound mainTag = new NbtCompound();
			mainTag.putInt("count", te.getCount());
			mainTag.putBoolean("lessThan", te.isLessThan());
			lastCount = te.getCount();
			lessThan = te.isLessThan();
			NetworkHandler.sendTo(pinv.player, mainTag);
		}
		super.sendContentUpdates();
	}
}
