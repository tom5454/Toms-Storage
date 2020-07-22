package com.tom.storagemod.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.screen.AbstractRecipeScreenHandler;

import com.google.common.collect.Lists;

import com.tom.storagemod.gui.ContainerCraftingTerminal;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin {

	@Inject(at = @At("HEAD"), method = "getGroups(Lnet/minecraft/screen/AbstractRecipeScreenHandler;)Ljava/util/List;", cancellable = true)
	private static void onGetGroups(AbstractRecipeScreenHandler<?> handler, CallbackInfoReturnable<List<RecipeBookGroup>> cbi) {
		if(handler instanceof ContainerCraftingTerminal) {
			cbi.setReturnValue(Lists.newArrayList(RecipeBookGroup.SEARCH, RecipeBookGroup.CRAFTING_EQUIPMENT, RecipeBookGroup.CRAFTING_BUILDING_BLOCKS, RecipeBookGroup.CRAFTING_MISC, RecipeBookGroup.CRAFTING_REDSTONE));
		}
	}
}
