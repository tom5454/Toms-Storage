package com.tom.storagemod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Content;
import com.tom.storagemod.menu.FilingCabinetMenu;
import com.tom.storagemod.util.FilingCabinetContainer;

public class FilingCabinetBlockEntity extends BlockEntity implements MenuProvider, Nameable {
	private FilingCabinetContainer inv = new FilingCabinetContainer(512, this::setChanged, this::canInteractWith);
	private Component name;

	public FilingCabinetBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
		super(Content.filingCabinetBE.get(), p_155229_, p_155230_);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		tag.put("inventory", inv.createTag(provider));
		if (this.name != null) {
			tag.putString("CustomName", Component.Serializer.toJson(this.name, provider));
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		inv.fromTag(tag.getListOrEmpty("inventory"), provider);
		this.name = parseCustomNameSafe(tag.get("CustomName"), provider);
	}

	@Override
	public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
		return new FilingCabinetMenu(p_39954_, p_39955_, inv);
	}

	public Component getDefaultName() {
		return Component.translatable("menu.toms_storage.filing_cabinet");
	}

	public Container getInv() {
		return inv;
	}

	private boolean canInteractWith(Player player) {
		if(level.getBlockEntity(worldPosition) != this)return false;
		return player.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) < 64;
	}

	@Override
	public Component getName() {
		return this.name != null ? this.name : this.getDefaultName();
	}

	@Override
	public Component getDisplayName() {
		return this.getName();
	}

	@Override
	public Component getCustomName() {
		return this.name;
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		Containers.dropContents(level, pos, inv);
	}
}
