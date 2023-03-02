package com.tom.storagemod.platform;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import com.tom.storagemod.Content;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.util.GameObject.GameRegistry;
import com.tom.storagemod.util.GameObject.GameRegistryBE;

public class Platform {

	public static final GameRegistry<Item> ITEMS = new GameRegistry<>(BuiltInRegistries.ITEM);
	public static final GameRegistry<Block> BLOCKS = new GameRegistry<>(BuiltInRegistries.BLOCK);
	public static final GameRegistryBE BLOCK_ENTITY = new GameRegistryBE(BuiltInRegistries.BLOCK_ENTITY_TYPE);
	public static final GameRegistry<MenuType<?>> MENU_TYPE = new GameRegistry<>(BuiltInRegistries.MENU);

	private static List<Item> tabItems = new ArrayList<>();

	public static Properties itemProp() {
		return new Properties();
	}

	public static <I extends Item> I registerItem(I item) {
		tabItems.add(item);
		return item;
	}

	public static final CreativeModeTab STORAGE_MOD_TAB = FabricItemGroup.builder(StorageMod.id("tab")).icon(() -> new ItemStack(Content.terminal.get())).displayItems((flag, out, bool) -> {
		tabItems.forEach(out::accept);
	}).build();

	public static BlockState readBlockState(Level level, CompoundTag tag) {
		HolderGetter<Block> holdergetter = level != null ? level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup();
		return NbtUtils.readBlockState(holdergetter, tag);
	}

	public static Level getDimension(Level worldIn, ResourceLocation dim) {
		return worldIn.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dim));
	}

	public static ResourceLocation getItemId(Item item) {
		return BuiltInRegistries.ITEM.getKey(item);
	}

	public static Block getBlockById(ResourceLocation rl) {
		return BuiltInRegistries.BLOCK.get(rl);
	}

	public static TagKey<Block> getBlockTag(ResourceLocation rl) {
		return TagKey.create(Registries.BLOCK, rl);
	}

	public static TagKey<Item> getItemTag(ResourceLocation rl) {
		return TagKey.create(Registries.ITEM, rl);
	}

	public static void writeItemId(FriendlyByteBuf buf, Item item) {
		buf.writeId(BuiltInRegistries.ITEM, item);
	}

	public static Item readItemId(FriendlyByteBuf buf) {
		return buf.readById(BuiltInRegistries.ITEM);
	}

	public static CompoundTag getSyncTag(ItemStack stack) {
		Item item = stack.getItem();
		CompoundTag compoundtag = null;
		if (item.canBeDepleted() || item.shouldOverrideMultiplayerNbt()) {
			compoundtag = stack.getTag();
		}
		return compoundtag;
	}

	public static InteractionResult checkUse(Level worldIn, BlockHitResult lookingAt, Player playerIn, InteractionHand handIn) {
		InteractionResult r = UseBlockCallback.EVENT.invoker().interact(playerIn, worldIn, handIn, lookingAt);
		if(r == InteractionResult.PASS)return null;
		return r;
	}

	public static <C extends Container> ItemStack assembleRecipe(Level level, C container, Recipe<C> recipe) {
		return recipe.assemble(container);
	}

	public static <M extends AbstractContainerMenu> MenuType<M> createMenuType(MenuSupplier<M> create) {
		return new MenuType<>(create);
	}
}
