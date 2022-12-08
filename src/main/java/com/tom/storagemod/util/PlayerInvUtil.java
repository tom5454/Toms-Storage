package com.tom.storagemod.util;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.fml.ModList;

public class PlayerInvUtil {
	private static boolean curios = ModList.get().isLoaded("curios");

	public static <T> T findItem(Player player, Predicate<ItemStack> is, T def, Function<ItemStack, T> map) {
		if(is.test(player.getMainHandItem()))return map.apply(player.getMainHandItem());
		if(is.test(player.getOffhandItem()))return map.apply(player.getOffhandItem());
		Inventory inv = player.getInventory();
		int size = inv.getContainerSize();
		for(int i = 0;i<size;i++) {
			ItemStack s = inv.getItem(i);
			if(is.test(s)) {
				return map.apply(s);
			}
		}
		if(curios) {
			/*List<SlotResult> s = CuriosApi.getCuriosHelper().findCurios(player, is);
			if(!s.isEmpty())return map.apply(s.get(0).stack());*/
		}
		return def;
	}
}
