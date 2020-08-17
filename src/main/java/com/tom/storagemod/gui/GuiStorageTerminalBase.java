package com.tom.storagemod.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import net.minecraftforge.fml.ModList;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.StoredItemStack.ComparatorAmount;
import com.tom.storagemod.StoredItemStack.IStoredItemStackComparator;
import com.tom.storagemod.StoredItemStack.SortingTypes;
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
	protected int slotIDUnderMouse = -1, controllMode, rowCount, searchType;
	protected String searchLast = "";
	protected boolean loadedSearch = false;
	private IStoredItemStackComparator comparator = new ComparatorAmount(false);
	protected static final ResourceLocation creativeInventoryTabs = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	protected GuiButton buttonSortingType, buttonDirection, buttonSearchType, buttonCtrlMode;

	public GuiStorageTerminalBase(T screenContainer, PlayerInventory inv, ITextComponent titleIn) {
		super(screenContainer, inv, titleIn);
		screenContainer.onPacket = this::onPacket;
	}

	private void onPacket() {
		int s = container.terminalData;
		controllMode = (s & 0b000_00_0_11) % ControllMode.VALUES.length;
		boolean rev = (s & 0b000_00_1_00) > 0;
		int type = (s & 0b000_11_0_00) >> 3;
		comparator = SortingTypes.VALUES[type % SortingTypes.VALUES.length].create(rev);
		searchType = (s & 0b111_00_0_00) >> 5;
		searchField.setCanLoseFocus((searchType & 1) == 0);
		if(!searchField.isFocused() && (searchType & 1) > 0) {
			searchField.setFocused2(true);
		}
		buttonSortingType.state = type;
		buttonDirection.state = rev ? 1 : 0;
		buttonSearchType.state = searchType;
		buttonCtrlMode.state = controllMode;

		if(!loadedSearch) {
			loadedSearch = true;
			if((searchType & 2) > 0)
				searchField.setText(container.search);
		}
	}

	private void sendUpdate() {
		int d = 0;
		d |= (controllMode & 0b00_0_11);
		d |= ((comparator.isReversed() ? 1 : 0) << 2);
		d |= (comparator.type() << 3);
		d |= ((searchType & 0b111) << 5);
		d = (d << 1) | 1;
		this.minecraft.playerController.sendEnchantPacket((this.container).windowId, d);
	}

	@Override
	protected void init() {
		children.clear();
		buttons.clear();
		field_238745_s_ = ySize - 92;
		super.init();
		this.searchField = new TextFieldWidget(getFont(), this.guiLeft + 82, this.guiTop + 6, 89, this.getFont().FONT_HEIGHT, null);
		this.searchField.setMaxStringLength(100);
		this.searchField.setEnableBackgroundDrawing(false);
		this.searchField.setVisible(true);
		this.searchField.setTextColor(16777215);
		buttons.add(searchField);
		buttonSortingType = addButton(new GuiButton(guiLeft - 18, guiTop + 5, 0, b -> {
			comparator = SortingTypes.VALUES[(comparator.type() + 1) % SortingTypes.VALUES.length].create(comparator.isReversed());
			sendUpdate();
		}));
		buttonDirection = addButton(new GuiButton(guiLeft - 18, guiTop + 5 + 18, 1, b -> {
			comparator.setReversed(!comparator.isReversed());
			sendUpdate();
		}));
		buttonSearchType = addButton(new GuiButton(guiLeft - 18, guiTop + 5 + 18*2, 2, b -> {
			searchType = (searchType + 1) & ((ModList.get().isLoaded("jei") || this instanceof GuiCraftingTerminal) ? 0b111 : 0b011);
			sendUpdate();
		}) {
			@Override
			public void renderButton(MatrixStack st, int mouseX, int mouseY, float pt) {
				if (this.visible) {
					mc.getTextureManager().bindTexture(getGui());
					RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
					this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
					//int i = this.getYImage(this.isHovered);
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
					this.blit(st, this.x, this.y, texX, texY + tile * 16, this.width, this.height);
					if((state & 1) > 0)this.blit(st, this.x+1, this.y+1, texX + 16, texY + tile * 16, this.width-2, this.height-2);
					if((state & 2) > 0)this.blit(st, this.x+1, this.y+1, texX + 16+14, texY + tile * 16, this.width-2, this.height-2);
					if((state & 4) > 0)this.blit(st, this.x+1, this.y+1, texX + 16+14*2, texY + tile * 16, this.width-2, this.height-2);
				}
			}
		});
		buttonCtrlMode = addButton(new GuiButton(guiLeft - 18, guiTop + 5 + 18*3, 3, b -> {
			controllMode = (controllMode + 1) % ControllMode.VALUES.length;
			sendUpdate();
		}));
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
				String dspName = searchMod ? is.getStack().getItem().delegate.name().getNamespace() : is.getStack().getDisplayName().getString();
				notDone = true;
				if (m.matcher(dspName.toLowerCase()).find()) {
					addStackToClientList(is);
					notDone = false;
				}
				if (notDone) {
					for (ITextComponent lp : is.getStack().getTooltip(mc.player, getTooltipFlag())) {
						if (m.matcher(lp.getString()).find()) {
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
			if ((searchType & 4) > 0) {
				//if(ModList.get().isLoaded("jei"))
				//JEIHandler.setJeiSearchText(searchString);
			}
			if ((searchType & 2) > 0) {
				CompoundNBT nbt = new CompoundNBT();
				nbt.putString("s", searchString);
				container.sendMessage(nbt);
			}
			onUpdateSearch(searchString);
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
	public void render(MatrixStack st, int mouseX, int mouseY, float partialTicks) {
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
		super.render(st, mouseX, mouseY, partialTicks);

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderHelper.disableStandardItemLighting();
		minecraft.textureManager.bindTexture(creativeInventoryTabs);
		i = k;
		j = l;
		k = j1;
		this.blit(st, i, j + (int) ((k - j - 17) * this.currentScroll), 232 + (this.needsScrollBars() ? 0 : 12), 0, 12, 15);
		st.push();
		RenderHelper.enableStandardItemLighting();
		slotIDUnderMouse = getContainer().drawSlots(st, this, mouseX, mouseY);
		st.pop();
		this.func_230459_a_(st, mouseX, mouseY);

		if (buttonSortingType.isHovered()) {
			renderTooltip(st, new TranslationTextComponent("tooltip.toms_storage.sorting_" + buttonSortingType.state), mouseX, mouseY);
		}
		if (buttonSearchType.isHovered()) {
			renderTooltip(st, new TranslationTextComponent("tooltip.toms_storage.search_" + buttonSearchType.state), mouseX, mouseY);
		}
		if (buttonCtrlMode.isHovered()) {
			func_243308_b(st, Arrays.stream(I18n.format("tooltip.toms_storage.ctrlMode_" + buttonCtrlMode.state).split("\\\\")).map(StringTextComponent::new).collect(Collectors.toList()), mouseX, mouseY);
		}
	}

	/*private void renderTooltip(MatrixStack p0, List<String> p1, int p2, int p3) {
		func_238654_b_(p0, p1.stream().map(StringTextComponent::new).collect(Collectors.toList()), p2, p3);
	}*/

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
							//windowClick(isTransferOne(mouseButton) ? -3 : -2, i, SlotAction.PULL_ONE);
							storageSlotClick(getContainer().getSlotByID(slotIDUnderMouse).stack.getStack(), SlotAction.PULL_ONE, isTransferOne(mouseButton) ? 1 : 0);
							return true;
						}
					}
				}
				return true;
			} else if (pullHalf(mouseButton)) {
				if (!mc.player.inventory.getItemStack().isEmpty()) {
					//windowClick(-2, 0, SlotAction.GET_HALF);
					storageSlotClick(ItemStack.EMPTY, hasControlDown() ? SlotAction.GET_QUARTER : SlotAction.GET_HALF, 0);
				} else {
					if (getContainer().getSlotByID(slotIDUnderMouse).stack != null && getContainer().getSlotByID(slotIDUnderMouse).stack.getQuantity() > 0) {
						for (int i = 0;i < getContainer().itemList.size();i++) {
							if (getContainer().getSlotByID(slotIDUnderMouse).stack.equals(getContainer().itemList.get(i))) {
								//windowClick(-2, i, hasShiftDown() ? SlotAction.GET_QUARTER : SlotAction.GET_HALF);
								storageSlotClick(getContainer().getSlotByID(slotIDUnderMouse).stack.getStack(), hasControlDown() ? SlotAction.GET_QUARTER : SlotAction.GET_HALF, 0);
								return true;
							}
						}
					}
				}
			} else if (pullNormal(mouseButton)) {
				if (!mc.player.inventory.getItemStack().isEmpty()) {
					//windowClick(-(slotIDUnderMouse + 2), 0, SlotAction.PULL_OR_PUSH_STACK);
					storageSlotClick(ItemStack.EMPTY, SlotAction.PULL_OR_PUSH_STACK, 0);
				} else {
					if (getContainer().getSlotByID(slotIDUnderMouse).stack != null) {
						if (getContainer().getSlotByID(slotIDUnderMouse).stack.getQuantity() > 0) {
							for (int i = 0;i < getContainer().itemList.size();i++) {
								if (getContainer().getSlotByID(slotIDUnderMouse).stack.equals(getContainer().itemList.get(i))) {
									//windowClick(-2, i, hasShiftDown() ? SlotAction.SHIFT_PULL : SlotAction.PULL_OR_PUSH_STACK);
									storageSlotClick(getContainer().getSlotByID(slotIDUnderMouse).stack.getStack(), hasShiftDown() ? SlotAction.SHIFT_PULL : SlotAction.PULL_OR_PUSH_STACK, 0);
									return true;
								}
							}
						}
					}
				}
			}
		} else if (GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_SPACE) != GLFW.GLFW_RELEASE) {
			//windowClick(-1, 0, SlotAction.SPACE_CLICK);
			storageSlotClick(ItemStack.EMPTY, SlotAction.SPACE_CLICK, 0);
		} else {
			if (mouseButton == 1 && isPointInRegion(searchField.x - guiLeft, searchField.y - guiTop, searchField.getWidth(), searchField.getHeight(), mouseX, mouseY))
				searchField.setText("");
			else if(this.searchField.mouseClicked(mouseX, mouseY, mouseButton))return true;
			else
				return super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		return true;
	}

	/*protected void windowClick(int i, int j, SlotAction pullOne) {
		mc.playerController.windowClick(this.getContainer().windowId, i, j, ClickType.values()[pullOne.ordinal()], this.mc.player);
	}*/

	protected void storageSlotClick(ItemStack slotStack, SlotAction act, int mod) {
		CompoundNBT c = new CompoundNBT();
		c.put("s", slotStack.write(new CompoundNBT()));
		c.putInt("a", act.ordinal());
		c.putByte("m", (byte) mod);
		CompoundNBT msg = new CompoundNBT();
		msg.put("a", c);
		container.sendMessage(msg);
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

	public final void renderItemInGui(MatrixStack st, ItemStack stack, int x, int y, int mouseX, int mouseY, boolean hasBg, int color, boolean tooltip, String... extraInfo) {
		if (stack != null) {
			if (!tooltip) {
				if (hasBg) {
					fill(st, x, y, 16, 16, color | 0x80000000);
				}
				st.translate(0.0F, 0.0F, 32.0F);
				//this.setBlitOffset(100);
				//this.itemRenderer.zLevel = 100.0F;
				FontRenderer font = null;
				if (stack != null)
					font = stack.getItem().getFontRenderer(stack);
				if (font == null)
					font = this.getFont();
				RenderSystem.enableDepthTest();
				this.itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
				this.itemRenderer.renderItemOverlayIntoGUI(font, stack, x, y, null);
				//this.setBlitOffset(0);
				//this.itemRenderer.zLevel = 0.0F;
			} else if (mouseX >= x - 1 && mouseY >= y - 1 && mouseX < x + 17 && mouseY < y + 17) {
				List<ITextComponent> list = getTooltipFromItem(stack);
				// list.add(I18n.format("tomsmod.gui.amount", stack.stackSize));
				if (extraInfo != null && extraInfo.length > 0) {
					for (int i = 0; i < extraInfo.length; i++) {
						list.add(new StringTextComponent(extraInfo[i]));
					}
				}
				for (int i = 0;i < list.size();++i) {
					ITextComponent t = list.get(i);
					IFormattableTextComponent t2 = t instanceof IFormattableTextComponent ? (IFormattableTextComponent) t : t.deepCopy();
					if (i == 0) {
						list.set(i, t2.func_240699_a_(stack.getRarity().color));
					} else {
						list.set(i, t2.func_240699_a_(TextFormatting.GRAY));
					}
				}
				this.func_243308_b(st, list, mouseX, mouseY);
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

	public abstract ResourceLocation getGui();

	@Override
	protected void func_230450_a_(MatrixStack st, float partialTicks, int mouseX, int mouseY) {
		mc.textureManager.bindTexture(getGui());
		this.blit(st, this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}

	public class GuiButton extends Button {
		protected int tile;
		protected int state;
		protected int texX = 194;
		protected int texY = 30;
		public GuiButton(int x, int y, int tile, IPressable pressable) {
			super(x, y, 16, 16, null, pressable);
			this.tile = tile;
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
				mc.getTextureManager().bindTexture(getGui());
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				//int i = this.getYImage(this.isHovered);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				this.blit(st, this.x, this.y, texX + state * 16, texY + tile * 16, this.width, this.height);
			}
		}
	}

	protected void onUpdateSearch(String text) {}

	/*@Override
	protected void func_230451_b_(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_) {
		this.font.func_238422_b_(p_230451_1_, this.title, this.field_238742_p_, this.field_238743_q_, 4210752);
		this.font.func_238422_b_(p_230451_1_, this.playerInventory.getDisplayName(), this.field_238744_r_, this.field_238745_s_, 4210752);
	}*/
}
