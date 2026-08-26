package com.leaf.skiller;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.api.registry.SkillerRegistries;
import com.leaf.skiller.content.skill.resource.EmptyResource;
import com.leaf.skiller.foundation.SkillResource;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

/**
 * Enumeration of all built-in skill resources provided by the Skiller mod.
 * Skiller 模组提供的所有内置技能资源的枚举。
 * <p>
 * This enum defines and registers all default skill resource types that
 * can be used by skills in the mod. Skill resources represent the "fuel"
 * or "cost" system that skills consume when activated.
 * 此枚举定义和注册模组中技能可以使用的所有默认技能资源类型。
 * 技能资源代表技能激活时消耗的"燃料"或"成本"系统。
 * </p>
 * <p>
 * Each enum value corresponds to a registered skill resource type with
 * a unique identifier and factory method for creating instances.
 * 每个枚举值对应一个注册的技能资源类型，具有唯一标识符和
 * 用于创建实例的工厂方法。
 * </p>
 *
 * @author Skiller Development Team
 * @author Skiller 开发团队
 * @version 1.0.0
 * @see SkillResource
 * @see SkillerRegistries
 * @see SkillerBuiltInRegistries
 * @see ResourceKey
 * @since 1.0.0
 */
public enum AllSkillResources {
    /**
     * The empty skill resource, representing no cost or limitation.
     * 空技能资源，表示无成本或限制。
     * <p>
     * This resource type is used for skills that have no activation cost
     * or limitation. Skills using this resource can be activated freely
     * without any constraints.
     * 此资源类型用于没有激活成本或限制的技能。使用此资源的技能
     * 可以自由激活，没有任何约束。
     * </p>
     */
    EMPTY("empty", EmptyResource::new)
    ;

    /**
     * The resource key that identifies this skill resource type in registries.
     * 在注册表中标识此技能资源类型的资源键。
     * <p>
     * This key is used for referencing this resource type in data packs,
     * recipes, and other systems that need to specify resource types.
     * 此键用于在数据包、配方和其他需要指定资源类型的系统中引用
     * 此资源类型。
     * </p>
     */
    public final ResourceKey<SkillResource> key;

    /**
     * The factory method for creating new instances of this skill resource.
     * 用于创建此技能资源新实例的工厂方法。
     * <p>
     * This supplier is called during registration to create the singleton
     * instance of the resource type, and can be used to create additional
     * instances if needed.
     * 此供应商在注册期间调用以创建资源类型的单例实例，
     * 如果需要可用于创建其他实例。
     * </p>
     */
    private final Supplier<SkillResource> factory;

    /**
     * Constructs a new skill resource enum entry with the specified ID and factory.
     * 使用指定的 ID 和工厂构造新的技能资源枚举条目。
     * <p>
     * This constructor creates a resource key by combining the mod's namespace
     * with the provided ID, and stores the factory for creating resource instances.
     * 此构造函数通过将模组的命名空间与提供的 ID 结合来创建资源键，
     * 并存储用于创建资源实例的工厂。
     * </p>
     *
     * @param id The unique identifier for this resource type (without mod namespace)
     *           此资源类型的唯一标识符（不带模组命名空间）
     * @param factory A supplier that creates new instances of this resource type
     *                创建此资源类型新实例的供应商
     * @see ResourceKey
     * @see Skiller#modLoc(String)
     * @since 1.0.0
     */
    AllSkillResources(String id, Supplier<SkillResource> factory) {
        this.key = ResourceKey.create(SkillerRegistries.SKILL_RESOURCE, Skiller.modLoc(id));
        this.factory = factory;
    }

    /**
     * Creates a new instance of this skill resource type.
     * 创建此技能资源类型的新实例。
     * <p>
     * This method calls the factory supplier to generate a fresh instance
     * of the skill resource. Useful for scenarios requiring multiple
     * instances rather than the singleton registered instance.
     * 此方法调用工厂供应商生成技能资源的新实例。
     * 适用于需要多个实例而不是单例注册实例的场景。
     * </p>
     *
     * @return A new instance of this skill resource type
     *         此技能资源类型的新实例
     * @see SkillResource
     * @since 1.0.0
     */
    public SkillResource resource() {
        return factory.get();
    }

    /**
     * Registers all skill resources with the game's registry system.
     * 向游戏的注册表系统注册所有技能资源。
     * <p>
     * This method is called during mod initialization to ensure all skill
     * resource types are properly registered. The actual registration
     * occurs in the static initializer.
     * 此方法在模组初始化期间调用，确保所有技能资源类型正确注册。
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
    public static void register() {

    }

    /**
     * Static initialization block that registers all skill resources with the game registry.
     * 向游戏注册表注册所有技能资源的静态初始化块。
     * <p>
     * This block runs once when the class is loaded, registering each enum
     * value's resource instance with the appropriate registry. Each resource
     * is registered under its unique resource key.
     * 此块在类加载时运行一次，将每个枚举值的资源实例注册到
     * 相应的注册表。每个资源在其唯一的资源键下注册。
     * </p>
     *
     * @see SkillerBuiltInRegistries
     * @see Registry
     * @since 1.0.0
     */
    static {
        for (AllSkillResources entry : values()) {
            Registry.register(
                    SkillerBuiltInRegistries.SKILL_RESOURCES,
                    entry.key,
                    entry.factory.get()
            );
        }
    }
}
