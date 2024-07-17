package com.tom.storagemod.emi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.menu.CraftingTerminalMenu;
import com.tom.storagemod.util.IAutoFillTerminal;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.Widget;

public class EmiTransferHandler implements StandardRecipeHandler<CraftingTerminalMenu> {

	@Override
	public List<Slot> getInputSources(CraftingTerminalMenu handler) {
		return Collections.emptyList();
	}

	@Override
	public List<Slot> getCraftingSlots(CraftingTerminalMenu handler) {
		return Collections.emptyList();
	}

	@Override
	public EmiPlayerInventory getInventory(AbstractContainerScreen<CraftingTerminalMenu> screen) {
		List<EmiStack> stacks = new ArrayList<>();
		screen.getMenu().slots.subList(1, screen.getMenu().slots.size()).stream().map(Slot::getItem).map(EmiStack::of).forEach(stacks::add);
		//Causes lag
		//screen.getMenu().getStoredItems().forEach(s -> stacks.add(EmiStack.of(s.getStack(), s.getQuantity())));
		return new EmiPlayerInventory(stacks);
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
	}

	@Override
	public boolean canCraft(EmiRecipe recipe, EmiCraftContext<CraftingTerminalMenu> context) {
		return true;
	}

	@Override
	public boolean craft(EmiRecipe recipe, EmiCraftContext<CraftingTerminalMenu> context) {
		AbstractContainerScreen<CraftingTerminalMenu> screen = context.getScreen();
		handleRecipe(recipe, screen, false);
		Minecraft.getInstance().setScreen(screen);
		return true;
	}

	@Override
	public void render(EmiRecipe recipe, EmiCraftContext<CraftingTerminalMenu> context, List<Widget> widgets,
			GuiGraphics matrices) {
		RenderSystem.enableDepthTest();
		List<Integer> missing = handleRecipe(recipe, context.getScreen(), true);
		int i = 0;
		for (Widget w : widgets) {
			if (w instanceof SlotWidget sw) {
				int j = i++;
				EmiIngredient stack = sw.getStack();
				Bounds bounds = sw.getBounds();
				if (sw.getRecipe() == null && !stack.isEmpty()) {
					if (missing.contains(j)) {
						matrices.fill(bounds.x(), bounds.y(), bounds.x() + bounds.width(), bounds.y() + bounds.height(), 0x44FF0000);
					}
				}
			}
		}
	}

	private static List<Integer> handleRecipe(EmiRecipe recipe, AbstractContainerScreen<CraftingTerminalMenu> screen, boolean simulate) {
		IAutoFillTerminal term = screen.getMenu();
		Minecraft mc = Minecraft.getInstance();
		ItemStack[][] stacks = recipe.getInputs().stream().map(i ->
		i.getEmiStacks().stream().map(EmiStack::getItemStack).filter(s -> !s.isEmpty()).toArray(ItemStack[]::new)
				).toArray(ItemStack[][]::new);

		int width = recipe.getDisplayWidth();
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
						missing.add(width == 1 ? i * 3 : width == 2 ? ((i % 2) + i / 2 * 3) : i);
						//missing.add(i);
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
								CompoundTag.put("i" + (k++), stacks[i][j].save(mc.level.registryAccess()));
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
		return missing;
	}
}
