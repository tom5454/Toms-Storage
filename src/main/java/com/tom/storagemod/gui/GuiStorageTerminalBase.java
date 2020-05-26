package com.tom.storagemod.gui;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.StoredItemStack.ComparatorAmount;
import com.tom.storagemod.gui.ContainerStorageTerminal.SlotAction;

public abstract class GuiStorageTerminalBase<T extends ContainerStorageTerminal> extends ContainerScreen<T> {
	protected Minecraft mc = Minecraft.getInstance();

	/** Amount scrolled in Creative mode inventory (0 = top, 1 = bottom) */
	protected float currentScroll;
	/** True if the scrollbar is being dragged */
	protected boolean isScrolling;
	/**
	 * True if the left mouse button was held down last time drawScreen was
	 * called.
	 */
	protected boolean wasClicking;
	protected TextFieldWidget searchField;
	protected int slotIDUnderMouse = -1, sortData, controllMode, rowCount;
	protected String searchLast = "";

	private ComparatorAmount comparator = new ComparatorAmount(false);
	protected static final ResourceLocation creativeInventoryTabs = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

	public GuiStorageTerminalBase(T screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
	}

	@Override
	protected void init() {
		children.clear();
		buttons.clear();
		super.init();
		this.searchField = new TextFieldWidget(font, this.guiLeft + 82, this.guiTop + 6, 89, this.font.FONT_HEIGHT, searchLast);
		this.searchField.setMaxStringLength(100);
		this.searchField.setEnableBackgroundDrawing(false);
		this.searchField.setVisible(true);
		this.searchField.setTextColor(16777215);
		buttons.add(searchField);
		updateSearch();
	}

	protected void updateSearch() {
		String searchString = searchField.getText();
		getContainer().itemListClientSorted.clear();
		boolean searchMod = false;
		if (searchString.startsWith("@")) {
			searchMod = true;
			searchString = searchString.substring(1);
		}
		Pattern m = null;
		try {
			m = Pattern.compile(searchString.toLowerCase(), Pattern.CASE_INSENSITIVE);
		} catch (Throwable ignore) {
			try {
				m = Pattern.compile(Pattern.quote(searchString.toLowerCase()), Pattern.CASE_INSENSITIVE);
			} catch (Throwable __) {
				return;
			}
		}
		boolean notDone = false;
		for (int i = 0;i < getContainer().itemListClient.size();i++) {
			StoredItemStack is = getContainer().itemListClient.get(i);
			if (is != null && is.getStack() != null) {
				String dspName = searchMod ? is.getStack().getItem().delegate.name().getNamespace() : is.getStack().getDisplayName().getUnformattedComponentText();
				notDone = true;
				if (m.matcher(dspName.toLowerCase()).find()) {
					addStackToClientList(is);
					notDone = false;
				}
				if (notDone) {
					for (ITextComponent lp : is.getStack().getTooltip(mc.player, getTooltipFlag())) {
						if (m.matcher(lp.getUnformattedComponentText()).find()) {
							addStackToClientList(is);
							notDone = false;
							break;
						}
					}
				}
			}
		}
		Collections.sort(getContainer().itemListClientSorted, comparator);
		if (!searchLast.equals(searchString)) {
			getContainer().scrollTo(0);
			this.currentScroll = 0;
		} else {
			getContainer().scrollTo(this.currentScroll);
		}
		this.searchLast = searchString;
	}

	private void addStackToClientList(StoredItemStack is) {
		getContainer().itemListClientSorted.add(is);
	}

	public static ITooltipFlag getTooltipFlag(){
		return Minecraft.getInstance().gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL;
	}

