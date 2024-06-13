package com.tom.storagemod;

import org.ladysnake.cca.api.v3.block.BlockComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.block.BlockComponentInitializer;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.google.common.base.Predicates;

import com.tom.storagemod.inventory.BlockFilterComponent;

public class StorageModComponents implements BlockComponentInitializer {
	public static final ComponentKey<BlockFilterComponent> BLOCK_FILTER =
			ComponentRegistry.getOrCreate(ResourceLocation.tryBuild(StorageMod.modid, "block_filter"), BlockFilterComponent.class);

	@Override
	public void registerBlockComponentFactories(BlockComponentFactoryRegistry registry) {
		registry.beginRegistration(BlockEntity.class, BLOCK_FILTER).filter(Predicates.alwaysTrue()).impl(BlockFilterComponent.class).end(BlockFilterComponent::new);
	}

}
