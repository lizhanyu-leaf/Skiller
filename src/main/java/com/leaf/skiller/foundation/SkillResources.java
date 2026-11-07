package com.leaf.skiller.foundation;

import com.leaf.skiller.Skiller;
import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public class SkillResources {
    public static final ResourceLocation DEFAULT_ID = Skiller.modLoc("empty");

    /**
     * Codec of a skill resource by its registry id. Decoding fails with an explicit
     * error for unknown ids instead of silently producing a null resource.
     * 按注册表ID编解码技能资源。未知ID的解码会以显式错误失败，
     * 而不是静默产生 null 资源。
     */
    public static final Codec<SkillResource> CODEC = ResourceLocation.CODEC.flatXmap(
            id -> {
                SkillResource resource = SkillerBuiltInRegistries.SKILL_RESOURCES.get(id);
                return resource == null
                        ? DataResult.error(() -> "Unknown skill resource: " + id)
                        : DataResult.success(resource);
            },
            resource -> {
                ResourceLocation id = SkillerBuiltInRegistries.SKILL_RESOURCES.getKey(resource);
                return id == null
                        ? DataResult.error(() -> "Unregistered skill resource: " + resource)
                        : DataResult.success(id);
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
