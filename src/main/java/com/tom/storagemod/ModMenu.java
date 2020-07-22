package com.tom.storagemod;

import java.util.function.Function;

import net.minecraft.client.gui.screen.Screen;

import io.github.prospector.modmenu.api.ModMenuApi;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;

public class ModMenu implements ModMenuApi {

	@Override
	public String getModId() {
		return "toms_storage";
	}

	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return screen -> AutoConfig.getConfigScreen(Config.class, screen).get();
	}

}
