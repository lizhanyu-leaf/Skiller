package com.leaf.skiller.api.registry;

import com.leaf.skiller.foundation.SkillInstanceFactory;
import com.leaf.skiller.foundation.SkillResource;
import com.leaf.skiller.foundation.skill.ItemSkill;
import com.leaf.skiller.foundation.strategy.SkillStrategy;
import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import net.neoforged.neoforge.registries.RegistryBuilder;

/**
 * Factory class for creating and registering all built-in registries for the Skiller mod system.
 * 为 Skiller 模块系统创建和注册所有内置注册表的工厂类。
 *
 * <p>This class initializes the actual registry instances that will hold the game data
 * 该类初始化将保存游戏数据的实际注册表实例
 * for skills, resources, factories, and strategies.
 * 用于技能、资源、工厂和策略。
 *
 * <p>Registries created here are synchronized across client and server in multiplayer.
 * 此处创建的注册表在多人游戏中的客户端和服务器之间同步。
 *
 * <p>This class should not be instantiated - all members are static.
 * 该类不应被实例化 - 所有成员都是静态的。
 *
 * @see SkillerRegistries
 * @see Registry
 * @see RegistryBuilder
 * @since 1.0.0
 */
public class SkillerBuiltInRegistries {

    /**
     * The built-in registry for skill resources, containing all registered skill resource types.
 * 技能资源的内置注册表，包含所有已注册的技能资源类型。
     *
     * <p>This registry holds the definitions for all skill resources in the game.
     * 该注册表保存游戏中所有技能资源的定义。
     *
     * @see SkillResource
     * @see SkillerRegistries#SKILL_RESOURCE
     * @since 1.0.0
     */
    public static final Registry<SkillResource> SKILL_RESOURCES = simple(SkillerRegistries.SKILL_RESOURCE);

    /**
     * The built-in registry for skill instance factories, containing all registered factory types.
 * 技能实例工厂的内置注册表，包含所有已注册的工厂类型。
     *
     * <p>This registry holds the factories responsible for creating skill instances.
     * 该注册表保存负责创建技能实例的工厂。
     *
     * @see SkillInstanceFactory
     * @see SkillerRegistries#SKILL_FACTORY
     * @since 1.0.0
     */
    public static final Registry<SkillInstanceFactory<?, ?>> SKILL_FACTORIES = simple(SkillerRegistries.SKILL_FACTORY);

    /**
     * The built-in registry for item skills, containing all registered skill definitions.
 * 物品技能的内置注册表，包含所有已注册的技能定义。
     *
     * <p>This registry holds all item-based skill definitions that players can use.
     * 该注册表保存玩家可以使用的所有基于物品的技能定义。
     *
     * @see ItemSkill
     * @see SkillerRegistries#SKILL
     * @since 1.0.0
     */
    public static final Registry<ItemSkill<?>> SKILLS = simple(SkillerRegistries.SKILL);

    /**
     * The built-in registry for skill strategies, containing all registered strategy implementations.
 * 技能策略的内置注册表，包含所有已注册的策略实现。
     *
     * <p>This registry holds the strategy implementations that define skill behavior.
     * 该注册表保存定义技能行为的策略实现。
     *
     * @see SkillStrategy
     * @see SkillerRegistries#STRATEGY
     * @since 1.0.0
     */
    public static final Registry<SkillStrategy<?, ?>> STRATEGIES = simple(SkillerRegistries.STRATEGY);

    /**
     * Creates a simple registry without intrusive holders or freeze callbacks.
 * 创建一个简单的注册表，没有侵入式持有者或冻结回调。
     *
     * <p>This is a convenience method for creating basic registries with default configuration.
     * 这是一个用于创建具有默认配置的基本注册表的便捷方法。
     *
     * @param <T> the type of registry entries / 注册表条目的类型
     * @param key the registry key identifying this registry / 识别此注册表的注册表键
     * @return the created registry / 创建的注册表
     * @see #register(ResourceKey, boolean, Runnable)
     * @since 1.0.0
     */
    private static <T> Registry<T> simple(ResourceKey<Registry<T>> key) {
        return register(key, false, () -> {});
    }

