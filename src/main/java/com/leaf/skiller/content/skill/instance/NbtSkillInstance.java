package com.leaf.skiller.content.skill.instance;

import com.leaf.skiller.foundation.SkillResource;
import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.ItemSkill;
import com.leaf.skiller.foundation.skill.ItemSkillRegistration;
import net.minecraft.nbt.CompoundTag;

/**
 * Abstract base class for skill instances backed by NBT data storage.
 * 由 NBT 数据存储支持的技能实例的抽象基类。
 * <p>
 * This class provides a foundation for implementing skill instances that store
 * their persistent data in NBT (Named Binary Tag) format, which is Minecraft's
 * standard data serialization format. It manages the core components of a skill:
 * the skill definition, resource type, level, and associated data.
 * 此类为实现以 NBT（命名二进制标签）格式存储持久化数据的技能实例提供基础，
 * 这是 Minecraft 的标准数据序列化格式。它管理技能的核心组件：技能定义、资源类型、等级和关联数据。
 * </p>
 * <p>
 * Subclasses should extend this class to provide specific behavior while inheriting
 * the robust NBT-based persistence mechanism. The NBT data is copied upon construction
 * to ensure data isolation between instances.
 * 子类应扩展此类以提供特定行为，同时继承健壮的基于 NBT 的持久化机制。
 * NBT 数据在构造时被复制以确保实例之间的数据隔离。
 * </p>
 *
 * @param <T> the skill context type, must extend SkillContext
 *            技能上下文类型，必须扩展 SkillContext
 * @see ISkillInstance
 * @see ItemSkill
 * @see SkillResource
 * @see CompoundTag
 * @since 1.0.0
 */
public abstract class NbtSkillInstance<T extends SkillContext> extends ISkillInstance<T> {

    /**
     * The skill definition that this instance is based on.
     * 此实例所基于的技能定义。
     * <p>
     * This field stores the reference to the ItemSkill that defines the behavior,
     * properties, and metadata for this skill instance.
     * 此字段存储对 ItemSkill 的引用，该引用定义了此技能实例的行为、属性和元数据。
     * </p>
     */
    private final ItemSkillRegistration<T> skill;

    /**
     * The resource type associated with this skill instance.
     * 与此技能实例关联的资源类型。
     * <p>
     * This field defines what type of resource is consumed or managed by this skill,
     * such as mana, stamina, energy, or other game resources.
     * 此字段定义此技能消耗或管理的资源类型，如法力、体力、能量或其他游戏资源。
     * </p>
     */
    private final SkillResource resource;

    /**
     * The NBT compound tag containing persistent data for this skill instance.
     * 包含此技能实例持久化数据的 NBT 复合标签。
     * <p>
     * This field stores all persistent data associated with the skill instance,
     * including custom properties, state information, and any other data that
     * needs to survive across game sessions.
     * 此字段存储与技能实例相关的所有持久化数据，包括自定义属性、状态信息和任何需要在游戏会话之间保留的其他数据。
     * </p>
     */
    private final CompoundTag data;

    /**
     * The level of this skill instance.
     * 此技能实例的等级。
     * <p>
     * This field represents the power or proficiency level of the skill,
     * typically affecting damage, duration, resource costs, or other properties.
     * Higher levels generally indicate more powerful or effective skills.
     * 此字段表示技能的威力或熟练度等级，通常影响伤害、持续时间、资源消耗或其他属性。
     * 较高的等级通常表示更强大或更有效的技能。
     * </p>
     */
    private final int level;

