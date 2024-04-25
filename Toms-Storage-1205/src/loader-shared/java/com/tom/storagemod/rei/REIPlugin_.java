package com.tom.storagemod.rei;

import java.util.Collection;

import me.shedaniel.rei.api.client.config.addon.ConfigAddonRegistry;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.entry.renderer.EntryRendererRegistry;
import me.shedaniel.rei.api.client.favorites.FavoriteEntryType.Registry;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.search.method.InputMethodRegistry;
import me.shedaniel.rei.api.client.subsets.SubsetsRegistry;
import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.comparison.FluidComparatorRegistry;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.entry.settings.EntrySettingsAdapterRegistry;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.fluid.FluidSupportProvider;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.api.common.registry.Reloadable;
import me.shedaniel.rei.forge.REIPluginClient;

@REIPluginClient
public class REIPlugin_ implements REIClientPlugin {
	private final REIClientPlugin delegate = new REIPlugin();

	@Override
	public String getPluginProviderName() {
		return delegate.getPluginProviderName();
	}

	@Override
	public double getPriority() {
		return delegate.getPriority();
	}

	@Override
	public int compareTo(me.shedaniel.rei.api.common.plugins.REIPlugin o) {
		return delegate.compareTo(o);
	}

	@Override
	public void registerEntryRenderers(EntryRendererRegistry registry) {
		delegate.registerEntryRenderers(registry);
	}

	@Override
	public void registerEntryTypes(EntryTypeRegistry registry) {
		delegate.registerEntryTypes(registry);
	}

	@Override
	public void registerEntrySettingsAdapters(EntrySettingsAdapterRegistry registry) {
		delegate.registerEntrySettingsAdapters(registry);
	}

	@Override
	public void registerCategories(CategoryRegistry registry) {
		delegate.registerCategories(registry);
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		delegate.registerDisplays(registry);
	}

	@Override
	public void registerItemComparators(ItemComparatorRegistry registry) {
		delegate.registerItemComparators(registry);
	}

	@Override
	public void registerScreens(ScreenRegistry registry) {
		delegate.registerScreens(registry);
	}

	@Override
	public void registerFluidComparators(FluidComparatorRegistry registry) {
		delegate.registerFluidComparators(registry);
	}

	@Override
	public void registerExclusionZones(ExclusionZones zones) {
		delegate.registerExclusionZones(zones);
	}

	@Override
	public void registerFluidSupport(FluidSupportProvider support) {
		delegate.registerFluidSupport(support);
	}

	@Override
	public void registerEntries(EntryRegistry registry) {
		delegate.registerEntries(registry);
	}

	@Override
	public void registerBasicEntryFiltering(BasicFilteringRule<?> rule) {
		delegate.registerBasicEntryFiltering(rule);
	}

	@Override
	public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
		delegate.registerDisplaySerializer(registry);
	}

	@Override
	public void preStage(PluginManager<REIClientPlugin> manager, ReloadStage stage) {
		delegate.preStage(manager, stage);
	}

	@Override
	public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
		delegate.registerCollapsibleEntries(registry);
	}

	@Override
	public void postStage(PluginManager<REIClientPlugin> manager, ReloadStage stage) {
		delegate.postStage(manager, stage);
	}

	@Override
	public Collection<REIClientPlugin> provide() {
		return delegate.provide();
	}

	@Override
	public void registerFavorites(Registry registry) {
		delegate.registerFavorites(registry);
	}

	@Override
	public boolean shouldBeForcefullyDoneOnMainThread(Reloadable<?> reloadable) {
		return delegate.shouldBeForcefullyDoneOnMainThread(reloadable);
	}

	@Override
	public void registerSubsets(SubsetsRegistry registry) {
		delegate.registerSubsets(registry);
	}

	@Override
	public void registerTransferHandlers(TransferHandlerRegistry registry) {
		delegate.registerTransferHandlers(registry);
	}

	@Override
	public void registerConfigAddons(ConfigAddonRegistry registry) {
		delegate.registerConfigAddons(registry);
	}

	@Override
	public void registerInputMethods(InputMethodRegistry registry) {
		delegate.registerInputMethods(registry);
	}

	@Override
	public Class<REIClientPlugin> getPluginProviderClass() {
		return delegate.getPluginProviderClass();
	}
}
