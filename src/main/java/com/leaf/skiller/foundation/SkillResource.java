package com.leaf.skiller.foundation;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Represents a resource that can be consumed by skill execution.
 * 表示可以被技能执行消耗的资源。
 * <p>
 * SkillResources define the various types of costs associated with using skills,
 * such as mana, stamina, energy, or other custom resource types.
 * 技能资源定义了使用技能相关的各种成本类型，如法力、体力、能量或其他自定义资源类型。
 * </p>
 * <p>
 * This interface provides methods to query current amounts, validate consumption,
 * and perform the actual resource consumption.
 * 此接口提供查询当前数量、验证消耗和执行实际资源消耗的方法。
 * </p>
 * <p>
 * Implementations are registered in the resource registry and can be referenced
 * by ResourceKey for type-safe resource handling.
 * 实现在资源注册表中注册，可以通过ResourceKey引用以进行类型安全的资源处理。
 * </p>
 *
 * @since 1.0.0
 * @see Consumable
 * @see ResourceKey
 */
public interface SkillResource {
    /**
     * Returns the registry key that uniquely identifies this resource type.
     * 返回唯一标识此资源类型的注册表键。
     * <p>
     * This key is used to reference this resource type throughout the system.
     * 此键用于在整个系统中引用此资源类型。
     * </p>
     *
     * @return The resource key for this skill resource type
     *         此技能资源类型的资源键
     *
     * @since 1.0.0
     * @see ResourceKey
     */
    ResourceKey<SkillResource> key();

    /**
     * Returns the current amount of this resource for the given player.
     * 返回给定玩家的此资源的当前数量。
     * <p>
     * This method queries the player's current resource level, which may be
     * affected by various game mechanics and previous consumptions.
     * 此方法查询玩家当前的资源等级，可能受各种游戏机制和先前消耗的影响。
     * </p>
     *
     * @param player The player to query the resource amount for
     *               要查询资源数量的玩家
     *
     * @return The current amount of this resource available to the player
     *         玩家当前可用的此资源数量
     *
     * @since 1.0.0
     */
    int getAmount(Player player);

    /**
     * Consumes the specified amount of this resource from the player.
     * 从玩家消耗指定数量的此资源。
     * <p>
     * This method performs the actual resource deduction and should only be
     * called after validating with canConsume.
     * 此方法执行实际的资源扣除，仅在通过canConsume验证后才应调用。
     * </p>
     * <p>
     * Implementations should handle the resource consumption logic and update
     * the player's resource state accordingly.
     * 实现应处理资源消耗逻辑并相应地更新玩家的资源状态。
     * </p>
     *
     * @param player The player to consume resources from
     *               要从中消耗资源的玩家
     * @param amount The amount of resource to consume
     *               要消耗的资源数量
     *
     * @throws IllegalArgumentException if the amount is negative
     *         如果数量为负数
     *
     * @since 1.0.0
     * @see #canConsume(Player, int)
     */
    void consume(Player player, int amount);

    /**
     * Checks whether the player has enough of this resource to consume the specified amount.
     * 检查玩家是否有足够的此资源来消耗指定数量。
     * <p>
     * This method validates resource availability without actually consuming the resource.
     * 此方法验证资源可用性，而不实际消耗资源。
     * </p>
     * <p>
     * Should be called before consume to ensure the player has sufficient resources.
     * 应在consume之前调用，以确保玩家有足够的资源。
     * </p>
     *
     * @param player The player to check resource availability for
     *               要检查资源可用性的玩家
     * @param amount The amount of resource to check for
     *               要检查的资源数量
     *
     * @return true if the player has at least the specified amount of this resource
     *         如果玩家拥有至少指定数量的此资源，则返回true
     *
     * @since 1.0.0
     * @see #consume(Player, int)
     * @see #getAmount(Player)
     */
    boolean canConsume(Player player, int amount);

    /**
     * Creates a Consumable for this resource and player that consumes immediately.
     * 为此资源和玩家创建一个立即消耗的Consumable。
     * <p>
     * The returned Consumable will check availability and consume the resource
     * in a single operation.
     * 返回的Consumable将在单个操作中检查可用性并消耗资源。
     * </p>
     *
     * @param player The player to create the consumable for
     *               要为其创建可消耗项的玩家
     *
     * @return A Consumable that consumes this resource immediately
     *         一个立即消耗此资源的Consumable
     *
     * @since 1.0.0
     * @see Consumable
     */
    default Consumable getConsumable(Player player) {
        return amount -> {
            if (canConsume(player, amount))
                consume(player, amount);
        };
    }

