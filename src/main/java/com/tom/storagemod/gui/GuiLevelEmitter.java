package com.tom.storagemod.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import com.tom.storagemod.NetworkHandler;
import com.tom.storagemod.NetworkHandler.IDataReceiver;

public class GuiLevelEmitter extends HandledScreen<ContainerLevelEmitter> implements IDataReceiver {
	private static final Identifier gui = new Identifier("toms_storage", "textures/gui/level_emitter.png");
	private GuiButton lessThanBtn;
	private TextFieldWidget textF;
	private boolean lt;
	private int count = 1;
	private List<AmountBtn> amountBtns = new ArrayList<>();

	public GuiLevelEmitter(ContainerLevelEmitter screenContainer, PlayerInventory inv, Text titleIn) {
		super(screenContainer, inv, titleIn);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		amountBtns.forEach(AmountBtn::update);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.drawMouseoverTooltip(matrixStack, mouseX, mouseY);
		if(lessThanBtn.isHovered()) {
			renderTooltip(matrixStack, Arrays.stream(I18n.translate("tooltip.toms_storage.lvlEm_lt_" + lessThanBtn.state).split("\\\\")).map(LiteralText::new).collect(Collectors.toList()), mouseX, mouseY);
		}
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
	protected void init() {
		clearChildren();
		amountBtns.clear();
		super.init();
		textF = new TextFieldWidget(textRenderer, x + 70, y + 41, 89, textRenderer.fontHeight, new TranslatableText("narrator.toms_storage.level_emitter_amount"));
		textF.setMaxLength(100);
		textF.setDrawsBackground(false);
		textF.setVisible(true);
		textF.setEditableColor(16777215);
		textF.setText(Integer.toString(count));
		textF.setChangedListener(t -> {
			try {
				int c = Integer.parseInt(t);
				if(c >= 1) {
					count = c;
					send();
				}
			} catch (NumberFormatException e) {
			}
		});
		addDrawableChild(textF);
		lessThanBtn = new GuiButton(x - 18, y + 5, 0, b -> {
			lt = !lt;
			lessThanBtn.state = lt ? 1 : 0;
			send();
		});
		lessThanBtn.state = lt ? 1 : 0;
		addDrawableChild(lessThanBtn);
		amountBtns.add(new AmountBtn( 20, 0,    1,  1, 20));
		amountBtns.add(new AmountBtn( 45, 0,   10, 16, 25));
		amountBtns.add(new AmountBtn( 75, 0,  100, 32, 30));
		amountBtns.add(new AmountBtn(110, 0, 1000, 64, 35));

		amountBtns.add(new AmountBtn( 20, 40,    -1,  -1, 20));
		amountBtns.add(new AmountBtn( 45, 40,   -10, -16, 25));
		amountBtns.add(new AmountBtn( 75, 40,  -100, -32, 30));
		amountBtns.add(new AmountBtn(110, 40, -1000, -64, 35));
	}

	@Override
	public void receive(NbtCompound tag) {
		count = tag.getInt("count");
		boolean lt = tag.getBoolean("lessThan");
		lessThanBtn.state = lt ? 1 : 0;
		this.lt = lt;
		textF.setText(Integer.toString(count));
	}

	public class GuiButton extends ButtonWidget {
		protected int tile;
		protected int state;
		protected int texX = 176;
		protected int texY = 0;
		public GuiButton(int x, int y, int tile, PressAction pressable) {
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
				this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, gui);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
				//int i = this.getYImage(this.isHovered);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
				this.drawTexture(st, this.x, this.y, texX + state * 16, texY + tile * 16, this.width, this.height);
			}
		}
	}

	private void send() {
		NbtCompound mainTag = new NbtCompound();
		mainTag.putInt("count", count);
		mainTag.putBoolean("lessThan", lt);
		NetworkHandler.sendToServer(mainTag);
	}

	private class AmountBtn {
		private ButtonWidget btn;
		private int v, sv;
		public AmountBtn(int x, int y, int v, int sv, int len) {
			btn = new ButtonWidget(GuiLevelEmitter.this.x + x, GuiLevelEmitter.this.y + y + 16, len, 20, new LiteralText((v > 0 ? "+" : "") + v), this::evt);
			addDrawableChild(btn);
			this.v = v;
			this.sv = sv;
		}

		private void evt(ButtonWidget b) {
			count += hasShiftDown() ? sv : v;
			if(count < 1)count = 1;
			textF.setText(Integer.toString(count));
			send();
		}

		private void update() {
			if(hasShiftDown()) {
				btn.setMessage(new LiteralText((sv > 0 ? "+" : "") + sv));
			} else {
				btn.setMessage(new LiteralText((v > 0 ? "+" : "") + v));
			}
		}
	}
}
