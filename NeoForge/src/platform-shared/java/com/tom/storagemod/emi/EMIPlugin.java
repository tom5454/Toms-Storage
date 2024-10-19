package com.tom.storagemod.emi;

import com.tom.storagemod.Content;
import com.tom.storagemod.screen.AbstractStorageTerminalScreen;
import com.tom.storagemod.util.IAutoFillTerminal;
import com.tom.storagemod.util.IAutoFillTerminal.ISearchHandler;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;

@EmiEntrypoint
public class EMIPlugin implements EmiPlugin {

	@Override
	public void register(EmiRegistry registry) {
		registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(Content.craftingTerminal.get()));
		registry.addRecipeHandler(Content.craftingTerminalMenu.get(), new EmiTransferHandler());
		registry.addGenericDragDropHandler(new EmiGhostIngredientHandler());
		registry.addGenericStackProvider((scr, x, y) -> {
			if(scr instanceof AbstractStorageTerminalScreen<?> t) {
				net.minecraft.world.inventory.Slot sl = t.getSlotUnderMouse();
				if(sl != null)return new EmiStackInteraction(EmiStack.of(sl.getItem()), null, false);
			}
			return EmiStackInteraction.EMPTY;
		});
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
