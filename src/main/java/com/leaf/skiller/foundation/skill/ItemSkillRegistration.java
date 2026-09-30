package com.leaf.skiller.foundation.skill;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.api.registry.SkillerRegistries;
import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.config.SkillContextFactory;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ItemSkillRegistration<C extends SkillContext> implements IPersistedSerializable {

    public static Codec<ItemSkillRegistration<?>> CODEC
            = PersistedParser.createCodec(ItemSkillRegistration::new);

    public static StreamCodec<ByteBuf, ItemSkillRegistration<?>> STREAM_CODEC
            = PersistedParser.createStreamCodec(ItemSkillRegistration::new);


    @Persisted(key = "type")
    private ResourceLocation type;
    @Persisted(key = "factory")
    private ResourceLocation factoryKey;
    @Persisted(key = "skill")
    private ResourceLocation skillId;

    private ItemSkill<C> skill;

    public ItemSkillRegistration() {}

    public ItemSkillRegistration(ResourceLocation id, SkillType type, ResourceKey<SkillContextFactory<C>> factoryKey, ItemSkill<C> skill) {
        this.type = type.getId();
        this.factoryKey = factoryKey.location();
        this.skill = skill;
        this.skillId = id;
    }

    /**
     * Returns the skill type that categorizes this skill.
     返回对此技能进行分类的技能类型。
     * <p>
     * Skill types are used to group and organize related skills.
     技能类型用于对相关技能进行分组和组织。
     * </p>
     *
     * @return The skill type for this skill
     *         此技能的技能类型
     *
     * @since 1.0.0
     * @see SkillType
     */
    public SkillType getType() {
        return SkillTypeFactory.of(type);
    }

    @SuppressWarnings("unchecked")
    public ItemSkill<C> getSkill() {
        if (skill == null) {
            var reg = SkillerBuiltInRegistries.SKILLS.get(skillId);
            if (reg == null) {
                throw new IllegalStateException("Unknown skill: " + skillId);
            }
            skill = (ItemSkill<C>) reg.skill;
        }
        return skill;
    }

    /**
     * Returns the unique identifier for this skill.
     返回此技能的唯一标识符。
     * <p>
     * The ID is automatically retrieved from the skills registry.
     * ID会自动从技能注册表中检索。
     * </p>
     *
     * @return The resource location representing this skill's unique identifier
     *         表示此技能唯一标识符的资源位置
     *
     * @since 1.0.0
     * @see SkillerBuiltInRegistries#SKILLS
     */
    public ResourceLocation getId() {
        return skillId;
    }

    @SuppressWarnings({"unchecked"})
    public SkillContextFactory<C> getFactory() {
        return (SkillContextFactory<C>) SkillerBuiltInRegistries.CONTEXT_FACTORIES.get(
                ResourceKey.create(SkillerRegistries.CONTEXT_FACTORY, factoryKey));
    }
}
