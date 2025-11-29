package com.tom.storagemod.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;

import com.mojang.serialization.Codec;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.menu.TagItemFilterMenu;
import com.tom.storagemod.network.NetworkHandler;
import com.tom.storagemod.screen.widget.IconButton;
import com.tom.storagemod.screen.widget.ListWidget;
import com.tom.storagemod.screen.widget.ToggleButton;
import com.tom.storagemod.util.IDataReceiver;

public class TagItemFilterScreen extends AbstractFilteredScreen<TagItemFilterMenu> implements IDataReceiver {
	private static final ResourceLocation GUI_TEXTURES = ResourceLocation.tryBuild(StorageMod.modid, "textures/gui/tag_filter.png");
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

		itemTagList = addRenderableWidget(new ListHandler(leftPos + 28, topPos + 15));
		itemTagList.setList(() -> itemTags);
		filterList = addRenderableWidget(new ListHandler(leftPos + 109, topPos + 15));
		filterList.setList(() -> filterTags);

		buttonAdd = addRenderableWidget(new IconButton(leftPos + 90, topPos + 14, Component.translatable(""), ResourceLocation.tryBuild(StorageMod.modid, "icons/add"), b -> {
			String sel = itemTagList.getSelected();
			if(sel != null) {
				if(!filterTags.contains(sel))
					filterTags.add(sel);
				itemTagList.setSelected(null);
				sync();
			}
		}));

		buttonRemove = addRenderableWidget(new IconButton(leftPos + 90, topPos + 32, Component.translatable(""), ResourceLocation.tryBuild(StorageMod.modid, "icons/deny"), b -> {
			String sel = filterList.getSelected();
			if(sel != null) {
				filterTags.remove(sel);
				filterList.setSelected(null);
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
			itemTagList.setSelected(null);
			itemTagList.setCurrentScroll(0f);
		}
	}

	private void click(int id, boolean val) {
		this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, (id << 1) | (val ? 1 : 0));
	}

	@Override
	public void render(GuiGraphics matrixStack, int mouseX, int mouseY, float partialTicks) {
		buttonAllowList.setState(menu.allowList);
		itemTagList.preRender(mouseX, mouseY);
		filterList.preRender(mouseX, mouseY);
		buttonAdd.active = itemTagList.getSelected() != null;
		buttonRemove.active = filterList.getSelected() != null;
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.renderTooltip(matrixStack, mouseX, mouseY);

		itemTagList.tooltip(matrixStack, mouseX, mouseY);
		filterList.tooltip(matrixStack, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics matrixStack, float partialTicks, int x, int y) {
		int i = (this.width - this.imageWidth) / 2;
		int j = (this.height - this.imageHeight) / 2;
		matrixStack.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
	}

	@Override
	public void receive(ValueInput tag) {
		filterTags.clear();
		filterTags.addAll(tag.listOrEmpty("l", Codec.string(0, 256)).stream().toList());
		filterList.setSelected(null);
		filterList.setCurrentScroll(0f);
	}

	public class ListHandler extends ListWidget<String> {

		public ListHandler(int x, int y) {
			super(x, y, 61, 58, 14, Component.empty());
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
		protected Component toComponent(String data) {
			return Component.literal(data);
		}

		@Override
		protected void renderTooltip(GuiGraphics graphics, String data, int mouseX, int mouseY) {
			graphics.setTooltipForNextFrame(font, Component.literal(data), mouseX, mouseY);
		}
	}

	@Override
	public void getExclusionAreas(Consumer<Box> consumer) {
		consumer.accept(new Box(leftPos - 20, topPos, 25, 25));
	}
}
