package com.tom.storagemod.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;

import com.tom.storagemod.StorageMod;

public class GameObject<T> {
	private final T value;

	private GameObject(T value) {
		this.value = value;
	}

	/*public static <V, T extends V> GameObject<T> register(Registry<V> registry, ResourceLocation resourceLocation, T value) {
		Registry.register(registry, resourceLocation, value);
		return new GameObject<>(value);
	}*/

	public T get() {
		return value;
	}

	public static class GameRegistry<T> {
		protected final Registry<T> registry;

		public GameRegistry(Registry<T> registry) {
			this.registry = registry;
		}

		public <I extends T> GameObject<I> register(final String name, final Supplier<? extends I> sup) {
			I obj = sup.get();
			Registry.register(registry, new ResourceLocation(StorageMod.modid, name), obj);
			return new GameObject<>(obj);
		}
	}

	public static class GameRegistryBE extends GameRegistry<BlockEntityType<?>> {
		private List<GameObjectBlockEntity<?>> blockEntities = new ArrayList<>();

		public GameRegistryBE(Registry<BlockEntityType<?>> registry) {
			super(registry);
		}

		@SuppressWarnings("unchecked")
		public <BE extends BlockEntity, I extends BlockEntityType<BE>> GameObjectBlockEntity<BE> registerBE(String name, BlockEntitySupplier<BE> sup, GameObject<? extends Block>... blocks) {
			GameObjectBlockEntity<BE> e = new GameObjectBlockEntity<>(this, name, new ArrayList<>(Arrays.asList(blocks)), sup);
			blockEntities.add(e);
			return e;
		}

		public void register() {
			blockEntities.forEach(GameObjectBlockEntity::register);
		}
	}

	public static class GameObjectBlockEntity<T extends BlockEntity> extends GameObject<BlockEntityType<T>> {
		private BlockEntityType<T> value;
		private List<GameObject<? extends Block>> blocks;
		private BlockEntitySupplier<T> factory;
		private GameRegistryBE registry;
		private String name;

		public GameObjectBlockEntity(GameRegistryBE registry, String name, List<GameObject<? extends Block>> blocks, BlockEntitySupplier<T> factory) {
			super(null);
			this.name = name;
			this.blocks = blocks;
			this.factory = factory;
			this.registry = registry;
		}

		protected void register() {
			value = BlockEntityType.Builder.<T>of(factory, blocks.stream().map(GameObject::get).toArray(Block[]::new)).build(null);
			Registry.register(registry.registry, new ResourceLocation(StorageMod.modid, name), value);
		}

		@Override
		public BlockEntityType<T> get() {
			return value;
		}

		@SuppressWarnings("unchecked")
		public void addBlocks(GameObject<? extends Block>... blocks) {
			this.blocks.addAll(Arrays.asList(blocks));
		}
	}
}
