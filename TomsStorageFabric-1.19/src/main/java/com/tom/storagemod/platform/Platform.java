package com.tom.storagemod.platform;

import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.Content;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.util.GameObject.GameRegistry;
import com.tom.storagemod.util.GameObject.GameRegistryBE;

public class Platform {

	public static final GameRegistry<Item> ITEMS = new GameRegistry<>(Registry.ITEM);
	public static final GameRegistry<Block> BLOCKS = new GameRegistry<>(Registry.BLOCK);
	public static final GameRegistryBE BLOCK_ENTITY = new GameRegistryBE(Registry.BLOCK_ENTITY_TYPE);
	public static final GameRegistry<MenuType<?>> MENU_TYPE = new GameRegistry<>(Registry.MENU);

	public static Properties itemProp() {
		return new Properties().tab(STORAGE_MOD_TAB);
	}

	public static <I extends Item> I registerItem(I item) {
		return item;
	}

	public static final CreativeModeTab STORAGE_MOD_TAB = FabricItemGroupBuilder.build(StorageMod.id("tab"), () -> new ItemStack(Content.terminal.get()));

	public static BlockState readBlockState(Level level, CompoundTag tag) {
		return NbtUtils.readBlockState(tag);
	}

	public static Level getDimension(Level worldIn, ResourceLocation dim) {
		return worldIn.getServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, dim));
	}

	public static ResourceLocation getItemId(Item item) {
		return Registry.ITEM.getKey(item);
	}

	public static Block getBlockById(ResourceLocation rl) {
		return Registry.BLOCK.get(rl);
	}

	public static TagKey<Block> getBlockTag(ResourceLocation rl) {
		return TagKey.create(Registry.BLOCK_REGISTRY, rl);
	}
}
