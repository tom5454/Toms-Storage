package com.tom.storagemod.components;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TagFilterComponent(List<TagKey<Item>> tags, boolean allowList) {

	public static final Codec<TagFilterComponent> CODEC = ExtraCodecs.validate(RecordCodecBuilder.mapCodec(b -> {
		return b.group(
				Codec.list(TagKey.codec(Registries.ITEM)).fieldOf("tags").forGetter(TagFilterComponent::tags),
				Codec.BOOL.fieldOf("allow_list").forGetter(TagFilterComponent::allowList)
				).apply(b, TagFilterComponent::new);
	}), TagFilterComponent::validate).codec();

	private static DataResult<TagFilterComponent> validate(TagFilterComponent p_286361_) {
		return DataResult.success(p_286361_);
	}
}
