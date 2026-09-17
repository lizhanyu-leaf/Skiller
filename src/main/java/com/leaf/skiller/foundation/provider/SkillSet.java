package com.leaf.skiller.foundation.provider;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.content.skill.SkillComponent;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.SkillBundle;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A skill collector whose single responsibility is merging {@link SkillComponent}s.
 * 技能收集器，其唯一职责是合并 {@link SkillComponent}。
 *
 * <p>Components go in, a merged component comes out. This class implements
 * {@link SkillCollector} to merge the key bindings and skill instances of multiple
 * components (e.g. one per provider or per item) into a single component.
 * 组件进，合并后的组件出。此类实现 {@link SkillCollector}，
 * 将多个组件（例如每个提供者或每个物品一个）的按键绑定和技能实例合并为单个组件。
 *
 * <p><b>Deduplication Strategy:</b> When the same skill is bound to the same key
 * by multiple components, only the instance with the highest level is retained.
 * The same skill bound to different keys is kept once per key, since each key
 * binding is an independent entry.
 * <p><b>去重策略：</b> 当多个组件将同一技能绑定到同一按键时，仅保留具有最高等级的实例。
 * 同一技能绑定到不同按键时会按按键各保留一份，因为每个按键绑定是独立的条目。
 *
 * <p><b>Usage Example:</b>
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // Create a skill set and merge components from providers
 * // 创建技能集并合并来自提供者的组件
 * SkillSet skillSet = new SkillSet();
 * for (SkillProvider provider : providers) {
 *     provider.collectSkills(skillSet, player);
 * }
 *
 * // Get the merged component (key bindings preserved)
 * // 获取合并后的组件（保留按键绑定）
 * SkillComponent component = skillSet.toComponent();
 * }</pre>
 *
 * <p><b>Thread Safety:</b> This class is not thread-safe.
 * <p><b>线程安全性：</b> 此类不是线程安全的。
 * Concurrent modifications should be externally synchronized.
 * 并发修改应该外部同步。
 *
 * @see SkillCollector
 * @see SkillProvider
 * @see SkillComponent
 * @since 1.0.0
 */
public class SkillSet implements SkillCollector {

    /**
     * Internal map storing, for each key binding, the best (highest level) instance per skill.
     * 内部映射，为每个按键绑定存储每个技能的最佳（最高等级）实例。
     *
     * <p>Outer keys are the integer key binding indices (slot IDs) used by the client
     * and server to identify which skill key was pressed.
     * 外层键是整数按键绑定索引（槽位ID），客户端和服务端用它标识按下的技能按键。
     *
     * <p>Inner map keys are {@link ResourceLocation} objects representing unique skill IDs
     * from the {@link SkillerBuiltInRegistries#SKILLS} registry; values are the
     * {@link ISkillInstance} objects with the highest level merged for that key and skill.
     * 内层映射的键是表示唯一技能ID的 {@link ResourceLocation} 对象，
     * 来自 {@link SkillerBuiltInRegistries#SKILLS} 注册表；值是为该按键和技能合并到的
     * 具有最高等级的 {@link ISkillInstance} 对象。
     *
     * <p><b>Implementation Note:</b> Uses HashMap for O(1) lookup performance
     * when checking for existing instances of a skill.
     * <p><b>实现注意：</b> 使用 HashMap 以实现 O(1) 查找性能，
     * 当检查技能的现有实例时。
     */
    private final Map<Integer, Map<ResourceLocation, ISkillInstance<?>>> bestByKey = new HashMap<>();

    /**
     * Merges a skill component into this skill set.
     * 将一个技能组件合并到此技能集。
     *
     * <p>This method iterates over every key binding of the component and merges
     * each bound skill instance into this set using the per-key deduplication logic:
     * 此方法遍历组件的每个按键绑定，并使用按按键的去重逻辑
     * 将每个绑定的技能实例合并到此集合中：
     * <ul>
     * <li>If the skill ID is not present for this key, the instance is added.</li>
     *     <li>如果该按键下此技能ID不存在，则添加该实例。</li>
     * <li>If the skill ID already exists for this key, the instance is kept only if
     *     its level is higher than the existing instance.</li>
     *     <li>如果该按键下此技能ID已存在，则仅当实例等级高于现有实例时才保留。</li>
     * </ul>
     *
     * @param component The skill component to merge (required, non-null).
     *                  要合并的技能组件（必需，非空）。
     * @throws NullPointerException if component is null.
     *                                如果组件为null，则抛出NullPointerException。
     * @see SkillComponent#bindings()
     * @see SkillBundle#getAllData()
     * @since 1.0.0
     */
    @Override
    public void add(SkillComponent component) {
        component.bindings().forEach((key, bundle) ->
                bundle.getAllData().forEach(instance -> addInstance(key, instance)));
    }

