package com.tom.storagemod.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.storage.ValueInput;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.menu.InventoryLinkMenu;
import com.tom.storagemod.menu.InventoryLinkMenu.LinkChannel;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.screen.widget.IconButton;
import com.tom.storagemod.screen.widget.ListWidget;
import com.tom.storagemod.screen.widget.ToggleButton;
import com.tom.storagemod.util.IDataReceiver;

public class InventoryLinkScreen extends TSContainerScreen<InventoryLinkMenu> implements IDataReceiver {
	private static final Identifier gui = Identifier.tryBuild(StorageMod.modid, "textures/gui/inventory_link.png");
	private static final Identifier privateChannel = Identifier.tryBuild(StorageMod.modid, "icons/lock_on");
	private static final Identifier publicChannel = Identifier.tryBuild(StorageMod.modid, "icons/lock_off");
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
	public void receive(ValueInput tag) {
		connections.clear();
		tag.listOrEmpty("list", LinkChannel.CODEC.codec()).stream().forEach(l -> {
			connections.put(l.id(), l);
		});
		tag.read("selected", UUIDUtil.CODEC).ifPresentOrElse(sel -> {
			var selC = connections.get(sel);
			if (selC != null) {
				channelsList.setSelected(selC);
				textF.setValue(selC.displayName());
			} else {
				channelsList.setSelected(null);
			}
		}, () -> {
			channelsList.setSelected(null);
		});
		Comparator<LinkChannel> cmp = Comparator.comparing(LinkChannel::publicChannel);
		cmp = cmp.thenComparing(LinkChannel::displayName);
		sortedList = connections.values().stream().sorted(cmp).collect(Collectors.toList());
		update();
	}

	@Override
	protected void renderBg(GuiGraphics matrixStack, float partialTicks, int x, int y) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		matrixStack.blit(RenderPipelines.GUI_TEXTURED, gui, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	protected void renderLabels(GuiGraphics st, int p_97809_, int p_97810_) {
		st.drawString(font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
		st.drawString(font, I18n.get("label.toms_storage.inventory_connector.beacon_level", menu.beaconLvl), this.titleLabelX, this.titleLabelY + 10, 0xFF404040, false);
	}

	@Override
	protected void init() {
		clearWidgets();
		super.init();
		createBtn = addRenderableWidget(new IconButton(leftPos + 121, topPos + 24, Component.translatable(""), Identifier.tryBuild(StorageMod.modid, "icons/add"), () -> {
			String name = textF.getValue().trim();
			if (!name.isEmpty()) {
				sendEdit(null, new LinkChannel(publicBtn.getState(), name));
			}
		}));
		deleteBtn = addRenderableWidget(new IconButton(leftPos + 138, topPos + 24, Component.translatable(""), Identifier.tryBuild(StorageMod.modid, "icons/deny"), () -> {
			sendEdit(channelsList.getSelected(), null);
		}));
		publicBtn = addRenderableWidget(ToggleButton.builder(leftPos + 155, topPos + 24).
				iconOff(privateChannel).
				iconOn(publicChannel).
				build(s -> {
					var sel = channelsList.getSelected();
					if(sel != null && sel.owner().equals(minecraft.player.getUUID())) {
						sendEdit(sel, new LinkChannel(s, sel.displayName()));
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
		textF.setTextColor(0xFFFFFFFF);
		textF.setValue("");
		textF.setResponder(t -> {
			channelsList.setSelected(null);
			for (Entry<UUID, LinkChannel> e : connections.entrySet()) {
				if(e.getValue().displayName().equals(t)) {
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
		if(id != null)tag.store("id", UUIDUtil.CODEC, id.id());
		if(ch != null)ch.saveToServer(tag);
		NetworkHandler.sendDataToServer(tag);
	}

	private void sendSelect(UUID id) {
		CompoundTag tag = new CompoundTag();
		tag.store("id", UUIDUtil.CODEC, id);
		tag.putBoolean("select", true);
		NetworkHandler.sendDataToServer(tag);
	}

	private void update() {
		var sel = channelsList.getSelected();
		if(sel != null) {
			deleteBtn.active = true;
			publicBtn.setState(sel.publicChannel());
			boolean owner = sel.owner().equals(minecraft.player.getUUID());
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
	public boolean keyPressed(KeyEvent keyEvent) {
		if (keyEvent.key() == 256) {
			this.onClose();
			return true;
		}
		if(keyEvent.key() == GLFW.GLFW_KEY_TAB)return super.keyPressed(keyEvent);
		return !this.textF.keyPressed(keyEvent) && !this.textF.canConsumeInput() ? super.keyPressed(keyEvent) : true;
	}

	@Override
	public boolean charTyped(CharacterEvent characterEvent) {
		if(textF.charTyped(characterEvent))return true;
		return super.charTyped(characterEvent);
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
			return Component.literal(data.displayName());
		}

		@Override
		protected void renderTooltip(GuiGraphics graphics, LinkChannel data, int mouseX, int mouseY) {
			List<Component> tt = new ArrayList<>();
			if (!data.ownerName().isEmpty()) {
				boolean owner = data.owner().equals(minecraft.player.getUUID());
				tt.add(Component.translatable("tooltip.toms_storage.inventory_connector.channel.owner" + (owner ? ".self" : ""), data.ownerName()));
			} else {
				tt.add(Component.translatable("tooltip.toms_storage.inventory_connector.channel.owner.unknown"));
			}
			tt.add(Component.translatable("tooltip.toms_storage.inv_link." + (data.publicChannel() ? "public" : "private")));

			graphics.setComponentTooltipForNextFrame(font, tt, mouseX, mouseY);
		}

		@Override
		protected void renderEntry(GuiGraphics st, int x, int y, int width, int height, LinkChannel id, int mouseX,
				int mouseY, float pt) {
			st.blitSprite(RenderPipelines.GUI_TEXTURED, id.publicChannel() ? publicChannel : privateChannel, x + width - 16, y, 16, 16);
		}

		@Override
		protected void selectionChanged(LinkChannel to) {
			sendSelect(to.id());
			update();
		}
	}

	@Override
	public void getExclusionAreas(Consumer<Box> consumer) {
	}
}
