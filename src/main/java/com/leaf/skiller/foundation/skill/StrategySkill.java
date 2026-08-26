package com.leaf.skiller.foundation.skill;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.strategy.SkillStrategy;
import net.minecraft.resources.ResourceKey;

/**
 * A skill interface that uses strategies for flexible behavior implementation.
 * 使用策略实现灵活行为的技能接口
 * <p>
 * StrategySkill extends ItemSkill to provide a strategy-based approach to skill behavior.
 * This allows for dynamic skill behavior that can be configured through different strategies
 * rather than hard-coding behavior in the skill itself.
 * StrategySkill 扩展了 ItemSkill，提供了基于策略的技能行为方法。
 * 这允许通过不同的策略配置动态技能行为，而不是在技能本身中硬编码行为
 * </p>
 * <p>
 * The strategy pattern enables:
 * 策略模式支持：
 * </p>
 * <ul>
 *     <li>Separation of skill definition from behavior implementation
 *     技能定义与行为实现的分离</li>
 *     <li>Runtime strategy switching for adaptive skills
 *     运行时策略切换以实现自适应技能</li>
 *     <li>Reusable strategy components across multiple skills
 *     跨多个技能的可重用策略组件</li>
 * </ul>
 *
 * @param <T> The strategy type parameter for this skill
 *            此技能的策略类型参数
 * @param <C> The skill context type, must extend SkillContext
 *            技能上下文类型，必须扩展 SkillContext
 * @see ItemSkill
 * @see SkillStrategy
 * @see SkillContext
 * @see com.leaf.skiller.api.registry.SkillerBuiltInRegistries#STRATEGIES
 * @since 1.0.0
 * @author Leaf
 */
public interface StrategySkill<T, C extends SkillContext> extends ItemSkill<C> {
    /**
     * Returns the resource key identifying the strategy used by this skill.
     * 返回标识此技能所使用策略的资源键
     * <p>
     * The resource key is used to look up the actual strategy implementation
     * from the strategies registry.
     * 资源键用于从策略注册表中查找实际的策略实现
     * </p>
     *
     * @return The resource key of the skill strategy
     *         技能策略的资源键
     * @see SkillStrategy
     * @see com.leaf.skiller.api.registry.SkillerBuiltInRegistries#STRATEGIES
     * @see ResourceKey
     * @since 1.0.0
     */
    ResourceKey<SkillStrategy<?, ?>> getStrategy();

    /**
     * Retrieves the actual strategy implementation for this skill from the registry.
     * 从注册表中检索此技能的实际策略实现
     * <p>
     * This default method uses the resource key returned by {@link #getStrategy()}
     * to look up the strategy instance in the built-in strategies registry.
     * 此默认方法使用 {@link #getStrategy()} 返回的资源键在内置策略注册表中查找策略实例
     * </p>
     *
     * @return The strategy implementation for this skill
     *         此技能的策略实现
     * @throws ClassCastException If the strategy type is incompatible with the expected type
     *                            如果策略类型与预期类型不兼容
     * @see #getStrategy()
     * @see SkillStrategy
     * @see com.leaf.skiller.api.registry.SkillerBuiltInRegistries#STRATEGIES
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default SkillStrategy<T, C> strategy() {
        return (SkillStrategy<T, C>) SkillerBuiltInRegistries.STRATEGIES.get(getStrategy());
    }
}
