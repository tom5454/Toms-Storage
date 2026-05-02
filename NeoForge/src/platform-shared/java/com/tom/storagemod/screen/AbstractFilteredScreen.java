package com.tom.storagemod.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import com.tom.storagemod.menu.AbstractFilteredMenu;

public abstract class AbstractFilteredScreen<T extends AbstractFilteredMenu> extends TSContainerScreen<T> {

	public AbstractFilteredScreen(T p_97741_, Inventory p_97742_, Component p_97743_) {
		super(p_97741_, p_97742_, p_97743_);
	}

}