    /**
     * Constructs a new NbtSkillInstance with the specified components.
     * 使用指定组件构造新的 NbtSkillInstance。
     * <p>
     * This constructor initializes all core components of the skill instance.
     * The provided NBT data is copied to ensure that modifications to the
     * original tag do not affect this instance, maintaining data isolation.
     * 此构造函数初始化技能实例的所有核心组件。提供的 NBT 数据被复制以确保
     * 对原始标签的修改不会影响此实例，从而保持数据隔离。
     * </p>
     *
     * @param skill    the item skill definition that this instance represents
     *                 此实例代表的物品技能定义
     * @param resource the skill resource type for consumption/management
     *                 用于消耗/管理的技能资源类型
     * @param level    the skill level, affecting behavior and power
     *                 技能等级，影响行为和威力
     * @param data     the NBT compound tag containing persistent skill data
     *                 包含持久化技能数据的 NBT 复合标签
     * @throws NullPointerException if skill, resource, or data is null
     *                               如果 skill、resource 或 data 为 null 则抛出异常
     * @see ItemSkill
     * @see SkillResource
     * @see CompoundTag
     * @since 1.0.0
     */
    protected NbtSkillInstance(ItemSkillRegistration<T> skill, SkillResource resource, int level, CompoundTag data) {
        this.skill = skill;
        this.resource = resource;
        this.level = level;
        this.data = data.copy();
    }

    /**
     * Returns the skill definition that this instance is based on.
     * 返回此实例所基于的技能定义。
     * <p>
     * This method provides access to the ItemSkill that defines the fundamental
     * behavior and properties of this skill instance, including metadata, identifiers,
     * and default configuration.
     * 此方法提供对 ItemSkill 的访问，该 ItemSkill 定义了此技能实例的基本行为和属性，
     * 包括元数据、标识符和默认配置。
     * </p>
     *
     * @return the item skill definition for this instance
     *         此实例的物品技能定义
     * @see ItemSkill
     * @since 1.0.0
     */
    @Override
    public ItemSkillRegistration<T> skill() {
        return skill;
    }

    /**
     * Returns the resource type associated with this skill instance.
     * 返回与此技能实例关联的资源类型。
     * <p>
     * This method provides access to the SkillResource that defines what type of
     * resource this skill consumes or manages, such as checking availability,
     * consuming resources, or displaying resource information to players.
     * 此方法提供对 SkillResource 的访问，该 SkillResource 定义此技能消耗或管理的资源类型，
     * 如检查可用性、消耗资源或向玩家显示资源信息。
     * </p>
     *
     * @return the skill resource type for this instance
     *         此实例的技能资源类型
     * @see SkillResource
     * @since 1.0.0
     */
    @Override
    public SkillResource getResource() {
        return resource;
    }

    /**
     * Returns the level of this skill instance.
     * 返回此技能实例的等级。
     * <p>
     * This method provides access to the skill level, which is a core property
     * that influences the power, effectiveness, and behavior of the skill.
     * Higher levels typically correspond to increased damage, duration, range,
     * or other beneficial effects.
     * 此方法提供对技能等级的访问，这是影响技能威力、有效性和行为的核心属性。
     * 较高的等级通常对应于增加的伤害、持续时间、范围或其他有益效果。
     * </p>
     *
     * @return the skill level for this instance
     *         此实例的技能等级
     * @since 1.0.0
     */
    @Override
    public int level() {
        return level;
    }

    /**
     * Returns a copy of the NBT data associated with this skill instance.
     * 返回与此技能实例关联的 NBT 数据的副本。
     * <p>
     * This method provides access to the persistent data stored in this skill instance.
     * A copy of the NBT tag is returned to prevent external modifications from affecting
     * the internal state of this instance, maintaining data integrity and encapsulation.
     * 此方法提供对此技能实例中存储的持久化数据的访问。返回 NBT 标签的副本以防止
     * 外部修改影响此实例的内部状态，从而维护数据完整性和封装性。
     * </p>
     *
     * @return a copy of the NBT compound tag containing this skill's persistent data
     *         包含此技能持久化数据的 NBT 复合标签的副本
     * @see CompoundTag
     * @since 1.0.0
     */
    @Override
    public CompoundTag data() {
        return data.copy();
    }
}
