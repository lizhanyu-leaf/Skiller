package com.leaf.skiller;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.content.factory.DefaultSkillFactory;
import com.leaf.skiller.foundation.SkillData;
import com.leaf.skiller.foundation.SkillInstanceFactory;
import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.ItemSkill;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Enumeration of all built-in skill instance factories provided by the Skiller mod.
 * Skiller 模组提供的所有内置技能实例工厂的枚举。
 * <p>
 * This enum defines and registers all default skill instance factory types that
 * handle the creation and serialization of skill instances. Factories are responsible
 * for creating skill objects from skill definitions and persisted data.
 * 此枚举定义和注册所有默认技能实例工厂类型，它们处理技能实例的创建
 * 和序列化。工厂负责从技能定义和持久化数据创建技能对象。
 * </p>
 * <p>
 * Each factory type implements different strategies for instantiating skills,
 * allowing for flexible skill creation patterns and data persistence methods.
 * 每个工厂类型实现不同的技能实例化策略，允许灵活的技能创建模式
 * 和数据持久化方法。
 * </p>
 *
 * @author Skiller Development Team
 * @author Skiller 开发团队
 * @version 1.0.0
 * @see SkillInstanceFactory
 * @see ISkillInstance
 * @see ItemSkill
 * @see SkillContext
 * @since 1.0.0
 */
public enum AllSkillInstanceFactories {
    /**
     * The default skill instance factory for standard skill creation.
     * 标准技能创建的默认技能实例工厂。
     * <p>
     * This factory creates skill instances using the standard implementation
     * that supports most common skill patterns. It provides default behavior
     * for skill activation, persistence, and data handling.
     * 此工厂使用支持最常见技能模式的标准实现创建技能实例。
     * 它为技能激活、持久化和数据处理提供默认行为。
     * </p>
     */
    DEFAULT("default", DefaultSkillFactory::new)
    ;

    /**
     * The unique resource location identifying this factory in registries.
     * 在注册表中标识此工厂的唯一资源位置。
     * <p>
     * This ID is used for referencing this factory type in data packs,
     * saved data, and other systems that need to specify which factory
     * to use for skill instantiation.
     * 此 ID 用于在数据包、保存的数据和其他需要指定使用哪个工厂
     * 进行技能实例化的系统中引用此工厂类型。
     * </p>
     */
    private final ResourceLocation id;

    /**
     * The supplier that creates new instances of this skill factory.
     * 创建此技能工厂新实例的供应商。
     * <p>
     * This supplier is called during registration and when factory instances
     * are needed for skill creation. It encapsulates the factory constructor
     * and any required initialization parameters.
     * 此供应商在注册期间以及需要工厂实例进行技能创建时调用。
     * 它封装工厂构造函数和任何所需的初始化参数。
     * </p>
     */
    private final Supplier<SkillInstanceFactory<?, ?>> factory;

    /**
     * Constructs a new skill instance factory enum entry with the specified ID and factory supplier.
     * 使用指定的 ID 和工厂供应商构造新的技能实例工厂枚举条目。
     * <p>
     * This constructor creates a namespaced resource location for the factory
     * ID and stores the supplier for creating factory instances.
     * 此构造函数为工厂 ID 创建命名空间资源位置，并存储用于创建
     * 工厂实例的供应商。
     * </p>
     *
     * @param id The unique identifier for this factory type (without mod namespace)
     *           此工厂类型的唯一标识符（不带模组命名空间）
     * @param factory A supplier that creates new instances of this factory type
     *                创建此工厂类型新实例的供应商
     * @see ResourceLocation
     * @see Skiller#modLoc(String)
     * @since 1.0.0
     */
    AllSkillInstanceFactories(String id, Supplier<SkillInstanceFactory<?, ?>> factory) {
        this.id = Skiller.modLoc(id);
        this.factory = factory;
    }

