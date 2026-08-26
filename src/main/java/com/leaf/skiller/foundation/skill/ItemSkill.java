package com.leaf.skiller.foundation.skill;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.foundation.Consumable;
import com.leaf.skiller.foundation.context.SkillContext;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents an item-based skill that can be released and consumes resources.
 * 表示一种可以释放并消耗资源的基于物品的技能。
 * <p>
 * ItemSkills are the main interface for defining skill behaviors in the Skiller system.
 * 物品技能是技能系统中定义技能行为的主要接口。
 * </p>
 * <p>
 * Each ItemSkill is associated with a specific context type and manages resource consumption
 * each time the skill is released.
 * 每个物品技能都关联到特定的上下文类型，并在每次释放技能时管理资源消耗。
 * </p>
 *
 * @param <T> The type of skill context associated with this skill
 *            与此技能关联的技能上下文类型
 *
 * @since 1.0.0
 * @see SkillContext
 * @see ISkillInstance
 * @see SkillType
 */
public interface ItemSkill<T extends SkillContext> {
    /**
     * Releases the skill with the given context and instance.
     使用给定的上下文和实例释放技能。
     * <p>
     * This method is called when a player activates the skill, triggering its primary effect.
     当玩家激活技能时调用此方法，触发技能的主要效果。
     * </p>
     *
     * @param context  The skill context providing environment information for skill execution
     *                 技能上下文，为技能执行提供环境信息
     * @param instance The skill instance containing skill-specific data and state
     *                 技能实例，包含特定于技能的数据和状态
     *
     * @since 1.0.0
     */
    void release(T context, ISkillInstance<T> instance);

    /**
     * Consumes resources for this skill execution.
     消耗此技能执行所需的资源。
     * <p>
     * This method handles the resource consumption logic when the skill is used.
     此方法处理技能使用时的资源消耗逻辑。
     * </p>
     *
     * @param context     The skill context providing environment information
     *                    技能上下文，提供环境信息
     * @param consumable  The consumable resource handler for this skill execution
     *                    此技能执行的可消耗资源处理器
     * @param instance    The skill instance containing skill-specific data
     *                    技能实例，包含特定于技能的数据
     *
     * @since 1.0.0
     * @see Consumable
     */
    void consumeResource(T context, Consumable consumable, ISkillInstance<T> instance);

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
    SkillType getType();

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
    default ResourceLocation getId() {
        return SkillerBuiltInRegistries.SKILLS.getKey(this);
    }
}
