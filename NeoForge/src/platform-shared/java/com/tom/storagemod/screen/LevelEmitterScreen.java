package com.tom.storagemod.screen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.client.ClientUtil;
import com.tom.storagemod.menu.LevelEmitterMenu;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.screen.widget.ToggleButton;

public class LevelEmitterScreen extends AbstractFilteredScreen<LevelEmitterMenu> {
	private static final ResourceLocation gui = ResourceLocation.tryBuild(StorageMod.modid, "textures/gui/level_emitter.png");
	private ToggleButton lessThanBtn;
	private EditBox textF;
	private List<AmountBtn> amountBtns = new ArrayList<>();

	public LevelEmitterScreen(LevelEmitterMenu screenContainer, Inventory inv, Component titleIn) {
		super(screenContainer, inv, titleIn);
		menu.onPacket = this::receive;
	}

	@Override
	public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack, mouseX, mouseY, partialTicks);
		amountBtns.forEach(AmountBtn::update);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics matrixStack, float partialTicks, int x, int y) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		matrixStack.blit(gui, i, j, 0, 0, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void init() {
		clearWidgets();
		amountBtns.clear();
		super.init();
		textF = new EditBox(font, leftPos + 70, topPos + 41, 89, font.lineHeight, Component.translatable("narrator.toms_storage.level_emitter_amount"));
		textF.setMaxLength(100);
		textF.setBordered(false);
		textF.setVisible(true);
		textF.setTextColor(16777215);
		textF.setValue(Integer.toString(menu.count));
		textF.setResponder(t -> {
			try {
				int c = Integer.parseInt(t);
				if(c >= 0) {
					menu.count = c;
					send();
				}
			} catch (NumberFormatException e) {
			}
		});
		addRenderableWidget(textF);
		lessThanBtn = ToggleButton.builder(leftPos - 18, topPos + 5).
				iconOff(ResourceLocation.tryBuild(StorageMod.modid, "icons/greater_than")).
				iconOn(ResourceLocation.tryBuild(StorageMod.modid, "icons/less_than")).
				build(s -> {
					lessThanBtn.setState(s);
					menu.lessThan = s;
					send();
				});
		lessThanBtn.setTooltip(Tooltip.create(ClientUtil.multilineTooltip("tooltip.toms_storage.level_emitter.greater_than")), Tooltip.create(ClientUtil.multilineTooltip("tooltip.toms_storage.level_emitter.less_than")));
		lessThanBtn.setState(menu.lessThan);
		addRenderableWidget(lessThanBtn);
		amountBtns.add(new AmountBtn( 20, 0,    1,  1, 20));
		amountBtns.add(new AmountBtn( 45, 0,   10, 16, 25));
		amountBtns.add(new AmountBtn( 75, 0,  100, 32, 30));
		amountBtns.add(new AmountBtn(110, 0, 1000, 64, 35));

		amountBtns.add(new AmountBtn( 20, 40,    -1,  -1, 20));
		amountBtns.add(new AmountBtn( 45, 40,   -10, -16, 25));
		amountBtns.add(new AmountBtn( 75, 40,  -100, -32, 30));
		amountBtns.add(new AmountBtn(110, 40, -1000, -64, 35));
	}

	private void receive() {
		lessThanBtn.setState(menu.lessThan);
		textF.setValue(Integer.toString(menu.count));
	}

	private void send() {
		CompoundTag mainTag = new CompoundTag();
		mainTag.putInt("count", menu.count);
		mainTag.putBoolean("lessThan", menu.lessThan);
		NetworkHandler.sendDataToServer(mainTag);
	}

	private class AmountBtn {
		private Button btn;
		private int v, sv;
		public AmountBtn(int x, int y, int v, int sv, int len) {
			btn = Button.builder(Component.literal((v > 0 ? "+" : "") + v), this::evt).bounds(leftPos + x, topPos + y + 16, len, 20).build();
			addRenderableWidget(btn);
			this.v = v;
			this.sv = sv;
		}

		private void evt(Button b) {
			menu.count += hasShiftDown() ? sv : v;
			if(menu.count < 0)menu.count = 0;
			textF.setValue(Integer.toString(menu.count));
			send();
		}

		private void update() {
			if(hasShiftDown()) {
				btn.setMessage(Component.literal((sv > 0 ? "+" : "") + sv));
			} else {
				btn.setMessage(Component.literal((v > 0 ? "+" : "") + v));
			}
		}
	}
}
