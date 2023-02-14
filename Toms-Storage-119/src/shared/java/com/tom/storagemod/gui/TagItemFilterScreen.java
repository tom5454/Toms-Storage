package com.tom.storagemod.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

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
			click(0, buttonAllowList.state != 1);
		}));

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
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		buttonAllowList.state = menu.allowList ? 1 : 0;
		itemTagList.preRender(mouseX, mouseY);
		filterList.preRender(mouseX, mouseY);
		buttonAdd.active = itemTagList.selected != null;
		buttonRemove.active = filterList.selected != null;
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		itemTagList.render(matrixStack);
		filterList.render(matrixStack);
		this.renderTooltip(matrixStack, mouseX, mouseY);

		if (buttonAllowList.isHoveredOrFocused()) {
			renderTooltip(matrixStack, Component.translatable("tooltip.toms_storage.allowList_" + buttonAllowList.state), mouseX, mouseY);
		}
		itemTagList.tooltip(matrixStack, mouseX, mouseY);
		filterList.tooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURES);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
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

		public void tooltip(PoseStack matrixStack, int mouseX, int mouseY) {
			listEntries.stream().filter(s -> s.isHoveredOrFocused()).findFirst().ifPresent(le -> {
				String id = le.getId();
				if(id != null)renderTooltip(matrixStack, Component.literal(id), mouseX, mouseY);
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

		public void render(PoseStack st) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, GUI_TEXTURES);
			int i = this.x + 55;
			int k = this.y + list.get().size() * 14;
			blit(st, i, this.y - 1 + (int) (49 * this.currentScroll), 176 + (this.needsScrollBars() ? 5 : 0), 32, 5, 9);
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

		public class ListEntry extends PlatformButton {
			private int id;

			public ListEntry(int x, int y, int id) {
				super(x, y, 53, 14, null, null);
				this.id = id;
				addRenderableWidget(this);
			}

			/**
			 * Draws this button to the screen.
			 */
			@Override
			public void renderButton(PoseStack st, int mouseX, int mouseY, float pt) {
				if (this.visible) {
					String id = getId();
					if(id != null) {
						int x = getX();
						int y = getY();
						RenderSystem.setShader(GameRenderer::getPositionTexShader);
						RenderSystem.setShaderTexture(0, GUI_TEXTURES);
						RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
						RenderSystem.enableBlend();
						RenderSystem.defaultBlendFunc();
						RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
						this.isHovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
						int i = this.getYImage(this.isHoveredOrFocused()) - 1;
						this.blit(st, x, y, 176, 41 + i * 14 + (id.equals(selected) ? 28 : 0), this.width, this.height);
						int j = 0xffffffff;
						st.pushPose();
						st.translate(x + this.width / 2, y + (this.height - 8) / 2, 0);
						float s = 0.75f;
						int l = (int) (font.width(id) * s);
						st.scale(s, s, s);
						int m = this.width - 4;
						if (l > m) {
							double d = Util.getMillis() / 1000.0;
							double e = Math.sin(1.5707963267948966 * Math.cos(d));
							int n = l - m;
							enableScissor(x + 2, y + 2, x + this.width - 2, y + this.height - 2);
							float tx = -(l / 2) / s -(float) (e * n * s);
							font.drawShadow(st, id, tx, 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
							disableScissor();
						} else {
							drawCenteredString(st, font, id, 0, 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
						}
						st.popPose();
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
