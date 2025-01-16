package com.tom.storagemod.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import com.tom.storagemod.Content;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.entity.BlockFilterAttachment;
import com.tom.storagemod.platform.GameObject.GameRegistry;
import com.tom.storagemod.platform.GameObject.GameRegistryBE;

import io.netty.buffer.ByteBuf;
import top.theillusivec4.curios.api.CuriosCapability;

public class Platform {

	public static final GameRegistry<Item> ITEMS = new GameRegistry<>(Registries.ITEM);
	public static final GameRegistry<Block> BLOCKS = new GameRegistry<>(Registries.BLOCK);
	public static final GameRegistryBE BLOCK_ENTITY = new GameRegistryBE();
	public static final GameRegistry<MenuType<?>> MENU_TYPE = new GameRegistry<>(Registries.MENU);
	public static final DeferredRegister<CreativeModeTab> TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StorageMod.modid);
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, StorageMod.modid);
	public static final GameRegistry<DataComponentType<?>> DATA_COMPONENT_TYPES = new GameRegistry<>(Registries.DATA_COMPONENT_TYPE);

	private static List<Item> tabItems = new ArrayList<>();
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> STORAGE_MOD_TAB = TAB.register("tab", () ->
	CreativeModeTab.builder()
	.title(Component.translatable("itemGroup.toms_storage.tab"))
	.icon(() -> new ItemStack(Content.terminal.get()))
	.displayItems((p, out) -> {
		tabItems.forEach(out::accept);
	})
	.build()
			);

	public static final Supplier<AttachmentType<BlockFilterAttachment>> BLOCK_FILTER = ATTACHMENT_TYPES.register(
			"block_filter", () -> AttachmentType.serializable(BlockFilterAttachment::new).build());

	public static boolean vivecraft = ModList.get().isLoaded("vivecraft");

	public static <I extends Item> I addItemToTab(I item) {
		tabItems.add(item);
		return item;
	}

	public static void register(IEventBus bus) {
		ITEMS.register(bus);
		BLOCKS.register(bus);
		BLOCK_ENTITY.register(bus);
		MENU_TYPE.register(bus);
		TAB.register(bus);
		ATTACHMENT_TYPES.register(bus);
		DATA_COMPONENT_TYPES.register(bus);
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
			var handler = player.getCapability(CuriosCapability.INVENTORY);
			if (handler == null)return def;
			var s = handler.findCurios(is);
			if(!s.isEmpty())return map.apply(s.get(0).stack());
		}
		return def;
	}

	public static boolean notifyBlocks(Level level, BlockPos worldPosition, Direction direction) {
		return !EventHooks.onNeighborNotify(level, worldPosition, level.getBlockState(worldPosition), EnumSet.of(direction), false).isCanceled();
	}

	public static RegistryFriendlyByteBuf makeRegByteBuf(ByteBuf buffer, RegistryAccess reg) {
		return new RegistryFriendlyByteBuf(buffer, reg, ConnectionType.NEOFORGE);
	}

	public static Iterable<Holder<Item>> getIngredientItems(Ingredient ingr) {
		if (ingr.isCustom())return Collections.emptyList();
		return ingr.getValues();
	}
}
