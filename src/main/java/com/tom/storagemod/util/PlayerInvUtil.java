package com.tom.storagemod.util;

import java.util.function.Function;
import java.util.function.Predicate;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import dev.emi.trinkets.api.TrinketsApi;

public class PlayerInvUtil {
	private static boolean trinkets = FabricLoader.getInstance().isModLoaded("trinkets");

	public static <T> T findItem(PlayerEntity player, Predicate<ItemStack> is, T def, Function<ItemStack, T> map) {
		if(is.test(player.getMainHandStack()))return map.apply(player.getMainHandStack());
		if(is.test(player.getOffHandStack()))return map.apply(player.getOffHandStack());
		Inventory inv = player.getInventory();
		int size = inv.size();
		for(int i = 0;i<size;i++) {
			ItemStack s = inv.getStack(i);
			if(is.test(s)) {
				return map.apply(s);
			}
		}
		if(trinkets) {
			var tc = TrinketsApi.getTrinketComponent(player).orElse(null);
			if(tc != null) {
				var s = tc.getEquipped(is);
				if(!s.isEmpty())return map.apply(s.get(0).getRight());
			}
		}
		return def;
	}
}
