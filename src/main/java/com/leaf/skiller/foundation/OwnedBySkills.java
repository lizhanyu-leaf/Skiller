package com.leaf.skiller.foundation;

import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.ItemSkill;
import com.leaf.skiller.foundation.skill.ItemSkillRegistration;
import com.leaf.skiller.foundation.skill.SkillType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Interface for objects that own and manage a collection of skills.
 * 用于拥有和管理技能集合的对象的接口。
 * <p>
 * OwnedBySkills defines the contract for entities that can hold multiple skills,
 * organized by their types.
 * OwnedBySkills定义了可以持有按类型组织的多个技能的实体的契约。
 * </p>
 * <p>
 * This interface provides methods to query, add, remove, and release skills.
 * Implementations typically include items, entities, or other game objects that
 * can have skills attached to them.
 * 此接口提供查询、添加、删除和释放技能的方法。实现通常包括可以附加技能的物品、实体或其他游戏对象。
 * </p>
 * <p>
 * Skills are organized by SkillType, allowing for efficient grouping and
 * batch operations on related skills.
 * 技能按SkillType组织，允许对相关技能进行高效分组和批量操作。
 * </p>
 *
 * @since 1.0.0
 * @see ItemSkill
 * @see SkillType
 */
@SuppressWarnings("UnusedReturnValue")
public interface OwnedBySkills {

    /**
     * Returns the map of skill types to their associated skill lists.
     * 返回技能类型到其关联技能列表的映射。
     * <p>
     * This map is the backing storage for all skills owned by this object.
     * 此映射是此对象拥有的所有技能的底层存储。
     * </p>
     * <p>
     * Modifications to this map should be done through the provided methods
     * (addSkill, removeSkill) to maintain consistency.
     * 应通过提供的方法（addSkill、removeSkill）修改此映射以保持一致性。
     * </p>
     *
     * @return A map where keys are skill types and values are lists of skills of that type
     *         键为技能类型、值为该类型的技能列表的映射
     *
     * @since 1.0.0
     * @see SkillType
     * @see ItemSkillRegistration
     */
    Map<SkillType, List<ItemSkillRegistration<?>>> skills();

    /**
     * Returns all skills of the specified type.
     * 返回指定类型的所有技能。
     * <p>
     * This method retrieves all skills that belong to the given skill type.
     * 此方法检索属于给定技能类型的所有技能。
     * </p>
     * <p>
     * Returns null if no skills of this type are present.
     * 如果不存在此类型的技能，则返回null。
     * </p>
     *
     * @param type The skill type to query
     *             要查询的技能类型
     *
     * @return A list of all skills of the specified type, or null if none exist
     *         指定类型的所有技能的列表；如果不存在则返回null
     *
     * @since 1.0.0
     * @see SkillType
     * @see ItemSkill
     */
    default List<ItemSkillRegistration<?>> getSkills(SkillType type) {
        return skills().get(type);
    }

