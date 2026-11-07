package com.leaf.skiller.foundation;

import com.leaf.skiller.Skiller;
import com.leaf.skiller.util.RateExpression;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

/**
 * An owner of a {@link SkillResource} that carries its own NBT and applies a rate
 * expression to every consumption.
 * 携带自身NBT的 {@link SkillResource} 拥有者，对每次消耗应用变化率表达式。
 * <p>
 * Instances do not hold a bare {@link SkillResource} — they hold an owner, so the
 * resource always travels together with the NBT that configures it (the
 * {@value #RATE_KEY} rate expression, a member list for
 * {@code MultiSkillResource}, or any other resource-specific keys). The NBT's
 * {@value #RATE_KEY} key holds an algebraic rate expression over {@code x}
 * (see {@link RateExpression}); every consumption amount is first pushed through it,
 * and the resulting value is what the wrapped resource actually consumes. A missing
 * key means identity ({@code x} stays {@code x}); an invalid expression logs a warning
 * and also falls back to identity, so a typo can never brick skill releases.
 * 实例不再持有裸 {@link SkillResource}——而是持有拥有者，
 * 因此资源总是与配置它的NBT一起出现（{@value #RATE_KEY} 变化率表达式、
 * {@code MultiSkillResource} 的成员列表，或任何其他资源专属键）。
 * NBT 的 {@value #RATE_KEY} 键保存关于 {@code x} 的代数变化率表达式
 * （见 {@link RateExpression}）；每次消耗的数量都先经过它处理，
 * 得到的值才是包装资源实际消耗的值。键缺失表示恒等（{@code x} 原样通过）；
 * 表达式非法时记录警告并同样回退恒等，因此手误不会让技能释放瘫痪。
 * </p>
 * <p>
 * Typical wiring in {@code SkillBundle#releaseSkills}: one shared inner
 * {@link SkillResource.DelayConsumable} accumulates all costs for a resource key,
 * while each instance routes its own contribution through its owner, so rated
 * costs from different instances still combine into a single all-or-nothing check.
 * {@code SkillBundle#releaseSkills} 中的典型接线：每个资源键共享一个内部
 * {@link SkillResource.DelayConsumable} 累积所有成本，
 * 而每个实例经自己的拥有者汇入贡献，
 * 因此不同实例的带率成本仍合并进同一次全有或全无检查。
 * </p>
 * <p>
 * Owners are value objects and serialize as a small compound
 * ({@code {resource: <registry id>, data: {...}}}) through {@link #CODEC} and
 * {@link #STREAM_CODEC}.
 * 拥有者是值对象，经 {@link #CODEC} 与 {@link #STREAM_CODEC}
 * 序列化为一个小的复合标签（{@code {resource: <注册表ID>, data: {...}}}）。
 * </p>
 *
 * @see RateExpression
 * @see SkillResource
 * @see Consumable
 * @see SkillResources
 * @since 1.0.0
 * @author Leaf
 */
public class SkillResourceOwner {

    /**
     * NBT key of the rate expression ({@code "x*1.5+2"} style). Missing or empty
     * means identity.
     * 变化率表达式的NBT键（{@code "x*1.5+2"} 风格）。缺失或为空表示恒等。
     */
    public static final String RATE_KEY = "rate";

    /**
     * Serialized key of the owned resource's registry id.
     * 被拥有资源的注册表ID的序列化键。
     */
    public static final String RESOURCE_KEY = "resource";

    /**
     * Serialized key of the owner NBT compound.
     * 拥有者NBT复合标签的序列化键。
     */
    public static final String DATA_KEY = "data";

