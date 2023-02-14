package com.tom.storagemod.platform;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import com.tom.storagemod.Content;
import com.tom.storagemod.util.GameObject.GameRegistry;
import com.tom.storagemod.util.GameObject.GameRegistryBE;

public class Platform {

	public static final GameRegistry<Item> ITEMS = new GameRegistry<>(ForgeRegistries.ITEMS);
	public static final GameRegistry<Block> BLOCKS = new GameRegistry<>(ForgeRegistries.BLOCKS);
	public static final GameRegistryBE BLOCK_ENTITY = new GameRegistryBE();
	public static final GameRegistry<MenuType<?>> MENU_TYPE = new GameRegistry<>(ForgeRegistries.MENU_TYPES);

	public static Properties itemProp() {
		return new Properties().tab(STORAGE_MOD_TAB);
	}

	public static <I extends Item> I registerItem(I item) {
		return item;
	}

	public static void register() {
		ITEMS.register();
		BLOCKS.register();
		BLOCK_ENTITY.register();
		MENU_TYPE.register();
	}

	public static final CreativeModeTab STORAGE_MOD_TAB = new CreativeModeTab("toms_storage.tab") {

		@Override
		@OnlyIn(Dist.CLIENT)
		public ItemStack makeIcon() {
			return new ItemStack(Content.terminal.get());
		}
	};

	public static BlockState readBlockState(Level level, CompoundTag tag) {
		return NbtUtils.readBlockState(tag);
	}

	public static Level getDimension(Level worldIn, ResourceLocation dim) {
		return worldIn.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, dim));
	}

	public static ResourceLocation getItemId(Item item) {
		return ForgeRegistries.ITEMS.getKey(item);
	}
}
