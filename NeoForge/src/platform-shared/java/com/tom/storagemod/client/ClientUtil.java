package com.tom.storagemod.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.tom.storagemod.Config;
import com.tom.storagemod.Content;
import com.tom.storagemod.StorageTags;
import com.tom.storagemod.block.IConfiguratorHighlight;
import com.tom.storagemod.item.WirelessTerminalItem;
import com.tom.storagemod.platform.Platform;
import com.tom.storagemod.util.ComponentJoiner;

public class ClientUtil {

	public static void tooltip(String key, List<Component> tooltip, Object... args) {
		tooltip(key, true, tooltip, args);
	}

	public static void tooltip(String key, boolean addShift, List<Component> tooltip, Object... args) {
		if(Screen.hasShiftDown()) {
			String[] sp = Component.translatable("tooltip.toms_storage." + key, args).getString().split("\\\\");
			for (int i = 0; i < sp.length; i++) {
				tooltip.add(Component.literal(sp[i]));
			}
		} else if(addShift) {
			tooltip.add(Component.translatable("tooltip.toms_storage.hold_shift_for_info").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
		}
	}

	private static Component[] tooltipExt = new Component[0];
	public static void setTooltip(Component... string) {
		tooltipExt = string;
	}

	public static void collectExtraTooltips(ItemStack stack, List<Component> toolip) {
		Collections.addAll(toolip, tooltipExt);
	}

	public static void drawTerminalOutline(PoseStack ps) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null)
			return;

		if (!WirelessTerminalItem.isPlayerHolding(player))
			return;

