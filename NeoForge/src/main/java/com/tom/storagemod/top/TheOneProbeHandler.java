package com.tom.storagemod.top;

import java.util.Locale;
import java.util.function.Function;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.tom.storagemod.StorageMod;
import com.tom.storagemod.block.entity.BasicInventoryHopperBlockEntity;
import com.tom.storagemod.block.entity.InventoryConnectorBlockEntity;
import com.tom.storagemod.block.entity.InventoryConnectorBlockEntity.UsageInfo;
import com.tom.storagemod.inventory.BlockFilter;
import com.tom.storagemod.platform.Platform;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.styles.LayoutStyle;

public class TheOneProbeHandler implements Function<ITheOneProbe, Void>, IProbeInfoProvider {
	public static ITheOneProbe theOneProbeImp;

	public static TheOneProbeHandler create() {
		return new TheOneProbeHandler();
	}

	@Override
	public Void apply(ITheOneProbe input) {
		theOneProbeImp = input;
		theOneProbeImp.registerProvider(this);
		return null;
	}

	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState blockState, IProbeHitData data) {
		BlockEntity te = world.getBlockEntity(data.getPos());
		if (te instanceof InventoryConnectorBlockEntity be) {
			UsageInfo usage = be.getUsage();
			probeInfo.text(Component.translatable("tooltip.toms_storage.connector_info.size", usage.free(), usage.all()));
			probeInfo.progress(usage.all() - usage.free(), usage.all(), probeInfo.defaultProgressStyle().filledColor(0xFF8e691d).alternateFilledColor(0xFF342f26).showText(false));
			probeInfo.text(Component.translatable("tooltip.toms_storage.connector_info.inv", usage.blocks()));
		}
		if (te instanceof BasicInventoryHopperBlockEntity be) {
			var f = be.getFilter();
			if (!f.isEmpty()) {
				addFilterInfo(player, probeInfo, f, world);
			}
			if (!be.isEnabled()) {
				probeInfo.text(Component.translatable("tooltip.toms_storage.disabled_by_redstone"));
			}
		}
		if (te != null && te.hasData(Platform.BLOCK_FILTER)) {
			BlockFilter bf = te.getData(Platform.BLOCK_FILTER).getFilter();
			probeInfo.text(Component.translatable("tooltip.toms_storage.block_filter"));
			if (bf.skip()) {
				probeInfo.text(Component.translatable("tooltip.toms_storage.block_filter.skip"));
			} else {
				probeInfo.text(Component.translatable("tooltip.toms_storage.priority_" + bf.getPriority().name().toLowerCase(Locale.ROOT)));
				var f = bf.filter.getItem(0);
				if (!f.isEmpty()) {
					addFilterInfo(player, probeInfo, f, world);
				}
			}
		}
	}

	private static void addFilterInfo(Player player, IProbeInfo probeInfo, ItemStack f, Level world) {
		if (player.isSecondaryUseActive()) {
			IProbeInfo vertical = probeInfo.horizontal(new LayoutStyle().borderColor(0xFFFF0000).spacing(2));
			var tt = vertical.item(f).vertical();
			f.getTooltipLines(TooltipContext.of(world), player, TooltipFlag.Default.NORMAL).forEach(tt::text);
		} else {
			probeInfo.text(Component.translatable("tooltip.toms_storage.block_filter.item", f.getHoverName()));
		}
	}

	@Override
	public ResourceLocation getID() {
		return ResourceLocation.tryBuild(StorageMod.modid, "top");
	}
}
