package com.tom.storagemod.util;

import java.util.Arrays;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.tom.storagemod.StorageMod;

public class GameObject<T> {
	private final DeferredHolder<? super T, T> value;

	protected GameObject(DeferredHolder<? super T, T> value) {
		this.value = value;
	}

	public T get() {
		return value.get();
	}

	public static class GameRegistry<T> {
		protected final DeferredRegister<T> handle;

		public GameRegistry(ResourceKey<? extends Registry<T>> reg) {
			handle = DeferredRegister.create(reg, StorageMod.modid);
		}

		public <I extends T> GameObject<I> register(final String name, final Supplier<? extends I> sup) {
			return new GameObject<>(handle.register(name, sup));
		}

		public void register(IEventBus bus) {
			handle.register(bus);
		}
	}

	public ResourceLocation getId() {
		return value.getId();
	}

	public static class GameRegistryBE extends GameRegistry<BlockEntityType<?>> {

		public GameRegistryBE() {
			super(Registries.BLOCK_ENTITY_TYPE);
		}

		@SuppressWarnings("unchecked")
		public <BE extends BlockEntity, I extends BlockEntityType<BE>> GameObjectBlockEntity<BE> registerBE(String name, BlockEntitySupplier<BE> sup, GameObject<? extends Block>... blocks) {
			return new GameObjectBlockEntity<>(handle.register(name, () -> {
				return BlockEntityType.Builder.<BE>of(sup, Arrays.stream(blocks).map(GameObject::get).toArray(Block[]::new)).build(null);
			}));
		}
	}

	public static class GameObjectBlockEntity<T extends BlockEntity> extends GameObject<BlockEntityType<T>> {

		protected GameObjectBlockEntity(DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> value) {
			super(value);
		}

	}
}