		BlockHitResult lookingAt = rayTrace(player, Config.get().wirelessRange, true);
		BlockState state = mc.level.getBlockState(lookingAt.getBlockPos());
		if(state.is(StorageTags.REMOTE_ACTIVATE)) {
			BlockPos pos = lookingAt.getBlockPos();
			Vec3 renderPos = mc.gameRenderer.getMainCamera().getPosition();
			VertexConsumer buf = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
			drawShape(ps, buf, state.getOcclusionShape(player.level(), pos), pos.getX() - renderPos.x, pos.getY() - renderPos.y, pos.getZ() - renderPos.z, 1, 1, 1, 0.4f);
			mc.renderBuffers().bufferSource().endBatch(RenderType.lines());
		}
	}

	private static BlockHitResult rayTrace(Player player, double maxDist, boolean hitFluids) {
		if (Platform.vivecraft) {
			var vr = ViveCraftHelper.rayTraceVR(maxDist, hitFluids);
			if (vr != null)return (BlockHitResult) vr;
		}
		return (BlockHitResult) player.pick(maxDist, 0f, hitFluids);
	}

	public static void drawConfiguratorOutline(PoseStack ps) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null)
			return;

		ItemStack is = player.getItemInHand(InteractionHand.MAIN_HAND);
		if (!is.is(Content.invConfig.get()))is = player.getItemInHand(InteractionHand.OFF_HAND);
		if (!is.is(Content.invConfig.get()))return;

		var c = is.get(Content.configuratorComponent.get());
		if(!c.selecting() && !c.showInvBox()) {
			drawPaintedHighlights(ps);
			return;
		}

		Vec3 renderPos = mc.gameRenderer.getMainCamera().getPosition();
		VertexConsumer buf = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());

		for (var pos : c.selection()) {
			double x = pos.getX() - renderPos.x;
			double y = pos.getY() - renderPos.y;
			double z = pos.getZ() - renderPos.z;

			LevelRenderer.renderLineBox(ps, buf, x, y, z, x + 1, y + 1, z + 1, 1, 1, 1, 0.4f);
		}

		VertexConsumer bufNd = mc.renderBuffers().bufferSource().getBuffer(CustomRenderTypes.linesNoDepth());

		if (c.massSelect() && mc.hitResult instanceof BlockHitResult hr) {
			int sx = c.boxStart().getX();
			int sy = c.boxStart().getY();
			int sz = c.boxStart().getZ();

			AABB bb = AABB.encapsulatingFullBlocks(new BlockPos(sx, sy, sz), hr.getBlockPos());
			bb = bb.move(-renderPos.x, -renderPos.y, -renderPos.z);
			LevelRenderer.renderLineBox(ps, bufNd, bb, 1, 1, 0, 0.4f);
		}

		if (c.isBound()) {
			double x = c.bound().getX() - renderPos.x;
			double y = c.bound().getY() - renderPos.y;
			double z = c.bound().getZ() - renderPos.z;

			LevelRenderer.renderLineBox(ps, bufNd, x, y, z, x + 1, y + 1, z + 1, 1f, 0, 0, 1f);
		}

		mc.renderBuffers().bufferSource().endBatch();
	}

	private static void drawPaintedHighlights(PoseStack ps) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		VertexConsumer buf = mc.renderBuffers().bufferSource().getBuffer(CustomRenderTypes.linesNoDepth());
		Vec3 renderPos = mc.gameRenderer.getMainCamera().getPosition();

		BlockPos.betweenClosedStream(new AABB(player.blockPosition()).inflate(7)).forEach(pos -> {
			double dist = renderPos.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
			if (dist > 49)return;

			var state = mc.level.getBlockState(pos);
			if (state.getBlock() instanceof IConfiguratorHighlight h) {
				var shape = h.getHighlightShape(state, mc.level, pos);
				int color = h.getHighlightColor();
				float a = 1f;
				if (dist > 25) {
					a = 1f - ((float) ((dist - 25) / 24f));
				}

				drawShape(ps, buf, shape,
						pos.getX() - renderPos.x, pos.getY() - renderPos.y, pos.getZ() - renderPos.z,
						FastColor.ARGB32.red(color) / 255f, FastColor.ARGB32.green(color) / 255f, FastColor.ARGB32.blue(color) / 255f,
						a);
			}
		});
	}

	private static void drawShape(PoseStack matrices, VertexConsumer vertexConsumer, VoxelShape voxelShape, double d, double e, double f, float g, float h, float i, float j) {
		PoseStack.Pose entry = matrices.last();
		voxelShape.forAllEdges((k, l, m, n, o, p) -> {
			float q = (float)(n - k);
			float r = (float)(o - l);
			float s = (float)(p - m);
			float t = Mth.sqrt(q * q + r * r + s * s);
			q /= t;
			r /= t;
			s /= t;
			vertexConsumer.addVertex(entry.pose(), (float)(k + d), (float)(l + e), (float)(m + f)).setColor(g, h, i, j).setNormal(entry, q, r, s);
			vertexConsumer.addVertex(entry.pose(), (float)(n + d), (float)(o + e), (float)(p + f)).setColor(g, h, i, j).setNormal(entry, q, r, s);
		});
	}

	public static Component multilineTooltip(String text, Object... objects) {
		return Arrays.stream(I18n.get(text, objects).split("\\\\")).map(Component::literal).collect(ComponentJoiner.joining(Component.empty(), Component.literal("\n")));
	}

	public static void drawConfiguratorOverlay(GuiGraphics gr) {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		if (player == null || mc.screen != null)
			return;

		ItemStack is = player.getItemInHand(InteractionHand.MAIN_HAND);
		if (!is.is(Content.invConfig.get()))is = player.getItemInHand(InteractionHand.OFF_HAND);
		if (!is.is(Content.invConfig.get()))return;

		List<Component> messages = new ArrayList<>();
		Component leftClk = Component.keybind("key.attack").withStyle(ChatFormatting.GREEN);
		Component rightClk = Component.keybind("key.use").withStyle(ChatFormatting.GREEN);
		Component shift = Component.keybind("key.sneak").withStyle(ChatFormatting.GREEN);

		var c = is.get(Content.configuratorComponent.get());
		if (c.isBound()) {
			int x = c.bound().getX();
			int y = c.bound().getY();
			int z = c.bound().getZ();
			BlockPos pos = new BlockPos(x, y, z);
			if (mc.player.distanceToSqr(x, y, z) < 64*64) {
				Component lookingAt = null;
				if (mc.hitResult != null) {
					var item = Platform.getCloneItemStack(mc.level, mc.hitResult, pos, player);
					if (!item.isEmpty()) {
						lookingAt = item.getHoverName();
					}
				}
				if (lookingAt == null)lookingAt = Component.literal("?");
				if (mc.showOnlyReducedInfo()) {
					messages.add(Component.translatable("tooltip.toms_storage.config_overlay.block_alt", lookingAt));
				} else {
					messages.add(Component.translatable("tooltip.toms_storage.config_overlay.block", x, y, z));
					messages.add(lookingAt);
				}
				if (c.selecting()) {
					if (c.massSelect()) {
						messages.add(Component.translatable("tooltip.toms_storage.config_overlay.mass_select").withStyle(ChatFormatting.GOLD));
						messages.add(Component.translatable("tooltip.toms_storage.config_overlay.mass_select.finish", leftClk));
						messages.add(Component.translatable("tooltip.toms_storage.config_overlay.mass_select.abort", shift, leftClk));
					} else {
						messages.add(Component.translatable("tooltip.toms_storage.config_overlay.select").withStyle(ChatFormatting.GOLD));
						messages.add(Component.translatable("tooltip.toms_storage.config_overlay.select.single", rightClk));
						messages.add(Component.translatable("tooltip.toms_storage.config_overlay.select.finish", rightClk));
						messages.add(Component.translatable("tooltip.toms_storage.config_overlay.select.start_mass", leftClk));
					}
				}
			} else {
				messages.add(Component.translatable("tooltip.toms_storage.config_overlay.too_far"));
			}
		} else {
			messages.add(Component.translatable("tooltip.toms_storage.config_overlay.configure", rightClk));
			messages.add(Component.translatable("tooltip.toms_storage.config_overlay.show", leftClk));
		}

		if (!messages.isEmpty()) {
			gr.renderTooltip(mc.font, messages, Optional.empty(), 5, 25);
		}
	}
}
