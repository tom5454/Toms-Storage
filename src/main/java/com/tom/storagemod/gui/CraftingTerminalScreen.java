package com.tom.storagemod.gui;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.recipebook.RecipeBookGhostSlots;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

public class CraftingTerminalScreen extends AbstractStorageTerminalScreen<CraftingTerminalMenu> implements RecipeBookProvider {
	private static final Identifier gui = new Identifier("toms_storage", "textures/gui/crafting_terminal.png");
	private final RecipeBookWidget recipeBookGui;
	private boolean widthTooNarrow;
	private static final Identifier RECIPE_BUTTON_TEXTURE = new Identifier("textures/gui/recipe_button.png");
	private GuiButton buttonPullFromInv;
	private boolean pullFromInv;

	public CraftingTerminalScreen(CraftingTerminalMenu screenContainer, PlayerInventory inv, Text titleIn) {
		super(screenContainer, inv, titleIn);
		recipeBookGui = new RecipeBookWidget();
		recipeBookGui.recipeFinder = handler.new TerminalRecipeItemHelper();
	}

	@Override
	public Identifier getGui() {
		return gui;
	}

	@Override
	protected void onUpdateSearch(String text) {
		if(FabricLoader.getInstance().isModLoaded("roughlyenoughitems") || (searchType & 4) > 0) {
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
		this.x = this.recipeBookGui.findLeftEdge(this.width, this.backgroundWidth);
		addDrawableChild(this.recipeBookGui);
		this.setInitialFocus(this.recipeBookGui);
		GuiButtonClear btnClr = new GuiButtonClear(x + 80, y + 110, b -> clearGrid());
		addDrawableChild(btnClr);
		buttonPullFromInv = addDrawableChild(new GuiButton(x - 18, y + 5 + 18*4, 4, b -> {
			pullFromInv = !pullFromInv;
			buttonPullFromInv.state = pullFromInv ? 1 : 0;
			sendUpdate();
		}));
		this.addDrawableChild(new TexturedButtonWidget(this.x + 4, this.height / 2, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (buttonWidget) -> {
			this.recipeBookGui.reset();
			this.recipeBookGui.toggleOpen();
			this.x = this.recipeBookGui.findLeftEdge(this.width, this.backgroundWidth);
			((TexturedButtonWidget)buttonWidget).setPos(this.x + 4, this.height / 2);
			super.searchField.setX(this.x + 82);
			btnClr.setX(this.x + 80);
			buttonSortingType.setX(x - 18);
			buttonDirection.setX(x - 18);
			if(recipeBookGui.isOpen()) {
				buttonSearchType.setX(x - 36);
				buttonCtrlMode.setX(x - 36);
				buttonPullFromInv.setX(x - 54);
				buttonSearchType.y = y + 5;
				buttonCtrlMode.y = y + 5 + 18;
				buttonPullFromInv.y = y + 5 + 18;
			} else {
				buttonSearchType.setX(x - 18);
				buttonCtrlMode.setX(x - 18);
				buttonPullFromInv.setX(x - 18);
				buttonSearchType.y = y + 5 + 18*2;
				buttonCtrlMode.y = y + 5 + 18*3;
				buttonPullFromInv.y = y + 5 + 18*4;
			}
		}));
		if(recipeBookGui.isOpen()) {
			buttonSortingType.setX(x - 18);
			buttonDirection.setX(x - 18);
			buttonSearchType.setX(x - 36);
			buttonCtrlMode.setX(x - 36);
			buttonPullFromInv.setX(x - 54);
			buttonSearchType.y = y + 5;
			buttonCtrlMode.y = y + 5 + 18;
			buttonPullFromInv.y = y + 5 + 18;
			super.searchField.setX(this.x + 82);
		}
		onPacket();
	}

	@Override
	protected void onPacket() {
		super.onPacket();
		int s = handler.terminalData;
		pullFromInv = (s & (1 << 8)) != 0;
		buttonPullFromInv.state = pullFromInv ? 1 : 0;
	}

	@Override
	protected int updateData() {
		int d = super.updateData();
		d |= (pullFromInv ? 1 : 0) << 8;
		return d;
	}

	@Override
	public void handledScreenTick() {
		super.handledScreenTick();
		this.recipeBookGui.update();
	}

	@Override
	public void render(MatrixStack st, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(st);
		if (this.recipeBookGui.isOpen() && this.widthTooNarrow) {
			this.drawBackground(st, partialTicks, mouseX, mouseY);
			this.recipeBookGui.render(st, mouseX, mouseY, partialTicks);
		} else {
			this.recipeBookGui.render(st, mouseX, mouseY, partialTicks);
			super.render(st, mouseX, mouseY, partialTicks);
			this.recipeBookGui.drawGhostSlots(st, this.x, this.y, true, partialTicks);
		}

		this.drawMouseoverTooltip(st, mouseX, mouseY);
		this.recipeBookGui.drawTooltip(st, this.x, this.y, mouseX, mouseY);
		this.setFocused(this.recipeBookGui);

		if (buttonPullFromInv.isHovered()) {
			renderTooltip(st, Text.translatable("tooltip.toms_storage.pull_" + buttonPullFromInv.state), mouseX, mouseY);
		}
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

	@Override
	public boolean keyPressed(int code, int p_231046_2_, int p_231046_3_) {
		if(code == GLFW.GLFW_KEY_S && focusedSlot != null) {
			ItemStack itemstack = null;

			for (int i = 0; i < this.recipeBookGui.ghostSlots.getSlotCount(); ++i) {
				RecipeBookGhostSlots.GhostInputSlot ghostrecipe$ghostingredient = this.recipeBookGui.ghostSlots.getSlot(i);
				int j = ghostrecipe$ghostingredient.getX();
				int k = ghostrecipe$ghostingredient.getY();
				if (j == focusedSlot.x && k == focusedSlot.y) {
					itemstack = ghostrecipe$ghostingredient.getCurrentItemStack();
				}
			}
			if(itemstack != null) {
				super.searchField.setText(itemstack.getName().getString());
				super.searchField.setTextFieldFocused(false);
				return true;
			}
		}
		return super.keyPressed(code, p_231046_2_, p_231046_3_);
	}

	public class GuiButtonClear extends ButtonWidget {

		public GuiButtonClear(int x, int y, PressAction pressable) {
			super(x, y, 11, 11, Text.literal(""), pressable);
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
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, getGui());
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
