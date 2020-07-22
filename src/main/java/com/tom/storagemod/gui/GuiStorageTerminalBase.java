package com.tom.storagemod.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.fabriclibs.ext.IRegistered;
import com.tom.storagemod.StoredItemStack;
import com.tom.storagemod.StoredItemStack.ComparatorAmount;
import com.tom.storagemod.StoredItemStack.IStoredItemStackComparator;
import com.tom.storagemod.StoredItemStack.SortingTypes;
import com.tom.storagemod.gui.ContainerStorageTerminal.SlotAction;

public abstract class GuiStorageTerminalBase<T extends ContainerStorageTerminal> extends HandledScreen<T> {
	protected MinecraftClient mc = MinecraftClient.getInstance();

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
	protected static final Identifier creativeInventoryTabs = new Identifier("textures/gui/container/creative_inventory/tabs.png");
	protected GuiButton buttonSortingType, buttonDirection, buttonSearchType, buttonCtrlMode;

	public GuiStorageTerminalBase(T screenContainer, PlayerInventory inv, Text titleIn) {
		super(screenContainer, inv, titleIn);
		screenContainer.onPacket = this::onPacket;
	}

	private void onPacket() {
		int s = handler.terminalData;
		controllMode = (s & 0b000_00_0_11) % ControllMode.VALUES.length;
		boolean rev = (s & 0b000_00_1_00) > 0;
		int type = (s & 0b000_11_0_00) >> 3;
		comparator = SortingTypes.VALUES[type % SortingTypes.VALUES.length].create(rev);
		searchType = (s & 0b111_00_0_00) >> 5;
		searchField.setFocusUnlocked((searchType & 1) == 0);
		if(!searchField.isFocused() && (searchType & 1) > 0) {
			searchField.setFocused(true);
		}
		buttonSortingType.state = type;
		buttonDirection.state = rev ? 1 : 0;
		buttonSearchType.state = searchType;
		buttonCtrlMode.state = controllMode;

		if(!loadedSearch) {
			loadedSearch = true;
			if((searchType & 2) > 0)
				searchField.setText(handler.search);
		}
	}

	private void sendUpdate() {
		int d = 0;
		d |= (controllMode & 0b00_0_11);
		d |= ((comparator.isReversed() ? 1 : 0) << 2);
		d |= (comparator.type() << 3);
		d |= ((searchType & 0b111) << 5);
		d = (d << 1) | 1;
		this.mc.interactionManager.clickButton((this.handler).syncId, d);
	}

	@Override
	protected void init() {
		children.clear();
		buttons.clear();
		playerInventoryTitleY = backgroundHeight - 92;
		super.init();
		this.searchField = new TextFieldWidget(textRenderer, this.x + 82, this.y + 6, 89, this.textRenderer.fontHeight, new LiteralText(""));
		this.searchField.setText(searchLast);
		this.searchField.setMaxLength(100);
		this.searchField.setHasBorder(false);
		this.searchField.setVisible(true);
		this.searchField.setEditableColor(16777215);
		buttons.add(searchField);
		buttonSortingType = addButton(new GuiButton(x - 18, y + 5, 0, b -> {
			comparator = SortingTypes.VALUES[(comparator.type() + 1) % SortingTypes.VALUES.length].create(comparator.isReversed());
			sendUpdate();
		}));
		buttonDirection = addButton(new GuiButton(x - 18, y + 5 + 18, 1, b -> {
			comparator.setReversed(!comparator.isReversed());
			sendUpdate();
		}));
		buttonSearchType = addButton(new GuiButton(x - 18, y + 5 + 18*2, 2, b -> {
			searchType = (searchType + 1) & ((this instanceof GuiCraftingTerminal) ? 0b111 : 0b011);//ModList.get().isLoaded("jei") ||
			sendUpdate();
		}) {
			@Override
			public void renderButton(MatrixStack st, int mouseX, int mouseY, float pt) {
				if (this.visible) {
					mc.getTextureManager().bindTexture(getGui());
					RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
					this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
					//int i = this.getYImage(this.isHovered);
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
					drawTexture(st, this.x, this.y, texX, texY + tile * 16, this.width, this.height);
					if((state & 1) > 0)drawTexture(st, this.x+1, this.y+1, texX + 16, texY + tile * 16, this.width-2, this.height-2);
					if((state & 2) > 0)drawTexture(st, this.x+1, this.y+1, texX + 16+14, texY + tile * 16, this.width-2, this.height-2);
					if((state & 4) > 0)drawTexture(st, this.x+1, this.y+1, texX + 16+14*2, texY + tile * 16, this.width-2, this.height-2);
				}
			}
		});
		buttonCtrlMode = addButton(new GuiButton(x - 18, y + 5 + 18*3, 3, b -> {
			controllMode = (controllMode + 1) % ControllMode.VALUES.length;
			sendUpdate();
		}));
		updateSearch();
	}

