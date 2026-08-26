package com.leaf.skiller.foundation.provider;

import com.leaf.skiller.AllDataComponents;
import com.leaf.skiller.content.skill.SkillComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

/**
 * A provider interface for collecting and supplying skills to players.
 * 技能提供者接口，用于向玩家收集和提供技能。
 *
 * <p>Implementations of this interface are responsible for determining which skills
 * 实现此接口的类负责确定哪些技能应该对特定玩家可用，
 * and key bindings should be available to a specific player.
 * 以及哪些按键绑定应该对该玩家可用。
 *
 * <p>Providers are registered with {@link SkillProviders} and are called during
 * 提供者注册到 {@link SkillProviders}，在技能收集过程中被调用。
 * skill collection processes.
 *
 * <p>This interface also provides default utility methods for processing
 * 此接口还提供了处理物品堆以提取技能和按键绑定的默认实用方法。
 * ItemStacks to extract skills and key bindings.
 *
 * @see SkillCollector
 * @see SkillProviders
 * @see SkillSet
 * @since 1.0.0
 */
public interface SkillProvider {

    /**
     * Collects and provides skills for the specified player.
     * 为指定玩家收集和提供技能。
     *
     * <p>This method is called during skill collection to gather all skills
     * 此方法在技能收集期间被调用，以收集所有应对玩家可用的技能。
     * that should be available to the player.
     *
     * <p>Implementations should call methods on the collector to add skills
     * 实现应该调用收集器上的方法来添加技能到集合中。
     * to the collection.
     *
     * @param collector The collector instance to accumulate skills into (required).
     *                  用于累积技能的收集器实例（必需）。
     * @param player    The player to collect skills for (required).
     *                  要为其收集技能的玩家（必需）。
     * @see SkillCollector#add(ISkillInstance)
     * @see SkillCollector#add(SkillBundle)
     * @see SkillCollector#addAll(List)
     * @since 1.0.0
     */
    void collectSkills(SkillCollector collector, Player player);

    /**
     * Collects and provides key binding IDs for the specified player.
     * 为指定玩家收集和提供按键绑定ID。
     *
     * <p>This method is called during key binding collection to gather all
     * 此方法在按键绑定收集期间被调用，以收集所有应对玩家可用的按键ID。
     * key binding IDs that should be available to the player.
     *
     * <p>Key bindings are used to map keyboard inputs to specific skills.
     * 按键绑定用于将键盘输入映射到特定技能。
     *
     * @param keys   The set to accumulate key binding IDs into (required).
     *               用于累积按键绑定ID的集合（必需）。
     * @param player The player to collect key bindings for (required).
     *               要为其收集按键绑定的玩家（必需）。
     * @since 1.0.0
     */
    void collectKeys(Set<Integer> keys, Player player);

    /**
     * Utility method to extract skills from an ItemStack and add them to a collector.
     * 从物品堆提取技能并添加到收集器的实用方法。
     *
     * <p>This default implementation reads the {@link SkillComponent} from the item's
     * 此默认实现从物品的数据组件中读取 {@link SkillComponent}，
     * data components and adds all bound skills to the collector.
     * 并将所有绑定的技能添加到收集器中。
     *
     * <p>This method safely handles empty stacks and items without skill components.
     * 此方法安全地处理空堆和没有技能组件的物品。
     *
     * @param collector The collector to add skills to (required).
     *                  要添加技能到的收集器（必需）。
     * @param stack     The ItemStack to extract skills from (required).
     *                  要从中提取技能的物品堆（必需）。
     * @see SkillCollector#add(ISkillInstance)
     * @see SkillComponent
     * @since 1.0.0
     */
    default void addStack(SkillCollector collector, ItemStack stack) {
        if (stack.isEmpty()) return;
        SkillComponent component = stack.get(AllDataComponents.SKILL_COMPONENT);
        if (component == null) return;
        component.bindings().values().forEach(collector::add);
    }

    /**
     * Utility method to extract key binding IDs from an ItemStack and add them to a set.
     * 从物品堆提取按键绑定ID并添加到集合的实用方法。
     *
     * <p>This default implementation reads the {@link SkillComponent} from the item's
     * 此默认实现从物品的数据组件中读取 {@link SkillComponent}，
     * data components and adds all bound key binding IDs to the set.
     * 并将所有绑定的按键绑定ID添加到集合中。
     *
     * <p>This method safely handles empty stacks and items without skill components.
     * 此方法安全地处理空堆和没有技能组件的物品。
     *
     * @param keys   The set to add key binding IDs to (required).
     *               要添加按键绑定ID到的集合（必需）。
     * @param stack  The ItemStack to extract key bindings from (required).
     *               要从中提取按键绑定的物品堆（必需）。
     * @see SkillComponent
     * @since 1.0.0
     */
    default void addStack(Set<Integer> keys, ItemStack stack) {
        if (stack.isEmpty()) return;
        SkillComponent component = stack.get(AllDataComponents.SKILL_COMPONENT);
        if (component == null) return;
        keys.addAll(component.bindings().keySet());
    }
}
