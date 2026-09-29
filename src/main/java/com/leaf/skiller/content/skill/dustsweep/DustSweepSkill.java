package com.leaf.skiller.content.skill.dustsweep;

import com.leaf.skiller.foundation.Consumable;
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
 * Triggered in two ways, both of which end up in {@link #release(DustSweepContext, ISkillInstance)}:
 * 有两种触发方式，最终都会进入 {@link #release(DustSweepContext, ISkillInstance)}：
 * <ul>
 *     <li>Right-clicking a block — released by the server-side trigger for
 *     {@link com.leaf.skiller.AllSkillTypes#RIGHT_CLICK_BLOCK} skills.
 *     右键点击方块——由服务端针对
 *     {@link com.leaf.skiller.AllSkillTypes#RIGHT_CLICK_BLOCK} 类型技能的触发器释放。</li>
 *     <li>Holding the bound skill key — released on the key-down edge; while the
 *     key is held, the client additionally previews the affected items through
 *     the outline renderer (rendering is gated on the pressed key, so a
 *     right-click trigger never shows the preview).
 *     按住绑定的技能键——在按下沿释放；按住期间客户端还会通过轮廓渲染器
 *     预览受影响的物品（渲染以按下的按键为门控，因此右键触发不会显示预览）。</li>
 * </ul>
 * <p>
 * Item collection is delegated to {@link DustSweepStrategy}; this class only
 * applies the attraction effect to the collected {@link ItemEntity}s.
 * 物品收集委托给 {@link DustSweepStrategy}；此类只对收集到的
 * {@link ItemEntity} 施加吸引效果。
 * </p>
 *
 * @see StrategySkill
 * @see DustSweepStrategy
 * @see DustSweepContext
 * @see DustSweepContextFactory
 * @see com.leaf.skiller.AllSkills
 * @since 1.0.0
 * @author Leaf
 */
public class DustSweepSkill implements StrategySkill<Entity, DustSweepContext> {

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
     * <p>
     * Collection parameters (radius, pull strength) come from the context built
     * by {@link DustSweepContextFactory}; each attracted item has its pickup
     * delay cleared so it can be collected on contact.
     * 收集参数（半径、拉力）来自 {@link DustSweepContextFactory} 构建的上下文；
     * 每个被吸取的物品会清除拾取延迟，以便接触时即可拾取。
     * </p>
     *
     * @param context  The Dust Sweep context / 扫尘上下文
     * @param instance The skill instance being released / 正在释放的技能实例
     * @see DustSweepStrategy#collect(Set, DustSweepContext, ISkillInstance)
     * @see ItemEntity#setNoPickUpDelay()
     * @since 1.0.0
     */
    @Override
    public void release(DustSweepContext context, ISkillInstance<DustSweepContext> instance) {
        SkillStrategy<Entity, DustSweepContext> sweep = strategy();
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
     * @param context    The Dust Sweep context / 扫尘上下文
     * @param consumable The consumable accumulating the cost / 累积成本的可消耗项
     * @param instance   The skill instance / 技能实例
     * @see com.leaf.skiller.foundation.SkillResource
     * @since 1.0.0
     */
    @Override
    public void consumeResource(DustSweepContext context, Consumable consumable, ISkillInstance<DustSweepContext> instance) {
        // No cost for now; accumulate here once a real resource is configured.
        // 当前无成本；配置真实资源后在这里累积。
    }

    /**
     * Applies the attraction effect to a single item entity.
     * 对单个掉落物实体施加吸引效果。
     * <p>
     * The item is given a velocity pointing at the center of the player's body
     * with the context's pull strength; items already at the player's feet are
     * skipped.
     * 物品被赋予一个指向玩家身体中心、大小为上下文拉力的速度；
     * 已在玩家脚边的物品会被跳过。
     * </p>
     *
     * @param item    The item entity to attract / 要吸引的掉落物实体
     * @param context The Dust Sweep context providing the pull strength / 提供拉力的扫尘上下文
     * @since 1.0.0
     */
    private static void attract(ItemEntity item, DustSweepContext context) {
        var player = context.getPlayer();
        Vec3 target = player.position().add(0, player.getBbHeight() * 0.5, 0);
        Vec3 direction = target.subtract(item.position());
        if (direction.lengthSqr() < 0.01) return;

        item.setDeltaMovement(direction.normalize().scale(context.getPullStrength()));
        item.setNoPickUpDelay();
    }
}
