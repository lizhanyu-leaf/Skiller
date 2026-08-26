package com.leaf.skiller.foundation.skill;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents a category or type that skills can be grouped under.
 * 表示技能可以归类到的类别或类型。
 * <p>
 * SkillTypes are used to organize and categorize related ItemSkills within the system.
 * 技能类型用于在系统中组织和分类相关的物品技能。
 * </p>
 * <p>
 * This interface provides a lightweight way to reference and identify skill categories
 * without requiring the full ItemSkill definition.
 * 此接口提供了一种轻量级的方式来引用和识别技能类别，而无需完整的ItemSkill定义。
 * </p>
 * <p>
 * Common uses include filtering skills by type, organizing skill UI elements,
 * and applying effects to groups of related skills.
 * 常见用途包括按类型过滤技能、组织技能UI元素，以及对相关技能组应用效果。
 * </p>
 *
 * @since 1.0.0
 * @see ItemSkill
 * @see OwnedBySkills
 */
public interface SkillType {
    /**
     * Returns the unique identifier for this skill type.
     * 返回此技能类型的唯一标识符。
     * <p>
     * The ID is used to uniquely identify and reference this skill type
     * within the registry system.
     * 此ID用于在注册表系统中唯一标识和引用此技能类型。
     * </p>
     * <p>
     * This identifier should be consistent and unique across all skill types.
     * 此标识符在所有技能类型中应保持一致且唯一。
     * </p>
     *
     * @return The resource location representing this skill type's unique identifier
     *         表示此技能类型唯一标识符的资源位置
     *
     * @since 1.0.0
     * @see ResourceLocation
     */
    ResourceLocation getId();
}
