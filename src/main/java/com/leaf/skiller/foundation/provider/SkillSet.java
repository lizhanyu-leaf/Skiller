package com.leaf.skiller.foundation.provider;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.SkillBundle;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A skill collector that maintains the highest level instance of each unique skill.
 * 技能收集器，维护每个唯一技能的最高等级实例。
 *
 * <p>This class implements {@link SkillCollector} to collect skills
 * 此类实现 {@link SkillCollector} 以从多个提供者收集技能，
 * from multiple providers while automatically deduplicating by skill ID
 * 同时通过技能ID自动去重，保留最高等级的实例。
 * and keeping only the highest level instance.
 *
 * <p><b>Deduplication Strategy:</b> When multiple instances of the same skill
 * <p><b>去重策略：</b> 当收集同一技能的多个实例时，
 * are collected, only the instance with the highest level is retained.
 * 仅保留具有最高等级的实例。
 * This ensures players always have access to their most powerful version of each skill.
 * 这确保玩家始终可以访问每个技能的最强版本。
 *
 * <p><b>Usage Example:</b>
 * <p><b>使用示例：</b>
 * <pre>{@code
 * // Create a skill set and collect from providers
 * // 创建技能集并从提供者收集
 * SkillSet skillSet = new SkillSet();
 * for (SkillProvider provider : providers) {
 *     provider.collectSkills(skillSet, player);
 * }
 *
 * // Convert to bundle for use
 * // 转换为捆绑以供使用
 * SkillBundle bundle = skillSet.toBundle();
 * }</pre>
 *
 * <p><b>Thread Safety:</b> This class is not thread-safe.
 * <p><b>线程安全性：</b> 此类不是线程安全的。
 * Concurrent modifications should be externally synchronized.
 * 并发修改应该外部同步。
 *
 * @see SkillCollector
 * @see SkillProvider
 * @see ISkillInstance
 * @see SkillBundle
 * @since 1.0.0
 */
public class SkillSet implements SkillCollector {

    /**
     * Internal map storing the best (highest level) instance for each skill.
     * 内部映射，存储每个技能的最佳（最高等级）实例。
     *
     * <p>Keys are {@link ResourceLocation} objects representing unique skill IDs
     * 键是表示唯一技能ID的 {@link ResourceLocation} 对象，
     * from the {@link SkillerBuiltInRegistries#SKILLS} registry.
     * 来自 {@link SkillerBuiltInRegistries#SKILLS} 注册表。
     *
     * <p>Values are the {@link ISkillInstance} objects with the highest level
     * 值是为该技能收集到的具有最高等级的 {@link ISkillInstance} 对象。
     * that have been collected for that skill.
     *
     * <p><b>Implementation Note:</b> Uses HashMap for O(1) lookup performance
     * <p><b>实现注意：</b> 使用 HashMap 以实现 O(1) 查找性能，
     * when checking for existing instances of a skill.
     * 当检查技能的现有实例时。
     */
    private final Map<ResourceLocation, ISkillInstance<?>> bestBySkill = new HashMap<>();

    /**
     * Adds all skills from a skill bundle to this skill set.
     * 将技能捆绑中的所有技能添加到此技能集。
     *
     * <p>This implementation extracts all skill instances from the bundle
     * 此实现从捆绑中提取所有技能实例并逐个添加，
     * and adds them individually using {@link #add(ISkillInstance)}.
     * 使用 {@link #add(ISkillInstance)} 逐个添加。
     *
     * <p>The deduplication logic is applied for each skill,
     * 对每个技能应用去重逻辑，
     * so only the highest level instance of each skill is retained.
     * 因此仅保留每个技能的最高等级实例。
     *
     * @param bundle The skill bundle containing skills to add (required, non-null).
     *               包含要添加的技能的技能捆绑（必需，非空）。
     * @throws NullPointerException if bundle is null.
     *                                如果捆绑为null，则抛出NullPointerException。
     * @see #add(ISkillInstance)
     * @see #addAll(List)
     * @see SkillBundle
     * @since 1.0.0
     */
    @Override
    public void add(SkillBundle bundle) {
        addAll(bundle.getAllData());
    }

