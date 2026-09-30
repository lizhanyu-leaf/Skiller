package com.leaf.skiller.foundation;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * 包含表示和重新创建技能实例所需的所有数据。
 * <p>
 * 作为技能实例的序列化表示，包含技能 ID、工厂 ID 和实例特定的 NBT 数据。
 * </p>
 */
public class SkillData implements IPersistedSerializable {

    public static final Codec<SkillData> CODEC
            = PersistedParser.createCodec(SkillData::new);

    public static final StreamCodec<ByteBuf, SkillData> STREAM_CODEC
            = PersistedParser.createStreamCodec(SkillData::new);

    /** 技能的唯一标识符 */
    @Persisted(key = "skill")
    private ResourceLocation skillId;

    /** 创建此实例的工厂标识符 */
    @Persisted(key = "factory")
    private ResourceLocation factoryId;

    /** 实例特定的 NBT 数据 */
    @Persisted(key = "nbt")
    private CompoundTag nbt;

    public SkillData() {}

    public SkillData(ResourceLocation skillId, ResourceLocation factoryId, CompoundTag nbt) {
        this.skillId = skillId;
        this.factoryId = factoryId;
        this.nbt = nbt;
    }

    public ResourceLocation skillId() {
        return skillId;
    }

    public ResourceLocation factoryId() {
        return factoryId;
    }

    public CompoundTag nbt() {
        return nbt;
    }

    // ========== 反序列化 ==========

    /**
     * 从序列化的 SkillData 创建技能实例。
     *
     * @return 技能实例，如果工厂未找到则返回 null
     */
    public ISkillInstance<?> createInstance() {
        SkillInstanceFactory<?, ?> factory = SkillerBuiltInRegistries.SKILL_FACTORIES.get(factoryId);
        if (factory == null) return null;
        return factory.createFromData(this);
    }

    // ========== Object 方法 ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkillData that)) return false;
        return skillId.equals(that.skillId)
                && factoryId.equals(that.factoryId)
                && nbt.equals(that.nbt);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(skillId, factoryId, nbt);
    }

    @Override
    public @NotNull String toString() {
        return "SkillData{" + skillId + ", factory=" + factoryId + ", nbt=" + nbt + '}';
    }
}