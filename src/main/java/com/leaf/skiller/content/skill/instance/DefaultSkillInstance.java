package com.leaf.skiller.content.skill.instance;

import com.leaf.skiller.AllSkillInstanceFactories;
import com.leaf.skiller.foundation.SkillInstanceFactory;
import com.leaf.skiller.foundation.SkillResource;
import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.ItemSkill;
import com.leaf.skiller.foundation.skill.ItemSkillRegistration;
import net.minecraft.nbt.CompoundTag;

/**
 * Default implementation of a skill instance backed by NBT data storage.
 * 由 NBT 数据存储支持的技能实例的默认实现。
 * <p>
 * This class provides a concrete implementation of {@link ISkillInstance} that extends
 * {@link NbtSkillInstance}, offering a standard way to create skill instances with
 * persistent data storage through NBT tags.
 * 此类提供 {@link ISkillInstance} 的具体实现，扩展了 {@link NbtSkillInstance}，
 * 提供通过 NBT 标签创建具有持久化数据存储的技能实例的标准方式。
 * </p>
 * <p>
 * The generic type parameter T represents the context type for this skill instance,
 * which must extend {@link SkillContext} and defines the environment in which
 * the skill operates.
 * 泛型类型参数 T 表示此技能实例的上下文类型，必须扩展 {@link SkillContext} 并定义技能运行的环境。
 * </p>
 *
 * @param <T> the skill context type, must extend SkillContext
 *            技能上下文类型，必须扩展 SkillContext
 * @see NbtSkillInstance
 * @see ISkillInstance
 * @see com.leaf.skiller.foundation.skill.ItemSkill
 * @since 1.0.0
 */
public class DefaultSkillInstance<T extends SkillContext> extends NbtSkillInstance<T> {
    /**
     * Constructs a new DefaultSkillInstance with the specified parameters.
     * 使用指定参数构造新的 DefaultSkillInstance。
     * <p>
     * This constructor initializes a skill instance with all required components:
     * the skill definition, resource type, level, and persistent NBT data.
     * The provided NBT data is copied to ensure immutability of the original.
     * 此构造函数使用所有必需的组件初始化技能实例：技能定义、资源类型、等级和持久化 NBT 数据。
     * 提供的 NBT 数据被复制以确保原始数据的不可变性。
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
     * @see ItemSkillRegistration
     * @see SkillResource
     * @see CompoundTag
     * @since 1.0.0
     */
    public DefaultSkillInstance(ItemSkillRegistration<T> skill, SkillResource resource, int level, CompoundTag data) {
        super(skill, resource, level, data);
    }

    /**
     * Returns the factory instance used to create this type of skill instance.
     * 返回用于创建此类技能实例的工厂实例。
     * <p>
     * This method is used during serialization and deserialization to identify
     * which factory should be used to reconstruct this skill instance from data.
     * It returns the default factory instance from the registry.
     * 此方法在序列化和反序列化期间使用，用于标识应该使用哪个工厂从数据重建此技能实例。
     * 它返回注册表中的默认工厂实例。
     * </p>
     *
     * @return the skill instance factory for default skill instances
     *         默认技能实例的技能实例工厂
     * @see SkillInstanceFactory
     * @see AllSkillInstanceFactories
     * @since 1.0.0
     */
    @Override
    protected SkillInstanceFactory<T, ? extends ISkillInstance<T>> getFactory() {
        return AllSkillInstanceFactories.DEFAULT.getFactory();
    }
}
