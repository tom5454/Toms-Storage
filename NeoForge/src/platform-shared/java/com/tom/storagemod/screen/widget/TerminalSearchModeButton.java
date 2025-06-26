package com.tom.storagemod.screen.widget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.util.IAutoFillTerminal;
import com.tom.storagemod.util.PopupMenuManager;
import com.tom.storagemod.util.PopupMenuManager.ButtonElement;

public class TerminalSearchModeButton extends IconButton {
	private static final ResourceLocation ICON = ResourceLocation.tryBuild(StorageMod.modid, "icons/search_mode");
	private static final ResourceLocation AUTO = ResourceLocation.tryBuild(StorageMod.modid, "icons/search_mode_auto");
	private static final ResourceLocation KEEP = ResourceLocation.tryBuild(StorageMod.modid, "icons/search_mode_keep");
	private static final ResourceLocation SYNC = ResourceLocation.tryBuild(StorageMod.modid, "icons/search_mode_sync");
	private int searchType = -1;

	public TerminalSearchModeButton(int x, int y, PopupMenuManager menu, boolean canSync, Runnable sendUpdate) {
		super(x, y, Component.translatable(""), ICON, handleClick(menu, canSync, sendUpdate));
	}

	private static OnPress handleClick(PopupMenuManager menu, boolean canSync, Runnable sendUpdate) {
		return b0 -> {
			TerminalSearchModeButton b = (TerminalSearchModeButton) b0;
			String sh = IAutoFillTerminal.getHandlerNameOr(I18n.get("tooltip.toms_storage.recipe_book"));
			menu.open(b.getX() + 16, b.getY() + 16,
					new ButtonElement(
							() -> Component.translatable("tooltip.toms_storage.opt.search_auto", (b.searchType & 1) != 0 ? CommonComponents.GUI_YES : CommonComponents.GUI_NO),
							() -> {
								b.flipBit(1);
								sendUpdate.run();
							}),
					new ButtonElement(
							() -> Component.translatable("tooltip.toms_storage.opt.search_keep", (b.searchType & 2) != 0 ? CommonComponents.GUI_YES : CommonComponents.GUI_NO),
							() -> {
								b.flipBit(2);
								sendUpdate.run();
							}),
					new ButtonElement(
							() -> {
								return canSync ?
										Component.translatable("tooltip.toms_storage.opt.search_sync", sh, (b.searchType & 4) != 0 ? CommonComponents.GUI_YES : CommonComponents.GUI_NO) :
											Component.literal("").append(Component.translatable("tooltip.toms_storage.opt.search_sync", sh, CommonComponents.GUI_NO).withStyle(ChatFormatting.DARK_GRAY));
							},
							() -> {
								b.flipBit(4);
								sendUpdate.run();
							}),
					new ButtonElement(
							() -> {
								return canSync ?
										Component.translatable("tooltip.toms_storage.opt.search_smart", sh, (b.searchType & 8) == 0 ? CommonComponents.GUI_YES : CommonComponents.GUI_NO) :
											Component.literal("").append(Component.translatable("tooltip.toms_storage.opt.search_smart", sh, CommonComponents.GUI_NO).withStyle(ChatFormatting.DARK_GRAY));
							},
							() -> {
								b.flipBit(8);
								sendUpdate.run();
							})
					);
		};
	}

	private void flipBit(int i) {
		int s = searchType;
		if ((s & i) != 0)
			s &= ~i;
		else
			s |= i;
		setSearchType(s);
	}

	@Override
	protected void drawIcon(GuiGraphics st, int mouseX, int mouseY, float pt) {
		int x = getX();
		int y = getY();
		st.blitSprite(RenderPipelines.GUI_TEXTURED, ICON, x + 1, y + 1, 14, 14);
		if((searchType & 1) > 0)st.blitSprite(RenderPipelines.GUI_TEXTURED, AUTO, x + 1, y + 1, 14, 14);
		if((searchType & 2) > 0)st.blitSprite(RenderPipelines.GUI_TEXTURED, KEEP, x + 1, y + 1, 14, 14);
		if((searchType & 4) > 0)st.blitSprite(RenderPipelines.GUI_TEXTURED, SYNC, x + 1, y + 1, 14, 14);
	}

	public void setSearchType(int searchType) {
		if (this.searchType != searchType) {
			StringBuilder sb = new StringBuilder("tooltip.toms_storage.search");
			if((searchType & 1) > 0)sb.append("_auto");
			if((searchType & 2) > 0)sb.append("_keep");
			if((searchType & 4) > 0)sb.append("_sync");
			setTooltip(Tooltip.create(Component.translatable(sb.toString(), IAutoFillTerminal.getHandlerNameOr(I18n.get("tooltip.toms_storage.recipe_book")))));
		}
		this.searchType = searchType;
	}

	public int getSearchType() {
		return searchType;
	}
}
