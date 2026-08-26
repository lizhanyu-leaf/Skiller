package com.leaf.skiller.foundation;

import com.leaf.skiller.Skiller;
import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class SkillResources {
    public static final ResourceLocation DEFAULT_ID = Skiller.modLoc("empty");

    public static final Codec<SkillResource> CODEC = ResourceLocation.CODEC.xmap(
            SkillerBuiltInRegistries.SKILL_RESOURCES::get,
            resource -> {
                ResourceLocation id = SkillerBuiltInRegistries.SKILL_RESOURCES.getKey(resource);
                return id == null ? DEFAULT_ID : id;
            });

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillResource> STREAM_CODEC =
            StreamCodec.of(
                    (buf, resource) -> {
                        ResourceLocation id = SkillerBuiltInRegistries.SKILL_RESOURCES.getKey(resource);
                        buf.writeResourceLocation(id == null ? DEFAULT_ID : id);
                    },
                    buf -> Objects.requireNonNull(SkillerBuiltInRegistries.SKILL_RESOURCES.get(buf.readResourceLocation()))
            );
}