	protected void updateSearch() {
		String searchString = searchField.getText();
		getScreenHandler().itemListClientSorted.clear();
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
		for (int i = 0;i < getScreenHandler().itemListClient.size();i++) {
			StoredItemStack is = getScreenHandler().itemListClient.get(i);
			if (is != null && is.getStack() != null) {
				String dspName = searchMod ? ((IRegistered) is.getStack().getItem()).getRegistryName().getNamespace() : is.getStack().getName().asString();
				notDone = true;
				if (m.matcher(dspName.toLowerCase()).find()) {
					addStackToClientList(is);
					notDone = false;
				}
				if (notDone) {
					for (Text lp : is.getStack().getTooltip(mc.player, getTooltipFlag())) {
						if (m.matcher(lp.asString()).find()) {
							addStackToClientList(is);
							notDone = false;
							break;
						}
					}
				}
			}
		}
		Collections.sort(getScreenHandler().itemListClientSorted, comparator);
		if (!searchLast.equals(searchString)) {
			getScreenHandler().scrollTo(0);
			this.currentScroll = 0;
			if ((searchType & 4) > 0) {
				/*if(ModList.get().isLoaded("jei"))
					JEIHandler.setJeiSearchText(searchString);*/
			}
			if ((searchType & 2) > 0) {
				CompoundTag nbt = new CompoundTag();
				nbt.putString("s", searchString);
				handler.sendMessage(nbt);
			}
			onUpdateSearch(searchString);
		} else {
			getScreenHandler().scrollTo(this.currentScroll);
		}
		this.searchLast = searchString;
	}

	private void addStackToClientList(StoredItemStack is) {
		getScreenHandler().itemListClientSorted.add(is);
	}

	public static TooltipContext getTooltipFlag(){
		return MinecraftClient.getInstance().options.advancedItemTooltips ? TooltipContext.Default.ADVANCED : TooltipContext.Default.NORMAL;
	}

	@Override
	public void tick() {
		super.tick();
		updateSearch();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
		boolean flag = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_RELEASE;
		int i = this.x;
		int j = this.y;
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
			getScreenHandler().scrollTo(this.currentScroll);
		}
		super.render(matrices, mouseX, mouseY, partialTicks);

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		DiffuseLighting.disableGuiDepthLighting();
		mc.getTextureManager().bindTexture(creativeInventoryTabs);
		i = k;
		j = l;
		k = j1;
		drawTexture(matrices, i, j + (int) ((k - j - 17) * this.currentScroll), 232 + (this.needsScrollBars() ? 0 : 12), 0, 12, 15);
		matrices.push();
		DiffuseLighting.enableGuiDepthLighting();
		slotIDUnderMouse = getScreenHandler().drawSlots(matrices, this, mouseX, mouseY);
		matrices.pop();
		this.drawMouseoverTooltip(matrices, mouseX, mouseY);

