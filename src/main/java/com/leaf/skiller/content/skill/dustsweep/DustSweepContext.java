package com.leaf.skiller.content.skill.dustsweep;

import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.config.impl.SkillColorContext;
import com.leaf.skiller.util.ColorConfig;
import net.minecraft.world.entity.player.Player;

/**
 * Execution context for the Dust Sweep skill.
 * 扫尘技能的执行上下文。
 * <p>
 * Carries everything the skill and its strategy need: the casting player, the
 * collection radius, the pull strength applied to attracted items, and the
 * outline color used by the client-side preview renderer. Instances are built
 * exclusively by {@link DustSweepContextFactory}, which reads the per-instance
 * config from the instance's NBT data — the skill and strategy never touch the
 * instance data themselves.
 * 携带技能及其策略所需的全部信息：施法玩家、收集半径、
 * 吸取物品时施加的拉力，以及客户端预览渲染器使用的轮廓颜色。
 * 实例只能由 {@link DustSweepContextFactory} 构建，
 * 工厂从实例的 NBT 数据读取每实例配置——技能和策略自身从不接触实例数据。
 * </p>
 *
 * @see DustSweepContextFactory
 * @see DustSweepSkill
 * @see DustSweepStrategy
 * @see SkillColorContext
 * @since 1.0.0
 * @author Leaf
 */
public class DustSweepContext implements SkillContext, SkillColorContext {

    /**
     * The player casting the skill; items are attracted toward this player.
     * 施放技能的玩家；掉落物将被吸向该玩家。
     */
    private final Player player;

    /**
     * Collection radius in blocks around the player.
     * 以玩家为中心的收集半径（格）。
     */
    private final double radius;

    /**
     * Velocity magnitude applied to attracted item entities.
     * 施加到被吸取掉落物上的速度大小。
     */
    private final double pullStrength;

    /**
     * Outline color used by the client-side preview renderer.
     * 客户端预览渲染器使用的轮廓颜色。
     */
    private final ColorConfig color;

    /**
     * Creates a new Dust Sweep context.
     * 创建新的扫尘上下文。
     *
     * @param player       The casting player / 施法玩家
     * @param radius       The collection radius in blocks / 收集半径（格）
     * @param pullStrength The velocity applied to attracted items / 施加到被吸取物品的速度
     * @param color        The outline color for the preview renderer / 预览渲染器的轮廓颜色
     * @since 1.0.0
     */
    public DustSweepContext(Player player, double radius, double pullStrength, ColorConfig color) {
        this.player = player;
        this.radius = radius;
        this.pullStrength = pullStrength;
        this.color = color;
    }

    /**
     * Returns the casting player.
     * 返回施法玩家。
     *
     * @return The casting player / 施法玩家
     * @see SkillContext#getPlayer()
     * @since 1.0.0
     */
    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the collection radius in blocks.
     * 返回收集半径（格）。
     *
     * @return The collection radius / 收集半径
     * @since 1.0.0
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Returns the velocity magnitude applied to attracted items.
     * 返回施加到被吸取物品上的速度大小。
     *
     * @return The pull strength / 拉力强度
     * @since 1.0.0
     */
    public double getPullStrength() {
        return pullStrength;
    }

    /**
     * Returns the outline color for the preview renderer.
     * 返回预览渲染器的轮廓颜色。
     *
     * @return The outline color config / 轮廓颜色配置
     * @see SkillColorContext#getColor()
     * @since 1.0.0
     */
    @Override
    public ColorConfig getColor() {
        return color;
    }
}
