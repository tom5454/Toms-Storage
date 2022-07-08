package com.tom.storagemod.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import com.tom.storagemod.network.IDataReceiver;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.util.RemoteConnections;
import com.tom.storagemod.util.RemoteConnections.Channel;

public class InventoryLinkScreen extends AbstractContainerScreen<InventoryLinkMenu> implements IDataReceiver {
	protected static final ResourceLocation creativeInventoryTabs = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	private static final ResourceLocation gui = new ResourceLocation("toms_storage", "textures/gui/inventory_link.png");
	private static final int LINES = 7;
	private EditBox textF;
	private Map<UUID, Channel> connections = new HashMap<>();
	private UUID selected;
	private int beaconLvl;
	private GuiButton createBtn, deleteBtn, publicBtn, remoteBtn;
	private List<ListEntry> listEntries = new ArrayList<>();
	private List<UUID> sortedList = new ArrayList<>();
	protected float currentScroll;
	protected boolean isScrolling;
	protected boolean wasClicking;

	public InventoryLinkScreen(InventoryLinkMenu p_97741_, Inventory p_97742_, Component p_97743_) {
		super(p_97741_, p_97742_, p_97743_);
	}

	@Override
	public void receive(CompoundTag tag) {
		ListTag list = tag.getList("list", Tag.TAG_COMPOUND);
		RemoteConnections.load(list, connections);
		if(tag.contains("selected")) {
			selected = tag.getUUID("selected");
			Channel ch = connections.get(selected);
			textF.setValue(ch.displayName);
		} else {
			selected = null;
		}
		remoteBtn.state = tag.getBoolean("remote") ? 1 : 0;
		beaconLvl = tag.getInt("lvl");
		Comparator<Entry<UUID, Channel>> cmp = Comparator.comparing(e -> e.getValue().publicChannel);
		cmp = cmp.thenComparing(e -> e.getValue().displayName);
		sortedList = connections.entrySet().stream().sorted(cmp).map(Entry::getKey).collect(Collectors.toList());
		update();
	}

