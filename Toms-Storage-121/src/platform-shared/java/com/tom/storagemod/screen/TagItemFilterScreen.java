package com.tom.storagemod.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.menu.TagItemFilterMenu;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.screen.widget.IconButton;
import com.tom.storagemod.screen.widget.ToggleButton;
import com.tom.storagemod.util.IDataReceiver;

public class TagItemFilterScreen extends AbstractFilteredScreen<TagItemFilterMenu> implements IDataReceiver {
	private static final ResourceLocation GUI_TEXTURES = ResourceLocation.tryBuild(StorageMod.modid, "textures/gui/tag_filter.png");
	private static final int LINES = 4;
	protected static final WidgetSprites LIST_BUTTON_SPRITES = new WidgetSprites(
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_button"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_button_disabled"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_button_hovered")
			);
	protected static final WidgetSprites LIST_BUTTON_SPRITES_S = new WidgetSprites(
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_button_selected"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_button_disabled"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_button_selected_hovered")
			);
	protected static final WidgetSprites SCROLL_SPRITES = new WidgetSprites(
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_scroll"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_scroll_disabled"),
			ResourceLocation.tryBuild(StorageMod.modid, "widget/small_scroll_hovered")
			);
	private ToggleButton buttonAllowList;
	private IconButton buttonAdd, buttonRemove;
	private List<String> itemTags = new ArrayList<>();
	private List<String> filterTags = new ArrayList<>();
	private ListHandler itemTagList, filterList;

	public TagItemFilterScreen(TagItemFilterMenu container, Inventory playerInventory, Component textComponent) {
		super(container, playerInventory, textComponent);
	}

	@Override
	protected void init() {
		super.init();
		//this.titleX = (this.xSize - this.font.getStringPropertyWidth(this.title)) / 2;

		buttonAllowList = addRenderableWidget(ToggleButton.builder(leftPos - 18, topPos + 5).
				iconOff(ResourceLocation.tryBuild(StorageMod.modid, "icons/deny")).
				iconOn(ResourceLocation.tryBuild(StorageMod.modid, "icons/allow")).
				build(s -> click(0, s)));
		buttonAllowList.setTooltip(Tooltip.create(Component.translatable("tooltip.toms_storage.denyList")), Tooltip.create(Component.translatable("tooltip.toms_storage.allowList")));

		itemTagList = new ListHandler(leftPos + 28, topPos + 15);
		itemTagList.list = () -> itemTags;
		filterList = new ListHandler(leftPos + 109, topPos + 15);
		filterList.list = () -> filterTags;

		buttonAdd = addRenderableWidget(new IconButton(leftPos + 90, topPos + 14, Component.translatable(""), ResourceLocation.tryBuild(StorageMod.modid, "icons/add"), b -> {
			if(itemTagList.selected != null) {
				if(!filterTags.contains(itemTagList.selected))
					filterTags.add(itemTagList.selected);
				itemTagList.selected = null;
				sync();
			}
		}));

		buttonRemove = addRenderableWidget(new IconButton(leftPos + 90, topPos + 32, Component.translatable(""), ResourceLocation.tryBuild(StorageMod.modid, "icons/deny"), b -> {
			if(filterList.selected != null) {
				filterTags.remove(filterList.selected);
				filterList.selected = null;
				sync();
			}
		}));
	}

	private void sync() {
		ListTag list = new ListTag();
		filterTags.forEach(t -> list.add(StringTag.valueOf(t)));
		CompoundTag tag = new CompoundTag();
		tag.put("l", list);
		NetworkHandler.sendDataToServer(tag);
	}

	@Override
	protected void containerTick() {
		ItemStack s = menu.slots.get(0).getItem();
		List<String> tags = s.getTags().map(t -> t.location().toString()).toList();
		if(!itemTags.equals(tags)) {
			itemTags.clear();
			itemTags.addAll(tags);
			itemTagList.selected = null;
			itemTagList.currentScroll = 0f;
		}
	}

	private void click(int id, boolean val) {
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, (id << 1) | (val ? 1 : 0));
	}

	@Override
	public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack, mouseX, mouseY, partialTicks);
		buttonAllowList.setState(menu.allowList);
		itemTagList.preRender(mouseX, mouseY);
		filterList.preRender(mouseX, mouseY);
		buttonAdd.active = itemTagList.selected != null;
		buttonRemove.active = filterList.selected != null;
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		itemTagList.render(matrixStack, mouseX, mouseY);
		filterList.render(matrixStack, mouseX, mouseY);
		this.renderTooltip(matrixStack, mouseX, mouseY);

		itemTagList.tooltip(matrixStack, mouseX, mouseY);
		filterList.tooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics matrixStack, float partialTicks, int x, int y) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		matrixStack.blit(GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	public void receive(CompoundTag tag) {
		ListTag list = tag.getList("l", Tag.TAG_STRING);
		filterTags.clear();
		for (int i = 0; i < list.size(); i++) {
			filterTags.add(list.getString(i));
		}
		filterList.selected = null;
		filterList.currentScroll = 0f;
	}

	@Override
	public boolean mouseScrolled(double x, double y, double xd, double yd) {
		if(isHovering(27, 14, 61, 58, x, y) && itemTagList.mouseScrolled(yd))return true;
		else if(isHovering(108, 14, 61, 58, x, y) && filterList.mouseScrolled(yd))return true;
		return super.mouseScrolled(x, y, xd, yd);
	}

	public class ListHandler {
		protected float currentScroll;
		protected boolean isScrolling;
		protected boolean wasClicking;
		protected Supplier<List<String>> list;
		protected String selected;
		private List<ListEntry> listEntries = new ArrayList<>();
		private int x, y;

		public ListHandler(int x, int y) {
			for(int i = 0;i<LINES;i++) {
				listEntries.add(new ListEntry(x, y + i * 14, i));
			}
			this.x = x;
			this.y = y;
		}

		public void tooltip(GuiGraphics matrixStack, int mouseX, int mouseY) {
			listEntries.stream().filter(s -> s.isHovered()).findFirst().ifPresent(le -> {
				String id = le.getId();
				if(id != null)matrixStack.renderTooltip(font, Component.literal(id), mouseX, mouseY);
			});
		}

		public boolean needsScrollBars() {
			return list.get().size() > LINES;
		}

		public void preRender(int mouseX, int mouseY) {
			boolean flag = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_RELEASE;

			int k = this.x + 56;
			int l = this.y - 1;
			int i1 = k + 14;
			int j1 = l + 58;

			if (!this.wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1) {
				this.isScrolling = this.needsScrollBars();
			}

			if (!flag) {
				this.isScrolling = false;
			}
			this.wasClicking = flag;

			if (this.isScrolling) {
				this.currentScroll = (mouseY - l - 4.5F) / (j1 - l - 9.0F);
				this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);
			}
		}

		public void render(GuiGraphics st, int mouseX, int mouseY) {
			int x = this.x + 55;
			int y = this.y - 1 + (int) (49 * this.currentScroll);
			boolean isHovered = mouseX >= x && mouseY >= y && mouseX < x + 5 && mouseY < y + 9;
			st.blitSprite(SCROLL_SPRITES.get(this.needsScrollBars(), isHovered), x, y, 5, 9);
		}

		public boolean mouseScrolled(double dir) {
			if (!this.needsScrollBars()) {
				return false;
			} else {
				int i = list.get().size() - LINES;
				this.currentScroll = (float)(this.currentScroll - dir / i);
				this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);
				return true;
			}
		}

		public class ListEntry extends Button {
			private int id;

			public ListEntry(int x, int y, int id) {
				super(x, y, 53, 14, Component.empty(), null, DEFAULT_NARRATION);
				this.id = id;
				addRenderableWidget(this);
			}

			/**
			 * Draws this button to the screen.
			 */
			@Override
			public void renderWidget(GuiGraphics st, int mouseX, int mouseY, float pt) {
				if (this.visible) {
					String id = getId();
					if(id != null) {
						int x = getX();
						int y = getY();
						st.setColor(1.0f, 1.0f, 1.0f, this.alpha);
						RenderSystem.enableBlend();
						RenderSystem.enableDepthTest();
						this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
						var spr = (id.equals(selected) ? LIST_BUTTON_SPRITES_S : LIST_BUTTON_SPRITES).get(this.active, this.isHoveredOrFocused());
						st.blitSprite(spr, this.getX(), this.getY(), this.getWidth(), this.getHeight());
						int c = this.active ? 0xFFFFFF : 0xA0A0A0;
						int v = c | Mth.ceil(this.alpha * 255.0f) << 24;
						int k = this.getX() + 2;
						int l = this.getX() + this.getWidth() - 2;
						AbstractWidget.renderScrollingString(st, font, Component.literal(id), k, this.getY(), l, this.getY() + this.getHeight(), v);
					}
				}
			}

			@Override
			public void onPress() {
				String id = getId();
				if(id != null) {
					selected = id;
				}
			}

			private String getId() {
				List<String> l = list.get();
				int i = l.size() - LINES;
				int j = (int) (currentScroll * i + 0.5D);
				if (j < 0) {
					j = 0;
				}
				if(this.id + j < l.size()) {
					return l.get(this.id + j);
				}
				return null;
			}
		}
	}
}