	@Override
	public void tick() {
		super.tick();
		updateSearch();
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		boolean flag = GLFW.glfwGetMouseButton(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_RELEASE;
		int i = this.guiLeft;
		int j = this.guiTop;
		int k = i + 174;
		int l = j + 18;
		int i1 = k + 14;
		int j1 = l + rowCount * 18;

		if (!this.wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1) {
			this.isScrolling = this.needsScrollBars();
		}

		if (!flag) {
			this.isScrolling = false;
		}
		this.wasClicking = flag;

		if (this.isScrolling) {
			this.currentScroll = (mouseY - l - 7.5F) / (j1 - l - 15.0F);
			this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
			getContainer().scrollTo(this.currentScroll);
		}
		super.render(mouseX, mouseY, partialTicks);

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderHelper.disableStandardItemLighting();
		minecraft.textureManager.bindTexture(creativeInventoryTabs);
		i = k;
		j = l;
		k = j1;
		this.blit(i, j + (int) ((k - j - 17) * this.currentScroll), 232 + (this.needsScrollBars() ? 0 : 12), 0, 12, 15);
		RenderSystem.pushMatrix();
		RenderHelper.enableStandardItemLighting();
		slotIDUnderMouse = getContainer().drawSlots(this, mouseX, mouseY);
		RenderSystem.popMatrix();
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	protected boolean needsScrollBars() {
		return this.getContainer().itemListClientSorted.size() > rowCount * 9;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (slotIDUnderMouse > -1) {
			if (isPullOne(mouseButton)) {
				if (getContainer().getSlotByID(slotIDUnderMouse).stack != null && getContainer().getSlotByID(slotIDUnderMouse).stack.getQuantity() > 0) {
					for (int i = 0;i < getContainer().itemList.size();i++) {
						if (getContainer().getSlotByID(slotIDUnderMouse).stack.equals(getContainer().itemList.get(i))) {
							windowClick(isTransferOne(mouseButton) ? -3 : -2, i, SlotAction.PULL_ONE);
							return true;
						}
					}
				}
				return true;
			} else if (pullHalf(mouseButton)) {
				if (!mc.player.inventory.getItemStack().isEmpty()) {
					windowClick(-2, 0, SlotAction.GET_HALF);
				} else {
					if (getContainer().getSlotByID(slotIDUnderMouse).stack != null && getContainer().getSlotByID(slotIDUnderMouse).stack.getQuantity() > 0) {
						for (int i = 0;i < getContainer().itemList.size();i++) {
							if (getContainer().getSlotByID(slotIDUnderMouse).stack.equals(getContainer().itemList.get(i))) {
								windowClick(-2, i, hasShiftDown() ? SlotAction.GET_QUARTER : SlotAction.GET_HALF);
								return true;
							}
						}
					}
				}
			} else if (pullNormal(mouseButton)) {
				if (!mc.player.inventory.getItemStack().isEmpty()) {
					windowClick(-(slotIDUnderMouse + 2), 0, SlotAction.PULL_OR_PUSH_STACK);
				} else {
					if (getContainer().getSlotByID(slotIDUnderMouse).stack != null) {
						if (getContainer().getSlotByID(slotIDUnderMouse).stack.getQuantity() > 0) {
							for (int i = 0;i < getContainer().itemList.size();i++) {
								if (getContainer().getSlotByID(slotIDUnderMouse).stack.equals(getContainer().itemList.get(i))) {
									windowClick(-2, i, hasShiftDown() ? SlotAction.SHIFT_PULL : SlotAction.PULL_OR_PUSH_STACK);
									return true;
								}
							}
						}
					}
				}
			}
		} else if (GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_SPACE) != GLFW.GLFW_RELEASE) {
			windowClick(-1, 0, SlotAction.SPACE_CLICK);
		} else {
			if (mouseButton == 1 && isPointInRegion(searchField.x - guiLeft, searchField.y - guiTop, searchField.getWidth(), searchField.getHeight(), mouseX, mouseY))
				searchField.setText("");
			else if(this.searchField.mouseClicked(mouseX, mouseY, mouseButton))return true;
			else
				return super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		return true;
	}

	protected void windowClick(int i, int j, SlotAction pullOne) {
		mc.playerController.windowClick(this.getContainer().windowId, i, j, ClickType.values()[pullOne.ordinal()], this.mc.player);
	}

	public boolean isPullOne(int mouseButton) {
		switch (ctrlm()) {
		case AE:
			return mouseButton == 1 && hasShiftDown();
		case RS:
			return mouseButton == 2;
		default:
			return false;
		}
	}

	public boolean isTransferOne(int mouseButton) {
		switch (ctrlm()) {
		case AE:
			return hasShiftDown() && hasControlDown();//not in AE
		case RS:
			return hasShiftDown() && mouseButton == 2;
		default:
			return false;
		}
	}

	public boolean pullHalf(int mouseButton) {
		switch (ctrlm()) {
		case AE:
			return mouseButton == 1;
		case RS:
			return mouseButton == 1;
		default:
			return false;
		}
	}

	public boolean pullNormal(int mouseButton) {
		switch (ctrlm()) {
		case AE:
			return mouseButton == 0;
		case RS:
			return mouseButton == 0;
		default:
			return false;
		}
	}

	private ControllMode ctrlm() {
		return ControllMode.VALUES[controllMode];
	}

	public final void renderItemInGui(ItemStack stack, int x, int y, int mouseX, int mouseY, boolean hasBg, int color, boolean tooltip, String... extraInfo) {
		if (stack != null) {
			if (!tooltip) {
				if (hasBg) {
					fill(x, y, 16, 16, color | 0x80000000);
				}
				RenderSystem.translated(0.0F, 0.0F, 32.0F);
				this.setBlitOffset(100);
				this.itemRenderer.zLevel = 100.0F;
				FontRenderer font = null;
				if (stack != null)
					font = stack.getItem().getFontRenderer(stack);
				if (font == null)
					font = this.font;
				RenderSystem.enableDepthTest();
				this.itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
				this.itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, null);
				this.setBlitOffset(0);
				this.itemRenderer.zLevel = 0.0F;
			} else if (mouseX >= x - 1 && mouseY >= y - 1 && mouseX < x + 17 && mouseY < y + 17) {
				List<String> list = getTooltipFromItem(stack);
				// list.add(I18n.format("tomsmod.gui.amount", stack.stackSize));
				if (extraInfo != null && extraInfo.length > 0) {
					for (int i = 0; i < extraInfo.length; i++) {
						list.add(extraInfo[i]);
					}
				}
				for (int i = 0;i < list.size();++i) {
					if (i == 0) {
						list.set(i, stack.getRarity().color + list.get(i));
					} else {
						list.set(i, TextFormatting.GRAY + list.get(i));
					}
				}
				this.renderTooltip(list, mouseX, mouseY);
			}
		}
	}

	public FontRenderer getFont() {
		return font;
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (p_keyPressed_1_ == 256) {
			this.minecraft.player.closeScreen();
			return true;
		}

		return !this.searchField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) && !this.searchField.canWrite() ? super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) : true;
	}

	@Override
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
		if(searchField.charTyped(p_charTyped_1_, p_charTyped_2_))return true;
		return super.charTyped(p_charTyped_1_, p_charTyped_2_);
	}

	@Override
	public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
		if (!this.needsScrollBars()) {
			return false;
		} else {
			int i = ((this.container).itemList.size() + 9 - 1) / 9 - 5;
			this.currentScroll = (float)(this.currentScroll - p_mouseScrolled_5_ / i);
			this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
			this.container.scrollTo(this.currentScroll);
			return true;
		}
	}
}
