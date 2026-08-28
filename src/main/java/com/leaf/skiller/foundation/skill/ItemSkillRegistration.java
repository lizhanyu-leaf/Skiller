package com.leaf.skiller.foundation.skill;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.config.SkillContextFactory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ItemSkillRegistration<C extends SkillContext> {
    private final SkillType type;
    private final ResourceKey<SkillContextFactory<C>> factoryKey;
    private final ItemSkill<C> skill;

    public ItemSkillRegistration(SkillType type, ResourceKey<SkillContextFactory<C>> factoryKey, ItemSkill<C> skill) {
        this.type = type;
        this.factoryKey = factoryKey;
        this.skill = skill;
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
        return type;
    }

    public ItemSkill<C> getSkill() {
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
        return SkillerBuiltInRegistries.SKILLS.getKey(this);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SkillContextFactory<C> getFactory() {
        return (SkillContextFactory<C>) SkillerBuiltInRegistries.CONTEXT_FACTORIES.get((ResourceKey) factoryKey);
    }
}
