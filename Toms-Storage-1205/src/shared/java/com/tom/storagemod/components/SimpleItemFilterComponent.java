package com.tom.storagemod.components;

import java.util.List;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SimpleItemFilterComponent(List<ItemStack> stacks, boolean matchComp, boolean allowList) {

	public static final Codec<SimpleItemFilterComponent> CODEC = ExtraCodecs.validate(RecordCodecBuilder.mapCodec(b -> {
		return b.group(
				Codec.list(ItemStack.OPTIONAL_CODEC).fieldOf("stacks").forGetter(SimpleItemFilterComponent::stacks),
				Codec.BOOL.fieldOf("match_component").forGetter(SimpleItemFilterComponent::matchComp),
				Codec.BOOL.fieldOf("allow_list").forGetter(SimpleItemFilterComponent::allowList)
				).apply(b, SimpleItemFilterComponent::new);
	}), SimpleItemFilterComponent::validate).codec();

	private static DataResult<SimpleItemFilterComponent> validate(SimpleItemFilterComponent p_286361_) {
		return DataResult.success(p_286361_);
	}
}
