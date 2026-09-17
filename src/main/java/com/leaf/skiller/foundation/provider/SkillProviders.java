package com.leaf.skiller.foundation.provider;

import com.leaf.skiller.content.skill.SkillComponent;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A central registry and utility class for managing skill providers.
 * 用于管理技能提供者的中央注册表和实用类。
 *
 * <p>This class serves as the main entry point for the skill provider system,
 * 此类作为技能提供者系统的主要入口点，
 * providing methods for registering providers and collecting skills and keys.
 * 提供注册提供者以及收集技能和按键的方法。
 *
 * <p>Providers registered with this class are called during skill and key collection
 * 注册到此类的提供者在技能和按键收集期间被调用，
 * to aggregate all available skills and key bindings for a player.
 * 以聚合玩家所有可用的技能和按键绑定。
 *
 * <p><b>Usage Example:</b>
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // Register a custom provider
 * // 注册自定义提供者
 * SkillProviders.register(new MyCustomSkillProvider());
 *
 * // Collect all skills for a player (key bindings included)
 * // 为玩家收集所有技能（包含按键绑定）
 * SkillComponent skills = SkillProviders.collectAllSkills(player);
 *
 * // Collect all key bindings for a player
 * // 为玩家收集所有按键绑定
 * Set<Integer> keys = SkillProviders.collectAllKeys(player);
 * }</pre>
 *
 * <p><b>Thread Safety:</b> This class is not thread-safe.
 * <p><b>线程安全性：</b> 此类不是线程安全的。
 * Provider registration should happen during initialization,
 * 提供者注册应该在初始化期间完成，
 * before any collection operations are performed.
 * 在执行任何收集操作之前。
 *
 * @see SkillProvider
 * @see SkillCollector
 * @see SkillSet
 * @since 1.0.0
 */
public class SkillProviders {

    /**
     * The internal list of registered skill providers.
     * 已注册技能提供者的内部列表。
     *
     * <p>This list maintains all providers that have been registered
     * 此列表维护所有已注册的提供者，
     * and are called during collection operations.
     * 并在收集操作期间被调用。
     *
     * <p>Providers are called in the order they were registered.
     * 提供者按注册顺序被调用。
     *
     * <p><b>Implementation Note:</b> This list is initialized as an ArrayList
     * <p><b>实现注意：</b> 此列表初始化为 ArrayList，
     * for efficient iteration during collection operations.
     * 以在收集操作期间高效迭代。
     */
    private static final List<SkillProvider> PROVIDERS = new ArrayList<>();

    /**
     * Registers a skill provider with the system.
     * 向系统注册技能提供者。
     *
     * <p>Registered providers will be called during all subsequent
     * 已注册的提供者将在所有后续的技能和按键收集操作期间被调用。
     * skill and key collection operations.
     *
     * <p>Providers should typically be registered during mod initialization
     * 提供者通常应该在模组初始化期间注册，
     * before any game logic requires skill collection.
     * 在任何游戏逻辑需要技能收集之前。
     *
     * <p><b>Registration Order:</b> Providers are called in the order
     * <p><b>注册顺序：</b> 提供者按注册顺序被调用。
     * they are registered, which may affect skill priority.
     * 这可能会影响技能优先级。
     *
     * @param provider The skill provider to register (required, non-null).
     *                 要注册的技能提供者（必需，非空）。
     * @throws NullPointerException if provider is null.
     *                                如果提供者为null，则抛出NullPointerException。
     * @see SkillProvider
     * @see #collectAllSkills(Player)
     * @see #collectAllKeys(Player)
     * @since 1.0.0
     */
    public static void register(SkillProvider provider) {
        PROVIDERS.add(provider);
    }

    /**
     * Collects all skills available to the specified player from all registered providers.
     * 从所有已注册的提供者收集指定玩家可用的所有技能。
     *
     * <p>This method creates a new {@link SkillSet} and calls
     * 此方法创建一个新的 {@link SkillSet} 并调用
     * {@link SkillProvider#collectSkills(SkillCollector, Player)} on all registered providers.
     * 所有已注册提供者的 {@link SkillProvider#collectSkills(SkillCollector, Player)}。
     *
     * <p>The resulting skill bundle contains the best version of each skill
     * 结果技能捆绑包含从所有提供者聚合的每个技能的最佳版本。
     * (highest level) aggregated from all providers.
     *
     * <p><b>Performance:</b> This method creates a new SkillSet on each call,
     * <p><b>性能：</b> 此方法在每次调用时创建新的 SkillSet，
     * so frequent calls should be avoided if possible.
     * 因此如果可能应避免频繁调用。
     *
     * @param player The player to collect skills for (required, non-null).
     *               要为其收集技能的玩家（必需，非空）。
     * @return A {@link SkillComponent} containing all collected skills with their key bindings (non-null).
     *         包含所有收集技能及其按键绑定的 {@link SkillComponent}（非空）。
     * @throws NullPointerException if player is null.
     *                                如果玩家为null，则抛出NullPointerException。
     * @see SkillProvider#collectSkills(SkillCollector, Player)
     * @see SkillSet
     * @see SkillComponent
     * @since 1.0.0
     */
    public static SkillComponent collectAllSkills(Player player) {
        SkillSet all = new SkillSet();
        for (SkillProvider provider : PROVIDERS) {
            provider.collectSkills(all, player);
        }
        return all.toComponent();
    }

    /**
     * Collects all key binding IDs available to the specified player from all registered providers.
     * 从所有已注册的提供者收集指定玩家可用的所有按键绑定ID。
     *
     * <p>This method creates a new {@link Set} and calls
     * 此方法创建一个新的 {@link Set} 并调用
     * {@link SkillProvider#collectKeys(Set, Player)} on all registered providers.
     * 所有已注册提供者的 {@link SkillProvider#collectKeys(Set, Player)}。
     *
     * <p>The resulting set contains all unique key binding IDs
     * 结果集包含从所有提供者聚合的所有唯一按键绑定ID。
     * aggregated from all providers.
     *
     * <p><b>Key Binding IDs:</b> These IDs correspond to keyboard inputs
     * <p><b>按键绑定ID：</b> 这些ID对应于键盘输入，
     * that can trigger skills for the player.
     * 可以触发玩家的技能。
     *
     * @param player The player to collect key bindings for (required, non-null).
     *               要为其收集按键绑定的玩家（必需，非空）。
     * @return A {@link Set} containing all collected key binding IDs (non-null, may be empty).
     *         包含所有收集按键绑定ID的 {@link Set}（非空，可能为空）。
     * @throws NullPointerException if player is null.
     *                                如果玩家为null，则抛出NullPointerException。
     * @see SkillProvider#collectKeys(Set, Player)
     * @since 1.0.0
     */
    public static Set<Integer> collectAllKeys(Player player) {
        Set<Integer> all = new HashSet<>();
        for (SkillProvider provider : PROVIDERS) {
            provider.collectKeys(all, player);
        }
        return all;
    }
}
