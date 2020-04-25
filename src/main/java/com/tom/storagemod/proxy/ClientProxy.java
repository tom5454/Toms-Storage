package com.tom.storagemod.proxy;

import net.minecraft.client.gui.ScreenManager;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.gui.GuiStorageTerminal;

public class ClientProxy implements IProxy {

	@Override
	public void setup() {

	}

	@Override
	public void clientSetup() {
		ScreenManager.registerFactory(StorageMod.storageTerminal, GuiStorageTerminal::new);
	}

}
