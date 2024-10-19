package com.tom.storagemod.platform;

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
	private final ResourceLocation id;
	private final Supplier<? extends T> sup;
	private T value;

	private GameObject(ResourceLocation id,Supplier<? extends T> sup) {
		this.id = id;
		this.sup = sup;
	}

	public T get() {
		return value;
	}

	protected T make() {
		value = sup.get();
		return value;
	}

	public static class GameRegistry<T> {
		protected final Registry<T> registry;
		protected List<GameObject<? extends T>> toRegister = new ArrayList<>();

		public GameRegistry(Registry<T> registry) {
			this.registry = registry;
		}

		public <I extends T> GameObject<I> register(final String name, final Supplier<? extends I> sup) {
			GameObject<I> obj = new GameObject<>(ResourceLocation.tryBuild(StorageMod.modid, name), sup);
			toRegister.add(obj);
			return obj;
		}

		public void runRegistration() {
			for (GameObject<? extends T> gameObject : toRegister) {
				Registry.register(registry, gameObject.getId(), gameObject.make());
			}
		}
	}

	public ResourceLocation getId() {
		return id;
	}

	public static class GameRegistryBE extends GameRegistry<BlockEntityType<?>> {

		public GameRegistryBE(Registry<BlockEntityType<?>> registry) {
			super(registry);
		}

		@SuppressWarnings("unchecked")
		public <BE extends BlockEntity, I extends BlockEntityType<BE>> GameObjectBlockEntity<BE> registerBE(String name, BlockEntitySupplier<BE> sup, GameObject<? extends Block>... blocks) {
			GameObjectBlockEntity<BE> e = new GameObjectBlockEntity<>(name, new ArrayList<>(Arrays.asList(blocks)), sup);
			toRegister.add(e);
			return e;
		}
	}

	public static class GameObjectBlockEntity<T extends BlockEntity> extends GameObject<BlockEntityType<T>> {
		private List<GameObject<? extends Block>> blocks;

		public GameObjectBlockEntity(String name, List<GameObject<? extends Block>> blocks, BlockEntitySupplier<T> factory) {
			super(ResourceLocation.tryBuild(StorageMod.modid, name), () -> BlockEntityType.Builder.<T>of(factory, blocks.stream().map(GameObject::get).toArray(Block[]::new)).build(null));
			this.blocks = blocks;
		}

		@SuppressWarnings("unchecked")
		public void addBlocks(GameObject<? extends Block>... blocks) {
			this.blocks.addAll(Arrays.asList(blocks));
		}
	}
}
