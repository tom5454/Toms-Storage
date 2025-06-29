package com.tom.storagemod.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.menu.InventoryLinkMenu;
import com.tom.storagemod.menu.InventoryLinkMenu.LinkChannel;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.screen.widget.IconButton;
import com.tom.storagemod.screen.widget.ListWidget;
import com.tom.storagemod.screen.widget.ToggleButton;
import com.tom.storagemod.util.IDataReceiver;

public class InventoryLinkScreen extends TSContainerScreen<InventoryLinkMenu> implements IDataReceiver {
	private static final ResourceLocation gui = ResourceLocation.tryBuild(StorageMod.modid, "textures/gui/inventory_link.png");
	private static final ResourceLocation privateChannel = ResourceLocation.tryBuild(StorageMod.modid, "icons/lock_on");
	private static final ResourceLocation publicChannel = ResourceLocation.tryBuild(StorageMod.modid, "icons/lock_off");
	private EditBox textF;
	private Map<UUID, LinkChannel> connections = new HashMap<>();
	private IconButton createBtn, deleteBtn;
	private ToggleButton publicBtn;
	private List<LinkChannel> sortedList = new ArrayList<>();
	private ListHandler channelsList;
	protected float currentScroll;
	protected boolean isScrolling;
	protected boolean wasClicking;

	public InventoryLinkScreen(InventoryLinkMenu p_97741_, Inventory p_97742_, Component p_97743_) {
		super(p_97741_, p_97742_, p_97743_);
	}

	@Override
	public void receive(CompoundTag tag) {
		ListTag list = tag.getList("list", Tag.TAG_COMPOUND);
		LinkChannel.loadAll(list, connections);
		if(tag.contains("selected")) {
			var sel = tag.getUUID("selected");
			var selC = connections.get(sel);
			if (selC != null) {
				channelsList.setSelected(selC);
				textF.setValue(selC.displayName);
			} else {
				channelsList.setSelected(null);
			}
		} else {
			channelsList.setSelected(null);
		}
		Comparator<LinkChannel> cmp = Comparator.comparing(e -> e.publicChannel);
		cmp = cmp.thenComparing(e -> e.displayName);
		sortedList = connections.values().stream().sorted(cmp).collect(Collectors.toList());
		update();
	}

