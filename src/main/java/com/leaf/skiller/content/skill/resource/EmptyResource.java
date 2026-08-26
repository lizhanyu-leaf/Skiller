package com.leaf.skiller.content.skill.resource;

import com.leaf.skiller.AllSkillResources;
import com.leaf.skiller.foundation.SkillResource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

/**
 * A resource implementation that represents an empty or unlimited resource type.
 * 表示空或无限资源类型的资源实现。
 * <p>
 * This class provides a skill resource implementation that essentially represents
 * no resource constraint, returning a large constant value for available amounts
 * and allowing unlimited consumption. It is primarily used as a default or fallback
 * resource when skills do not require specific resource management.
 * 此类提供一种技能资源实现，本质上表示无资源约束，为可用量返回一个大的常量值
 * 并允许无限消耗。它主要用作技能不需要特定资源管理时的默认或后备资源。
 * </p>
 * <p>
 * This implementation is useful for skills that should always be usable regardless
 * of player state, such as passive skills, cooldown-based skills, or skills with
 * alternative activation conditions.
 * 此实现对于无论玩家状态如何都应始终可用的技能很有用，例如被动技能、基于冷却的技能
 * 或具有替代激活条件的技能。
 * </p>
 *
 * @see SkillResource
 * @see com.leaf.skiller.foundation.skill.ItemSkill
 * @since 1.0.0
 */
public class EmptyResource implements SkillResource {
    /**
     * Returns the resource key that identifies this empty resource type.
     * 返回标识此空资源类型的资源键。
     * <p>
     * This method provides the registry key used to identify this resource type
     * within the skill resource registry. It returns the predefined EMPTY key
     * from the central resource registry.
     * 此方法提供在技能资源注册表中用于标识此资源类型的注册表键。
     * 它返回中央资源注册表中预定义的 EMPTY 键。
     * </p>
     *
     * @return the resource key for the empty resource type
     *         空资源类型的资源键
     * @see ResourceKey
     * @see AllSkillResources
     * @since 1.0.0
     */
    @Override
    public ResourceKey<SkillResource> key() {
        return AllSkillResources.EMPTY.key;
    }

    /**
     * Returns the current amount of this resource available to the player.
     * 返回玩家当前可用的此资源数量。
     * <p>
     * This implementation returns a large constant value (114514) to simulate
     * effectively unlimited resources. This ensures that skills using this resource
     * will never be restricted by resource availability.
     * 此实现返回一个大的常量值（114514）以模拟实际上无限的资源。
     * 这确保使用此资源的技能永远不会受到资源可用性的限制。
     * </p>
     *
     * @param player the player entity to query resource amount for
     *               要查询资源数量的玩家实体
     * @return a large constant value representing unlimited resource availability
     *         表示无限资源可用性的大常量值
     * @see Player
     * @since 1.0.0
     */
    @Override
    public int getAmount(Player player) {
        return 114514;
    }

    /**
     * Consumes the specified amount of this resource from the player.
     * 从玩家消耗指定数量的此资源。
     * <p>
     * This is a no-op implementation that does nothing, as this resource type
     * represents unlimited availability. No actual resource consumption occurs,
     * and the player's state remains unchanged.
     * 这是一个空操作实现，什么都不做，因为此资源类型表示无限可用性。
     * 不发生实际的资源消耗，玩家状态保持不变。
     * </p>
     *
     * @param player the player entity to consume resources from
     *               要从中消耗资源的玩家实体
     * @param amount the amount of resource to consume (ignored in this implementation)
     *               要消耗的资源数量（在此实现中被忽略）
     * @see Player
     * @since 1.0.0
     */
    @Override
    public void consume(Player player, int amount) {}

    /**
     * Determines whether the player can consume the specified amount of this resource.
     * 确定玩家是否可以消耗指定数量的此资源。
     * <p>
     * This implementation always returns true, indicating that the player can always
     * consume any amount of this resource. This reflects the unlimited nature of
     * the empty resource type.
     * 此实现始终返回 true，表示玩家始终可以消耗任何数量的此资源。
     * 这反映了空资源类型的无限性质。
     * </p>
     *
     * @param player the player entity to check resource availability for
     *               要检查资源可用性的玩家实体
     * @param amount the amount of resource to check for consumption
     *               要检查消耗的资源数量
     * @return true, indicating unlimited resource availability
     *         true，表示无限资源可用性
     * @see Player
     * @since 1.0.0
     */
    @Override
    public boolean canConsume(Player player, int amount) {
        return true;
    }
}