    /**
     * Returns an unmodifiable list containing all skills owned by this object.
     * 返回包含此对象拥有的所有技能的不可修改列表。
     * <p>
     * This method aggregates all skills from all types into a single list.
     * 此方法将所有类型的所有技能聚合到单个列表中。
     * </p>
     * <p>
     * Returns an empty list if no skills are present.
     * 如果不存在技能，则返回空列表。
     * </p>
     *
     * @return An unmodifiable list of all owned skills
     *         所有拥有的技能的不可修改列表
     *
     * @since 1.0.0
     * @see ItemSkill
     */
    default List<ItemSkillRegistration<?>> getAllSkills() {
        if (skills().isEmpty()) {
            return Collections.emptyList();
        }
        List<ItemSkillRegistration<?>> all = new ArrayList<>();
        for (List<ItemSkillRegistration<?>> skillList : skills().values()) {
            all.addAll(skillList);
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Checks whether this object owns any skills.
     * 检查此对象是否拥有任何技能。
     * <p>
     * This is a convenience method to quickly determine if the skills collection is empty.
 * 这是一个便捷方法，用于快速确定技能集合是否为空。
     * </p>
     *
     * @return true if no skills are owned by this object
     *         如果此对象不拥有任何技能，则返回true
     *
     * @since 1.0.0
     */
    default boolean isEmpty() {
        return skills().isEmpty();
    }

    /**
     * Checks whether this object owns any skills of the specified type.
     * 检查此对象是否拥有指定类型的任何技能。
     * <p>
     * This method tests for the presence of at least one skill of the given type.
     * 此方法测试是否存在至少一个给定类型的技能。
     * </p>
     *
     * @param type The skill type to check for
     *             要检查的技能类型
     *
     * @return true if at least one skill of the specified type is owned
     *         如果拥有至少一个指定类型的技能，则返回true
     *
     * @since 1.0.0
     * @see SkillType
     */
    default boolean hasSkill(SkillType type) {
        return skills().containsKey(type);
    }

    /**
     * Checks whether this object owns the specified specific skill.
     * 检查此对象是否拥有指定的特定技能。
     * <p>
     * This method performs a more specific check than hasSkill(SkillType),
     * verifying the exact skill instance is owned.
     * 此方法执行比hasSkill(SkillType)更具体的检查，验证拥有确切的技能实例。
     * </p>
     *
     * @param skill The specific skill to check for
     *              要检查的特定技能
     *
     * @return true if the specified skill is owned by this object
     *         如果此对象拥有指定的技能，则返回true
     *
     * @since 1.0.0
     * @see ItemSkill
     * @see #hasSkill(SkillType)
     */
    default boolean hasSkill(ItemSkillRegistration<?> skill) {
        return skills().containsKey(skill.getType()) && skills().get(skill.getType()).contains(skill);
    }

    /**
     * Adds a skill to this object's collection.
     * 将技能添加到此对象的集合中。
     * <p>
     * If this is the first skill of its type, a new list is created for that type.
     * 如果这是其类型的第一个技能，则会为该类型创建一个新列表。
     * </p>
     * <p>
     * This method returns this object for method chaining.
     * 此方法返回此对象以支持方法链。
     * </p>
     *
     * @param skill The skill to add
     *              要添加的技能
     *
     * @return This object for method chaining
     *         此对象，用于方法链
     *
     * @since 1.0.0
     * @see ItemSkill
     */
    default OwnedBySkills addSkill(ItemSkillRegistration<?> skill) {
        if (!skills().containsKey(skill.getType())) {
            skills().put(skill.getType(), new ArrayList<>());
        }
        skills().get(skill.getType()).add(skill);
        return this;
    }

    /**
     * Removes all skills of the specified type from this object.
     * 从此对象中移除指定类型的所有技能。
     * <p>
     * This method completely removes the skill type and all associated skills.
     * 此方法完全移除技能类型和所有关联的技能。
     * </p>
     * <p>
     * This method returns this object for method chaining.
     * 此方法返回此对象以支持方法链。
     * </p>
     *
     * @param type The skill type to remove
     *             要移除的技能类型
     *
     * @return This object for method chaining
     *         此对象，用于方法链
     *
     * @since 1.0.0
     * @see SkillType
     */
    default OwnedBySkills removeSkill(SkillType type) {
        skills().remove(type);
        return this;
    }

    /**
     * Removes the specified specific skill from this object.
     * 从此对象中移除指定的特定技能。
     * <p>
     * This method removes only the specified skill while keeping other skills
     * of the same type.
     * 此方法仅移除指定的技能，同时保留相同类型的其他技能。
     * </p>
     * <p>
     * If this was the last skill of its type, the type list remains but empty.
     * 如果这是其类型的最后一个技能，则类型列表保留但为空。
     * </p>
     * <p>
     * This method returns this object for method chaining.
     * 此方法返回此对象以支持方法链。
     * </p>
     *
     * @param skill The specific skill to remove
     *              要移除的特定技能
     *
     * @return This object for method chaining
     *         此对象，用于方法链
     *
     * @since 1.0.0
     * @see ItemSkill
     * @see #removeSkill(SkillType)
     */
    default OwnedBySkills removeSkill(ItemSkillRegistration<?> skill) {
        SkillType type = skill.getType();
        if (hasSkill(type)) {
            skills().get(type).remove(skill);
        }
        return this;
    }

    /**
     * Releases all skills of the specified type with the given context.
     * 使用给定的上下文释放指定类型的所有技能。
     * Releases all skills of the specified type with the given context.
     * 使用给定的上下文释放指定类型的所有技能。
     * <p>
     * This method iterates through all skills of the specified type and
     * releases them with the provided context.
     * 此方法遍历指定类型的所有技能，并使用提供的上下文释放它们。
     * </p>
     * <p>
     * Returns true if at least one skill was successfully released.
     * 如果至少成功释放了一个技能，则返回true。
     * </p>
     * <p>
     * May throw ClassCastException if the context type doesn't match the
     * skills' expected context type.
     * 如果上下文类型与技能期望的上下文类型不匹配，可能会抛出ClassCastException。
     * </p>
     *
     * @param <T>     The type of skill context
     *                技能上下文的类型
     * @param type    The skill type to release
     *                要释放的技能类型
     * @param context The skill context to use for releasing skills
     *                用于释放技能的技能上下文
     *
     * @return true if at least one skill was released, false otherwise
     *         如果至少释放了一个技能，则返回true；否则返回false
     *
     * @throws ClassCastException if the context type is incompatible with the skills
     *         如果上下文类型与技能不兼容
     *
     * @since 1.0.0
     * @see SkillType
     * @see SkillContext
     * @see ItemSkill#release
     */
    <T extends SkillContext> boolean releaseSkills(SkillType type, T context);
}
