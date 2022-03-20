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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.storagemod.NetworkHandler;
import com.tom.storagemod.NetworkHandler.IDataReceiver;
import com.tom.storagemod.util.RemoteConnections;
import com.tom.storagemod.util.RemoteConnections.Channel;

public class GuiInventoryLink extends HandledScreen<ContainerInventoryLink> implements IDataReceiver {
	protected static final Identifier creativeInventoryTabs = new Identifier("textures/gui/container/creative_inventory/tabs.png");
	private static final Identifier gui = new Identifier("toms_storage", "textures/gui/inventory_link.png");
	private static final int LINES = 7;
	private TextFieldWidget textF;
	private Map<UUID, Channel> connections = new HashMap<>();
	private UUID selected;
	private int beaconLvl;
	private GuiButton createBtn, deleteBtn, publicBtn, remoteBtn;
	private List<ListEntry> listEntries = new ArrayList<>();
	private List<UUID> sortedList = new ArrayList<>();
	protected float currentScroll;
	protected boolean isScrolling;
	protected boolean wasClicking;

	public GuiInventoryLink(ContainerInventoryLink p_97741_, PlayerInventory p_97742_, Text p_97743_) {
		super(p_97741_, p_97742_, p_97743_);
	}

	@Override
	public void receive(NbtCompound tag) {
		NbtList list = tag.getList("list", NbtCompound.COMPOUND_TYPE);
		RemoteConnections.load(list, connections);
		if(tag.contains("selected")) {
			selected = tag.getUuid("selected");
			Channel ch = connections.get(selected);
			if(ch != null)
				textF.setText(ch.displayName);
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
	protected void drawBackground(MatrixStack matrixStack, float partialTicks, int x, int y) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, gui);
		int i = (this.width - this.backgroundWidth) / 2;
		int j = (this.height - this.backgroundHeight) / 2;
		this.drawTexture(matrixStack, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
	}

	@Override
	protected void drawForeground(MatrixStack p_97808_, int p_97809_, int p_97810_) {
		this.textRenderer.draw(p_97808_, this.title, this.titleX, this.titleY, 4210752);
		this.textRenderer.draw(p_97808_, I18n.translate("ts.inventory_connector.beacon_level", beaconLvl), this.titleX, this.titleY + 10, 4210752);
	}

	public class GuiButton extends ButtonWidget {
		protected int tile;
		protected int state;
		protected int texX = 176;
		protected int texY = 0;
		public GuiButton(int x, int y, int tile, Runnable pressable) {
			super(x, y, 16, 16, null, b -> pressable.run());
			this.tile = tile;
			addDrawableChild(this);
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
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, gui);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
				this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				int i = this.getYImage(this.isHovered());
				this.drawTexture(st, this.x, this.y, texX + i * 16, 16, this.width, this.height);
				this.drawTexture(st, this.x, this.y, texX + tile * 16 + state * 16, texY, this.width, this.height);
			}
		}
	}

	@Override
	protected void init() {
		clearChildren();
		super.init();
		createBtn = new GuiButton(x + 121, y + 24, 0, () -> {
			Channel chn = new Channel(null, publicBtn.state == 1, textF.getText());
			sendEdit(null, chn);
		});
		deleteBtn = new GuiButton(x + 138, y + 24, 1, () -> {
			sendEdit(selected, null);
		});
		publicBtn = new GuiButton(x + 155, y + 24, 2, () -> {
			if(selected != null && connections.containsKey(selected)) {
				Channel ch = connections.get(selected);
				if(ch.owner.equals(client.player.getUuid())) {
					ch.publicChannel = !ch.publicChannel;
					publicBtn.state = ch.publicChannel ? 1 : 0;
					sendEdit(selected, ch);
				}
			} else
				publicBtn.state = publicBtn.state == 0 ? 1 : 0;
		});
		remoteBtn = new GuiButton(x + 155, y + 7, 0, () -> {
			NbtCompound tag = new NbtCompound();
			tag.putBoolean("remote", remoteBtn.state == 0);
			NetworkHandler.sendToServer(tag);
		});
		remoteBtn.texY = 32;
		textF = new TextFieldWidget(textRenderer, x + 13, y + 28, 105, textRenderer.fontHeight, new TranslatableText("narrator.toms_storage.inventory_link_channel"));
		textF.setMaxLength(50);
		textF.setDrawsBackground(false);
		textF.setVisible(true);
		textF.setEditableColor(16777215);
		textF.setText("");
		textF.setChangedListener(t -> {
			selected = null;
			for (Entry<UUID, Channel> e : connections.entrySet()) {
				if(e.getValue().displayName.equals(t)) {
					selected = e.getKey();
					break;
				}
			}
			update();
		});
		addDrawableChild(textF);
		for(int i = 0;i<LINES;i++) {
			listEntries.add(new ListEntry(x + 12, y + 42 + i * 16, i));
		}
		update();
	}

	private void sendEdit(UUID id, Channel ch) {
		NbtCompound tag = new NbtCompound();
		if(id != null)tag.putUuid("id", id);
		if(ch != null)ch.saveNet(tag);
		NetworkHandler.sendToServer(tag);
	}

	private void sendSelect(UUID id) {
		NbtCompound tag = new NbtCompound();
		tag.putUuid("id", id);
		tag.putBoolean("select", true);
		NetworkHandler.sendToServer(tag);
	}

	private void update() {
		if(selected != null && connections.containsKey(selected)) {
			deleteBtn.active = true;
			Channel ch = connections.get(selected);
			publicBtn.state = ch.publicChannel ? 1 : 0;
			boolean owner = ch.owner.equals(client.player.getUuid());
			publicBtn.active = owner;
			createBtn.active = false;
		} else {
			deleteBtn.active = false;
			publicBtn.state = 0;
			createBtn.active = true;
		}
	}

	public class ListEntry extends ButtonWidget {
		private int id;

		public ListEntry(int x, int y, int id) {
			super(x, y, 106, 16, null, null);
			this.id = id;
			addDrawableChild(this);
		}

		/**
		 * Draws this button to the screen.
		 */
		@Override
		public void renderButton(MatrixStack st, int mouseX, int mouseY, float pt) {
			if (this.visible) {
				UUID id = getId();
				if(id != null) {
					Channel chn = connections.get(id);
					RenderSystem.setShader(GameRenderer::getPositionTexShader);
					RenderSystem.setShaderTexture(0, gui);
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
					this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
					int i = this.getYImage(this.isHovered());
					this.drawTexture(st, this.x, this.y, id.equals(selected) ? 106 : 0, 166 + i * 16, this.width, this.height);
					this.drawTexture(st, this.x + this.width - 16, this.y, 208 + (chn.publicChannel ? 16 : 0), 0, 16, 16);
					drawCenteredText(st, textRenderer, chn.displayName, this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xffffffff | MathHelper.ceil(this.alpha * 255.0F) << 24);
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
	public void render(MatrixStack st, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(st);
		boolean flag = GLFW.glfwGetMouseButton(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_RELEASE;
		int i = this.x;
		int j = this.y;
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
			this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
		}
		super.render(st, mouseX, mouseY, partialTicks);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, creativeInventoryTabs);
		i = k;
		j = l;
		k = j1;
		this.drawTexture(st, i, j + (int) ((k - j - 17) * this.currentScroll), 232 + (this.needsScrollBars() ? 0 : 12), 0, 12, 15);

		this.drawMouseoverTooltip(st, mouseX, mouseY);

		if (publicBtn.isHovered()) {
			renderTooltip(st, new TranslatableText("tooltip.toms_storage.link_public_" + publicBtn.state), mouseX, mouseY);
		}

		if (remoteBtn.isHovered()) {
			renderTooltip(st, new TranslatableText("tooltip.toms_storage.link_remote_" + remoteBtn.state), mouseX, mouseY);
		}
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (p_keyPressed_1_ == 256) {
			this.close();
			return true;
		}
		return !this.textF.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) && !this.textF.isActive() ? super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) : true;
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
			this.currentScroll = MathHelper.clamp(this.currentScroll, 0.0F, 1.0F);
			return true;
		}
	}
}
