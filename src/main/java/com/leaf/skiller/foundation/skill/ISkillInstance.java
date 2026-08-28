package com.leaf.skiller.foundation.skill;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.foundation.*;
import com.leaf.skiller.foundation.context.SkillContext;
import net.minecraft.nbt.CompoundTag;

/**
 * Abstract base class for skill instances containing skill-specific data and behavior.
 * 包含技能特定数据和行为的技能实例的抽象基类。
 * <p>
 * ISkillInstance represents a specific instance of an ItemSkill with its own data,
 * level, and resource information.
 * ISkillInstance表示ItemSkill的特定实例，具有自己的数据、等级和资源信息。
 * </p>
 * <p>
 * Each instance is tied to a specific skill and can be released, consuming resources
 * and producing effects based on its level and data.
 * 每个实例都绑定到特定技能，可以基于其等级和数据释放，消耗资源并产生效果。
 * </p>
 *
 * @param <T> The type of skill context associated with this instance
 *            与此实例关联的技能上下文类型
 *
 * @since 1.0.0
 * @see ItemSkill
 * @see SkillData
 * @see SkillInstanceFactory
 */
public abstract class ISkillInstance<T extends SkillContext> {
    /**
     * Returns the ItemSkill that this instance belongs to.
     返回此实例所属的ItemSkill。
     * <p>
     * The skill defines the core behavior and type of this instance.
     * 技能定义了此实例的核心行为和类型。
     * </p>
     *
     * @return The ItemSkill this instance is associated with
     *         此实例关联的ItemSkill
     *
     * @since 1.0.0
     */
    public abstract ItemSkillRegistration<T> skill();

    /**
     * Returns the NBT data containing this instance's serialized state.
     * 返回包含此实例序列化状态的NBT数据。
     * <p>
     * This data stores all instance-specific information for persistence and
     * network transmission.
     * 此数据存储所有实例特定的信息，用于持久化和网络传输。
     * </p>
     *
     * @return The compound tag containing this instance's data
     *         包含此实例数据的复合标签
     *
     * @since 1.0.0
     * @see CompoundTag
     */
    public abstract CompoundTag data();

    /**
     * Returns the resource type consumed by this skill instance.
     * 返回此技能实例消耗的资源类型。
     * <p>
     * Different skill instances may consume different types of resources.
     * 不同的技能实例可能消耗不同类型的资源。
     * </p>
     *
     * @return The skill resource this instance consumes
     *         此实例消耗的技能资源
     *
     * @since 1.0.0
     * @see SkillResource
     */
    public abstract SkillResource getResource();

    /**
     * Returns the current level of this skill instance.
     * 返回此技能实例的当前等级。
     * <p>
     * Level typically affects the power, duration, or cost of the skill.
     * 等级通常影响技能的威力、持续时间或成本。
     * </p>
     *
     * @return The current skill level (typically 1 or higher)
     *         当前技能等级（通常为1或更高）
     *
     * @since 1.0.0
     */
    public abstract int level();

    /**
     * Returns the factory used to create this skill instance type.
     * 返回用于创建此技能实例类型的工厂。
     * <p>
     * The factory is used for serialization and deserialization of instances.
     * 工厂用于实例的序列化和反序列化。
     * </p>
     *
     * @return The skill instance factory for this instance type
     *         此实例类型的技能实例工厂
     *
     * @since 1.0.0
     * @see SkillInstanceFactory
     */
    protected abstract SkillInstanceFactory<T, ? extends ISkillInstance<T>> getFactory();

    /**
     * Consumes resources for this skill instance using the provided context.
     * 使用提供的上下文为此技能实例消耗资源。
     * <p>
     * This method delegates to the underlying skill's consumeResource method.
     * 此方法委托给底层技能的consumeResource方法。
     * </p>
     *
     * @param context     The skill context providing environment information
     *                    技能上下文，提供环境信息
     * @param consumable  The consumable resource handler for this execution
     *                    此执行的可消耗资源处理器
     *
     * @since 1.0.0
     * @see Consumable
     * @see ItemSkill#consumeResource(SkillContext, Consumable, ISkillInstance)
     */
    public void consumeResource(T context, Consumable consumable) {
        skill().getSkill().consumeResource(context, consumable, this);
    }

    /**
     * Releases this skill instance with the provided context.
     * 使用提供的上下文释放此技能实例。
     * <p>
     * This is the main method that triggers the skill's effect.
     * 这是触发技能效果的主要方法。
     * </p>
     *
     * @param context The skill context providing environment information for skill execution
     *               技能上下文，为技能执行提供环境信息
     *
     * @since 1.0.0
     * @see ItemSkill#release(SkillContext, ISkillInstance)
     */
    public void release(T context) {
        skill().getSkill().release(context, this);
    }

    /**
     * Converts this skill instance to SkillData for serialization.
     * 将此技能实例转换为SkillData以进行序列化。
     * <p>
     * The resulting SkillData can be stored persistently or transmitted over network.
     * 生成的SkillData可以持久存储或通过网络传输。
     * </p>
     *
     * @return SkillData containing this instance's serialized information
     *         包含此实例序列化信息的SkillData
     *
     * @since 1.0.0
     * @see SkillData
     * @see SkillInstanceFactory#toData(ISkillInstance)
     */
    @SuppressWarnings("unchecked")
    public SkillData toData() {
        var factory = (SkillInstanceFactory<T, ISkillInstance<T>>)
                getFactory();
        return factory.toData(this);
    }

    /**
     * Creates a skill instance from serialized SkillData.
     * 从序列化的SkillData创建技能实例。
     * <p>
     * This static method deserializes skill data back into a functional instance.
     * 此静态方法将技能数据反序列化回可用的实例。
     * </p>
     * <p>
     * Returns null if the factory for the given data is not found.
     * 如果找不到给定数据的工厂，则返回null。
     * </p>
     *
     * @param data The serialized skill data to create an instance from
     *             要从中创建实例的序列化技能数据
     *
     * @return A new skill instance created from the data, or null if factory not found
     *         从数据创建的新技能实例；如果找不到工厂则返回null
     *
     * @since 1.0.0
     * @see SkillData
     * @see SkillInstanceFactory#createFromData(SkillData)
     */
    public static ISkillInstance<?> fromData(SkillData data) {
        var factoryId = data.factoryId();
        SkillInstanceFactory<?, ?> factory =
                SkillerBuiltInRegistries.SKILL_FACTORIES.get(factoryId);
        if (factory == null) return null;
        return factory.createFromData(data);
    }
}
