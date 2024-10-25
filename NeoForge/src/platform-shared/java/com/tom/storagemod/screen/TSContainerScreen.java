package com.tom.storagemod.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class TSContainerScreen<T extends AbstractContainerMenu> extends PlatformContainerScreen<T> {

	public TSContainerScreen(T p_97741_, Inventory p_97742_, Component p_97743_) {
		super(p_97741_, p_97742_, p_97743_);
	}

	@Override
	public boolean mouseScrolled(double p_364830_, double p_360707_, double p_364436_, double p_364417_) {
		return super.mouseScrolled(p_364830_, p_360707_, p_364436_, p_364417_) || mouseScrolled0(p_364830_, p_360707_, p_364436_, p_364417_);
	}

	// Copy from ContainerEventHandler
	private boolean mouseScrolled0(double p_94686_, double p_94687_, double p_94688_, double p_294830_) {
		return this.getChildAt(p_94686_, p_94687_).filter(p_293596_ -> p_293596_.mouseScrolled(p_94686_, p_94687_, p_94688_, p_294830_)).isPresent();
	}
}
