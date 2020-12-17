package com.tom.storagemod.rei;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;

import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.api.BuiltinPlugin;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.TransferRecipeDisplay;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.impl.Internals;

public class REIPlugin implements REIPluginV0 {

	@Override
	public Identifier getPluginIdentifier() {
		return new Identifier("toms_storage:plugin");
	}

	@Override
	public void registerOthers(RecipeHelper recipeHelper) {
		recipeHelper.registerAutoCraftingHandler(new AutoTransferHandler() {

			@Override
			public Result handle(Context context) {
				if(context.getContainer() instanceof IREIAutoFillTerminal) {
					if (!(context.getRecipe() instanceof TransferRecipeDisplay) ||
							!context.getRecipe().getRecipeCategory().equals(BuiltinPlugin.CRAFTING))
						return Result.createNotApplicable();
					if (!context.isActuallyCrafting())
						return Result.createSuccessful();
					TransferRecipeDisplay recipe = (TransferRecipeDisplay) context.getRecipe();
					ItemStack[][] stacks = recipe.getInputEntries().stream().map(l ->
					l.stream().map(EntryStack::getItemStack).filter(e -> e != null).toArray(ItemStack[]::new)
							).toArray(ItemStack[][]::new);
					CompoundTag compound = new CompoundTag();
					ListTag list = new ListTag();
					for (int i = 0;i < stacks.length;++i) {
						if (stacks[i] != null) {
							CompoundTag CompoundTag = new CompoundTag();
							CompoundTag.putByte("s", (byte) (recipe.getWidth() == 1 ? i * 3 : recipe.getWidth() == 2 ? ((i % 2) + i / 2 * 3) : i));
							for (int j = 0;j < stacks[i].length && j < 3;j++) {
								if (stacks[i][j] != null && !stacks[i][j].isEmpty()) {
									CompoundTag tag = new CompoundTag();
									stacks[i][j].toTag(tag);
									CompoundTag.put("i" + j, tag);
								}
							}
							CompoundTag.putByte("l", (byte) Math.min(3, stacks[i].length));
							list.add(CompoundTag);
						}
					}
					compound.put("i", list);
					((IREIAutoFillTerminal)context.getContainer()).sendMessage(compound);
					context.getMinecraft().openScreen(context.getContainerScreen());
					return Result.createSuccessful();
				}
				return Result.createNotApplicable();
			}
		});
	}

	@Override
	public void postRegister() {
		try {
			reiRuntime = Internals.getREIHelper();
		} catch(Throwable e) {
			reiRuntime = null;
		}
	}

	private static REIHelper reiRuntime;
	public static void setReiSearchText(String text) {
		if (reiRuntime != null) {
			if (reiRuntime.getSearchTextField() != null) {
				reiRuntime.getSearchTextField().setText(text);
			}
		}
	}
}