    /**
     * Retrieves the skill instance factory for this entry, type-cast to the specified context type.
     * 检索此条目的技能实例工厂，类型转换为指定的上下文类型。
     * <p>
     * This method provides type-safe access to the factory by casting it to
     * work with the specified skill context type. The unchecked cast is safe
     * because factories are registered with their correct types.
     * 此方法通过将工厂转换为使用指定的技能上下文类型来提供类型安全
     * 的访问。未检查的转换是安全的，因为工厂以正确的类型注册。
     * </p>
     *
     * @param <T> The skill context type that this factory works with
     *            此工厂使用的技能上下文类型
     * @return A type-casted skill instance factory for the specified context type
     *         用于指定上下文类型的类型转换技能实例工厂
     * @see SkillInstanceFactory
     * @see SkillContext
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public <T extends SkillContext> SkillInstanceFactory<T, ? extends ISkillInstance<T>> getFactory() {
        return (SkillInstanceFactory<T, ? extends ISkillInstance<T>>) factory.get();
    }

    /**
     * Static initialization block that registers all skill instance factories with the game registry.
     * 向游戏注册表注册所有技能实例工厂的静态初始化块。
     * <p>
     * This block runs once when the class is loaded, registering each enum
     * value's factory instance with the appropriate registry. Each factory
     * is registered under its unique resource location.
     * 此块在类加载时运行一次，将每个枚举值的工厂实例注册到
     * 相应的注册表。每个工厂在其唯一的资源位置下注册。
     * </p>
     *
     * @see SkillerBuiltInRegistries
     * @see Registry
     * @since 1.0.0
     */
    static {
        for (AllSkillInstanceFactories entry : values()) {
            Registry.register(
                    SkillerBuiltInRegistries.SKILL_FACTORIES,
                    entry.id,
                    entry.getFactory()
            );
        }
    }

    /**
     * Registers all skill instance factories with the game's registry system.
     * 向游戏的注册表系统注册所有技能实例工厂。
     * <p>
     * This method is called during mod initialization to ensure all factory
     * types are properly registered. The actual registration occurs in the
     * static initializer.
     * 此方法在模组初始化期间调用，确保所有工厂类型正确注册。
     * 实际注册发生在静态初始化块中。
     * </p>
     * <p>
     * This method is intentionally empty as registration occurs during
     * class initialization. It exists for API consistency with other
     * registry classes.
     * 此方法有意留空，因为注册在类初始化期间发生。
     * 它的存在是为了与其他注册表类保持 API 一致性。
     * </p>
     *
     * @see SkillerBuiltInRegistries
     * @since 1.0.0
     */
    public static void register() {}

    /**
     * Creates a custom skill instance factory with the specified creation and serialization functions.
     * 使用指定的创建和序列化函数创建自定义技能实例工厂。
     * <p>
     * This utility method provides a concise way to create skill instance factories
     * using lambda expressions or method references. It encapsulates the three
     * core operations required of all skill factories.
     * 此实用方法提供了一种使用 lambda 表达式或方法引用创建技能实例
     * 工厂的简洁方式。它封装了所有技能工厂所需的三个核心操作。
     * </p>
     * <p>
     * The factory creates skill instances in two ways:
     * 工厂以两种方式创建技能实例：
     * <ul>
     * <li>From an ItemSkill definition (default state)
     * 从 ItemSkill 定义（默认状态）</li>
     * <li>From persisted SkillData (restoring saved state)
     * 从持久化的 SkillData（恢复保存的状态）</li>
     * </ul>
     * And can serialize instances back to SkillData.
     * 并可以将实例序列化回 SkillData。
     * </p>
     *
     * @param <T> The skill context type
     *            技能上下文类型
     * @param <I> The skill instance type
     *            技能实例类型
     * @param createDefault Function to create a skill instance from an ItemSkill definition
     *                      从 ItemSkill 定义创建技能实例的函数
     * @param createData Function to create a skill instance from persisted SkillData
     *                   从持久化的 SkillData 创建技能实例的函数
     * @param toData Function to serialize a skill instance back to SkillData
     *               将技能实例序列化回 SkillData 的函数
     * @return A new skill instance factory with the specified behavior
     *         具有指定行为的新技能实例工厂
     * @see SkillInstanceFactory
     * @see ItemSkill
     * @see ISkillInstance
     * @see SkillData
     * @since 1.0.0
     */
    private static <T extends SkillContext, I extends ISkillInstance<T>> SkillInstanceFactory<T, I> of(
            Function<ItemSkill<T>, I> createDefault, Function<SkillData, I> createData, Function<I, SkillData> toData
    ) {
        return new SkillInstanceFactory<>() {
            @Override
            public I createDefault(ItemSkill<T> skill) {
                return createDefault.apply(skill);
            }

            @Override
            public I createFromData(SkillData data) {
                return createData.apply(data);
            }

            @Override
            public SkillData toData(I instance) {
                return toData.apply(instance);
            }
        };
    }
}
