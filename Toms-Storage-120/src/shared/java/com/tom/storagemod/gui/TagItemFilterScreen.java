package com.tom.storagemod.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.Tooltip;
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

import com.tom.storagemod.gui.GuiButton.CompositeButton;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.util.IDataReceiver;

public class TagItemFilterScreen extends AbstractFilteredScreen<TagItemFilterMenu> implements IDataReceiver {
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("toms_storage", "textures/gui/tag_filter.png");
	private static final int LINES = 4;
	private GuiButton buttonAllowList, buttonAdd, buttonRemove;
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

		buttonAllowList = addRenderableWidget(new GuiButton(leftPos - 18, topPos + 5, 0, b -> {
			click(0, buttonAllowList.getState() != 1);
		}));
		buttonAllowList.tooltipFactory = s -> Tooltip.create(Component.translatable("tooltip.toms_storage.allowList_" + s));

		itemTagList = new ListHandler(leftPos + 28, topPos + 15);
		itemTagList.list = () -> itemTags;
		filterList = new ListHandler(leftPos + 109, topPos + 15);
		filterList.list = () -> filterTags;

		buttonAdd = addRenderableWidget(makeCompositeButton(leftPos + 90, topPos + 14, 0, b -> {
			if(itemTagList.selected != null) {
				if(!filterTags.contains(itemTagList.selected))
					filterTags.add(itemTagList.selected);
				itemTagList.selected = null;
				sync();
			}
		}));

		buttonRemove = addRenderableWidget(makeCompositeButton(leftPos + 90, topPos + 32, 1, b -> {
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

	public CompositeButton makeCompositeButton(int x, int y, int tile, OnPress pressable) {
		CompositeButton btn = new CompositeButton(x, y, tile, pressable);
		btn.texX = 176;
		btn.texY = 0;
		btn.texture = GUI_TEXTURES;
		return btn;
	}

	@Override
	public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack, mouseX, mouseY, partialTicks);
		buttonAllowList.setState(menu.allowList ? 1 : 0);
		itemTagList.preRender(mouseX, mouseY);
		filterList.preRender(mouseX, mouseY);
		buttonAdd.active = itemTagList.selected != null;
		buttonRemove.active = filterList.selected != null;
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		itemTagList.render(matrixStack);
		filterList.render(matrixStack);
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
	public boolean mouseScrolled(double x, double y, double dir) {
		if(isHovering(27, 14, 61, 58, x, y) && itemTagList.mouseScrolled(dir))return true;
		else if(isHovering(108, 14, 61, 58, x, y) && filterList.mouseScrolled(dir))return true;
		return super.mouseScrolled(x, y, dir);
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

		public void render(GuiGraphics st) {
			int i = this.x + 55;
			int k = this.y + list.get().size() * 14;
			st.blit(GUI_TEXTURES, i, this.y - 1 + (int) (49 * this.currentScroll), 176 + (this.needsScrollBars() ? 5 : 0), 32, 5, 9);
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

		public class ListEntry extends ButtonExt {
			private int id;

			public ListEntry(int x, int y, int id) {
				super(x, y, 53, 14, Component.empty(), null);
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
						int i = this.getYImage(this.isHoveredOrFocused()) - 1;
						st.blit(GUI_TEXTURES, x, y, 176, 41 + i * 14 + (id.equals(selected) ? 28 : 0), this.width, this.height);
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
