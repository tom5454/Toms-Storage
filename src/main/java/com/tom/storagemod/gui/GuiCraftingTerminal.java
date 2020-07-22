package com.tom.storagemod.gui;

import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

public class GuiCraftingTerminal extends GuiStorageTerminalBase<ContainerCraftingTerminal> implements RecipeBookProvider {
	private static final Identifier gui = new Identifier("toms_storage", "textures/gui/crafting_terminal.png");
	private final RecipeBookWidget recipeBookGui = new RecipeBookWidget() {
		{
			recipeFinder = handler.new TerminalRecipeItemHelper();
		}
	};
	private boolean widthTooNarrow;
	private static final Identifier RECIPE_BUTTON_TEXTURE = new Identifier("textures/gui/recipe_button.png");

	public GuiCraftingTerminal(ContainerCraftingTerminal screenContainer, PlayerInventory inv, Text titleIn) {
		super(screenContainer, inv, titleIn);
	}

	@Override
	public Identifier getGui() {
		return gui;
	}

	@Override
	protected void onUpdateSearch(String text) {
		if((searchType & 4) > 0) {//ModList.get().isLoaded("jei") ||
			if(recipeBookGui.searchField != null)recipeBookGui.searchField.setText(text);
			recipeBookGui.refresh();
		}
	}

	@Override
	protected void init() {
		backgroundWidth = 194;
		backgroundHeight = 256;
		rowCount = 5;
		super.init();
		this.widthTooNarrow = this.width < 379;
		this.recipeBookGui.initialize(this.width, this.height, this.mc, this.widthTooNarrow, this.handler);
		this.x = this.recipeBookGui.findLeftEdge(this.widthTooNarrow, this.width, this.backgroundWidth);
		this.children.add(this.recipeBookGui);
		this.setInitialFocus(this.recipeBookGui);
		GuiButtonClear btnClr = new GuiButtonClear(x + 80, y + 110, b -> clearGrid());
		addButton(btnClr);
		this.addButton(new TexturedButtonWidget(this.x + 4, this.height / 2, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (buttonWidget) -> {
			this.recipeBookGui.reset(this.widthTooNarrow);
			this.recipeBookGui.toggleOpen();
			this.x = this.recipeBookGui.findLeftEdge(this.widthTooNarrow, this.width, this.backgroundWidth);
			((TexturedButtonWidget)buttonWidget).setPos(this.x + 4, this.height / 2);
			super.searchField.setX(this.x + 82);
			btnClr.setX(this.x + 80);
			buttonSortingType.setX(x - 18);
			buttonDirection.setX(x - 18);
			if(recipeBookGui.isOpen()) {
				buttonSearchType.setX(x - 36);
				buttonCtrlMode.setX(x - 36);
				buttonSearchType.y = y + 5;
				buttonCtrlMode.y = y + 5 + 18;
			} else {
				buttonSearchType.setX(x - 18);
				buttonCtrlMode.setX(x - 18);
				buttonSearchType.y = y + 5 + 18*2;
				buttonCtrlMode.y = y + 5 + 18*3;
			}
		}));
		if(recipeBookGui.isOpen()) {
			buttonSortingType.setX(x - 18);
			buttonDirection.setX(x - 18);
			buttonSearchType.setX(x - 36);
			buttonCtrlMode.setX(x - 36);
			buttonSearchType.y = y + 5;
			buttonCtrlMode.y = y + 5 + 18;
			super.searchField.setX(this.x + 82);
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.recipeBookGui.update();
	}

	@Override
	public void render(MatrixStack st, int p_render_1_, int p_render_2_, float p_render_3_) {
		this.renderBackground(st);
		if (this.recipeBookGui.isOpen() && this.widthTooNarrow) {
			this.drawBackground(st, p_render_3_, p_render_1_, p_render_2_);
			RenderSystem.disableLighting();
			this.recipeBookGui.render(st, p_render_1_, p_render_2_, p_render_3_);
		} else {
			RenderSystem.disableLighting();
			this.recipeBookGui.render(st, p_render_1_, p_render_2_, p_render_3_);
			super.render(st, p_render_1_, p_render_2_, p_render_3_);
			this.recipeBookGui.drawGhostSlots(st, this.x, this.y, true, p_render_3_);
		}

		this.drawMouseoverTooltip(st, p_render_1_, p_render_2_);
		this.recipeBookGui.drawTooltip(st, this.x, this.y, p_render_1_, p_render_2_);
		this.setFocused(this.recipeBookGui);
	}

	@Override
	protected boolean isPointWithinBounds(int x, int y, int width, int height, double mouseX, double mouseY) {
		return (!this.widthTooNarrow || !this.recipeBookGui.isOpen()) && super.isPointWithinBounds(x, y, width, height, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		if (this.recipeBookGui.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
			return true;
		} else {
			return this.widthTooNarrow && this.recipeBookGui.isOpen() ? true : super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
		}
	}

	@Override
	protected boolean isClickOutsideBounds(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
		boolean flag = mouseX < guiLeftIn || mouseY < guiTopIn || mouseX >= guiLeftIn + this.backgroundWidth || mouseY >= guiTopIn + this.backgroundHeight;
		return this.recipeBookGui.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, this.backgroundWidth, this.backgroundHeight, mouseButton) && flag;
	}

	/**
	 * Called when the mouse is clicked over a slot or outside the gui.
	 */
	@Override
	protected void onMouseClick(Slot slotIn, int slotId, int mouseButton, SlotActionType type) {
		super.onMouseClick(slotIn, slotId, mouseButton, type);
		this.recipeBookGui.slotClicked(slotIn);
	}

	@Override
	public void refreshRecipeBook() {
		this.recipeBookGui.refresh();
	}

	@Override
	public void removed() {
		this.recipeBookGui.close();
		super.removed();
	}

	@Override
	public RecipeBookWidget getRecipeBookWidget() {
		return this.recipeBookGui;
	}

	private void clearGrid() {
		this.mc.interactionManager.clickButton((this.handler).syncId, 0);
	}

	public class GuiButtonClear extends ButtonWidget {

		public GuiButtonClear(int x, int y, PressAction pressable) {
			super(x, y, 11, 11, new LiteralText(""), pressable);
		}

		public void setX(int i) {
			x = i;
		}

		/**
		 * Draws this button to the screen.
		 */
		@Override
		public void renderButton(MatrixStack m, int mouseX, int mouseY, float pt) {
			if (this.visible) {
				mc.getTextureManager().bindTexture(gui);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				int i = this.getYImage(this.hovered);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
				drawTexture(m, this.x, this.y, 194 + i * 11, 10, this.width, this.height);
			}
		}
	}
}
