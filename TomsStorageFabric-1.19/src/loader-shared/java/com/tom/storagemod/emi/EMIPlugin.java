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

import com.tom.storagemod.Content;
import com.tom.storagemod.gui.CraftingTerminalMenu;
import com.tom.storagemod.util.IAutoFillTerminal;
import com.tom.storagemod.util.IAutoFillTerminal.ISearchHandler;
import com.tom.storagemod.util.StoredItemStack;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiStack;

public class EMIPlugin implements EmiPlugin {

	@Override
	public void register(EmiRegistry registry) {
		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(Content.craftingTerminal.get()));
		registry.addRecipeHandler(Content.craftingTerminalCont.get(), new StandardRecipeHandler<CraftingTerminalMenu>() {

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
			public boolean craft(EmiRecipe recipe, EmiCraftContext<CraftingTerminalMenu> context) {
				AbstractContainerScreen<CraftingTerminalMenu> screen = context.getScreen();
				boolean p = handleRecipe(recipe, screen, false);
				Minecraft.getInstance().setScreen(screen);
				return p;
			}

			@Override
			public boolean canCraft(EmiRecipe recipe, EmiCraftContext<CraftingTerminalMenu> context) {
				return true;
			}
		});
	}

	private static boolean handleRecipe(EmiRecipe recipe, AbstractContainerScreen<CraftingTerminalMenu> screen, boolean simulate) {
		IAutoFillTerminal term = screen.getMenu();
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

	static {
		IAutoFillTerminal.updateSearch.add(new ISearchHandler() {

			@Override
			public void setSearch(String set) {
				EmiApi.setSearchText(set);
			}

			@Override
			public String getSearch() {
				return EmiApi.getSearchText();
			}

			@Override
			public String getName() {
				return "EMI";
			}
		});
	}
}
