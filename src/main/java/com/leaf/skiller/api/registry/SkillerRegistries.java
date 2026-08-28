package com.leaf.skiller.api.registry;

import com.leaf.skiller.Skiller;
import com.leaf.skiller.foundation.SkillInstanceFactory;
import com.leaf.skiller.foundation.SkillResource;
import com.leaf.skiller.foundation.skill.ItemSkill;
import com.leaf.skiller.foundation.skill.ItemSkillRegistration;
import com.leaf.skiller.foundation.skill.config.SkillContextFactory;
import com.leaf.skiller.foundation.strategy.SkillStrategy;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Central registry class defining all custom registry keys for the Skiller mod system.
 * Skiller 模块系统的核心注册表类，定义所有自定义注册表键。
 *
 * <p>This class contains {@link ResourceKey} definitions for all custom registries used by Skiller.
 * 该类包含 Skiller 使用的所有自定义注册表的 {@link ResourceKey} 定义。
 *
 * <p>These registry keys are used to identify and access the various custom registries
 * 这些注册表键用于识别和访问各种自定义注册表
 * for skills, resources, factories, and strategies in the mod.
 * 模组中的技能、资源、工厂和策略。
 *
 * @see SkillerBuiltInRegistries
 * @see Registry
 * @see ResourceKey
 * @since 1.0.0
 */
public class SkillerRegistries {

    /**
     * Registry key for skill resources, which define the data and behavior of skills.
 * 技能资源的注册表键，定义技能的数据和行为。
     *
     * <p>Skill resources contain the core data structures that define how skills function.
     * 技能资源包含定义技能如何运作的核心数据结构。
     *
     * @see SkillResource
     * @since 1.0.0
     */
    public static final ResourceKey<Registry<SkillResource>> SKILL_RESOURCE = key("skill_resource");

    /**
     * Registry key for skill instance factories, which create skill instances from NBT data or other sources.
 * 技能实例工厂的注册表键，用于从 NBT 数据或其他源创建技能实例。
     *
     * <p>Factories are responsible for instantiating skills with specific configurations and state.
     * 工厂负责实例化具有特定配置和状态的技能。
     *
     * @see SkillInstanceFactory
     * @since 1.0.0
     */
    public static final ResourceKey<Registry<SkillInstanceFactory<?, ?>>> SKILL_FACTORY = key("skill_factory");

    /**
     * Registry key for item skills, which represent skills that can be attached to items.
 * 物品技能的注册表键，表示可以附加到物品上的技能。
     *
     * <p>Item skills are the main skill type that players interact with through items.
     * 物品技能是玩家通过物品交互的主要技能类型。
     *
     * @see ItemSkill
     * @since 1.0.0
     */
    public static final ResourceKey<Registry<ItemSkillRegistration<?>>> SKILL = key("skill");

    /**
     * Registry key for skill strategies, which define the behavior and logic of skill execution.
 * 技能策略的注册表键，定义技能执行的行为和逻辑。
     *
     * <p>Strategies determine how skills are activated, used, and their effects applied.
     * 策略决定技能如何激活、使用及其效果如何应用。
     *
     * @see SkillStrategy
     * @since 1.0.0
     */
    public static final ResourceKey<Registry<SkillStrategy<?, ?>>> STRATEGY = key("skill_strategy");

    public static final ResourceKey<Registry<SkillContextFactory<?>>> CONTEXT_FACTORY = key("skill_context_factory");

    /**
     * Creates a registry key with the specified path, using the Skiller mod namespace.
 * 使用指定的路径创建注册表键，使用 Skiller 模组命名空间。
     *
     * <p>This is a utility method for generating standardized registry keys.
     * 这是一个用于生成标准化注册表键的工具方法。
     *
     * @param <T> the type of the registry entries / 注册表条目的类型
     * @param path the registry path identifier / 注册表路径标识符
     * @return a ResourceKey for the registry / 注册表的 ResourceKey
     * @see ResourceKey
     * @see Skiller#modLoc(String)
     * @since 1.0.0
     */
    private static <T> ResourceKey<Registry<T>> key(String path) {
        return ResourceKey.createRegistryKey(Skiller.modLoc(path));
    }
}
