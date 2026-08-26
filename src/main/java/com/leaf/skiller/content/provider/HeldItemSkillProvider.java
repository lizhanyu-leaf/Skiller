package com.leaf.skiller.content.provider;

import com.leaf.skiller.foundation.provider.SkillCollector;
import com.leaf.skiller.foundation.provider.SkillProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * A skill provider that collects skills from items held by the player.
 * 从玩家持有的物品中收集技能的技能提供者。
 * <p>
 * This class implements {@link SkillProvider} to provide skills that are associated
 * with items currently held by the player, including both the main hand and offhand
 * items. It enables dynamic skill acquisition based on equipment, allowing players
 * to gain access to different skills by equipping different items.
 * 此类实现 {@link SkillProvider} 以提供与玩家当前持有的物品相关联的技能，
 * 包括主手和副手物品。它实现基于装备的动态技能获取，允许玩家通过装备不同的物品来获得不同的技能。
 * </p>
 * <p>
 * The provider examines both hand slots and collects any skill bindings associated
 * with the held items, making it a fundamental component for item-based skill systems
 * where equipment determines available abilities.
 * 提供者检查两个手槽并收集与持有物品相关联的任何技能绑定，
 * 使其成为基于物品的技能系统的基本组件，其中装备决定可用的能力。
 * </p>
 *
 * @see SkillProvider
 * @see com.leaf.skiller.foundation.skill.ItemSkill
 * @see ItemStack
 * @since 1.0.0
 */
public class HeldItemSkillProvider implements SkillProvider {
    /**
     * Collects skills from items held by the player and adds them to the collector.
     * 从玩家持有的物品中收集技能并将其添加到收集器中。
     * <p>
     * This method implements the primary skill collection functionality by examining
     * both the main hand and offhand items of the player. For each held item, it
     * extracts any associated skill bindings and adds them to the provided collector
     * for further processing by the skill system.
     * 此方法通过检查玩家的主手和副手物品来实现主要的技能收集功能。
     * 对于每个持有的物品，它提取任何关联的技能绑定并将其添加到提供的收集器中，
     * 供技能系统进一步处理。
     * </p>
     * <p>
     * Skills are collected in order (main hand first, then offhand), which may affect
     * priority or display order in the user interface.
     * 技能按顺序收集（先是主手，然后是副手），这可能会影响用户界面中的优先级或显示顺序。
     * </p>
     *
     * @param collector the skill collector to which found skills will be added
     *                  将添加找到的技能的技能收集器
     * @param player    the player entity whose held items will be examined
     *                  将检查其持有物品的玩家实体
     * @throws NullPointerException if collector or player is null
     *                               如果 collector 或 player 为 null 则抛出异常
     * @see SkillCollector
     * @see Player
     * @see Player#getMainHandItem()
     * @see Player#getOffhandItem()
     * @since 1.0.0
     */
    @Override
    public void collectSkills(SkillCollector collector, Player player) {
        addStack(collector, player.getMainHandItem());
        addStack(collector, player.getOffhandItem());
    }

    /**
     * Collects skill slot keys from items held by the player.
     * 从玩家持有的物品中收集技能槽位键。
     * <p>
     * This method provides a lightweight alternative to full skill collection by
     * gathering only the slot keys (integer identifiers) associated with skills
     * from held items. This is useful for operations that only need to know which
     * skill slots are occupied without requiring the full skill instance data.
     * 此方法通过仅收集与持有物品中的技能相关联的槽位键（整数标识符），
     * 提供了完整技能收集的轻量级替代方案。这对于只需要知道哪些技能槽被占用
     * 而不需要完整技能实例数据的操作很有用。
     * </p>
     * <p>
     * Like {@link #collectSkills(SkillCollector, Player)}, this method examines both
     * hand slots and collects keys in order (main hand first, then offhand).
     * 与 {@link #collectSkills(SkillCollector, Player)} 类似，此方法检查两个手槽
     * 并按顺序收集键（先是主手，然后是副手）。
     * </p>
     *
     * @param set    the set to which skill slot keys will be added
     *               将向其中添加技能槽位键的集合
     * @param player the player entity whose held items will be examined
     *               将检查其持有物品的玩家实体
     * @throws NullPointerException if set or player is null
     *                               如果 set 或 player 为 null 则抛出异常
     * @see Set
     * @see Player
     * @see Player#getMainHandItem()
     * @see Player#getOffhandItem()
     * @since 1.0.0
     */
    @Override
    public void collectKeys(Set<Integer> set, Player player) {
        addStack(set, player.getMainHandItem());
        addStack(set, player.getOffhandItem());
    }
}