    /**
     * Creates a simple registry with a custom freeze callback but no intrusive holders.
 * 创建一个带有自定义冻结回调但没有侵入式持有者的简单注册表。
     *
     * <p>The freeze callback is invoked when the registry is frozen during game initialization.
     * 冻结回调在游戏初始化期间注册表被冻结时被调用。
     *
     * @param <T> the type of registry entries / 注册表条目的类型
     * @param key the registry key identifying this registry / 识别此注册表的注册表键
     * @param onBakeCallback the callback to run when the registry is baked/frozen / 注册表被烘焙/冻结时运行的回调
     * @return the created registry / 创建的注册表
     * @see #register(ResourceKey, boolean, Runnable)
     * @since 1.0.0
     */
    private static <T> Registry<T> simpleWithFreezeCallback(ResourceKey<Registry<T>> key, Runnable onBakeCallback) {
        return register(key, false, onBakeCallback);
    }

    /**
     * Creates a registry with intrusive holders enabled, allowing reference to entries before registration.
 * 创建一个启用了侵入式持有者的注册表，允许在注册之前引用条目。
     *
     * <p>Intrusive holders are useful for registries where entries need to reference each other.
     * 侵入式持有者对于条目需要相互引用的注册表很有用。
     *
     * @param <T> the type of registry entries / 注册表条目的类型
     * @param key the registry key identifying this registry / 识别此注册表的注册表键
     * @return the created registry / 创建的注册表
     * @see #register(ResourceKey, boolean, Runnable)
     * @since 1.0.0
     */
    private static <T> Registry<T> withIntrusiveHolders(ResourceKey<Registry<T>> key) {
        return register(key, true, () -> {});
    }

    /**
     * Creates and registers a custom registry with the specified configuration.
 * 使用指定的配置创建和注册自定义注册表。
     *
     * <p>This is the core registry creation method that builds and registers the registry
     * 这是核心注册表创建方法，构建和注册注册表
     * with Minecraft's built-in registry system.
     * 与 Minecraft 的内置注册表系统。
     *
     * <p>The registry is automatically synchronized between client and server in multiplayer.
     * 注册表在多人游戏中自动在客户端和服务器之间同步。
     *
     * @param <T> the type of registry entries / 注册表条目的类型
     * @param key the registry key identifying this registry / 识别此注册表的注册表键
     * @param hasIntrusiveHolders whether to enable intrusive holders for this registry / 是否为此注册表启用侵入式持有者
     * @param onBakeCallback the callback to run when the registry is baked/frozen / 注册表被烘焙/冻结时运行的回调
     * @return the created and registered registry / 创建并注册的注册表
     * @see RegistryBuilder
     * @see BuiltInRegistries#REGISTRY
     * @since 1.0.0
     */
    @SuppressWarnings({"deprecation", "unchecked", "rawtypes"})
    private static <T> Registry<T> register(ResourceKey<Registry<T>> key, boolean hasIntrusiveHolders, Runnable onBakeCallback) {
        RegistryBuilder<T> builder = new RegistryBuilder<>(key)
                .sync(true);

        if (hasIntrusiveHolders)
            builder.withIntrusiveHolders();

        builder.onBake(r -> onBakeCallback.run());

        Registry<T> registry = builder.create();
        ((WritableRegistry) BuiltInRegistries.REGISTRY)
                .register(key, registry, RegistrationInfo.BUILT_IN);
        return registry;
    }

    /**
     * Initializes the built-in registries by ensuring this class is loaded.
 * 通过确保此类被加载来初始化内置注册表。
     *
     * <p>This method is called during game initialization to trigger the static initialization
     * 该方法在游戏初始化期间被调用，以触发静态初始化
     * of all registry fields in this class.
     * 此类中所有注册表字段的。
     *
     * <p>Marked as {@literal @}Internal to indicate it is not intended for public API use.
     * 标记为 {@literal @}Internal 以表明它不打算供公共 API 使用。
     *
     * @apiNote This method should only be called by the mod's initialization system.
     * @apiNote 此方法只能由模组的初始化系统调用。
     * @see Internal
     * @since 1.0.0
     */
    @Internal
    public static void init() {
        // make sure the class is loaded.
        // this method is called at the tail of BuiltInRegistries
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
 * 私有构造函数，防止实例化此工具类。
     *
     * <p>All members of this class are static and should be accessed directly.
     * 此类的所有成员都是静态的，应该直接访问。
     *
     * @throws AssertionError always thrown if instantiation is attempted / 如果尝试实例化则总是抛出
     * @since 1.0.0
     */
    private SkillerBuiltInRegistries() {
        throw new AssertionError("This class should not be instantiated");
    }
}
