package com.leaf.skiller.content.skill.dustsweep;

import com.leaf.skiller.foundation.Consumable;
import com.leaf.skiller.foundation.context.AutoSkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.StrategySkill;
import com.leaf.skiller.foundation.strategy.SkillStrategy;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

/**
 * The Dust Sweep skill: attracts nearby dropped items toward the casting player.
 * 扫尘技能：将附近的掉落物吸引到施法玩家身边。
 * <p>
 * Uses the generic {@link AutoSkillContext}: all config values (radius, pull
 * strength, outline color) are resolved by its factory from the instance data
 * (with level-based fallbacks), so this class only reads resolved values from
 * {@link AutoSkillContext#getData()} — it never touches the raw instance data
 * or knows how defaults work.
 * 使用通用的 {@link AutoSkillContext}：所有配置值（半径、拉力、轮廓颜色）
 * 由其工厂从实例数据解析（缺失回退等级默认），
 * 因此此类只从 {@link AutoSkillContext#getData()} 读取解析后的值——
 * 从不接触原始实例数据，也不关心默认值的规则。
 * </p>
 * <p>
 * Triggered in two ways, both of which end up in {@link #release(AutoSkillContext, ISkillInstance)}:
 * 有两种触发方式，最终都会进入 {@link #release(AutoSkillContext, ISkillInstance)}：
 * <ul>
 *     <li>Right-clicking a block — released by the server-side trigger for
 *     {@link com.leaf.skiller.AllSkillTypes#RIGHT_CLICK_BLOCK} skills.
     右键点击方块——由服务端针对
     {@link com.leaf.skiller.AllSkillTypes#RIGHT_CLICK_BLOCK} 类型技能的触发器释放。</li>
 *     <li>Holding the bound skill key — the outline preview shows the affected
 *     items while the key is held (rendering is gated on the pressed key).
     按住绑定的技能键——按住期间轮廓预览显示受影响的物品
     （渲染以按下的按键为门控）。</li>
 * </ul>
 *
 * @see StrategySkill
 * @see DustSweepStrategy
 * @see AutoSkillContext
 * @see com.leaf.skiller.foundation.context.AutoSkillContextFactory
 * @see com.leaf.skiller.AllSkills
 * @since 1.0.0
 * @author Leaf
 */
public class DustSweepSkill implements StrategySkill<Entity, AutoSkillContext> {

    /**
     * Config key of the collection radius (blocks), resolved by the context factory.
     * 收集半径（格）的配置键，由上下文工厂解析。
     */
    public static final String KEY_RADIUS = "radius";

    /**
     * Config key of the pull strength applied to attracted items.
     * 施加到被吸取物品的拉力的配置键。
     */
    public static final String KEY_PULL_STRENGTH = "pull_strength";

    /**
     * The registry key of the strategy used by this skill.
     * 此技能所用策略的注册表键。
     */
    private final ResourceKey<SkillStrategy<?, ?>> strategyKey;

    /**
     * Creates the Dust Sweep skill bound to the given strategy key.
     * 创建绑定到给定策略键的扫尘技能。
     *
     * @param strategyKey The registry key of the {@link DustSweepStrategy}
     *                    {@link DustSweepStrategy} 的注册表键
     * @see com.leaf.skiller.AllSkills
     * @since 1.0.0
     */
    public DustSweepSkill(ResourceKey<SkillStrategy<?, ?>> strategyKey) {
        this.strategyKey = strategyKey;
    }

    /**
     * Returns the registry key identifying this skill's strategy.
     * 返回标识此技能策略的注册表键。
     *
     * @return The strategy registry key / 策略注册表键
     * @see StrategySkill#getStrategy()
     * @since 1.0.0
     */
    @Override
    public ResourceKey<SkillStrategy<?, ?>> getStrategy() {
        return strategyKey;
    }

    /**
     * Releases the skill: collects nearby dropped items and attracts them to the player.
     * 释放技能：收集附近的掉落物并将其吸向玩家。
     *
     * @param context  The resolved Dust Sweep context / 解析后的扫尘上下文
     * @param instance The skill instance being released / 正在释放的技能实例
     * @see DustSweepStrategy#collect(Set, AutoSkillContext, ISkillInstance)
     * @see ItemEntity#setNoPickUpDelay()
     * @since 1.0.0
     */
    @Override
    public void release(AutoSkillContext context, ISkillInstance<AutoSkillContext> instance) {
        SkillStrategy<Entity, AutoSkillContext> sweep = strategy();
        Set<Entity> targets = new HashSet<>();
        if (!sweep.canCollect(context, instance)) return;
        sweep.collect(targets, context, instance);

        for (Entity entity : targets) {
            if (entity instanceof ItemEntity item) {
                attract(item, context);
            }
        }
    }

    /**
     * Consumes the skill's resource cost. Dust Sweep currently has no cost
     * (it is registered with the EMPTY resource), so this is a no-op.
     * 消耗技能的资源成本。扫尘当前没有成本（以 EMPTY 资源注册），
     * 因此此方法为空操作。
     *
     * @param context    The resolved Dust Sweep context / 解析后的扫尘上下文
     * @param consumable The consumable accumulating the cost / 累积成本的可消耗项
     * @param instance   The skill instance / 技能实例
     * @since 1.0.0
     */
    @Override
    public void consumeResource(AutoSkillContext context, Consumable consumable, ISkillInstance<AutoSkillContext> instance) {
        // No cost for now; accumulate here once a real resource is configured.
        // 当前无成本；配置真实资源后在这里累积。
    }

    /**
     * Applies the attraction effect to a single item entity.
     * 对单个掉落物实体施加吸引效果。
     *
     * @param item    The item entity to attract / 要吸引的掉落物实体
     * @param context The resolved context providing the pull strength / 提供拉力的解析后上下文
     * @since 1.0.0
     */
    private static void attract(ItemEntity item, AutoSkillContext context) {
        var player = context.getPlayer();
        double pullStrength = context.getData().getDouble(KEY_PULL_STRENGTH);

        Vec3 target = player.position().add(0, player.getBbHeight() * 0.5, 0);
        Vec3 direction = target.subtract(item.position());
        if (direction.lengthSqr() < 0.01) return;

        item.setDeltaMovement(direction.normalize().scale(pullStrength));
        item.setNoPickUpDelay();
    }
}
