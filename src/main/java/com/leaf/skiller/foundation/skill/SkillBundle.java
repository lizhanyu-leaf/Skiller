package com.leaf.skiller.foundation.skill;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.foundation.OwnedBySkills;
import com.leaf.skiller.foundation.SkillData;
import com.leaf.skiller.foundation.SkillResource;
import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.config.SkillContextEnvironment;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 技能容器，将多个技能捆绑在一起进行管理和执行。
 * <p>
 * 支持序列化、网络传输和资源消耗。
 * </p>
 * <p>
 * <b>数据与缓存：</b>{@code skills}（{@link SkillData} 列表）是唯一的序列化数据源；
 * {@code skillRegistry}/{@code skillData} 是按类型分组的运行时缓存。
 * 缓存使用<b>脏标记</b>管理：任何受控的修改方法（{@link #addSkillData}、
 * {@link #removeSkillData}、{@link #clearSkillData}、覆写的 removeSkill 等）
 * 只标记缓存失效，真正的重建推迟到下一次查询（{@link #ensureCache()}），
 * 因此连续多次修改只重建一次。
 * </p>
 * <p>
 * <b>不要绕过受控方法修改数据</b>：{@link #getDataList()} 返回不可修改视图；
 * 覆写的 {@link #addSkill(ItemSkillRegistration)} 不受支持（构造默认实例需要环境），
 * 修改请优先使用实例路径（见 {@code SkillerCommands#bindDustSweep}）。
 * </p>
 *
 * @see OwnedBySkills
 * @see SkillData
 * @see ISkillInstance
 * @since 1.0.0
 * @author Leaf
 */
public class SkillBundle implements OwnedBySkills, IPersistedSerializable {

    public static final Codec<SkillBundle> CODEC =
            PersistedParser.createCodec(SkillBundle::new);

    public static final StreamCodec<ByteBuf, SkillBundle> STREAM_CODEC =
            PersistedParser.createStreamCodec(SkillBundle::new);

    /** 唯一序列化字段：所有技能的 SkillData（数据事实来源） */
    @Persisted
    private List<SkillData> skills = new ArrayList<>();

    // ========== 运行时缓存（不序列化，脏标记管理） ==========

    private transient Map<SkillType, List<ItemSkillRegistration<?>>> skillRegistry;
    private transient Map<SkillType, List<ISkillInstance<?>>> skillData;
    /** 缓存失效标记：初始为脏，保证反序列化后的首次查询一定重建 */
    private transient boolean cacheDirty = true;

    public static final SkillBundle EMPTY = new SkillBundle();

    public SkillBundle() {}

    /**
     * 拷贝构造：仅复制序列化数据源并标记缓存待重建。
     * 实例缓存不共享——重建成本低且能避免脏状态被复制。
     */
    public SkillBundle(SkillBundle other) {
        this.skills = new ArrayList<>(other.skills);
    }

    /**
     * 从实例列表构造：立即转换为 SkillData 并构建缓存（无需等查询）。
     */
    public SkillBundle(List<ISkillInstance<?>> instances) {
        if (instances == null || instances.isEmpty()) return;

        this.skills = instances.stream()
                .map(ISkillInstance::toData)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));

        rebuildCache();
    }

    // ========== 缓存管理（脏标记） ==========

    /**
     * 从 skills 列表还原运行时缓存，并清除脏标记。
     */
    private void rebuildCache() {
        List<ISkillInstance<?>> instances = new ArrayList<>();
        for (SkillData data : skills) {
            ISkillInstance<?> instance = ISkillInstance.fromData(data);
            if (instance != null) instances.add(instance);
        }

        var pair = groupSkills(instances);
        this.skillRegistry = pair.getFirst();
        this.skillData = pair.getSecond();
        this.cacheDirty = false;
    }

    /**
     * 查询前的缓存保证：仅在脏标记置位时重建，
     * 因此连续多次修改只触发一次重建。
     */
    private void ensureCache() {
        if (cacheDirty) {
            rebuildCache();
        }
    }

    /** 标记缓存失效；真正的重建推迟到下一次查询 */
    private void markCacheDirty() {
        cacheDirty = true;
    }

    // ========== 受控修改 API（全部标记缓存失效） ==========

    /**
     * 追加一条技能数据并使缓存失效。
     */
    public void addSkillData(SkillData data) {
        if (data == null) return;
        skills.add(data);
        markCacheDirty();
    }

    /**
     * 移除一条技能数据并使缓存失效。
     *
     * @return true if the data was present and removed / 存在且已移除时返回 true
     */
    public boolean removeSkillData(SkillData data) {
        boolean removed = skills.remove(data);
        if (removed) markCacheDirty();
        return removed;
    }

    /**
     * 清空所有技能数据并使缓存失效。
     */
    public void clearSkillData() {
        if (!skills.isEmpty()) {
            skills.clear();
            markCacheDirty();
        }
    }

    /**
     * 返回序列化数据的不可修改视图。
     * <p>修改必须走 {@link #addSkillData} 等受控方法，否则缓存不会失效。</p>
     */
    public List<SkillData> getDataList() {
        return Collections.unmodifiableList(skills);
    }

    // ========== OwnedBySkills ==========

    /**
     * {@inheritDoc}
     * <p>返回按类型分组的运行时缓存（查询时按需重建）。</p>
     */
    @Override
    public Map<SkillType, List<ItemSkillRegistration<?>>> skills() {
        ensureCache();
        return skillRegistry;
    }

    /**
     * {@inheritDoc}
     * <p>按注册表类型过滤序列化数据源并使缓存失效，
     * 而不是使用直接修改缓存的接口默认实现（那会与数据源脱节）。</p>
     */
    @Override
    public OwnedBySkills removeSkill(SkillType type) {
        boolean removed = skills.removeIf(data -> hasType(data, type));
        if (removed) markCacheDirty();
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>按注册项ID过滤序列化数据源并使缓存失效。</p>
     */
    @Override
    public OwnedBySkills removeSkill(ItemSkillRegistration<?> skill) {
        ResourceLocation id = SkillerBuiltInRegistries.SKILLS.getKey(skill);
        if (id == null) return this;
        boolean removed = skills.removeIf(data -> id.equals(data.skillId()));
        if (removed) markCacheDirty();
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>不受支持：添加注册项需要构造默认实例，而实例的上下文工厂依赖
     * {@link SkillContextEnvironment}。请经实例路径构造
     * （如 {@code new SkillBundle(List.of(instance))} 或 {@link #addSkillData}）。</p>
     */
    @Override
    public OwnedBySkills addSkill(ItemSkillRegistration<?> skill) {
        throw new UnsupportedOperationException(
                "Adding a registration requires creating a default instance with a "
                        + SkillContextEnvironment.class.getSimpleName()
                        + "; build the bundle from instances instead");
    }

    /**
     * 判断一条 SkillData 是否属于指定类型（按注册表查询）。
     */
    private static boolean hasType(SkillData data, SkillType type) {
        var registration = SkillerBuiltInRegistries.SKILLS.get(data.skillId());
        return registration != null && type.equals(registration.getType());
    }

    // ========== 释放技能 ==========

    @SuppressWarnings("unchecked")
    public boolean releaseSkills(SkillType type, SkillContextEnvironment env) {
        ensureCache();

        List<ISkillInstance<SkillContext>> instances =
                (List<ISkillInstance<SkillContext>>) (List<?>) skillData.get(type);
        if (instances == null || instances.isEmpty()) return true;

        Player player = env.getPlayer();

        Map<ISkillInstance<SkillContext>, SkillContext> contexts = new LinkedHashMap<>();
        for (ISkillInstance<SkillContext> instance : instances) {
            contexts.put(instance, instance.skill().getFactory().create(env, instance));
        }

        if (!player.isCreative()) {
            Map<ResourceKey<SkillResource>, SkillResource.DelayConsumable> consumables = new HashMap<>();
            for (ISkillInstance<SkillContext> instance : instances) {
                consumables.computeIfAbsent(instance.getResource().key(),
                        key -> instance.getResource().getDelayConsumable(player));
                instance.consumeResource(contexts.get(instance), consumables.get(instance.getResource().key()));
            }

            for (SkillResource.DelayConsumable consumable : consumables.values()) {
                if (!consumable.canConsume()) return false;
            }

            for (SkillResource.DelayConsumable consumable : consumables.values()) {
                consumable.apply();
            }
        }

        for (ISkillInstance<SkillContext> instance : instances) {
            instance.release(contexts.get(instance));
        }

        return true;
    }

    public boolean releaseAll(SkillContextEnvironment env) {
        ensureCache();
        for (SkillType type : List.copyOf(skillData.keySet())) {
            releaseSkills(type, env);
        }
        return true;
    }

    // ========== 数据访问 ==========

    /**
     * 返回全部技能实例（查询时按需重建缓存）。
     */
    public List<ISkillInstance<?>> getAllData() {
        ensureCache();
        List<ISkillInstance<?>> all = new ArrayList<>();
        for (List<ISkillInstance<?>> dataList : skillData.values()) {
            all.addAll(dataList);
        }
        return all;
    }

    // ========== Object 方法 ==========

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkillBundle that)) return false;
        return skills.equals(that.skills);
    }

    @Override
    public int hashCode() {
        return skills.hashCode();
    }

    @Override
    public String toString() {
        return "SkillBundle{" + skills + '}';
    }

    // ========== 静态工具 ==========

    private static Pair<Map<SkillType, List<ItemSkillRegistration<?>>>, Map<SkillType, List<ISkillInstance<?>>>>
    groupSkills(List<ISkillInstance<?>> instances) {
        Map<SkillType, List<ItemSkillRegistration<?>>> registryResult = new HashMap<>();
        Map<SkillType, List<ISkillInstance<?>>> dataResult = new HashMap<>();

        for (ISkillInstance<?> instance : instances) {
            ItemSkillRegistration<?> registration = instance.skill();
            registryResult.computeIfAbsent(registration.getType(), k -> new ArrayList<>()).add(registration);
            dataResult.computeIfAbsent(registration.getType(), k -> new ArrayList<>()).add(instance);
        }

        return Pair.of(registryResult, dataResult);
    }
}
