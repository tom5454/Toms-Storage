package com.tom.storagemod.rei;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;

public class REIPlugin implements REIClientPlugin {

	@Override
	public void registerTransferHandlers(TransferHandlerRegistry recipeHelper) {
		CategoryIdentifier<?> CRAFTING = CategoryIdentifier.of("minecraft", "plugins/crafting");
		recipeHelper.register(new TransferHandler() {

			@Override
			public Result handle(Context context) {
				if(context.getMenu() instanceof IREIAutoFillTerminal) {
					if (!context.getDisplay().getCategoryIdentifier().equals(CRAFTING))
						return Result.createNotApplicable();
					if (!context.isActuallyCrafting())
						return Result.createSuccessful();
					Display recipe = context.getDisplay();
					ItemStack[][] stacks = recipe.getInputEntries().stream().map(l ->
					l.stream().filter(es -> es.getDefinition().getValueType() == ItemStack.class).
					map(es -> es.getValue()).filter(e -> e != null).toArray(ItemStack[]::new)
							).toArray(ItemStack[][]::new);
					NbtCompound compound = new NbtCompound();
					NbtList list = new NbtList();
					int width = recipe instanceof SimpleGridMenuDisplay ? ((SimpleGridMenuDisplay)recipe).getWidth() : Integer.MAX_VALUE;
					for (int i = 0;i < stacks.length;++i) {
						if (stacks[i] != null) {
							NbtCompound CompoundTag = new NbtCompound();
							CompoundTag.putByte("s", (byte) (width == 1 ? i * 3 : width == 2 ? ((i % 2) + i / 2 * 3) : i));
							for (int j = 0;j < stacks[i].length && j < 3;j++) {
								if (stacks[i][j] != null && !stacks[i][j].isEmpty()) {
									NbtCompound tag = new NbtCompound();
									stacks[i][j].writeNbt(tag);
									CompoundTag.put("i" + j, tag);
								}
							}
							CompoundTag.putByte("l", (byte) Math.min(3, stacks[i].length));
							list.add(CompoundTag);
						}
					}
					compound.put("i", list);
					((IREIAutoFillTerminal)context.getMenu()).sendMessage(compound);
					context.getMinecraft().setScreen(context.getContainerScreen());
					return Result.createSuccessful();
				}
				return Result.createNotApplicable();
			}
		});
	}

	public static void setReiSearchText(String text) {
		REIRuntime.getInstance().getSearchTextField().setText(text);
	}
}
