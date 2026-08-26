package com.leaf.skiller.foundation;

/**
 * A functional interface for consuming resources or values.
 * 用于消耗资源或值的函数式接口。
 * <p>
 * Consumable represents a resource consumption operation that can be applied
 * with varying amounts.
 * Consumable表示可以应用不同数量的资源消耗操作。
 * </p>
 * <p>
 * This interface is used extensively throughout the skill system for handling
 * resource costs, including immediate consumption, delayed consumption, and
 * no-op (empty) consumption scenarios.
 * 此接口在技能系统中广泛使用，用于处理资源成本，包括立即消耗、延迟消耗和无操作（空）消耗场景。
 * </p>
 * <p>
 * Common implementations include resource-based consumption, batched consumption,
 * and empty consumption for skills with no resource cost.
 * 常见实现包括基于资源的消耗、批量消耗，以及无资源成本技能的空消耗。
 * </p>
 *
 * @since 1.0.0
 * @see SkillResource
 * @see SkillResource.DelayConsumable
 */
public interface Consumable {
    /**
     * Consumes the specified amount.
     * 消耗指定数量。
     * <p>
     * This method performs the consumption operation for the given amount.
     * 此方法对给定数量执行消耗操作。
     * </p>
     * <p>
     * The behavior depends on the implementation - it may consume actual resources,
     * accumulate for later consumption, or do nothing for empty consumables.
     * 行为取决于实现 - 可能消耗实际资源、累积以供稍后消耗，或对空可消耗项不执行任何操作。
     * </p>
     *
     * @param amount The amount to consume
     *               要消耗的数量
     *
     * @since 1.0.0
     */
    void consume(int amount);

    /**
     * Returns an empty Consumable that performs no consumption operation.
     * 返回一个不执行消耗操作的空Consumable。
     * <p>
     * This is useful for skills that have no resource cost or for situations
     * where consumption should be skipped.
     * 这对于没有资源成本的技能或应跳过消耗的情况很有用。
     * </p>
     * <p>
     * The returned instance is a singleton that ignores all consume calls.
     * 返回的实例是一个忽略所有consume调用的单例。
     * </p>
     *
     * @return An empty Consumable that performs no operations
     *         一个不执行任何操作的空Consumable
     *
     * @since 1.0.0
     * @see EmptyConsumable
     */
    static Consumable empty() {
        return new EmptyConsumable();
    }

    /**
     * An empty implementation of Consumable that performs no operations.
     * Consumable的空实现，不执行任何操作。
     * <p>
     * This class implements the Consumable interface but ignores all consume calls,
     * providing a no-op consumable for skills without resource costs.
     * 此类实现Consumable接口但忽略所有consume调用，为没有资源成本的技能提供无操作可消耗项。
     * </p>
     * <p>
     * This is more efficient than creating separate empty implementations.
     * 这比创建单独的空实现更高效。
     * </p>
     *
     * @since 1.0.0
     * @see Consumable#empty()
     */
    class EmptyConsumable implements Consumable {
        /**
         * Does nothing when called.
         * 调用时不执行任何操作。
         * <p>
         * This implementation ignores the amount parameter and performs no operation.
         * 此实现忽略数量参数且不执行任何操作。
         * </p>
         *
         * @param amount The amount to consume (ignored)
         *               要消耗的数量（被忽略）
         *
         * @since 1.0.0
         */
        public void consume(int amount) {}
    }
}
