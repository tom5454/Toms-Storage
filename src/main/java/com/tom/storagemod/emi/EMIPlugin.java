package com.tom.storagemod.emi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.gui.CraftingTerminalMenu;
import com.tom.storagemod.rei.IREIAutoFillTerminal;

import dev.emi.emi.api.EmiFillAction;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRecipeHandler;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;

public class EMIPlugin implements EmiPlugin {

	@Override
	public void register(EmiRegistry registry) {
		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(StorageMod.craftingTerminal));
		registry.addRecipeHandler(StorageMod.craftingTerminalCont, new EmiRecipeHandler<CraftingTerminalMenu>() {

			@Override
			public List<Slot> getInputSources(CraftingTerminalMenu handler) {
				return Collections.emptyList();
			}

			@Override
			public List<Slot> getCraftingSlots(CraftingTerminalMenu handler) {
				return Collections.emptyList();
			}

			@Override
			public boolean supportsRecipe(EmiRecipe recipe) {
				return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING;
			}

			@Override
			public boolean performFill(EmiRecipe recipe, AbstractContainerScreen<CraftingTerminalMenu> screen,
					EmiFillAction action, int amount) {
				boolean p = handleRecipe(recipe, screen, false);
				Minecraft.getInstance().setScreen(screen);
				return p;
			}

			@Override
			public boolean canCraft(EmiRecipe recipe, EmiPlayerInventory inventory,
					AbstractContainerScreen<CraftingTerminalMenu> screen) {
				return true;
			}
		});
	}

	private static boolean handleRecipe(EmiRecipe recipe, AbstractContainerScreen<CraftingTerminalMenu> screen, boolean simulate) {
		IREIAutoFillTerminal term = screen.getMenu();
		ItemStack[][] stacks = recipe.getInputs().stream().map(i ->
		i.getEmiStacks().stream().map(EmiStack::getItemStack).filter(s -> !s.isEmpty()).toArray(ItemStack[]::new)
				).toArray(ItemStack[][]::new);

		List<Integer> missing = new ArrayList<>();
		Set<StoredItemStack> stored = new HashSet<>(term.getStoredItems());
		{
			int i = 0;
			for (ItemStack[] list : stacks) {
				if(list.length > 0) {
					boolean found = false;
					for (ItemStack stack : list) {
						if (stack != null && Minecraft.getInstance().player.getInventory().findSlotMatchingItem(stack) != -1) {
							found = true;
							break;
						}
					}

					if (!found) {
						for (ItemStack stack : list) {
							StoredItemStack s = new StoredItemStack(stack);
							if(stored.contains(s)) {
								found = true;
								break;
							}
						}
					}

					if (!found) {
						//missing.add(width == 1 ? i * 3 : width == 2 ? ((i % 2) + i / 2 * 3) : i);
						missing.add(i);
					}
				}
				i++;
			}
		}

		if(!simulate) {
			CompoundTag compound = new CompoundTag();
			ListTag list = new ListTag();
			for (int i = 0;i < stacks.length;++i) {
				if (stacks[i] != null) {
					CompoundTag CompoundTag = new CompoundTag();
					//CompoundTag.putByte("s", (byte) (width == 1 ? i * 3 : width == 2 ? ((i % 2) + i / 2 * 3) : i));
					CompoundTag.putByte("s", (byte) (i));
					int k = 0;
					for (int j = 0;j < stacks[i].length && k < 9;j++) {
						if (stacks[i][j] != null && !stacks[i][j].isEmpty()) {
							StoredItemStack s = new StoredItemStack(stacks[i][j]);
							if(stored.contains(s) || Minecraft.getInstance().player.getInventory().findSlotMatchingItem(stacks[i][j]) != -1) {
								CompoundTag tag = new CompoundTag();
								stacks[i][j].save(tag);
								CompoundTag.put("i" + (k++), tag);
							}
						}
					}
					CompoundTag.putByte("l", (byte) Math.min(9, k));
					list.add(CompoundTag);
				}
			}
			compound.put("i", list);
			term.sendMessage(compound);
		}
		return missing.isEmpty();
	}
}
