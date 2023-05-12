package com.tom.storagemod.gui;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.storagemod.util.IAutoFillTerminal;

public class CraftingTerminalScreen extends AbstractStorageTerminalScreen<CraftingTerminalMenu> implements RecipeUpdateListener {
	private static final ResourceLocation gui = new ResourceLocation("toms_storage", "textures/gui/crafting_terminal.png");
	private final RecipeBookComponent recipeBookGui;
	private boolean widthTooNarrow;
	private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");
	private GuiButton buttonPullFromInv;
	private GuiButtonClear btnClr;
	private boolean pullFromInv;

	public CraftingTerminalScreen(CraftingTerminalMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn, 5, 256, 7, 17);
		recipeBookGui = new RecipeBookComponent();
	}

	@Override
	public ResourceLocation getGui() {
		return gui;
	}

	@Override
	protected void onUpdateSearch(String text) {
		if(IAutoFillTerminal.hasSync() || (searchType & 4) > 0) {
			if(recipeBookGui.searchBox != null)recipeBookGui.searchBox.setValue(text);
			recipeBookGui.recipesUpdated();
		}
	}

	@Override
	protected void init() {
		imageWidth = 194;
		imageHeight = 256;
		super.init();
		this.widthTooNarrow = this.width < 379;
		this.recipeBookGui.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
		this.leftPos = this.recipeBookGui.updateScreenPosition(this.width, this.imageWidth);
		addWidget(recipeBookGui);
		this.setInitialFocus(this.recipeBookGui);
		btnClr = new GuiButtonClear(leftPos + 80, topPos + 20 + rowCount * 18, b -> clearGrid());
		addRenderableWidget(btnClr);
		buttonPullFromInv = addRenderableWidget(makeButton(leftPos - 18, topPos + 5 + 18*6, 4, b -> {
			pullFromInv = !pullFromInv;
			buttonPullFromInv.setState(pullFromInv ? 1 : 0);
			sendUpdate();
		}));
		buttonPullFromInv.tooltipFactory = s -> Tooltip.create(Component.translatable("tooltip.toms_storage.pull_" + s));
		addRenderableWidget(new ImageButton(this.leftPos + 4, this.topPos + 38 + rowCount * 18, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (p_214076_1_) -> {
			this.recipeBookGui.initVisuals();
			this.recipeBookGui.toggleVisibility();
			this.leftPos = this.recipeBookGui.updateScreenPosition(this.width, this.imageWidth);
			((ImageButton)p_214076_1_).setPosition(this.leftPos + 4, this.topPos + 38 + rowCount * 18);
			setButtonsPos();
		}));
		setButtonsPos();
		onPacket();
	}

	private void setButtonsPos() {
		searchField.setX(this.leftPos + 82);
		btnClr.setX(this.leftPos + 80);
		int space = recipeBookGui.isVisible() ? PlatformEditBox.getY(recipeBookGui.searchBox) - 16 : imageHeight;
		List<PlatformButton> buttons = Arrays.asList(buttonSortingType, buttonDirection, buttonSearchType, buttonCtrlMode, buttonGhostMode, buttonPullFromInv, buttonTallMode);
		int y = topPos + 5;
		int x = leftPos - 18;
		for (int i = 0; i < buttons.size(); i++) {
			PlatformButton b = buttons.get(i);
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
		int s = menu.terminalData;
		pullFromInv = (s & (1 << 8)) != 0;
		buttonPullFromInv.setState(pullFromInv ? 1 : 0);
	}

	@Override
	protected int updateData() {
		int d = super.updateData();
		d |= (pullFromInv ? 1 : 0) << 8;
		return d;
	}

	@Override
	public void containerTick() {
		super.containerTick();
		this.recipeBookGui.tick();
	}

	@Override
	public void render(GuiGraphics st, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(st);
		if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
			this.renderBg(st, partialTicks, mouseX, mouseY);
			this.recipeBookGui.render(st, mouseX, mouseY, partialTicks);
		} else {
			this.recipeBookGui.render(st, mouseX, mouseY, partialTicks);
			super.render(st, mouseX, mouseY, partialTicks);
			this.recipeBookGui.renderGhostRecipe(st, this.leftPos, this.topPos, true, partialTicks);
		}

		this.renderTooltip(st, mouseX, mouseY);
		this.recipeBookGui.renderTooltip(st, this.leftPos, this.topPos, mouseX, mouseY);
		this.setInitialFocus(this.recipeBookGui);
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

	@Override
	public RecipeBookComponent getRecipeBookComponent() {
		return this.recipeBookGui;
	}

	private void clearGrid() {
		this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, 0);
	}

	@Override
	public boolean keyPressed(int code, int p_231046_2_, int p_231046_3_) {
		if(code == GLFW.GLFW_KEY_S && hoveredSlot != null) {
			ItemStack itemstack = null;

			for (int i = 0; i < this.recipeBookGui.ghostRecipe.size(); ++i) {
				GhostRecipe.GhostIngredient ghostrecipe$ghostingredient = this.recipeBookGui.ghostRecipe.get(i);
				int j = ghostrecipe$ghostingredient.getX();
				int k = ghostrecipe$ghostingredient.getY();
				if (j == hoveredSlot.x && k == hoveredSlot.y) {
					itemstack = ghostrecipe$ghostingredient.getItem();
				}
			}
			if(itemstack != null) {
				searchField.setValue(itemstack.getHoverName().getString());
				searchField.setFocus(false);
				return true;
			}
		}
		return super.keyPressed(code, p_231046_2_, p_231046_3_);
	}

	public class GuiButtonClear extends PlatformButton {

		public GuiButtonClear(int x, int y, OnPress pressable) {
			super(x, y, 11, 11, null, pressable);
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
				int i = this.getYImage(this.isHovered);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				st.blit(getGui(), x, y, 194 + i * 11, 10, this.width, this.height);
			}
		}
	}
}