		if (buttonSortingType.isHovered()) {
			renderTooltip(matrices, new TranslatableText("tooltip.toms_storage.sorting_" + buttonSortingType.state), mouseX, mouseY);
		}
		if (buttonSearchType.isHovered()) {
			renderTooltip(matrices, new TranslatableText("tooltip.toms_storage.search_" + buttonSearchType.state), mouseX, mouseY);
		}
		if (buttonCtrlMode.isHovered()) {
			renderTooltip(matrices, Arrays.stream(I18n.translate("tooltip.toms_storage.ctrlMode_" + buttonCtrlMode.state).split("\\\\")).map(LiteralText::new).collect(Collectors.toList()), mouseX, mouseY);
		}
	}

	protected boolean needsScrollBars() {
		return this.getScreenHandler().itemListClientSorted.size() > rowCount * 9;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (slotIDUnderMouse > -1) {
			if (isPullOne(mouseButton)) {
				if (getScreenHandler().getSlotByID(slotIDUnderMouse).stack != null && getScreenHandler().getSlotByID(slotIDUnderMouse).stack.getQuantity() > 0) {
					for (int i = 0;i < getScreenHandler().itemList.size();i++) {
						if (getScreenHandler().getSlotByID(slotIDUnderMouse).stack.equals(getScreenHandler().itemList.get(i))) {
							//windowClick(isTransferOne(mouseButton) ? -3 : -2, i, SlotAction.PULL_ONE);
							storageSlotClick(getScreenHandler().getSlotByID(slotIDUnderMouse).stack.getStack(), SlotAction.PULL_ONE, isTransferOne(mouseButton) ? 1 : 0);
							return true;
						}
					}
				}
				return true;
			} else if (pullHalf(mouseButton)) {
				if (!mc.player.inventory.getCursorStack().isEmpty()) {
					//windowClick(-2, 0, SlotAction.GET_HALF);
					storageSlotClick(ItemStack.EMPTY, hasControlDown() ? SlotAction.GET_QUARTER : SlotAction.GET_HALF, 0);
				} else {
					if (getScreenHandler().getSlotByID(slotIDUnderMouse).stack != null && getScreenHandler().getSlotByID(slotIDUnderMouse).stack.getQuantity() > 0) {
						for (int i = 0;i < getScreenHandler().itemList.size();i++) {
							if (getScreenHandler().getSlotByID(slotIDUnderMouse).stack.equals(getScreenHandler().itemList.get(i))) {
								//windowClick(-2, i, hasShiftDown() ? SlotAction.GET_QUARTER : SlotAction.GET_HALF);
								storageSlotClick(getScreenHandler().getSlotByID(slotIDUnderMouse).stack.getStack(), hasControlDown() ? SlotAction.GET_QUARTER : SlotAction.GET_HALF, 0);
								return true;
							}
						}
					}
				}
			} else if (pullNormal(mouseButton)) {
				if (!mc.player.inventory.getCursorStack().isEmpty()) {
					//windowClick(-(slotIDUnderMouse + 2), 0, SlotAction.PULL_OR_PUSH_STACK);
					storageSlotClick(ItemStack.EMPTY, SlotAction.PULL_OR_PUSH_STACK, 0);
				} else {
					if (getScreenHandler().getSlotByID(slotIDUnderMouse).stack != null) {
						if (getScreenHandler().getSlotByID(slotIDUnderMouse).stack.getQuantity() > 0) {
							for (int i = 0;i < getScreenHandler().itemList.size();i++) {
								if (getScreenHandler().getSlotByID(slotIDUnderMouse).stack.equals(getScreenHandler().itemList.get(i))) {
									//windowClick(-2, i, hasShiftDown() ? SlotAction.SHIFT_PULL : SlotAction.PULL_OR_PUSH_STACK);
									storageSlotClick(getScreenHandler().getSlotByID(slotIDUnderMouse).stack.getStack(), hasShiftDown() ? SlotAction.SHIFT_PULL : SlotAction.PULL_OR_PUSH_STACK, 0);
									return true;
								}
							}
						}
					}
				}
			}
		} else if (GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_SPACE) != GLFW.GLFW_RELEASE) {
			//windowClick(-1, 0, SlotAction.SPACE_CLICK);
			storageSlotClick(ItemStack.EMPTY, SlotAction.SPACE_CLICK, 0);
		} else {
			if (mouseButton == 1 && isPointWithinBounds(searchField.x - x, searchField.y - y, searchField.getWidth(), searchField.getHeight(), mouseX, mouseY))
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
		CompoundTag c = new CompoundTag();
		c.put("s", slotStack.toTag(new CompoundTag()));
		c.putInt("a", act.ordinal());
		c.putByte("m", (byte) mod);
		CompoundTag msg = new CompoundTag();
		msg.put("a", c);
		handler.sendMessage(msg);
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
				TextRenderer font = this.textRenderer;
				RenderSystem.enableDepthTest();
				this.itemRenderer.renderGuiItemIcon(stack, x, y);
				this.itemRenderer.renderGuiItemOverlay(font, stack, mouseX, mouseY);
				//this.setBlitOffset(0);
				//this.itemRenderer.zLevel = 0.0F;
			} else if (mouseX >= x - 1 && mouseY >= y - 1 && mouseX < x + 17 && mouseY < y + 17) {
				List<Text> list = getTooltipFromItem(stack);
				// list.add(I18n.format("tomsmod.gui.amount", stack.stackSize));
				if (extraInfo != null && extraInfo.length > 0) {
					for (int i = 0; i < extraInfo.length; i++) {
						list.add(new LiteralText(extraInfo[i]));
					}
				}
				for (int i = 0;i < list.size();++i) {
					Text t = list.get(i);
					MutableText t2 = t instanceof MutableText ? (MutableText) t : t.copy();
					if (i == 0) {
						list.set(i, t2.formatted(stack.getRarity().formatting));
					} else {
						list.set(i, t2.formatted(Formatting.GRAY));
					}
				}
				this.renderTooltip(st, list, mouseX, mouseY);
			}
		}
	}

	public TextRenderer getFont() {
		return textRenderer;
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (p_keyPressed_1_ == 256) {
			this.mc.player.closeScreen();
			return true;
		}

		return !this.searchField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) && !this.searchField.isActive() ? super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) : true;
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
			int i = ((this.handler).itemList.size() + 9 - 1) / 9 - 5;
			this.currentScroll = (float)(this.currentScroll - p_mouseScrolled_5_ / i);
			this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
			this.handler.scrollTo(this.currentScroll);
			return true;
		}
	}

	public abstract Identifier getGui();

	@Override
	protected void drawBackground(MatrixStack st, float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(getGui());
		drawTexture(st, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);
	}

	public class GuiButton extends ButtonWidget {
		protected int tile;
		protected int state;
		protected int texX = 194;
		protected int texY = 30;
		public GuiButton(int x, int y, int tile, PressAction pressable) {
			super(x, y, 16, 16, new LiteralText(""), pressable);
			this.tile = tile;
		}

		public void setX(int i) {
			x = i;
		}

		/**
		 * Draws this button to the screen.
		 */
		@Override
		public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float pt) {
			if (this.visible) {
				mc.getTextureManager().bindTexture(getGui());
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				//int i = this.getYImage(this.isHovered);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
				drawTexture(matrices, this.x, this.y, texX + state * 16, texY + tile * 16, this.width, this.height);
			}
		}
	}

	protected void onUpdateSearch(String text) {}

	public int getGuiLeft() {
		return x;
	}

	public int getGuiTop() {
		return y;
	}

	@Override
	protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
		//this.textRenderer.draw(matrices, this.title, this.titleX, this.titleY, 4210752);
		this.textRenderer.draw(matrices, this.playerInventory.getDisplayName(), this.playerInventoryTitleX, this.playerInventoryTitleY, 4210752);
	}
}
