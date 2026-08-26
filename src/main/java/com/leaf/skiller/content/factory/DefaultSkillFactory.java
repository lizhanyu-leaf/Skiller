package com.leaf.skiller.content.factory;

import com.leaf.skiller.AllSkillResources;
import com.leaf.skiller.content.skill.instance.DefaultSkillInstance;
import com.leaf.skiller.foundation.SkillResource;
import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.ItemSkill;
import net.minecraft.nbt.CompoundTag;

/**
 * Default factory implementation for creating skill instances with standard configuration.
 * 用于创建具有标准配置的技能实例的默认工厂实现。
 * <p>
 * This factory extends {@link NbtSkillInstanceFactory} to provide a concrete implementation
 * for creating {@link DefaultSkillInstance} objects. It uses predefined NBT keys "resource"
 * and "level" to serialize and deserialize skill instance data, ensuring consistent
 * data format across the system.
 * 此工厂扩展 {@link NbtSkillInstanceFactory} 以提供创建 {@link DefaultSkillInstance}
 * 对象的具体实现。它使用预定义的 NBT 键 "resource" 和 "level" 来序列化和反序列化
 * 技能实例数据，确保系统内的数据格式一致。
 * </p>
 * <p>
 * The factory provides default instance creation with sensible defaults (empty resource,
 * level 1, empty NBT data) for cases where skill instances need to be created without
 * existing data, such as during initialization or when adding new skills to items.
 * 工厂提供带有合理默认值（空资源、等级 1、空 NBT 数据）的默认实例创建，
 * 用于需要在不使用现有数据的情况下创建技能实例的情况，例如在初始化期间或向物品添加新技能时。
 * </p>
 *
 * @param <T> the skill context type, must extend SkillContext
 *            技能上下文类型，必须扩展 SkillContext
 * @see NbtSkillInstanceFactory
 * @see DefaultSkillInstance
 * @see com.leaf.skiller.foundation.SkillInstanceFactory
 * @since 1.0.0
 */
public class DefaultSkillFactory<T extends SkillContext> extends NbtSkillInstanceFactory<T, DefaultSkillInstance<T>> {
    /**
     * The NBT key used to store the resource type in serialized data.
     * 用于在序列化数据中存储资源类型的 NBT 键。
     */
    private static final String RESOURCE_KEY = "resource";

    /**
     * The NBT key used to store the skill level in serialized data.
     * 用于在序列化数据中存储技能等级的 NBT 键。
     */
    private static final String LEVEL_KEY = "level";

    /**
     * Constructs a new DefaultSkillFactory with standard NBT key configuration.
     * 使用标准 NBT 键配置构造新的 DefaultSkillFactory。
     * <p>
     * This constructor initializes the factory with predefined keys for serializing
     * resource type and skill level. These keys are used consistently across the
     * system to ensure proper data persistence and retrieval.
     * 此构造函数使用用于序列化资源类型和技能等级的预定义键初始化工厂。
     * 这些键在系统中一致使用，以确保正确的数据持久化和检索。
     * </p>
     *
     * @see NbtSkillInstanceFactory#NbtSkillInstanceFactory(String, String)
     * @since 1.0.0
     */
    public DefaultSkillFactory() {
        super("resource", "level");
    }

    /**
     * Creates a new DefaultSkillInstance with the specified parameters.
     * 使用指定参数创建新的 DefaultSkillInstance。
     * <p>
     * This method constructs a skill instance with the provided skill definition,
     * resource type, level, and NBT data. The NBT data may contain additional
     * custom properties specific to the skill or its current state.
     * 此方法使用提供的技能定义、资源类型、等级和 NBT 数据构造技能实例。
     * NBT 数据可能包含技能特定的附加自定义属性或其当前状态。
     * </p>
     *
     * @param skill    the item skill definition for this instance
     *                 此实例的物品技能定义
     * @param resource the skill resource type for consumption/management
     *                 用于消耗/管理的技能资源类型
     * @param level    the skill level affecting behavior and power
     *                 影响行为和威力的技能等级
     * @param nbt      the NBT compound tag containing persistent skill data
     *                 包含持久化技能数据的 NBT 复合标签
     * @return a new DefaultSkillInstance with the specified configuration
     *         具有指定配置的新 DefaultSkillInstance
     * @see DefaultSkillInstance
     * @see ItemSkill
     * @see SkillResource
     * @see CompoundTag
     * @since 1.0.0
     */
    @Override
    protected DefaultSkillInstance<T> create(ItemSkill<T> skill, SkillResource resource, int level, CompoundTag nbt) {
        return new DefaultSkillInstance<>(skill, resource, level, nbt);
    }

    /**
     * Creates a default skill instance with standard initial values.
     * 创建具有标准初始值的默认技能实例。
     * <p>
     * This method provides a convenience for creating skill instances with
     * sensible defaults when no existing data is available. The default configuration
     * includes:
     * 此方法为在没有现有数据的情况下创建具有合理默认值的技能实例提供便利。
     * 默认配置包括：
     * </p>
     * <ul>
     *   <li>Resource: Empty resource type (unlimited availability)</li>
     *   <li>资源：空资源类型（无限可用性）</li>
     *   <li>Level: 1 (base level)</li>
     *   <li>等级：1（基础等级）</li>
     *   <li>NBT Data: Empty compound tag (no custom properties)</li>
     *   <li>NBT 数据：空复合标签（无自定义属性）</li>
     * </ul>
     * <p>
     * This is typically used during initialization or when creating new skill
     * bindings that haven't been previously serialized.
     * 这通常在初始化期间或创建尚未序列化的新技能绑定时使用。
     * </p>
     *
     * @param skill the item skill definition for this instance
     *              此实例的物品技能定义
     * @return a new DefaultSkillInstance with default configuration
     *         具有默认配置的新 DefaultSkillInstance
     * @see DefaultSkillInstance
     * @see AllSkillResources#EMPTY
     * @see CompoundTag
     * @since 1.0.0
     */
    @Override
    public DefaultSkillInstance<T> createDefault(ItemSkill<T> skill) {
        return new DefaultSkillInstance<>(skill, AllSkillResources.EMPTY.resource(), 1, new CompoundTag());
    }
}
