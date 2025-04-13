package com.tom.storagemod.screen;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.menu.CraftingTerminalMenu;
import com.tom.storagemod.screen.widget.CraftingTerminalRecipeBookWidget;
import com.tom.storagemod.screen.widget.ToggleButton;
import com.tom.storagemod.util.IAutoFillTerminal;

public class CraftingTerminalScreen extends AbstractStorageTerminalScreen<CraftingTerminalMenu> implements RecipeUpdateListener {
	private static final ResourceLocation gui = ResourceLocation.tryBuild("toms_storage", "textures/gui/crafting_terminal.png");
	private final CraftingTerminalRecipeBookWidget recipeBookGui;
	private boolean widthTooNarrow;
	private ToggleButton buttonPullFromInv;
	private ButtonClear btnClr;

	public CraftingTerminalScreen(CraftingTerminalMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn, 5, 256, 7, 17);
		recipeBookGui = new CraftingTerminalRecipeBookWidget(screenContainer);
	}

	@Override
	public ResourceLocation getGui() {
		return gui;
	}

	@Override
	protected void onUpdateSearch(String text) {
		if(IAutoFillTerminal.hasSync() || (buttonSearchType.getSearchType() & 4) > 0) {
			if(recipeBookGui.searchBox != null)recipeBookGui.searchBox.setValue(text);
			recipeBookGui.recipesUpdated();
		}
	}

	@Override
	protected void init() {
		imageWidth = 194;
		imageHeight = 256;
		super.init();
		this.widthTooNarrow = this.width < 379 || true;
		if(this.recipeBookGui.isVisible())recipeBookGui.toggleVisibility();//
		this.recipeBookGui.init(this.width, this.height, this.minecraft, this.widthTooNarrow);
		this.leftPos = this.recipeBookGui.updateScreenPosition(this.width, this.imageWidth - 16);
		addWidget(recipeBookGui);
		this.setInitialFocus(this.recipeBookGui);
		btnClr = new ButtonClear(leftPos + 80, topPos + 20 + rowCount * 18, b -> clearGrid());
		addRenderableWidget(btnClr);
		buttonPullFromInv = addRenderableWidget(ToggleButton.builder(leftPos - 18, topPos + 5 + 18*6).
				iconOff(ResourceLocation.tryBuild(StorageMod.modid, "icons/refill_off")).
				iconOn(ResourceLocation.tryBuild(StorageMod.modid, "icons/refill_on")).
				build(s -> {
					buttonPullFromInv.setState(s);
					sendUpdate();
				}));
		buttonPullFromInv.setTooltip(Tooltip.create(Component.translatable("tooltip.toms_storage.crafting_pull_off")), Tooltip.create(Component.translatable("tooltip.toms_storage.crafting_pull_on")));
		addRenderableWidget(new RecipeBookButton(this.leftPos + 4, this.topPos + 38 + rowCount * 18, (p_214076_1_) -> {
			this.recipeBookGui.toggleVisibility();
			this.leftPos = this.recipeBookGui.updateScreenPosition(this.width, this.imageWidth - 16);
			((ImageButton)p_214076_1_).setPosition(this.leftPos + 4, this.topPos + 38 + rowCount * 18);
			setButtonsPos();
		}));
		setButtonsPos();
		onPacket();
	}

	private void setButtonsPos() {
		searchField.setX(this.leftPos + 82);
		btnClr.setX(this.leftPos + 80);
		int space = recipeBookGui.isVisible() ? recipeBookGui.searchBox.getY() - 16 : imageHeight;
		List<Button> buttons = Arrays.asList(buttonSortingType, buttonDirection, buttonSearchType, buttonCtrlMode, buttonGhostMode, buttonPullFromInv, buttonTallMode);
		int y = topPos + 5;
		int x = leftPos - 18;
		for (int i = 0; i < buttons.size(); i++) {
			Button b = buttons.get(i);
			if(y + 18 > space) {
				y = topPos + 5;
				x -= 18;
			}
			b.setX(x);
			b.setY(y);
			y += 18;
		}
	}

	@Override
	protected void onPacket() {
		super.onPacket();
		boolean pullFromInv = (menu.modes & 0x20) != 0;
		buttonPullFromInv.setState(pullFromInv);
	}

	@Override
	protected int writeModes() {
		int d = super.writeModes();
		d |= (buttonPullFromInv.getState() ? 0x20 : 0);
		return d;
	}

	@Override
	public void containerTick() {
		super.containerTick();
		this.recipeBookGui.tick();
	}

	@Override
	public void render(GuiGraphics st, int mouseX, int mouseY, float partialTicks) {
		if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
			st.pose().pushPose();
			st.pose().translate(0, 0, -1000);
			super.render(st, -1, -1, partialTicks);
			st.pose().popPose();
			this.recipeBookGui.render(st, mouseX, mouseY, partialTicks);
		} else {
			super.render(st, mouseX, mouseY, partialTicks);
			this.recipeBookGui.render(st, mouseX, mouseY, partialTicks);
		}

		this.renderTooltip(st, mouseX, mouseY);
		this.recipeBookGui.renderTooltip(st, mouseX, mouseY, hoveredSlot);
		this.setInitialFocus(this.recipeBookGui);
	}

	@Override
	protected void renderSlots(GuiGraphics p_376566_) {
		super.renderSlots(p_376566_);
		this.recipeBookGui.renderGhostRecipe(p_376566_, true);
	}

	@Override
	protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
		return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double x, double y, int b) {
		if (this.recipeBookGui.mouseClicked(x, y, b)) {
			this.setFocused(this.recipeBookGui);
			return true;
		}
		if (this.widthTooNarrow && this.recipeBookGui.isVisible()) {
			if (this.recipeBookGui.hasClickedOutside(x, y, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, b))
				this.recipeBookGui.toggleVisibility();
			return false;
		}
		return super.mouseClicked(x, y, b);
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
		boolean flag = mouseX < guiLeftIn || mouseY < guiTopIn || mouseX >= guiLeftIn + this.imageWidth || mouseY >= guiTopIn + this.imageHeight;
		return this.recipeBookGui.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, mouseButton) && flag;
	}

	/**
	 * Called when the mouse is clicked over a slot or outside the gui.
	 */
	@Override
	protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		super.slotClicked(slotIn, slotId, mouseButton, type);
		this.recipeBookGui.slotClicked(slotIn);
	}

	@Override
	public void recipesUpdated() {
		this.recipeBookGui.recipesUpdated();
	}

	private void clearGrid() {
		this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 0);
	}

	@Override
	public boolean keyPressed(int code, int p_231046_2_, int p_231046_3_) {
		if(code == GLFW.GLFW_KEY_S && hoveredSlot != null) {
			ItemStack itemstack = null;

			for (var e : this.recipeBookGui.ghostSlots.ingredients.entrySet()) {
				if (e.getKey() == hoveredSlot) {
					itemstack = e.getValue().getItem(this.recipeBookGui.ghostSlots.slotSelectTime.currentIndex());
					break;
				}
			}
			if(itemstack != null) {
				searchField.setValue(itemstack.getHoverName().getString());
				searchField.setFocused(false);
				return true;
			}
		}
		if(code == GLFW.GLFW_KEY_ESCAPE && this.recipeBookGui.isVisible() && this.widthTooNarrow) {
			this.recipeBookGui.toggleVisibility();
			return true;
		}
		return super.keyPressed(code, p_231046_2_, p_231046_3_);
	}

	public static class ButtonClear extends Button {
		private static final WidgetSprites SPRITES = new WidgetSprites(
				ResourceLocation.tryBuild(StorageMod.modid, "widget/clear_button"),
				ResourceLocation.tryBuild(StorageMod.modid, "widget/clear_button_disabled"),
				ResourceLocation.tryBuild(StorageMod.modid, "widget/clear_button_hovered")
				);

		public ButtonClear(int x, int y, OnPress pressable) {
			super(x, y, 11, 11, Component.empty(), pressable, DEFAULT_NARRATION);
		}

		/**
		 * Draws this button to the screen.
		 */
		@Override
		public void renderWidget(GuiGraphics st, int mouseX, int mouseY, float pt) {
			if (this.visible) {
				int x = getX();
				int y = getY();
				this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				st.blitSprite(RenderType::guiTextured, SPRITES.get(this.active, this.isHoveredOrFocused()), x, y, this.width, this.height);
			}
		}
	}

	@Override
	public void fillGhostRecipe(RecipeDisplay recipeDisplay) {
		recipeBookGui.fillGhostRecipe(recipeDisplay);
	}
}
