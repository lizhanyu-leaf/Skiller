package com.leaf.skiller.foundation.provider;

import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.SkillBundle;

import java.util.List;

/**
 * A collector interface for accumulating and managing skill instances during collection.
 * 收集器接口，用于在收集过程中累积和管理技能实例。
 *
 * <p>This interface provides a contract for objects that need to accumulate
 * 此接口为需要累积技能实例的对象提供契约。
 * skill instances, such as during player skill provisioning.
 * 例如在玩家技能提供期间。
 *
 * <p>Implementations can choose how to store and deduplicate skills,
 * 实现可以选择如何存储和去重技能，
 * with {@link SkillSet} providing a default implementation that keeps
 * 其中 {@link SkillSet} 提供默认实现，保留每个技能的最高等级版本。
 * the highest level version of each skill.
 *
 * <p>Collectors are typically used by {@link SkillProvider} implementations
 * 收集器通常由 {@link SkillProvider} 实现使用，
 * to provide skills to the system during collection phases.
 * 在收集阶段向系统提供技能。
 *
 * @see SkillProvider
 * @see SkillSet
 * @see ISkillInstance
 * @since 1.0.0
 */
public interface SkillCollector {

    /**
     * Adds a skill bundle to this collector.
     * 将技能捆绑添加到此收集器。
     *
     * <p>This method should add all skills contained in the bundle
     * 此方法应该将捆绑中包含的所有技能添加到收集中。
     * to the collection. The bundle may contain multiple skill instances.
     * 捆绑可能包含多个技能实例。
     *
     * <p>Implementation details:
     * 实现细节：
     * <ul>
     * <li>The collector may deduplicate skills based on skill ID.</li>
     *     <li>收集器可以基于技能ID去重技能。</li>
     * <li>The collector may select the best version when duplicates exist.</li>
     *     <li>当存在重复时，收集器可以选择最佳版本。</li>
     * </ul>
     *
     * @param bundle The skill bundle containing skills to add (required, non-null).
     *               包含要添加的技能的技能捆绑（必需，非空）。
     * @throws NullPointerException if bundle is null.
     *                                如果捆绑为null，则抛出NullPointerException。
     * @see SkillBundle
     * @see #add(ISkillInstance)
     * @see #addAll(List)
     * @since 1.0.0
     */
    void add(SkillBundle bundle);

    /**
     * Adds a single skill instance to this collector.
     * 将单个技能实例添加到此收集器。
     *
     * <p>This method adds one skill instance to the collection.
     * 此方法将一个技能实例添加到收集中。
     * The collector should handle any necessary deduplication logic.
     * 收集器应该处理任何必要的去重逻辑。
     *
     * <p>For adding multiple skills at once, consider using {@link #addAll(List)}.
     * 要一次添加多个技能，请考虑使用 {@link #addAll(List)}。
     *
     * @param instance The skill instance to add (required, non-null).
     *                 要添加的技能实例（必需，非空）。
     * @throws NullPointerException if instance is null.
     *                                如果实例为null，则抛出NullPointerException。
     * @see ISkillInstance
     * @see #add(SkillBundle)
     * @see #addAll(List)
     * @since 1.0.0
     */
    void add(ISkillInstance<?> instance);

    /**
     * Adds multiple skill instances to this collector in a single operation.
     * 在单个操作中将多个技能实例添加到此收集器。
     *
     * <p>This method provides a bulk operation for adding multiple skills
     * 此方法提供批量操作以添加多个技能，
     * which may be more efficient than individual {@link #add(ISkillInstance)} calls.
     * 这可能比单独的 {@link #add(ISkillInstance)} 调用更有效。
     *
     * <p>Implementation should iterate through the list and add each instance
     * 实现应该遍历列表并将每个实例添加到收集中，
     * to the collection, applying the same deduplication logic as single adds.
     * 应用与单个添加相同的去重逻辑。
     *
     * @param instances The list of skill instances to add (required, non-null).
     *                  要添加的技能实例列表（必需，非空）。
     * @throws NullPointerException if instances or any element is null.
     *                                如果实例或任何元素为null，则抛出NullPointerException。
     * @see #add(ISkillInstance)
     * @see #add(SkillBundle)
     * @since 1.0.0
     */
    void addAll(List<ISkillInstance<?>> instances);

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
     * Returns the number of unique skills currently in this collector.
     * 返回此收集器中当前唯一技能的数量。
     *
     * <p>The exact definition of "unique" depends on the implementation.
     * "唯一"的确切定义取决于实现。
     * For example, {@link SkillSet} counts unique skill IDs, keeping only
     * 例如，{@link SkillSet} 计算唯一技能ID，仅保留每个技能的最高等级。
     * the highest level of each skill.
     *
     * @return The number of unique skills in the collector (non-negative).
     *         收集器中唯一技能的数量（非负数）。
     * @see #isEmpty()
     * @since 1.0.0
     */
    int size();
}
