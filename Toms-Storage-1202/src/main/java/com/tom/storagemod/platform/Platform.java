package com.tom.storagemod.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.neoforged.fml.ModList;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

import com.tom.storagemod.Content;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.util.GameObject.GameRegistry;
import com.tom.storagemod.util.GameObject.GameRegistryBE;

public class Platform {

	public static final GameRegistry<Item> ITEMS = new GameRegistry<>(ForgeRegistries.ITEMS);
	public static final GameRegistry<Block> BLOCKS = new GameRegistry<>(ForgeRegistries.BLOCKS);
	public static final GameRegistryBE BLOCK_ENTITY = new GameRegistryBE();
	public static final GameRegistry<MenuType<?>> MENU_TYPE = new GameRegistry<>(ForgeRegistries.MENU_TYPES);
	public static final DeferredRegister<CreativeModeTab> TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StorageMod.modid);

	private static List<Item> tabItems = new ArrayList<>();
	public static final RegistryObject<CreativeModeTab> STORAGE_MOD_TAB = TAB.register("tab", () ->
	CreativeModeTab.builder()
	.title(Component.translatable("itemGroup.toms_storage.tab"))
	.icon(() -> new ItemStack(Content.terminal.get()))
	.displayItems((p, out) -> {
		tabItems.forEach(out::accept);
	})
	.build()
			);

	public static <I extends Item> I addItemToTab(I item) {
		tabItems.add(item);
		return item;
	}

	public static void register() {
		ITEMS.register();
		BLOCKS.register();
		BLOCK_ENTITY.register();
		MENU_TYPE.register();
		TAB.register(FMLJavaModLoadingContext.get().getModEventBus());
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
		NeoForge.EVENT_BUS.post(event);
		if (event.isCanceled()) {
			return event.getCancellationResult();
		}
		return null;
	}

	private static boolean curios = ModList.get().isLoaded("curios");
	public static <T> T checkExtraSlots(Player player, Predicate<ItemStack> is, T def, Function<ItemStack, T> map) {
		if(curios) {
			/*var handler = player.getCapability(CuriosCapability.INVENTORY).orElse(null);
			if (handler == null)return def;
			List<SlotResult> s = handler.findCurios(is);
			if(!s.isEmpty())return map.apply(s.get(0).stack());*/
		}
		return def;
	}
}
