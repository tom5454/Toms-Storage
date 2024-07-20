package com.tom.storagemod.screen;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponentPatch;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import com.mojang.blaze3d.systems.RenderSystem;

import com.google.common.base.Predicates;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import com.tom.storagemod.Config;
import com.tom.storagemod.StorageMod;
import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.inventory.StoredItemStack;
import com.tom.storagemod.inventory.StoredItemStack.ComparatorAmount;
import com.tom.storagemod.inventory.StoredItemStack.IStoredItemStackComparator;
import com.tom.storagemod.inventory.StoredItemStack.SortingTypes;
import com.tom.storagemod.menu.StorageTerminalMenu;
import com.tom.storagemod.menu.StorageTerminalMenu.SlotStorage;
import com.tom.storagemod.screen.widget.EnumCycleButton;
import com.tom.storagemod.screen.widget.TerminalSearchModeButton;
import com.tom.storagemod.screen.widget.ToggleButton;
import com.tom.storagemod.util.ComponentJoiner;
import com.tom.storagemod.util.IAutoFillTerminal;
import com.tom.storagemod.util.IDataReceiver;
import com.tom.storagemod.util.NumberFormatUtil;
import com.tom.storagemod.util.PopupMenuManager;
import com.tom.storagemod.util.TerminalSyncManager.SlotAction;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public abstract class AbstractStorageTerminalScreen<T extends StorageTerminalMenu> extends PlatformContainerScreen<T> implements IDataReceiver {
	private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.parse("container/creative_inventory/scroller");
	private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.parse("container/creative_inventory/scroller_disabled");
	private static final LoadingCache<StoredItemStack, List<String>> tooltipCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build(new CacheLoader<StoredItemStack, List<String>>() {
		private Minecraft mc = Minecraft.getInstance();

		@Override
		public List<String> load(StoredItemStack key) throws Exception {
			return key.getStack().getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, getTooltipFlag()).stream().map(Component::getString).collect(Collectors.toList());
		}

	});
	private static final LoadingCache<StoredItemStack, String> componentCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS).build(CacheLoader.from(
			key -> DataComponentPatch.CODEC.encodeStart(JsonOps.COMPRESSED, key.getStack().getComponentsPatch()).mapOrElse(JsonElement::toString, e -> "")
	));
	private static final ResourceLocation FLOATING_SLOT = ResourceLocation.tryBuild(StorageMod.modid, "widget/floating_slot");
	protected Minecraft mc = Minecraft.getInstance();

	/** Amount scrolled in Creative mode inventory (0 = top, 1 = bottom) */
	protected float currentScroll;
	/** True if the scrollbar is being dragged */
	protected boolean isScrolling;
	/**
	 * True if the left mouse button was held down last time drawScreen was
	 * called.
	 */
	private boolean refreshItemList;
	protected boolean wasClicking;
	protected EditBox searchField;
	protected int slotIDUnderMouse = -1, controllMode, rowCount;
	private String searchLast = "";
	protected boolean loadedSearch = false, ghostItems, tallMode;
	public final int textureSlotCount, guiHeight, slotStartX, slotStartY;
	private IStoredItemStackComparator comparator = new ComparatorAmount(false);
	protected EnumCycleButton<SortingTypes> buttonSortingType;
	protected EnumCycleButton<ControllMode> buttonCtrlMode;
	protected TerminalSearchModeButton buttonSearchType;
	protected ToggleButton buttonDirection, buttonGhostMode, buttonTallMode;
	private Comparator<StoredItemStack> sortComp;
	protected PopupMenuManager popup = new PopupMenuManager(this);

	public AbstractStorageTerminalScreen(T screenContainer, Inventory inv, Component titleIn, int textureSlotCount, int guiHeight, int slotStartX, int slotStartY) {
		super(screenContainer, inv, titleIn);
		screenContainer.onPacket = this::onPacket;
		this.textureSlotCount = textureSlotCount;
		this.guiHeight = guiHeight;
		this.slotStartX = slotStartX;
		this.slotStartY = slotStartY;
	}

	protected void onPacket() {
		int s = menu.terminalData;
		controllMode = (s & 0b000_00_0_11) % ControllMode.VALUES.length;
		boolean rev = (s & 0b000_00_1_00) > 0;
		int type = (s & 0b000_11_0_00) >> 3;
		comparator = SortingTypes.VALUES[type % SortingTypes.VALUES.length].create(rev);
		int searchType = (s & 0b111_00_0_00) >> 5;
		ghostItems = (s & 0b1_0_000_00_0_00) == 0;
		boolean tallMode  =  (s & 0b1_0_0_000_00_0_00) != 0;
		if (tallMode != this.tallMode) {
			this.tallMode = tallMode;
			init();
		}
		if(!searchField.isFocused() && (searchType & 1) > 0) {
			searchField.setFocused(true);
		}
		buttonSortingType.setState(SortingTypes.VALUES[type % SortingTypes.VALUES.length]);
		buttonDirection.setState(rev);
		buttonSearchType.setSearchType(searchType);
		buttonCtrlMode.setState(ControllMode.VALUES[controllMode]);
		buttonGhostMode.setState(ghostItems);
		buttonTallMode.setState(tallMode);

		if(!loadedSearch && menu.search != null) {
			loadedSearch = true;
			if((searchType & 2) > 0)
				searchField.setValue(menu.search);
		}
	}

	protected void sendUpdate() {
		CompoundTag c = new CompoundTag();
		c.putInt("d", updateData());
		CompoundTag msg = new CompoundTag();
		msg.put("c", c);
		menu.sendMessage(msg);
	}

	protected int updateData() {
		int d = 0;
		d |= (controllMode & 0b000_0_11);
		d |= ((comparator.isReversed() ? 1 : 0) << 2);
		d |= (comparator.type() << 3);
		d |= ((buttonSearchType.getSearchType() & 0b111) << 5);
		d |= ((ghostItems ? 0 : 1) << 9);
		d |= ((tallMode ? 1 : 0) << 10);
		return d;
	}

	@Override
	protected void init() {
		clearWidgets();
		if (tallMode) {
			int guiSize = guiHeight - textureSlotCount * 18;
			rowCount = (height - 20 - guiSize) / 18;
			imageHeight = guiSize + rowCount * 18;
			menu.setOffset(0, (rowCount - textureSlotCount) * 18);
			menu.addStorageSlots(rowCount, slotStartX + 1, slotStartY + 1);
		} else {
			rowCount = textureSlotCount;
			menu.setOffset(0, 0);
			menu.addStorageSlots(rowCount, slotStartX + 1, slotStartY + 1);
		}
		Slot offh = getMenu().offhand;
		if (mc.options.mainHand().get() == HumanoidArm.RIGHT) {
			offh.x = -26;
		} else {
			offh.x = 186;
		}
		inventoryLabelY = imageHeight - 92;
		super.init();
		this.searchField = new EditBox(getFont(), this.leftPos + 82, this.topPos + 6, 89, this.getFont().lineHeight, Component.translatable("narrator.toms_storage.terminal_search"));
		this.searchField.setMaxLength(100);
		this.searchField.setBordered(false);
		this.searchField.setVisible(true);
		this.searchField.setTextColor(16777215);
		this.searchField.setValue(searchLast);
		searchLast = "";
		addWidget(searchField);
		buttonSortingType = addRenderableWidget(new EnumCycleButton<>(leftPos - 18, topPos + 5, Component.translatable("narrator.toms_storage.terminal_sort"), "sort", SortingTypes.VALUES, n -> {
			comparator = n.create(comparator.isReversed());
			buttonSortingType.setState(n);
			sendUpdate();
			refreshItemList = true;
		}));
		buttonDirection = addRenderableWidget(ToggleButton.builder(leftPos - 18, topPos + 5 + 18).
				name(Component.translatable("narrator.toms_storage.terminal_sort_rev")).
				iconOff(ResourceLocation.tryBuild(StorageMod.modid, "icons/sort_desc")).
				iconOn(ResourceLocation.tryBuild(StorageMod.modid, "icons/sort_asc")).
				build(n -> {
					comparator.setReversed(n);
					buttonDirection.setState(n);
					sendUpdate();
					refreshItemList = true;
				}));
		buttonSearchType = addRenderableWidget(new TerminalSearchModeButton(leftPos - 18, topPos + 5 + 18*2, popup, IAutoFillTerminal.hasSync() || this instanceof CraftingTerminalScreen, this::sendUpdate));
		buttonCtrlMode = addRenderableWidget(new EnumCycleButton<>(leftPos - 18, topPos + 5 + 18*3, Component.translatable("narrator.toms_storage.terminal_control"), "control", ControllMode.VALUES, n -> {
			controllMode = n.ordinal();
			buttonCtrlMode.setState(n);
			sendUpdate();
		}));
		buttonGhostMode = addRenderableWidget(ToggleButton.builder(leftPos - 18, topPos + 5 + 18*4).
				name(Component.translatable("narrator.toms_storage.terminal_ghost_items")).
				iconOff(ResourceLocation.tryBuild(StorageMod.modid, "icons/keep_last_off")).
				iconOn(ResourceLocation.tryBuild(StorageMod.modid, "icons/keep_last_0")).
				build(n -> {
					ghostItems = n;
					buttonGhostMode.setState(n);
					sendUpdate();
				}));
		buttonGhostMode.setTooltip(Tooltip.create(Component.translatable("tooltip.toms_storage.ghostMode_off")), Tooltip.create(Component.translatable("tooltip.toms_storage.ghostMode_on")));
		buttonTallMode = addRenderableWidget(ToggleButton.builder(leftPos - 18, topPos + 5 + 18*5).
				name(Component.translatable("narrator.toms_storage.terminal_tall_mode")).
				iconOff(ResourceLocation.tryBuild(StorageMod.modid, "icons/tall_terminal_off")).
				iconOn(ResourceLocation.tryBuild(StorageMod.modid, "icons/tall_terminal_on")).
				build(n -> {
					tallMode = n;
					buttonTallMode.setState(n);
					sendUpdate();
					init();
				}));
		buttonTallMode.setTooltip(Tooltip.create(Component.translatable("tooltip.toms_storage.tallMode_off")), Tooltip.create(Component.translatable("tooltip.toms_storage.tallMode_on")));
		buttonSortingType.tooltipFactory = s -> Tooltip.create(Component.translatable("tooltip.toms_storage.sorting_" + s.name().toLowerCase(Locale.ROOT)));
		buttonCtrlMode.tooltipFactory = s -> Tooltip.create(Arrays.stream(I18n.get("tooltip.toms_storage.ctrlMode_" + s.name().toLowerCase(Locale.ROOT)).split("\\\\")).map(Component::literal).collect(ComponentJoiner.joining(Component.empty(), Component.literal("\n"))));
		updateSearch();
	}

	protected void updateSearch() {
		String searchString = searchField.getValue();
		if (refreshItemList || !searchLast.equals(searchString)) {
			getMenu().itemListClientSorted.clear();
			Predicate<StoredItemStack> pred = null;
			String[] or = searchString.split("\\|");
			for (int i = 0; i < or.length; i++) {
				String part = or[i].trim();
				if (part.isEmpty())continue;
				String[] sp = part.split(" ");
				Predicate<StoredItemStack> p = (__) -> true;
				for (int j = 0; j < sp.length; j++) {
					String s = sp[j].toLowerCase();
					if (s.startsWith("@")) {
						String fs = s.substring(1);
						p = p.and(is -> BuiltInRegistries.ITEM.getKey(is.getStack().getItem()).getNamespace().contains(fs));
					} else if (s.startsWith("#")) {
						String fs = s.substring(1);
						p = p.and(is -> {
							return is.getStack().getTags().map(t -> t.location().toString()).anyMatch(st -> st.contains(fs));
						});
					} else if (s.startsWith("$")) {
						String fs = s.substring(1);
						Pattern m;
						try {
							m = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
						} catch (Throwable ignore) {
							try {
								m = Pattern.compile(Pattern.quote(fs), Pattern.CASE_INSENSITIVE);
							} catch (Throwable ignored) {
								continue;
							}
						}
						final Pattern fm = m;
						p = p.and(is -> fm.matcher(Optional.ofNullable(componentCache.getIfPresent(is)).orElse("")).find());
					} else {
						Pattern m = null;
						try {
							m = Pattern.compile(s, Pattern.CASE_INSENSITIVE);
						} catch (Throwable ignore) {
							try {
								m = Pattern.compile(Pattern.quote(s), Pattern.CASE_INSENSITIVE);
							} catch (Throwable __) {
								continue;
							}
						}
						final Pattern fm = m;
						p = p.and(is -> {
							try {
								String dspName = is.getStack().getHoverName().getString();
								if (fm.matcher(dspName.toLowerCase()).find()) {
									return true;
								}
								for (String lp : tooltipCache.get(is)) {
									if (fm.matcher(lp).find()) {
										return true;
									}
								}
							} catch (Exception e) {
							}
							return false;
						});
					}
				}
				if (pred == null)pred = p;
				else pred = pred.or(p);
			}
			if (pred == null)pred = Predicates.alwaysTrue();
			try {
				for (int i = 0;i < getMenu().itemListClient.size();i++) {
					StoredItemStack is = getMenu().itemListClient.get(i);
					if (is != null && is.getStack() != null) {
						if (pred.test(is))
							addStackToClientList(is);
					}
				}
			} catch (Exception e) {
			}
			Collections.sort(getMenu().itemListClientSorted, menu.noSort ? sortComp : comparator);
			if(!searchLast.equals(searchString)) {
				getMenu().scrollTo(0);
				this.currentScroll = 0;
				int searchType = buttonSearchType.getSearchType();
				if (searchType != -1) {
					if ((searchType & 4) > 0) {
						IAutoFillTerminal.sync(searchString);
					}
					if ((searchType & 2) > 0) {
						CompoundTag nbt = new CompoundTag();
						nbt.putString("s", searchString);
						menu.sendMessage(nbt);
					}
				}
				onUpdateSearch(searchString);
			} else {
				getMenu().scrollTo(this.currentScroll);
			}
			refreshItemList = false;
			this.searchLast = searchString;
		}
	}

	private void addStackToClientList(StoredItemStack is) {
		getMenu().itemListClientSorted.add(is);
	}

	public static TooltipFlag getTooltipFlag(){
		return Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
	}

	@Override
	protected void containerTick() {
		updateSearch();
	}

	@Override
	public void render(GuiGraphics st, int mouseX, int mouseY, float partialTicks) {
		boolean flag = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_RELEASE;
		int i = this.leftPos;
		int j = this.topPos;
		int k = i + 174;
		int l = j + 18;
		int i1 = k + 14;
		int j1 = l + rowCount * 18;

		if(ghostItems && hasShiftDown()) {
			if(!menu.noSort) {
				List<StoredItemStack> list = getMenu().itemListClientSorted;
				Object2IntMap<StoredItemStack> map = new Object2IntOpenHashMap<>();
				map.defaultReturnValue(Integer.MAX_VALUE);
				for (int m = 0; m < list.size(); m++) {
					map.put(list.get(m), m);
				}
				sortComp = Comparator.comparing(map::getInt);
				menu.noSort = true;
			}
		} else if(menu.noSort) {
			sortComp = null;
			menu.noSort = false;
			refreshItemList = true;
			menu.itemListClient = new ArrayList<>(menu.itemList);
		}

		if (!this.wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1) {
			this.isScrolling = this.needsScrollBars();
		}

		if (!flag) {
			this.isScrolling = false;
		}
		this.wasClicking = flag;

		if (this.isScrolling) {
			this.currentScroll = (mouseY - l - 7.5F) / (j1 - l - 15.0F);
			this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);
			getMenu().scrollTo(this.currentScroll);
		}
		super.render(st, mouseX, mouseY, partialTicks);

		i = k;
		j = l;
		k = j1;
		st.blitSprite(this.needsScrollBars() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE, i, j + (int) ((k - j - 17) * this.currentScroll), 12, 15);

		searchField.render(st, mouseX, mouseY, partialTicks);

		if(menu.beaconLvl > 0) {
			int x = 176;
			int y = 24 + rowCount * 18;
			st.renderItem(new ItemStack(Items.BEACON), leftPos + x, topPos + y);

			if(isHovering(x, y, 16, 16, mouseX, mouseY)) {
				String info;
				if(Config.get().wirelessTermBeaconLvlCrossDim != -1 && menu.beaconLvl >= Config.get().wirelessTermBeaconLvlCrossDim)info = "\\" + I18n.get("tooltip.toms_storage.terminal_beacon.anywhere");
				else if(Config.get().wirelessTermBeaconLvl != -1 && menu.beaconLvl >= Config.get().wirelessTermBeaconLvl)info = "\\" + I18n.get("tooltip.toms_storage.terminal_beacon.sameDim");
				else info = "";
				st.renderComponentTooltip(font, Arrays.stream(I18n.get("tooltip.toms_storage.terminal_beacon", menu.beaconLvl, info).split("\\\\")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
			}
		}
		if (popup.render(st, font, mouseX, mouseY)) {
			if (this.menu.getCarried().isEmpty() && slotIDUnderMouse != -1) {
				SlotStorage slot = getMenu().storageSlotList.get(slotIDUnderMouse);
				if(slot.stack != null) {
					if (slot.stack.getQuantity() > 9999) {
						ClientUtil.setTooltip(Component.translatable("tooltip.toms_storage.amount", slot.stack.getQuantity()));
					}
					st.renderTooltip(font, slot.stack.getActualStack(), mouseX, mouseY);
					ClientUtil.setTooltip();
				}
			} else {
				this.renderTooltip(st, mouseX, mouseY);
			}
		}
	}

	@Override
	protected void renderLabels(GuiGraphics st, int mouseX, int mouseY) {
		super.renderLabels(st, mouseX, mouseY);
		st.pose().pushPose();
		slotIDUnderMouse = drawSlots(st, mouseX, mouseY);
		st.pose().popPose();
	}

	protected int drawSlots(GuiGraphics st, int mouseX, int mouseY) {
		StorageTerminalMenu term = getMenu();
		int slotHover = -1;
		for (int i = 0;i < term.storageSlotList.size();i++) {
			if(drawSlot(st, term.storageSlotList.get(i), mouseX, mouseY))slotHover = i;
		}
		return slotHover;
	}

	protected boolean drawSlot(GuiGraphics st, SlotStorage slot, int mouseX, int mouseY) {
		if (slot.stack != null) {
			ItemStack stack = slot.stack.getStack().copy().split(1);
			int i = slot.xDisplayPosition, j = slot.yDisplayPosition;

			st.renderItem(stack, i, j);
			st.renderItemDecorations(font, stack, i, j);

			drawStackSize(st, getFont(), slot.stack.getQuantity(), i, j);
		}

		if (mouseX >= getGuiLeft() + slot.xDisplayPosition - 1 && mouseY >= getGuiTop() + slot.yDisplayPosition - 1 && mouseX < getGuiLeft() + slot.xDisplayPosition + 17 && mouseY < getGuiTop() + slot.yDisplayPosition + 17) {
			int l = slot.xDisplayPosition;
			int t = slot.yDisplayPosition;
			renderSlotHighlight(st, l, t, 0);
			return true;
		}
		return false;
	}

	private void drawStackSize(GuiGraphics st, Font fr, long size, int x, int y) {
		float scaleFactor = 0.6f;
		RenderSystem.disableDepthTest();
		RenderSystem.disableBlend();
		String stackSize = NumberFormatUtil.formatNumber(size);
		st.pose().pushPose();
		st.pose().scale(scaleFactor, scaleFactor, scaleFactor);
		st.pose().translate(0, 0, 450);
		float inverseScaleFactor = 1.0f / scaleFactor;
		int X = (int) (((float) x + 0 + 16.0f - fr.width(stackSize) * scaleFactor) * inverseScaleFactor);
		int Y = (int) (((float) y + 0 + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
		st.drawString(fr, stackSize, X, Y, 16777215, true);
		st.pose().popPose();
		RenderSystem.enableDepthTest();
	}

	protected boolean needsScrollBars() {
		return this.getMenu().itemListClientSorted.size() > rowCount * 9;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (popup.mouseClick(mouseX, mouseY, mouseButton))return true;
		if (slotIDUnderMouse > -1) {
			if (isPullOne(mouseButton)) {
				if (getMenu().getSlotByID(slotIDUnderMouse).stack != null && getMenu().getSlotByID(slotIDUnderMouse).stack.getQuantity() > 0) {
					storageSlotClick(getMenu().getSlotByID(slotIDUnderMouse).stack, SlotAction.PULL_ONE, isTransferOne(mouseButton));
					return true;
				}
				return true;
			} else if (pullHalf(mouseButton)) {
				if (!menu.getCarried().isEmpty()) {
					storageSlotClick(null, hasControlDown() ? SlotAction.GET_QUARTER : SlotAction.GET_HALF, false);
				} else {
					if (getMenu().getSlotByID(slotIDUnderMouse).stack != null && getMenu().getSlotByID(slotIDUnderMouse).stack.getQuantity() > 0) {
						storageSlotClick(getMenu().getSlotByID(slotIDUnderMouse).stack, hasControlDown() ? SlotAction.GET_QUARTER : SlotAction.GET_HALF, false);
						return true;
					}
				}
			} else if (pullNormal(mouseButton)) {
				if (!menu.getCarried().isEmpty()) {
					storageSlotClick(null, SlotAction.PULL_OR_PUSH_STACK, false);
				} else {
					if (getMenu().getSlotByID(slotIDUnderMouse).stack != null) {
						if (getMenu().getSlotByID(slotIDUnderMouse).stack.getQuantity() > 0) {
							storageSlotClick(getMenu().getSlotByID(slotIDUnderMouse).stack, hasShiftDown() ? SlotAction.SHIFT_PULL : SlotAction.PULL_OR_PUSH_STACK, false);
							return true;
						}
					}
				}
			}
		} else if (GLFW.glfwGetKey(mc.getWindow().getWindow(), GLFW.GLFW_KEY_SPACE) != GLFW.GLFW_RELEASE) {
			storageSlotClick(null, SlotAction.SPACE_CLICK, false);
		} else {
			if (isHovering(searchField.getX() - leftPos, searchField.getY() - topPos, 89, this.getFont().lineHeight, mouseX, mouseY)) {
				if(mouseButton == 1)
					searchField.setValue("");
				else
					return super.mouseClicked(mouseX, mouseY, mouseButton);
			} else {
				searchField.setFocused(false);
				return super.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
		return true;
	}

	protected void storageSlotClick(StoredItemStack slotStack, SlotAction act, boolean mod) {
		menu.sync.sendInteract(slotStack, act, mod);
	}

	public boolean isPullOne(int mouseButton) {
		switch (ctrlm()) {
		case AE:
			return mouseButton == 1 && hasShiftDown();
		case RS:
			return mouseButton == 2;
		case DEF:
			return mouseButton == 1 && !menu.getCarried().isEmpty();
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
		case DEF:
			return mouseButton == 1 && hasShiftDown();
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
		case DEF:
			return mouseButton == 1 && menu.getCarried().isEmpty();
		default:
			return false;
		}
	}

	public boolean pullNormal(int mouseButton) {
		switch (ctrlm()) {
		case AE:
		case RS:
		case DEF:
			return mouseButton == 0;
		default:
			return false;
		}
	}

	private ControllMode ctrlm() {
		return ControllMode.VALUES[controllMode];
	}

	public Font getFont() {
		return font;
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		if (popup.keyPressed(pKeyCode, pScanCode, pModifiers))return true;
		if (pKeyCode == 256) {
			this.onClose();
			return true;
		}
		if(pKeyCode == GLFW.GLFW_KEY_TAB)return super.keyPressed(pKeyCode, pScanCode, pModifiers);
		if (this.searchField.keyPressed(pKeyCode, pScanCode, pModifiers) || this.searchField.canConsumeInput()) {
			return true;
		}
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}

	@Override
	public boolean charTyped(char pCodePoint, int pModifiers) {
		if(popup.charTyped(pCodePoint, pModifiers))return true;
		if(searchField.charTyped(pCodePoint, pModifiers))return true;
		return super.charTyped(pCodePoint, pModifiers);
	}

	@Override
	public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double xd, double p_mouseScrolled_5_) {
		if (!this.needsScrollBars()) {
			return false;
		} else {
			int i = ((this.menu).itemListClientSorted.size() + 9 - 1) / 9 - 5;
			this.currentScroll = (float)(this.currentScroll - p_mouseScrolled_5_ / i);
			this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);
			this.menu.scrollTo(this.currentScroll);
			return true;
		}
	}

	public abstract ResourceLocation getGui();

	@Override
	protected void renderBg(GuiGraphics st, float partialTicks, int mouseX, int mouseY) {
		if(tallMode) {
			st.blit(getGui(), this.leftPos, this.topPos, 0, 0, this.imageWidth, slotStartY);
			int guiStart = textureSlotCount * 18 + slotStartY;
			int guiRStart = rowCount * 18 + slotStartY;
			int guiSize = guiHeight - textureSlotCount * 18 - slotStartY;
			st.blit(getGui(), this.leftPos, this.topPos + guiRStart, 0, guiStart, this.imageWidth, guiSize);
			int scrollbarW = 25;
			st.blit(getGui(), this.leftPos, this.topPos + slotStartY, 0, slotStartY, slotStartX + 9 * 18 + scrollbarW, 18);
			for (int i = 1;i < rowCount - 1;i++) {
				st.blit(getGui(), this.leftPos, this.topPos + slotStartY + i * 18, 0, slotStartY + 18, slotStartX + 9 * 18 + scrollbarW, 18);
			}
			st.blit(getGui(), this.leftPos, this.topPos + slotStartY + (rowCount - 1) * 18, 0, slotStartY + (textureSlotCount - 1) * 18, slotStartX + 9 * 18 + scrollbarW, 18);
		} else
			st.blit(getGui(), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

		Slot offh = getMenu().offhand;
		st.blitSprite(FLOATING_SLOT, this.leftPos + offh.x - 8, this.topPos + offh.y - 8, 32, 32);
	}

	protected void onUpdateSearch(String text) {}

	@Override
	public void receive(CompoundTag tag) {
		menu.receiveClientNBTPacket(tag);
		refreshItemList = true;
	}

	private FakeSlot fakeSlotUnderMouse = new FakeSlot();
	private static class FakeSlot extends Slot {
		private static final Container DUMMY = new SimpleContainer(1);

		public FakeSlot() {
			super(DUMMY, 0, Integer.MIN_VALUE, Integer.MIN_VALUE);
		}

		@Override
		public boolean allowModification(Player p_150652_) {
			return false;
		}

		@Override
		public void set(ItemStack p_40240_) {}

		@Override
		public ItemStack remove(int p_40227_) {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public Slot getSlotUnderMouse() {
		Slot s = super.getSlotUnderMouse();
		if(s != null)return s;
		if(slotIDUnderMouse > -1 && getMenu().getSlotByID(slotIDUnderMouse).stack != null) {
			fakeSlotUnderMouse.container.setItem(0, getMenu().getSlotByID(slotIDUnderMouse).stack.getStack());
			return fakeSlotUnderMouse;
		}
		return null;
	}

	public static enum ControllMode {
		AE, RS, DEF,
		;
		public static final ControllMode[] VALUES = values();
	}

	public int getRowCount() {
		return rowCount;
	}
}