    /**
     * Creates a DelayConsumable for deferred resource consumption.
     * 创建一个DelayConsumable用于延迟资源消耗。
     * <p>
     * DelayConsumables allow accumulation of resource costs and batch consumption,
     * useful for skills with multiple resource requirements.
     * DelayConsumable允许累积资源成本和批量消耗，适用于具有多个资源需求的技能。
     * </p>
     *
     * @param player The player to create the delay consumable for
     *               要为其创建延迟可消耗项的玩家
     *
     * @return A DelayConsumable for deferred consumption of this resource
     *         一个用于延迟消耗此资源的DelayConsumable
     *
     * @since 1.0.0
     * @see DelayConsumable
     */
    default DelayConsumable getDelayConsumable(Player player) {
        return DelayConsumable.of(this::canConsume, this::consume).apply(player);
    }

    /**
     * A Consumable that accumulates resource costs and applies them later.
     * 累积资源成本并在之后应用的Consumable。
     * <p>
     * DelayConsumable is useful for skills that need to calculate total resource
     * costs before consuming, or when resource consumption should be batched.
     * DelayConsumable适用于需要在消耗前计算总资源成本，或资源消耗应分批处理的技能。
     * </p>
     * <p>
     * Resource costs are accumulated via the consume method, then applied all at once
     * via the apply method after validation.
     * 资源成本通过consume方法累积，然后在验证后通过apply方法一次性应用。
     * </p>
     *
     * @since 1.0.0
     * @see Consumable
     * @see SkillResource#getDelayConsumable(Player)
     */
    abstract class DelayConsumable implements Consumable {
        /**
         * The accumulated amount of resource to consume.
         * 要消耗的累积资源数量。
         */
        protected int amount = 0;

        /**
         * The player whose resources will be consumed.
         * 将消耗其资源的玩家。
         */
        protected final Player player;

        /**
         * Creates a new DelayConsumable for the given player.
         * 为给定玩家创建新的DelayConsumable。
         *
         * @param player The player this consumable is associated with
         *               此可消耗项关联的玩家
         *
         * @since 1.0.0
         */
        protected DelayConsumable(Player player) {
            this.player = player;
        }

        /**
         * Applies the accumulated resource consumption.
         * 应用累积的资源消耗。
         * <p>
         * This method should be called after accumulating all costs via consume,
         * and after validating with canConsume.
         * 此方法应在通过consume累积所有成本后，并在使用canConsume验证后调用。
         * </p>
         * <p>
         * Implementations should consume the accumulated amount from the player.
         * 实现应从玩家处消耗累积的数量。
         * </p>
         *
         * @since 1.0.0
         * @see #canConsume()
         */
        public abstract void apply();

        /**
         * Checks whether the accumulated amount can be consumed.
         * 检查累积的数量是否可以消耗。
         * <p>
         * This method validates that the player has sufficient resources
         * for the accumulated amount.
         * 此方法验证玩家是否有足够的资源用于累积的数量。
         * </p>
         *
         * @return true if the accumulated amount can be consumed
         *         如果累积的数量可以消耗，则返回true
         *
         * @since 1.0.0
         * @see #apply()
         */
        public abstract boolean canConsume();

        /**
         * Accumulates the specified amount to the pending consumption total.
         * 将指定数量累积到待消耗总计中。
         * <p>
         * This adds to the accumulated amount rather than replacing it.
         * 此方法将数量添加到累积总量中，而不是替换它。
         * </p>
         *
         * @param amount The amount to add to the accumulated total
         *               要添加到累积总量的数量
         *
         * @since 1.0.0
         * @see #apply()
         */
        @Override
        public void consume(int amount) {
            this.amount += amount;
        }

        /**
         * Creates a factory function for DelayConsumables with custom validation and consumption logic.
         * 创建具有自定义验证和消耗逻辑的DelayConsumable工厂函数。
         * <p>
         * This static method allows creation of DelayConsumables with custom behavior
         * through the provided BiPredicate and BiConsumer.
         * 此静态方法允许通过提供的BiPredicate和BiConsumer创建具有自定义行为的DelayConsumable。
         * </p>
         *
         * @param canConsume A predicate that checks if the player can consume the amount
         *                   检查玩家是否可以消耗数量的谓词
         * @param consumer   A consumer that performs the actual resource consumption
         *                   执行实际资源消耗的消费者
         *
         * @return A function that creates DelayConsumables for players
         *         为玩家创建DelayConsumable的函数
         *
         * @since 1.0.0
         * @see BiPredicate
         * @see BiConsumer
         */
        public static Function<Player, DelayConsumable> of(BiPredicate<Player, Integer> canConsume,
                                                           BiConsumer<Player, Integer> consumer) {
            return p -> new DelayConsumable(p) {
                @Override
                public void apply() {
                    consumer.accept(player, amount);
                }

                @Override
                public boolean canConsume() {
                    return canConsume.test(player, amount);
                }
            };
        }
    }
}