    /**
     * Merges a single skill instance bound to the specified key, keeping the highest level.
     * 合并绑定到指定按键的单个技能实例，保留最高等级。
     *
     * <p>This private helper implements the deduplication strategy of this set:
     * for the given key, only the highest level instance of a skill is retained.
     * 此私有辅助方法实现此集合的去重策略：
     * 对于给定的按键，仅保留技能的最高等级实例。
     *
     * @param key The key binding index the instance is bound to.
     *            实例绑定到的按键绑定索引。
     * @param instance The skill instance to merge (non-null).
     *                 要合并的技能实例（非空）。
     * @see ISkillInstance#level()
     * @since 1.0.0
     */
    private void addInstance(int key, ISkillInstance<?> instance) {
        ResourceLocation skillId = SkillerBuiltInRegistries.SKILLS
                .getKey(instance.skill());
        bestByKey.computeIfAbsent(key, k -> new HashMap<>())
                .merge(skillId, instance, (existing, candidate) ->
                        candidate.level() > existing.level() ? candidate : existing);
    }

    /**
     * Checks if this skill set is empty (contains no skills).
     * 检查此技能集是否为空（不包含任何技能）。
     *
     * <p>Returns {@code true} if no skill instances have been successfully merged
     * into the set under any key, {@code false} otherwise.
     * 如果没有任何按键下的技能实例被成功合并到集合中，则返回 {@code true}，
     * 否则返回 {@code false}。
     *
     * <p>This is equivalent to checking if {@link #size()} returns 0.
     * 这等同于检查 {@link #size()} 是否返回0。
     *
     * @return {@code true} if the skill set contains no skills, {@code false} otherwise.
     *         如果技能集不包含任何技能，则返回 {@code true}，否则返回 {@code false}。
     * @see #size()
     * @since 1.0.0
     */
    @Override
    public boolean isEmpty() {
        return bestByKey.isEmpty();
    }

    /**
     * Returns the number of unique skill entries in this skill set.
     * 返回此技能集中唯一技能条目的数量。
     *
     * <p>Each (key binding, skill ID) pair is counted once, regardless of how many
     * components supplied an instance for it. A skill bound to multiple keys is
     * counted once per key.
     * 每个（按键绑定，技能ID）对被计数一次，无论多少个组件为其提供了实例。
     * 绑定到多个按键的技能按按键各计数一次。
     *
     * @return The number of unique (key, skill) entries in the set (non-negative).
     *         集合中唯一（按键，技能）条目的数量（非负数）。
     * @see #isEmpty()
     * @since 1.0.0
     */
    @Override
    public int size() {
        return bestByKey.values().stream()
                .mapToInt(Map::size)
                .sum();
    }

    /**
     * Converts this skill set into the merged skill component.
     * 将此技能集转换为合并后的技能组件。
     *
     * <p>This method creates a new {@link SkillComponent} containing all the
     * highest-level skill instances merged into this set, preserving the key binding
     * information so that the client and server can correlate key presses with skills.
     * 此方法创建一个新的 {@link SkillComponent}，包含合并到此集合的所有最高等级技能实例，
     * 并保留按键绑定信息，使客户端和服务端能够将按键按下与技能关联起来。
     *
     * <p>The component is a new object and subsequent modifications to this
     * SkillSet will not affect the returned component.
     * 组件是一个新对象，对此 SkillSet 的后续修改不会影响返回的组件。
     *
     * @return A new {@link SkillComponent} containing all skills from this set with
     *         their key bindings (non-null).
     *         包含此集合中所有技能及其按键绑定的新 {@link SkillComponent}（非空）。
     * @see SkillComponent
     * @see SkillBundle
     * @since 1.0.0
     */
    public SkillComponent toComponent() {
        Map<Integer, SkillBundle> bindings = new HashMap<>();
        bestByKey.forEach((key, instances) ->
                bindings.put(key, new SkillBundle(new ArrayList<>(instances.values()))));
        return new SkillComponent(bindings);
    }
}
