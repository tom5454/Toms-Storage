package com.tom.storagemod.rei;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;

import me.shedaniel.rei.api.AutoTransferHandler;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import me.shedaniel.rei.plugin.crafting.DefaultCraftingDisplay;

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
					if (!(context.getRecipe() instanceof DefaultCraftingDisplay))
						return Result.createNotApplicable();
					if (!context.isActuallyCrafting())
						return Result.createSuccessful();
					DefaultCraftingDisplay recipe = (DefaultCraftingDisplay) context.getRecipe();
					ItemStack[][] stacks = recipe.getInputEntries().stream().map(l ->
					l.stream().map(EntryStack::getItemStack).filter(e -> e != null).toArray(ItemStack[]::new)
							).toArray(ItemStack[][]::new);
					CompoundTag compound = new CompoundTag();
					ListTag list = new ListTag();
					for (int i = 0;i < stacks.length;++i) {
						if (stacks[i] != null) {
							CompoundTag CompoundTag = new CompoundTag();
							CompoundTag.putByte("s", (byte) i);
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

}
