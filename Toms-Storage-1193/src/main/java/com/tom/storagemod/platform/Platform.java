package com.tom.storagemod.platform;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import com.tom.storagemod.Content;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.util.GameObject.GameRegistry;
import com.tom.storagemod.util.GameObject.GameRegistryBE;

public class Platform {

	public static final GameRegistry<Item> ITEMS = new GameRegistry<>(ForgeRegistries.ITEMS);
	public static final GameRegistry<Block> BLOCKS = new GameRegistry<>(ForgeRegistries.BLOCKS);
	public static final GameRegistryBE BLOCK_ENTITY = new GameRegistryBE();
	public static final GameRegistry<MenuType<?>> MENU_TYPE = new GameRegistry<>(ForgeRegistries.MENU_TYPES);

	public static CreativeModeTab STORAGE_MOD_TAB;
	private static List<Item> tabItems = new ArrayList<>();

	public static Properties itemProp() {
		return new Properties();
	}

	public static <I extends Item> I registerItem(I item) {
		tabItems.add(item);
		return item;
	}

	public static void register() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(Platform::makeTab);
		ITEMS.register();
		BLOCKS.register();
		BLOCK_ENTITY.register();
		MENU_TYPE.register();
	}

	private static void makeTab(CreativeModeTabEvent.Register evt) {
		STORAGE_MOD_TAB = evt.registerCreativeModeTab(new ResourceLocation(StorageMod.modid, "tab"), b -> {
			b.icon(() -> new ItemStack(Content.terminal.get()));
			b.displayItems((flag, out, bool) -> {
				tabItems.forEach(out::accept);
			});
		});
	}

	public static BlockState readBlockState(Level level, CompoundTag tag) {
		HolderGetter<Block> holdergetter = level != null ? level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup();
		return NbtUtils.readBlockState(holdergetter, tag);
	}

	public static Level getDimension(Level worldIn, ResourceLocation dim) {
		return worldIn.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dim));
	}

	public static ResourceLocation getItemId(Item item) {
		return ForgeRegistries.ITEMS.getKey(item);
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
		if (item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt()) {
			compoundtag = stack.getShareTag();
		}
		return compoundtag;
	}

	public static InteractionResult checkUse(Level worldIn, BlockHitResult lookingAt, Player playerIn, InteractionHand handIn) {
		PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(playerIn, handIn, lookingAt.getBlockPos(), lookingAt);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) {
			return event.getCancellationResult();
		}
		return null;
	}
}