    /**
     * Codec of the owner: {@code {resource: <registry id>, data: {rate: ..., ...}}}.
     * The {@code data} compound is optional and defaults to empty (identity rate).
     * 拥有者的Codec：{@code {resource: <注册表ID>, data: {rate: ..., ...}}}。
     * {@code data} 复合标签可选，默认为空（恒等变化率）。
     *
     * @see SkillResources#CODEC
     * @see #STREAM_CODEC
     * @since 1.0.0
     */
    public static final Codec<SkillResourceOwner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SkillResources.CODEC.fieldOf(RESOURCE_KEY).forGetter(SkillResourceOwner::resource),
            CompoundTag.CODEC.optionalFieldOf(DATA_KEY, new CompoundTag()).forGetter(SkillResourceOwner::nbt)
    ).apply(instance, SkillResourceOwner::new));

    /**
     * Stream codec of the owner for network transmission: the resource's registry
     * entry plus the owner NBT compound.
     * 拥有者用于网络传输的流Codec：资源的注册表条目加上拥有者NBT复合标签。
     *
     * @see SkillResources#STREAM_CODEC
     * @see ByteBufCodecs#COMPOUND_TAG
     * @see #CODEC
     * @since 1.0.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillResourceOwner> STREAM_CODEC = StreamCodec.composite(
            SkillResources.STREAM_CODEC, SkillResourceOwner::resource,
            ByteBufCodecs.COMPOUND_TAG, SkillResourceOwner::nbt,
            SkillResourceOwner::new
    );

    /**
     * The owned resource whose consumption is rate-adjusted.
     * 被拥有的资源，其消耗会被变化率调整。
     */
    private final SkillResource resource;

    /**
     * The owner NBT the rate expression was read from; also passed down to the
     * resource when consuming, so members can read their own keys from it.
     * 读取变化率表达式所用的拥有者NBT；消耗时也会下传给资源，
     * 因此成员可以从中读取各自的键。
     */
    private final CompoundTag nbt;

    /**
     * The parsed rate expression; {@code x} is substituted with the raw cost.
     * Null when no (valid) expression is configured — consumption stays identity.
     * 解析后的变化率表达式；{@code x} 会被替换为原始成本。
     * 未配置（有效）表达式时为 null——消耗保持恒等。
     */
    private final RateExpression rate;

    /**
     * Creates an owner for the given resource and NBT, parsing the rate expression
     * from the NBT up front.
     * 为给定资源与NBT创建拥有者，并预先从NBT解析变化率表达式。
     *
     * @param resource The owned skill resource / 被拥有的技能资源
     * @param nbt      The owner NBT (may carry {@value #RATE_KEY}) / 拥有者NBT（可含 {@value #RATE_KEY}）
     * @since 1.0.0
     */
    public SkillResourceOwner(SkillResource resource, CompoundTag nbt) {
        this.resource = resource;
        this.nbt = nbt;
        String source = nbt.getString(RATE_KEY).trim();
        if (source.isEmpty()) {
            // No rate configured: identity keeps the current behavior untouched.
            // 未配置变化率：恒等式保持现有行为不变。
            this.rate = null;
            return;
        }
        RateExpression parsed = null;
        try {
            parsed = RateExpression.parse(source);
        } catch (IllegalArgumentException e) {
            // A broken expression must not brick the skill; identity is the safe fallback.
            // 表达式损坏不能让技能瘫痪；恒等是安全的回退。
            Skiller.LOGGER.warn("Falling back to identity rate for expression \"{}\": {}", source, e.getMessage());
        }
        this.rate = parsed;
    }

    /**
     * Creates an identity owner (empty NBT, no rate) for the given resource.
     * 为给定资源创建恒等拥有者（空NBT，无变化率）。
     *
     * @param resource The owned skill resource / 被拥有的技能资源
     * @see #SkillResourceOwner(SkillResource, CompoundTag)
     * @since 1.0.0
     */
    public SkillResourceOwner(SkillResource resource) {
        this(resource, new CompoundTag());
    }

    /**
     * Returns the registry key of the owned resource — the identity used to share
     * consumables between instances in {@code SkillBundle#releaseSkills}.
     * 返回被拥有资源的注册表键——
     * 即 {@code SkillBundle#releaseSkills} 中实例间共享可消耗项所用的标识。
     *
     * @return The resource registry key / 资源注册表键
     * @see SkillResource#key()
     * @since 1.0.0
     */
    public ResourceKey<SkillResource> key() {
        return resource.key();
    }

    /**
     * Returns the owned resource.
     * 返回被拥有的资源。
     *
     * @return The owned skill resource / 被拥有的技能资源
     * @since 1.0.0
     */
    public SkillResource resource() {
        return resource;
    }

    /**
     * Returns the owner NBT the resource is consumed with.
     * 返回资源消耗时所用的拥有者NBT。
     *
     * @return A copy of the owner NBT / 拥有者NBT的副本
     * @since 1.0.0
     */
    public CompoundTag nbt() {
        return nbt.copy();
    }

    /**
     * Pushes a raw cost through the rate expression.
     * 将原始成本推入变化率表达式处理。
     * <p>
     * The result is floored (the resource rounding strategy) and clamped to
     * {@code [0, MAX_VALUE]}: negative outcomes become a free consumption, and
     * division results that are {@code NaN}/{@code Infinity} become effectively
     * unaffordable. The double-to-int cast saturates, so even astronomically large
     * quotients land on {@code MAX_VALUE} instead of overflowing negative.
     * 结果向下取整（资源取整策略）并钳制到 {@code [0, MAX_VALUE]}：
     * 负数结果视为免费消耗，除法产生的 {@code NaN}/{@code Infinity} 视为实际上无法负担。
     * double 到 int 的转换是饱和的，因此再大的商也会落在 {@code MAX_VALUE}，
     * 不会溢出为负数。
     * </p>
     *
     * @param amount The raw amount requested by the skill / 技能请求的原始数量
     * @return The rated amount to actually consume / 实际消耗的变化后数量
     * @see RateExpression#eval(double)
     * @since 1.0.0
     */
    public int rate(int amount) {
        if (rate == null) return amount;
        double value = rate.eval(amount);
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, (int) Math.floor(value));
    }

    /**
     * Wraps a shared inner consumable so this owner's rated costs accumulate into it.
     * 包装共享的内部可消耗项，使本拥有者的带率成本累积进它。
     * <p>
     * The wrapper transforms each raw amount with {@link #rate(int)} before forwarding,
     * keeping a single all-or-nothing check per resource key while still honoring
     * per-instance rates.
     * 包装器在转发前用 {@link #rate(int)} 变换每个原始数量，
     * 在每个资源键保持单次全有或全无检查的同时，仍然尊重每实例的变化率。
     * </p>
     *
     * @param inner The shared inner consumable to accumulate into / 要累积进的共享内部可消耗项
     * @return A consumable forwarding rated amounts into {@code inner} - 将带率数量转发进{@code inner}的可消耗项
     * @see #rate(int)
     * @since 1.0.0
     */
    public Consumable rated(Consumable inner) {
        return amount -> inner.consume(rate(amount));
    }
}