	@Override
	protected void renderBg(GuiGraphics matrixStack, float partialTicks, int x, int y) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		matrixStack.blit(RenderType::guiTextured, gui, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	protected void renderLabels(GuiGraphics st, int p_97809_, int p_97810_) {
		st.drawString(font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
		st.drawString(font, I18n.get("label.toms_storage.inventory_connector.beacon_level", menu.beaconLvl), this.titleLabelX, this.titleLabelY + 10, 4210752, false);
	}

	@Override
	protected void init() {
		clearWidgets();
		super.init();
		createBtn = addRenderableWidget(new IconButton(leftPos + 121, topPos + 24, Component.translatable(""), ResourceLocation.tryBuild(StorageMod.modid, "icons/add"), b -> {
			String name = textF.getValue().trim();
			if (!name.isEmpty()) {
				sendEdit(null, new LinkChannel(publicBtn.getState(), name));
			}
		}));
		deleteBtn = addRenderableWidget(new IconButton(leftPos + 138, topPos + 24, Component.translatable(""), ResourceLocation.tryBuild(StorageMod.modid, "icons/deny"), b -> {
			sendEdit(channelsList.getSelected(), null);
		}));
		publicBtn = addRenderableWidget(ToggleButton.builder(leftPos + 155, topPos + 24).
				iconOff(privateChannel).
				iconOn(publicChannel).
				build(s -> {
					var sel = channelsList.getSelected();
					if(sel != null && sel.owner.equals(minecraft.player.getUUID())) {
						sel.publicChannel = s;
						sendEdit(sel, sel);
					}
				}));
		publicBtn.setTooltip(Tooltip.create(Component.translatable("tooltip.toms_storage.inv_link.private")), Tooltip.create(Component.translatable("tooltip.toms_storage.inv_link.public")));

		channelsList = new ListHandler(leftPos + 12, topPos + 42);
		channelsList.setList(() -> sortedList);
		addRenderableWidget(channelsList);

		textF = new EditBox(font, leftPos + 13, topPos + 28, 105, font.lineHeight, Component.translatable("narrator.toms_storage.inventory_link_channel"));
		textF.setMaxLength(50);
		textF.setBordered(false);
		textF.setVisible(true);
		textF.setTextColor(16777215);
		textF.setValue("");
		textF.setResponder(t -> {
			channelsList.setSelected(null);
			for (Entry<UUID, LinkChannel> e : connections.entrySet()) {
				if(e.getValue().displayName.equals(t)) {
					channelsList.setSelected(e.getValue());
					break;
				}
			}
			update();
		});
		addRenderableWidget(textF);
		update();
	}

	private void sendEdit(LinkChannel id, LinkChannel ch) {
		CompoundTag tag = new CompoundTag();
		if(id != null)tag.putUUID("id", id.id);
		if(ch != null)ch.saveToServer(tag);
		NetworkHandler.sendDataToServer(tag);
	}

	private void sendSelect(UUID id) {
		CompoundTag tag = new CompoundTag();
		tag.putUUID("id", id);
		tag.putBoolean("select", true);
		NetworkHandler.sendDataToServer(tag);
	}

	private void update() {
		var sel = channelsList.getSelected();
		if(sel != null) {
			deleteBtn.active = true;
			publicBtn.setState(sel.publicChannel);
			boolean owner = sel.owner.equals(minecraft.player.getUUID());
			publicBtn.active = owner;
			createBtn.active = false;
		} else {
			deleteBtn.active = false;
			publicBtn.setState(false);
			createBtn.active = !textF.getValue().trim().isEmpty();
		}
	}

	@Override
	public void render(GuiGraphics st, int mouseX, int mouseY, float partialTicks) {
		channelsList.preRender(mouseX, mouseY);
		super.render(st, mouseX, mouseY, partialTicks);
		this.renderTooltip(st, mouseX, mouseY);
		channelsList.tooltip(st, mouseX, mouseY);
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (p_keyPressed_1_ == 256) {
			this.onClose();
			return true;
		}
		if(p_keyPressed_1_ == GLFW.GLFW_KEY_TAB)return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
		return !this.textF.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) && !this.textF.canConsumeInput() ? super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) : true;
	}

	@Override
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
		if(textF.charTyped(p_charTyped_1_, p_charTyped_2_))return true;
		return super.charTyped(p_charTyped_1_, p_charTyped_2_);
	}

	public class ListHandler extends ListWidget<LinkChannel> {

		public ListHandler(int x, int y) {
			super(x, y, 114, 100, 16, Component.empty());
		}

		@Override
		protected Font getFont() {
			return font;
		}

		@Override
		protected void addButton(AbstractWidget btn) {
			addRenderableWidget(btn);
		}

		@Override
		protected Component toComponent(LinkChannel data) {
			return Component.literal(data.displayName);
		}

		@Override
		protected void renderTooltip(GuiGraphics graphics, LinkChannel data, int mouseX, int mouseY) {
			List<Component> tt = new ArrayList<>();
			if (!data.ownerName.isEmpty()) {
				boolean owner = data.owner.equals(minecraft.player.getUUID());
				tt.add(Component.translatable("tooltip.toms_storage.inventory_connector.channel.owner" + (owner ? ".self" : ""), data.ownerName));
			} else {
				tt.add(Component.translatable("tooltip.toms_storage.inventory_connector.channel.owner.unknown"));
			}
			tt.add(Component.translatable("tooltip.toms_storage.inv_link." + (data.publicChannel ? "public" : "private")));

			graphics.renderTooltip(font, tt, Optional.empty(), mouseX, mouseY);
		}

		@Override
		protected void renderEntry(GuiGraphics st, int x, int y, int width, int height, LinkChannel id, int mouseX,
				int mouseY, float pt) {
			st.blitSprite(RenderType::guiTextured, id.publicChannel ? publicChannel : privateChannel, x + width - 16, y, 16, 16);
		}

		@Override
		protected void selectionChanged(LinkChannel to) {
			sendSelect(to.id);
			update();
		}
	}

	@Override
	public void getExclusionAreas(Consumer<Box> consumer) {
	}
}
