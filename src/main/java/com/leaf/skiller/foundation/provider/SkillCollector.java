package com.leaf.skiller.foundation.provider;

import com.leaf.skiller.content.skill.SkillComponent;

/**
 * A collector interface for merging skill components during collection.
 * 收集器接口，用于在收集过程中合并技能组件。
 *
 * <p>This interface provides a contract for objects that need to merge
 * {@link SkillComponent}s, such as during player skill provisioning.
 * Each component carries its own key bindings, and the collector decides
 * how to combine them.
 * 此接口为需要合并 {@link SkillComponent} 的对象提供契约，
 * 例如在玩家技能提供期间。每个组件携带自己的按键绑定，
 * 由收集器决定如何组合它们。
 *
 * <p>Implementations can choose how to store and deduplicate skills,
 * 实现可以选择如何存储和去重技能，
 * with {@link SkillSet} providing a default implementation that keeps
 * 其中 {@link SkillSet} 提供默认实现，按按键分别保留每个技能的最高等级版本。
 * the highest level version of each skill per key binding.
 *
 * <p>Collectors are typically used by {@link SkillProvider} implementations
 * 收集器通常由 {@link SkillProvider} 实现使用，
 * to provide skills to the system during collection phases.
 * 在收集阶段向系统提供技能。
 *
 * @see SkillProvider
 * @see SkillSet
 * @see SkillComponent
 * @since 1.0.0
 */
public interface SkillCollector {

    /**
     * Merges a skill component into this collector.
     * 将一个技能组件合并到此收集器。
     *
     * <p>The component carries skill instances together with their key bindings.
     * The collector should handle any necessary deduplication logic when the
     * same skill is already present for the same key.
     * 组件携带技能实例及其按键绑定。
     * 当同一按键下已存在相同技能时，收集器应该处理任何必要的去重逻辑。
     *
     * @param component The skill component to merge (required, non-null).
     *                  要合并的技能组件（必需，非空）。
     * @throws NullPointerException if component is null.
     *                                如果组件为null，则抛出NullPointerException。
     * @see SkillComponent
     * @see SkillSet
     * @since 1.0.0
     */
    void add(SkillComponent component);

    /**
     * Checks if this collector is empty (contains no skills).
     * 检查此收集器是否为空（不包含任何技能）。
     *
     * <p>Returns {@code true} if no skills have been added to this collector,
     * 如果没有技能被添加到此收集器，则返回 {@code true}，
     * {@code false} otherwise.
     * 否则返回 {@code false}。
     *
     * @return {@code true} if the collector contains no skills, {@code false} otherwise.
     *         如果收集器不包含任何技能，则返回 {@code true}，否则返回 {@code false}。
     * @see #size()
     * @since 1.0.0
     */
    boolean isEmpty();

    /**
     * Returns the number of unique skill entries currently in this collector.
     * 返回此收集器中当前唯一技能条目的数量。
     *
     * <p>The exact definition of "unique" depends on the implementation.
     * "唯一"的确切定义取决于实现。
     * For example, {@link SkillSet} counts unique (key binding, skill ID) pairs,
     * 例如，{@link SkillSet} 计数唯一的（按键绑定，技能ID）对，
     * keeping only the highest level of each skill per key.
     * 按按键仅保留每个技能的最高等级。
     *
     * @return The number of unique skill entries in the collector (non-negative).
     *         收集器中唯一技能条目的数量（非负数）。
     * @see #isEmpty()
     * @since 1.0.0
     */
    int size();
}