	@Override
	protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, gui);
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void renderLabels(PoseStack p_97808_, int p_97809_, int p_97810_) {
		this.font.draw(p_97808_, this.title, this.titleLabelX, this.titleLabelY, 4210752);
		this.font.draw(p_97808_, I18n.get("ts.inventory_connector.beacon_level", beaconLvl), this.titleLabelX, this.titleLabelY + 10, 4210752);
	}

	public class GuiButton extends Button {
		protected int tile;
		protected int state;
		protected int texX = 176;
		protected int texY = 0;
		public GuiButton(int x, int y, int tile, Runnable pressable) {
			super(x, y, 16, 16, null, b -> pressable.run());
			this.tile = tile;
			addRenderableWidget(this);
		}

		public void setX(int i) {
			x = i;
		}

		/**
		 * Draws this button to the screen.
		 */
		@Override
		public void renderButton(PoseStack st, int mouseX, int mouseY, float pt) {
			if (this.visible) {
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, gui);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				int i = this.getYImage(this.isHoveredOrFocused());
				this.blit(st, this.x, this.y, texX + i * 16, 16, this.width, this.height);
				this.blit(st, this.x, this.y, texX + tile * 16 + state * 16, texY, this.width, this.height);
			}
		}
	}

	@Override
	protected void init() {
		clearWidgets();
		super.init();
		createBtn = new GuiButton(leftPos + 121, topPos + 24, 0, () -> {
			Channel chn = new Channel(null, publicBtn.state == 1, textF.getValue());
			sendEdit(null, chn);
		});
		deleteBtn = new GuiButton(leftPos + 138, topPos + 24, 1, () -> {
			sendEdit(selected, null);
		});
		publicBtn = new GuiButton(leftPos + 155, topPos + 24, 2, () -> {
			if(selected != null && connections.containsKey(selected)) {
				Channel ch = connections.get(selected);
				if(ch.owner.equals(minecraft.player.getUUID())) {
					ch.publicChannel = !ch.publicChannel;
					publicBtn.state = ch.publicChannel ? 1 : 0;
					sendEdit(selected, ch);
				}
			} else
				publicBtn.state = publicBtn.state == 0 ? 1 : 0;
		});
		remoteBtn = new GuiButton(leftPos + 155, topPos + 7, 0, () -> {
			CompoundTag tag = new CompoundTag();
			tag.putBoolean("remote", remoteBtn.state == 0);
			NetworkHandler.sendDataToServer(tag);
		});
		remoteBtn.texY = 32;
		textF = new EditBox(font, leftPos + 13, topPos + 28, 105, font.lineHeight, Component.translatable("narrator.toms_storage.inventory_link_channel"));
		textF.setMaxLength(50);
		textF.setBordered(false);
		textF.setVisible(true);
		textF.setTextColor(16777215);
		textF.setValue("");
		textF.setResponder(t -> {
			selected = null;
			for (Entry<UUID, Channel> e : connections.entrySet()) {
				if(e.getValue().displayName.equals(t)) {
					selected = e.getKey();
					break;
				}
			}
			update();
		});
		addRenderableWidget(textF);
		for(int i = 0;i<LINES;i++) {
			listEntries.add(new ListEntry(leftPos + 12, topPos + 42 + i * 16, i));
		}
		update();
	}

	private void sendEdit(UUID id, Channel ch) {
		CompoundTag tag = new CompoundTag();
		if(id != null)tag.putUUID("id", id);
		if(ch != null)ch.saveNet(tag);
		NetworkHandler.sendDataToServer(tag);
	}

	private void sendSelect(UUID id) {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("id", id);
		tag.putBoolean("select", true);
		NetworkHandler.sendDataToServer(tag);
	}

	private void update() {
		if(selected != null && connections.containsKey(selected)) {
			deleteBtn.active = true;
			Channel ch = connections.get(selected);
			publicBtn.state = ch.publicChannel ? 1 : 0;
			boolean owner = ch.owner.equals(minecraft.player.getUUID());
			publicBtn.active = owner;
			createBtn.active = false;
		} else {
			deleteBtn.active = false;
			publicBtn.state = 0;
			createBtn.active = true;
		}
	}

	public class ListEntry extends Button {
		private int id;

		public ListEntry(int x, int y, int id) {
			super(x, y, 106, 16, null, null);
			this.id = id;
			addRenderableWidget(this);
		}

		/**
		 * Draws this button to the screen.
		 */
		@Override
		public void renderButton(PoseStack st, int mouseX, int mouseY, float pt) {
			if (this.visible) {
				UUID id = getId();
				if(id != null) {
					Channel chn = connections.get(id);
					RenderSystem.setShader(GameRenderer::getPositionTexShader);
					RenderSystem.setShaderTexture(0, gui);
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
					this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
					int i = this.getYImage(this.isHoveredOrFocused());
					this.blit(st, this.x, this.y, id.equals(selected) ? 106 : 0, 166 + i * 16, this.width, this.height);
					this.blit(st, this.x + this.width - 16, this.y, 208 + (chn.publicChannel ? 16 : 0), 0, 16, 16);
					int j = getFGColor();
					drawCenteredString(st, font, chn.displayName, this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
				}
			}
		}

		@Override
		public void onPress() {
			UUID id = getId();
			if(id != null) {
				sendSelect(id);
			}
		}

		private UUID getId() {
			int i = sortedList.size() - LINES;
			int j = (int) (currentScroll * i + 0.5D);
			if (j < 0) {
				j = 0;
			}
			if(this.id + j < sortedList.size()) {
				return sortedList.get(this.id + j);
			}
			return null;
		}
	}

	private boolean needsScrollBars() {
		return sortedList.size() > LINES;
	}

	@Override
	public void render(PoseStack st, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(st);
		boolean flag = GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_RELEASE;
		int i = this.leftPos;
		int j = this.topPos;
		int k = i + 122;
		int l = j + 42;
		int i1 = k + 14;
		int j1 = l + sortedList.size() * 16;

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
		}
		super.render(st, mouseX, mouseY, partialTicks);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, creativeInventoryTabs);
		i = k;
		j = l;
		k = j1;
		this.blit(st, i, j + (int) ((k - j - 17) * this.currentScroll), 232 + (this.needsScrollBars() ? 0 : 12), 0, 12, 15);

		this.renderTooltip(st, mouseX, mouseY);

		if (publicBtn.isHoveredOrFocused()) {
			renderTooltip(st, Component.translatable("tooltip.toms_storage.link_public_" + publicBtn.state), mouseX, mouseY);
		}

		if (remoteBtn.isHoveredOrFocused()) {
			renderTooltip(st, Component.translatable("tooltip.toms_storage.link_remote_" + remoteBtn.state), mouseX, mouseY);
		}
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (p_keyPressed_1_ == 256) {
			this.onClose();
			return true;
		}
		return !this.textF.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) && !this.textF.canConsumeInput() ? super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) : true;
	}

	@Override
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
		if(textF.charTyped(p_charTyped_1_, p_charTyped_2_))return true;
		return super.charTyped(p_charTyped_1_, p_charTyped_2_);
	}

	@Override
	public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_) {
		if (!this.needsScrollBars()) {
			return false;
		} else {
			int i = sortedList.size() - LINES;
			this.currentScroll = (float)(this.currentScroll - p_mouseScrolled_5_ / i);
			this.currentScroll = Mth.clamp(this.currentScroll, 0.0F, 1.0F);
			return true;
		}
	}
}