    /**
     * Adds a single skill instance to this skill set.
     * 将单个技能实例添加到此技能集。
     *
     * <p>This method implements the deduplication strategy:
     * 此方法实现去重策略：
     * <ul>
     * <li>If the skill ID is not present in the set, the instance is added.</li>
     *     <li>如果技能ID不存在于集合中，则添加该实例。</li>
     * <li>If the skill ID exists, the instance is added only if its level
     *     <li>如果技能ID已存在，则仅当实例等级高于现有实例时才添加。</li>
     * is higher than the existing instance.</li>
     * </ul>
     *
     * <p>This ensures that for each unique skill, only the most powerful
     * 这确保对于每个唯一技能，仅保留最高等级（最强大）的版本。
     * (highest level) version is retained.
     *
     * @param instance The skill instance to add (required, non-null).
     *                 要添加的技能实例（必需，非空）。
     * @throws NullPointerException if instance is null.
     *                                如果实例为null，则抛出NullPointerException。
     * @see ISkillInstance#level()
     * @since 1.0.0
     */
    @Override
    public void add(ISkillInstance<?> instance) {
        ResourceLocation skillId = SkillerBuiltInRegistries.SKILLS
                .getKey(instance.skill());
        ISkillInstance<?> existing = bestBySkill.get(skillId);

        if (existing == null || instance.level() > existing.level()) {
            bestBySkill.put(skillId, instance);
        }
    }

    /**
     * Adds multiple skill instances to this skill set in a single operation.
     * 在单个操作中将多个技能实例添加到此技能集。
     *
     * <p>This method iterates through the provided list and adds each
     * 此方法遍历提供的列表并使用 {@link #add(ISkillInstance)}
     * instance using {@link #add(ISkillInstance)}, applying the same
     * 逐个添加每个实例，应用相同的去重逻辑。
     * deduplication logic.
     *
     * <p>This is equivalent to calling {@link #add(ISkillInstance)} for each
     * 这相当于为列表中的每个实例调用 {@link #add(ISkillInstance)}，
     * instance in the list, but may be more convenient for bulk operations.
     * 但对于批量操作可能更方便。
     *
     * @param instances The list of skill instances to add (required, non-null).
     *                  要添加的技能实例列表（必需，非空）。
     * @throws NullPointerException if instances or any element is null.
     *                                如果实例或任何元素为null，则抛出NullPointerException。
     * @see #add(ISkillInstance)
     * @since 1.0.0
     */
    @Override
    public void addAll(List<ISkillInstance<?>> instances) {
        for (ISkillInstance<?> instance : instances) {
            add(instance);
        }
    }

    /**
     * Checks if this skill set is empty (contains no skills).
     * 检查此技能集是否为空（不包含任何技能）。
     *
     * <p>Returns {@code true} if no skill instances have been successfully
     * 如果没有技能实例被成功添加到集合中，则返回 {@code true}，
     * added to the set, {@code false} otherwise.
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
        return bestBySkill.isEmpty();
    }

    /**
     * Returns the number of unique skills in this skill set.
     * 返回此技能集中唯一技能的数量。
     *
     * <p>Each unique skill ID is counted once, regardless of how many
     * 每个唯一技能ID被计数一次，无论收集了多少个该技能的实例。
     * instances of that skill were collected.
     *
     * <p>This count represents the number of different skills available
     * 此计数表示通过收集过程可用的不同技能的数量。
     * through the collection process.
     *
     * @return The number of unique skills in the set (non-negative).
     *         集合中唯一技能的数量（非负数）。
     * @see #isEmpty()
     * @since 1.0.0
     */
    @Override
    public int size() {
        return bestBySkill.size();
    }

    /**
     * Converts this skill set into a skill bundle.
     * 将此技能集转换为技能捆绑。
     *
     * <p>This method creates a new {@link SkillBundle} containing
     * 此方法创建一个新的 {@link SkillBundle}，包含此集合中所有最高等级技能实例。
     * all the highest-level skill instances from this set.
     *
     * <p>The bundle is a new object and subsequent modifications to this
     * 捆绑是一个新对象，对此集合的后续修改不会影响返回的捆绑。
     * SkillSet will not affect the returned bundle.
     *
     * <p><b>Usage:</b> The returned bundle is typically used to provide
     * <p><b>用法：</b> 返回的捆绑通常用于向游戏逻辑提供技能，
     * skills to game logic or for serialization purposes.
     * 或用于序列化目的。
     *
     * @return A new {@link SkillBundle} containing all skills from this set (non-null).
     *         包含此集合中所有技能的新 {@link SkillBundle}（非空）。
     * @see SkillBundle
     * @since 1.0.0
     */
    public SkillBundle toBundle() {
        return new SkillBundle(new ArrayList<>(bestBySkill.values()));
    }
}
