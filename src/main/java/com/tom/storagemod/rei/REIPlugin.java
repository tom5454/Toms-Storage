package com.tom.storagemod.rei;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.gui.GuiStorageTerminalBase;

import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.SimpleGridMenuDisplay;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;

public class REIPlugin implements REIClientPlugin {

	@Override
	public void registerTransferHandlers(TransferHandlerRegistry recipeHelper) {
		CategoryIdentifier<?> CRAFTING = CategoryIdentifier.of("minecraft", "plugins/crafting");
		recipeHelper.register(new TransferHandler() {

			@Override
			public Result handle(Context context) {
				if(context.getMenu() instanceof IREIAutoFillTerminal) {
					if (!context.getDisplay().getCategoryIdentifier().equals(CRAFTING) || context.getMinecraft().currentScreen == context.getContainerScreen())
						return Result.createNotApplicable();
					Display recipe = context.getDisplay();
					ItemStack[][] stacks = recipe.getInputEntries().stream().map(l ->
					l.stream().filter(es -> es.getDefinition().getValueType() == ItemStack.class).
					map(es -> es.getValue()).filter(e -> e != null).toArray(ItemStack[]::new)
							).toArray(ItemStack[][]::new);
					IREIAutoFillTerminal term = (IREIAutoFillTerminal) context.getMenu();
					List<Integer> missing = new ArrayList<>();
					int width = recipe instanceof SimpleGridMenuDisplay ? ((SimpleGridMenuDisplay)recipe).getWidth() : Integer.MAX_VALUE;
					Set<StoredItemStack> stored = new HashSet<>(term.getStoredItems());
					{
						int i = 0;
						for (ItemStack[] list : stacks) {
							if(list.length > 0) {
								boolean found = false;
								for (ItemStack stack : list) {
									if (stack != null && context.getMinecraft().player.getInventory().getSlotWithStack(stack) != -1) {
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
								}
							}
							i++;
						}
					}
					if (context.isActuallyCrafting()) {
						NbtCompound compound = new NbtCompound();
						NbtList list = new NbtList();
						for (int i = 0;i < stacks.length;++i) {
							if (stacks[i] != null) {
								NbtCompound CompoundTag = new NbtCompound();
								CompoundTag.putByte("s", (byte) (width == 1 ? i * 3 : width == 2 ? ((i % 2) + i / 2 * 3) : i));
								int k = 0;
								for (int j = 0;j < stacks[i].length && k < 9;j++) {
									if (stacks[i][j] != null && !stacks[i][j].isEmpty()) {
										StoredItemStack s = new StoredItemStack(stacks[i][j]);
										if(stored.contains(s) || context.getMinecraft().player.getInventory().getSlotWithStack(stacks[i][j]) != -1) {
											NbtCompound tag = new NbtCompound();
											stacks[i][j].writeNbt(tag);
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
					if(!missing.isEmpty()) {
						return Result.createSuccessful().color(0x67aaaa00).blocksFurtherHandling(false).
								renderer((matrices, mouseX, mouseY, delta, widgets, bounds, d) -> {
									int i = 0;
									for (Widget widget : widgets) {
										if (widget instanceof Slot && ((Slot) widget).getNoticeMark() == Slot.INPUT) {
											if (missing.contains(i++)) {
												matrices.push();
												matrices.translate(0, 0, 400);
												Rectangle innerBounds = ((Slot) widget).getInnerBounds();
												Screen.fill(matrices, innerBounds.x, innerBounds.y, innerBounds.getMaxX(), innerBounds.getMaxY(), 0x40ff0000);
												matrices.pop();
											}
										}
									}
								});
					}
					return Result.createSuccessful().blocksFurtherHandling();
				}
				return Result.createNotApplicable();
			}
		});
	}

	public static void setReiSearchText(String text) {
		REIRuntime.getInstance().getSearchTextField().setText(text);
	}

	@Override
	public void registerCategories(CategoryRegistry registry) {
		registry.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(StorageMod.craftingTerminal));
	}

	@Override
	public void registerScreens(ScreenRegistry registry) {
		registry.registerFocusedStack((scr, point) -> {
			if(scr instanceof GuiStorageTerminalBase<?> t) {
				ItemStack stack = t.getStackUnderMouse(point.x, point.y);
				if(!stack.isEmpty())return CompoundEventResult.interruptTrue(EntryStack.of(VanillaEntryTypes.ITEM, stack));
			}
			return CompoundEventResult.pass();
		});
	}
}
