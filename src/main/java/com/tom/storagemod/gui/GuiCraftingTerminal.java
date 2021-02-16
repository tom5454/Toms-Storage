package com.tom.storagemod.gui;

import java.lang.reflect.Field;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraftforge.fml.ModList;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

public class GuiCraftingTerminal extends GuiStorageTerminalBase<ContainerCraftingTerminal> implements IRecipeShownListener {
	private static final ResourceLocation gui = new ResourceLocation("toms_storage", "textures/gui/crafting_terminal.png");
	private static Field stackedContentsField, searchBarField, ghostRecipeField;
	static {
		try {
			for (Field f : RecipeBookGui.class.getDeclaredFields()) {
				if(f.getType() == RecipeItemHelper.class) {
					f.setAccessible(true);
					stackedContentsField = f;
				} else if(f.getType() == TextFieldWidget.class) {
					f.setAccessible(true);
					searchBarField = f;
				} else if(f.getType() == GhostRecipe.class) {
					f.setAccessible(true);
					ghostRecipeField = f;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	private final RecipeBookGui recipeBookGui;
	private boolean widthTooNarrow;
	private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation("textures/gui/recipe_button.png");
	private TextFieldWidget searchField;
	private GhostRecipe ghostRecipe;
	private GuiButton buttonPullFromInv;
	private boolean pullFromInv;

	public GuiCraftingTerminal(ContainerCraftingTerminal screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);

		recipeBookGui = new RecipeBookGui();
		try {
			stackedContentsField.set(recipeBookGui, getContainer().new TerminalRecipeItemHelper());
			ghostRecipe = (GhostRecipe) ghostRecipeField.get(recipeBookGui);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResourceLocation getGui() {
		return gui;
	}

	@Override
	protected void onUpdateSearch(String text) {
		if(ModList.get().isLoaded("jei") || (searchType & 4) > 0) {
			if(searchField != null)searchField.setText(text);
			recipeBookGui.recipesUpdated();
		}
	}

	@Override
	protected void init() {
		xSize = 194;
		ySize = 256;
		rowCount = 5;
		super.init();
		this.widthTooNarrow = this.width < 379;
		this.recipeBookGui.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.container);
		this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
		this.children.add(this.recipeBookGui);
		this.setFocusedDefault(this.recipeBookGui);
		GuiButtonClear btnClr = new GuiButtonClear(guiLeft + 80, guiTop + 110, b -> clearGrid());
		addButton(btnClr);
		buttonPullFromInv = addButton(new GuiButton(guiLeft - 18, guiTop + 5 + 18*4, 4, b -> {
			pullFromInv = !pullFromInv;
			buttonPullFromInv.state = pullFromInv ? 1 : 0;
			sendUpdate();
		}));
		this.addButton(new ImageButton(this.guiLeft + 4, this.height / 2, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, (p_214076_1_) -> {
			this.recipeBookGui.initSearchBar(this.widthTooNarrow);
			try {
				searchField = (TextFieldWidget) searchBarField.get(recipeBookGui);
			} catch (Exception e) {
				searchField = null;
			}

			this.recipeBookGui.toggleVisibility();
			this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
			((ImageButton)p_214076_1_).setPosition(this.guiLeft + 4, this.height / 2);
			super.searchField.setX(this.guiLeft + 82);
			btnClr.setX(this.guiLeft + 80);
			buttonSortingType.setX(guiLeft - 18);
			buttonDirection.setX(guiLeft - 18);
			if(recipeBookGui.isVisible()) {
				buttonSearchType.setX(guiLeft - 36);
				buttonCtrlMode.setX(guiLeft - 36);
				buttonPullFromInv.setX(guiLeft - 54);
				buttonSearchType.y = guiTop + 5;
				buttonCtrlMode.y = guiTop + 5 + 18;
				buttonPullFromInv.y = guiTop + 5 + 18;
			} else {
				buttonSearchType.setX(guiLeft - 18);
				buttonCtrlMode.setX(guiLeft - 18);
				buttonPullFromInv.setX(guiLeft - 18);
				buttonSearchType.y = guiTop + 5 + 18*2;
				buttonCtrlMode.y = guiTop + 5 + 18*3;
				buttonPullFromInv.y = guiTop + 5 + 18*4;
			}
		}));
		if(recipeBookGui.isVisible()) {
			buttonSortingType.setX(guiLeft - 18);
			buttonDirection.setX(guiLeft - 18);
			buttonSearchType.setX(guiLeft - 36);
			buttonCtrlMode.setX(guiLeft - 36);
			buttonPullFromInv.setX(guiLeft - 54);
			buttonSearchType.y = guiTop + 5;
			buttonCtrlMode.y = guiTop + 5 + 18;
			buttonPullFromInv.y = guiTop + 5 + 18;
			super.searchField.setX(this.guiLeft + 82);
			try {
				searchField = (TextFieldWidget) searchBarField.get(recipeBookGui);
			} catch (Exception e) {
				searchField = null;
			}
		}
		onPacket();
	}

	@Override
	protected void onPacket() {
		super.onPacket();
		int s = container.terminalData;
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
	public void tick() {
		super.tick();
		this.recipeBookGui.tick();
	}

	@Override
	public void render(MatrixStack st, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(st);
		if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
			this.drawGuiContainerBackgroundLayer(st, partialTicks, mouseX, mouseY);
			RenderSystem.disableLighting();
			this.recipeBookGui.render(st, mouseX, mouseY, partialTicks);
		} else {
			RenderSystem.disableLighting();
			this.recipeBookGui.render(st, mouseX, mouseY, partialTicks);
			super.render(st, mouseX, mouseY, partialTicks);
			this.recipeBookGui.func_230477_a_(st, this.guiLeft, this.guiTop, true, partialTicks);
		}

		this.renderHoveredTooltip(st, mouseX, mouseY);
		this.recipeBookGui.func_238924_c_(st, this.guiLeft, this.guiTop, mouseX, mouseY);
		this.setFocusedDefault(this.recipeBookGui);

		if (buttonPullFromInv.isHovered()) {
			renderTooltip(st, new TranslationTextComponent("tooltip.toms_storage.pull_" + buttonPullFromInv.state), mouseX, mouseY);
		}
	}

	@Override
	protected boolean isPointInRegion(int x, int y, int width, int height, double mouseX, double mouseY) {
		return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super.isPointInRegion(x, y, width, height, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_) {
		if (this.recipeBookGui.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_)) {
			return true;
		} else {
			return this.widthTooNarrow && this.recipeBookGui.isVisible() ? true : super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
		}
	}

	@Override
	protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeftIn, int guiTopIn, int mouseButton) {
		boolean flag = mouseX < guiLeftIn || mouseY < guiTopIn || mouseX >= guiLeftIn + this.xSize || mouseY >= guiTopIn + this.ySize;
		return this.recipeBookGui.func_195604_a(mouseX, mouseY, this.guiLeft, this.guiTop, this.xSize, this.ySize, mouseButton) && flag;
	}

	/**
	 * Called when the mouse is clicked over a slot or outside the gui.
	 */
	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		this.recipeBookGui.slotClicked(slotIn);
	}

	@Override
	public void recipesUpdated() {
		this.recipeBookGui.recipesUpdated();
	}

	@Override
	public void closeScreen() {
		this.recipeBookGui.removed();
		super.closeScreen();
	}

	@Override
	public RecipeBookGui getRecipeGui() {
		return this.recipeBookGui;
	}

	private void clearGrid() {
		this.minecraft.playerController.sendEnchantPacket((this.container).windowId, 0);
	}

	@Override
	public boolean keyPressed(int code, int p_231046_2_, int p_231046_3_) {
		if(code == GLFW.GLFW_KEY_S && hoveredSlot != null) {
			ItemStack itemstack = null;

			for (int i = 0; i < this.ghostRecipe.size(); ++i) {
				GhostRecipe.GhostIngredient ghostrecipe$ghostingredient = this.ghostRecipe.get(i);
				int j = ghostrecipe$ghostingredient.getX();
				int k = ghostrecipe$ghostingredient.getY();
				if (j == hoveredSlot.xPos && k == hoveredSlot.yPos) {
					itemstack = ghostrecipe$ghostingredient.getItem();
				}
			}
			if(itemstack != null) {
				super.searchField.setText(itemstack.getDisplayName().getString());
				super.searchField.setFocused2(false);
				return true;
			}
		}
		return super.keyPressed(code, p_231046_2_, p_231046_3_);
	}

	public class GuiButtonClear extends Button {

		public GuiButtonClear(int x, int y, IPressable pressable) {
			super(x, y, 11, 11, null, pressable);
		}

		public void setX(int i) {
			x = i;
		}

		/**
		 * Draws this button to the screen.
		 */
		@Override
		public void renderButton(MatrixStack st, int mouseX, int mouseY, float pt) {
			if (this.visible) {
				mc.getTextureManager().bindTexture(gui);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				int i = this.getYImage(this.isHovered);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				this.blit(st, this.x, this.y, 194 + i * 11, 10, this.width, this.height);
			}
		}
	}
}
