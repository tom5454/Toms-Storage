package com.tom.storagemod.screen;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.menu.FilingCabinetMenu;

public class FilingCabinetScreen extends PlatformContainerScreen<FilingCabinetMenu> {
	private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.parse("textures/gui/container/generic_54.png");
	private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.parse("container/creative_inventory/scroller");
	private static final ResourceLocation SIDE_SCROLLBAR = ResourceLocation.tryBuild(StorageMod.modid, "textures/gui/side_scrollbar.png");
	private final int containerRows;
	private int lastScroll;
	protected float currentScroll;
	protected boolean isScrolling;
	protected boolean wasClicking;

	public FilingCabinetScreen(FilingCabinetMenu inv, Inventory p_97742_, Component p_97743_) {
		super(inv, p_97742_, p_97743_);
		this.containerRows = inv.getRowCount();
		this.imageHeight = 114 + this.containerRows * 18;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	public void render(GuiGraphics st, int mouseX, int mouseY, float p_281873_) {
		boolean flag = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_RELEASE;
		int i = this.leftPos;
		int j = this.topPos;
		int k = i + 174;
		int l = j + 18;
		int i1 = k + 14;
		int j1 = l + containerRows * 18;

		if (!this.wasClicking && flag && mouseX >= k && mouseY >= l && mouseX < i1 && mouseY < j1) {
			this.isScrolling = true;
		}

		if (!flag) {
			this.isScrolling = false;
		}
		this.wasClicking = flag;

		if (this.isScrolling) {
			this.currentScroll = (mouseY - l - 7.5F) / (j1 - l - 15.0F);
			this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);
		}
		super.render(st, mouseX, mouseY, p_281873_);
		i = k;
		j = l;
		k = j1;
		st.blitSprite(RenderType::guiTextured, SCROLLER_SPRITE, i, j + (int) ((k - j - 17) * this.currentScroll), 12, 15);
		this.renderTooltip(st, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics st, float p_282334_, int p_282603_, int p_282158_) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		st.blit(RenderType::guiTextured, CONTAINER_BACKGROUND, i, j, 0, 0, this.imageWidth, this.containerRows * 18 + 17, 256, 256);
		st.blit(RenderType::guiTextured, CONTAINER_BACKGROUND, i, j + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96, 256, 256);
		st.blit(RenderType::guiTextured, SIDE_SCROLLBAR, i + 170, j, 0, 0, 24, 115, 24, 115, 256, 256);
	}

	@Override
	protected void containerTick() {
		int i = (this.menu.getContainerSize() + 9 - 1) / 9 - containerRows;
		int scroll = (int) (currentScroll * i + 0.5D);

		if (lastScroll != scroll) {
			scroll(scroll);
			this.lastScroll = scroll;
		}
	}

	private void scroll(int id) {
		getMenu().setRow(id);
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
	}

	@Override
	public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double xd, double p_mouseScrolled_5_) {
		int i = (this.menu.getContainerSize() + 9 - 1) / 9 - 5;
		this.currentScroll = (float)(this.currentScroll - p_mouseScrolled_5_ / i);
		this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);
		return true;
	}
}
