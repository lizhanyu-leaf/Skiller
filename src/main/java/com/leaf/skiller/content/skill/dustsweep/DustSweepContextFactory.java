package com.leaf.skiller.content.skill.dustsweep;

import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.config.SkillContextEnvironment;
import com.leaf.skiller.foundation.skill.config.SkillContextFactory;
import com.leaf.skiller.util.ColorConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Context factory for the Dust Sweep skill.
 * 扫尘技能的上下文工厂。
 * <p>
 * Builds {@link DustSweepContext}s from two sources:
 * 从两个来源构建 {@link DustSweepContext}：
 * <ul>
 *     <li>Per-instance config stored in the instance's NBT data (keys
 *     {@code "radius"} and {@code "pull_strength"}), which takes precedence —
 *     this is the decoupled config channel: the skill/strategy never read
 *     instance data directly.
 *     存储在实例 NBT 数据中的每实例配置（键 {@code "radius"} 和 {@code "pull_strength"}），
 *     优先使用——这是解耦的配置通道：技能和策略从不直接读取实例数据。</li>
 *     <li>Level-based defaults used when the config is absent, so higher-level
 *     instances sweep a wider area with stronger pull.
 *     配置缺失时使用基于等级的默认值，等级越高的实例扫过的范围越大、拉力越强。</li>
 * </ul>
 *
 * @see DustSweepContext
 * @see DustSweepSkill
 * @see SkillContextFactory
 * @since 1.0.0
 * @author Leaf
 */
public class DustSweepContextFactory implements SkillContextFactory<DustSweepContext> {

    /**
     * NBT key for the per-instance collection radius config.
     * 每实例收集半径配置的 NBT 键。
     */
    public static final String KEY_RADIUS = "radius";

    /**
     * NBT key for the per-instance pull strength config.
     * 每实例拉力强度配置的 NBT 键。
     */
    public static final String KEY_PULL_STRENGTH = "pull_strength";

    /**
     * Outline color used for the item preview (gold, fully opaque).
     * 掉落物预览使用的轮廓颜色（金色，完全不透明）。
     */
    public static final ColorConfig DEFAULT_COLOR = new ColorConfig(1.0F, 0.84F, 0.0F, 1.0F);

    /**
     * Hard limits keeping NBT-provided config within sane bounds.
     * 硬性限制，保证 NBT 提供的配置处于合理范围。
     */
    private static final double MIN_RADIUS = 1.0;
    private static final double MAX_RADIUS = 16.0;
    private static final double MIN_PULL = 0.1;
    private static final double MAX_PULL = 3.0;

    /**
     * Creates a context for releasing an existing instance, honoring its config data.
     * 为释放现有实例创建上下文，遵循其配置数据。
     * <p>
     * Reads {@code "radius"} and {@code "pull_strength"} from the instance's
     * data tag; missing keys fall back to level-based defaults computed by
     * {@link #createDefault(SkillContextEnvironment, int)}. Values are clamped
     * to sane bounds.
     * 从实例的数据标签读取 {@code "radius"} 和 {@code "pull_strength"}；
     * 缺失的键回退到 {@link #createDefault(SkillContextEnvironment, int)}
     * 计算的基于等级的默认值。数值会被限制到合理范围内。
     * </p>
     *
     * @param env      The environment providing the player (and trigger event, if any)
     *                 提供玩家（以及触发事件，如有）的环境
     * @param instance The skill instance whose config and level are used
     *                 使用其配置和等级的技能实例
     * @return A ready-to-use Dust Sweep context / 可直接使用的扫尘上下文
     * @see SkillContextFactory#create(SkillContextEnvironment, ISkillInstance)
     * @since 1.0.0
     */
    @Override
    public DustSweepContext create(SkillContextEnvironment env, ISkillInstance<DustSweepContext> instance) {
        CompoundTag data = instance.data();
        Player player = env.getPlayer();

        double radius = data.contains(KEY_RADIUS)
                ? data.getDouble(KEY_RADIUS)
                : defaultRadius(instance.level());
        double pull = data.contains(KEY_PULL_STRENGTH)
                ? data.getDouble(KEY_PULL_STRENGTH)
                : defaultPull(instance.level());

        return new DustSweepContext(player, clampRadius(radius), clampPull(pull), DEFAULT_COLOR);
    }

    /**
     * Creates a default context for a given level, without per-instance config.
     * 为给定等级创建默认上下文，不使用每实例配置。
     * <p>
     * Defaults scale with level: radius {@code 3 + level} blocks and pull
     * strength {@code 0.6 + 0.1 * level}, both clamped to sane bounds.
     * 默认值随等级缩放：半径 {@code 3 + level} 格，拉力 {@code 0.6 + 0.1 * level}，
     * 均被限制到合理范围内。
     * </p>
     *
     * @param env   The environment providing the player / 提供玩家的环境
     * @param level The skill level used to scale the defaults / 用于缩放默认值的技能等级
     * @return A default Dust Sweep context / 默认的扫尘上下文
     * @see SkillContextFactory#createDefault(SkillContextEnvironment, int)
     * @since 1.0.0
     */
    @Override
    public DustSweepContext createDefault(SkillContextEnvironment env, int level) {
        return new DustSweepContext(
                env.getPlayer(),
                clampRadius(defaultRadius(level)),
                clampPull(defaultPull(level)),
                DEFAULT_COLOR
        );
    }

    /**
     * Returns the level-based default radius ({@code 3 + level} blocks).
     * 返回基于等级的默认半径（{@code 3 + level} 格）。
     *
     * @param level The skill level / 技能等级
     * @return The default radius / 默认半径
     * @since 1.0.0
     */
    private static double defaultRadius(int level) {
        return 3.0 + level;
    }

    /**
     * Returns the level-based default pull strength ({@code 0.6 + 0.1 * level}).
     * 返回基于等级的默认拉力（{@code 0.6 + 0.1 * level}）。
     *
     * @param level The skill level / 技能等级
     * @return The default pull strength / 默认拉力
     * @since 1.0.0
     */
    private static double defaultPull(int level) {
        return 0.6 + 0.1 * level;
    }

    /**
     * Clamps a radius value into {@link #MIN_RADIUS}..{@link #MAX_RADIUS}.
     * 将半径值限制到 {@link #MIN_RADIUS}..{@link #MAX_RADIUS} 之间。
     *
     * @param radius The raw radius / 原始半径
     * @return The clamped radius / 限制后的半径
     * @since 1.0.0
     */
    private static double clampRadius(double radius) {
        return Mth.clamp(radius, MIN_RADIUS, MAX_RADIUS);
    }

    /**
     * Clamps a pull strength value into {@link #MIN_PULL}..{@link #MAX_PULL}.
     * 将拉力值限制到 {@link #MIN_PULL}..{@link #MAX_PULL} 之间。
     *
     * @param pull The raw pull strength / 原始拉力
     * @return The clamped pull strength / 限制后的拉力
     * @since 1.0.0
     */
    private static double clampPull(double pull) {
        return Mth.clamp(pull, MIN_PULL, MAX_PULL);
    }
}
