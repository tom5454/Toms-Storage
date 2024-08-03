package com.tom.storagemod.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.ModList;

import top.theillusivec4.curios.api.CuriosApi;

public class PlayerInvUtil {
	private static boolean curios = ModList.get().isLoaded("curios");

	public static <T> T findItem(PlayerEntity player, Predicate<ItemStack> is, T def, Function<ItemStack, T> map) {
		if(is.test(player.getMainHandItem()))return map.apply(player.getMainHandItem());
		if(is.test(player.getOffhandItem()))return map.apply(player.getOffhandItem());
		PlayerInventory inv = player.inventory;
		int size = inv.getContainerSize();
		for(int i = 0;i<size;i++) {
			ItemStack s = inv.getItem(i);
			if(is.test(s)) {
				return map.apply(s);
			}
		}
		if(curios) {
			Optional<ImmutableTriple<String, Integer, ItemStack>> s = CuriosApi.getCuriosHelper().findEquippedCurio(is, player);
			if(s.isPresent())return map.apply(s.get().getRight().getStack());
		}
		return def;
	}
}