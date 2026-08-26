package com.leaf.skiller.foundation;

import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.ItemSkill;

/**
 * Factory interface for creating and serializing skill instances.
 * 用于创建和序列化技能实例的工厂接口。
 * <p>
 * SkillInstanceFactory defines the contract for creating new skill instances,
 * both with default values and from serialized data.
 * SkillInstanceFactory定义了创建新技能实例的契约，包括使用默认值和从序列化数据创建。
 * </p>
 * <p>
 * Implementations of this interface are registered in the SKILL_FACTORIES registry
 * and are responsible for managing the lifecycle of specific skill instance types.
 * 此接口的实现注册在SKILL_FACTORIES注册表中，负责管理特定技能实例类型的生命周期。
 * </p>
 * <p>
 * This factory pattern enables flexible creation, serialization, and deserialization
 * of skill instances with custom data and behavior.
 * 此工厂模式支持灵活创建、序列化和反序列化具有自定义数据和行为的技能实例。
 * </p>
 *
 * @param <T> The type of skill context associated with instances created by this factory
 *            与此工厂创建的实例关联的技能上下文类型
 * @param <I> The type of skill instance created by this factory
 *            此工厂创建的技能实例类型
 *
 * @since 1.0.0
 * @see ISkillInstance
 * @see ItemSkill
 * @see SkillData
 */
public interface SkillInstanceFactory<T extends SkillContext, I extends ISkillInstance<T>> {
    /**
     * Creates a new skill instance with default values for the given skill.
     * 为给定的技能创建一个具有默认值的新技能实例。
     * <p>
     * This method is used when initializing a new skill instance for the first time,
     * such as when a player first acquires a skill.
     * 此方法用于首次初始化新的技能实例，例如玩家第一次获得技能时。
     * </p>
     * <p>
     * The instance should be created with appropriate default data and level values.
     * 实例应使用适当的默认数据和等级值创建。
     * </p>
     *
     * @param skill The ItemSkill to create an instance for
     *              要为其创建实例的ItemSkill
     *
     * @return A new skill instance with default values
     *         一个具有默认值的新技能实例
     *
     * @since 1.0.0
     * @see ISkillInstance
     * @see ItemSkill
     */
    I createDefault(ItemSkill<T> skill);

    /**
     * Creates a skill instance from serialized SkillData.
     * 从序列化的SkillData创建技能实例。
     * <p>
     * This method is used to deserialize skill instances that have been previously
     * serialized for storage or network transmission.
     * 此方法用于反序列化先前为存储或网络传输而序列化的技能实例。
     * </p>
     * <p>
     * The implementation must extract and restore all instance data from the SkillData.
     * 实现必须从SkillData中提取并恢复所有实例数据。
     * </p>
     *
     * @param data The serialized skill data containing all instance information
     *             包含所有实例信息的序列化技能数据
     *
     * @return A deserialized skill instance restored from the data
     *         从数据恢复的反序列化技能实例
     *
     * @throws IllegalArgumentException if the data is invalid or incomplete
     *         如果数据无效或不完整
     *
     * @since 1.0.0
     * @see SkillData
     * @see ISkillInstance#fromData(SkillData)
     */
    I createFromData(SkillData data);

    /**
     * Serializes a skill instance into SkillData for storage or transmission.
     * 将技能实例序列化为SkillData以进行存储或传输。
     * <p>
     * This method captures all relevant instance data into a serializable format.
     * 此方法将所有相关实例数据捕获为可序列化格式。
     * </p>
     * <p>
     * The resulting SkillData should contain all information needed to recreate
     * the instance via createFromData.
     * 生成的SkillData应包含通过createFromData重新创建实例所需的所有信息。
     * </p>
     *
     * @param instance The skill instance to serialize
     *                 要序列化的技能实例
     *
     * @return SkillData containing the serialized instance information
     *         包含序列化实例信息的SkillData
     *
     * @since 1.0.0
     * @see SkillData
     * @see ISkillInstance#toData()
     */
    SkillData toData(I instance);
}
