package com.tom.storagemod.rei;

import java.util.Collection;
import java.util.List;

import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.forge.REIPluginLoaderClient;

@REIPluginLoaderClient
public class REIPluginLoader implements REIPluginProvider<REIPlugin> {

	@Override
	public Collection<REIPlugin> provide() {
		return List.of(new REIPlugin());
	}

	@Override
	public Class<REIPlugin> getPluginProviderClass() {
		return REIPlugin.class;
	}

}
