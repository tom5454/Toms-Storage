package com.tom.storagemod.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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

	public static <I extends Item> I addItemToTab(I item) {
		tabItems.add(item);
		return item;
	}

	private static final ResourceKey<CreativeModeTab> ITEM_GROUP = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(StorageMod.modid, "tab"));

	public static final CreativeModeTab STORAGE_MOD_TAB = FabricItemGroup.builder().title(Component.translatable("itemGroup.toms_storage.tab")).icon(() -> new ItemStack(Content.terminal.get())).displayItems((p, out) -> {
		tabItems.forEach(out::accept);
	}).build();

	static {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ITEM_GROUP, STORAGE_MOD_TAB);
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

	private static boolean trinkets = FabricLoader.getInstance().isModLoaded("trinkets");
	public static <T> T checkExtraSlots(Player player, Predicate<ItemStack> is, T def, Function<ItemStack, T> map) {
		/*if(trinkets) {
			var tc = TrinketsApi.getTrinketComponent(player).orElse(null);
			if(tc != null) {
				var s = tc.getEquipped(is);
				if(!s.isEmpty())return map.apply(s.get(0).getB());
			}
		}*/
		return def;
	}
}
