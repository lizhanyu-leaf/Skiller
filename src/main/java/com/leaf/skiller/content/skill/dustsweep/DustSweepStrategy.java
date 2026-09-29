package com.leaf.skiller.content.skill.dustsweep;

import com.leaf.skiller.Skiller;
import com.leaf.skiller.content.strategy.EntityStrategy;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.Set;

/**
 * Strategy for the Dust Sweep skill: collects dropped item entities around the player.
 * 扫尘技能的策略：收集玩家周围的掉落物实体。
 * <p>
 * The radius comes from the {@link DustSweepContext} built by
 * {@link DustSweepContextFactory}, so the strategy itself holds no config —
 * adjusting the sweep range only requires changing the instance config or the
 * factory defaults.
 * 半径来自由 {@link DustSweepContextFactory} 构建的 {@link DustSweepContext}，
 * 策略自身不持有任何配置——调整扫尘范围只需要修改实例配置或工厂默认值。
 * </p>
 * <p>
 * The same {@link #collect(Set, DustSweepContext, ISkillInstance)} result feeds
 * two consumers: the server-side release (attracting the items) and the
 * client-side {@code EntityOutlineRenderer} preview (outlining the items while
 * the skill key is held), guaranteeing the preview always matches the effect.
 * 同一个 {@link #collect(Set, DustSweepContext, ISkillInstance)} 结果供给两个消费方：
 * 服务端释放（吸取物品）和客户端 EntityOutlineRenderer 预览
 * （按住技能键时对物品描边），保证预览与实际效果一致。
 * </p>
 *
 * @see EntityStrategy
 * @see DustSweepSkill
 * @see DustSweepContext
 * @see ItemEntity
 * @since 1.0.0
 * @author Leaf
 */
public class DustSweepStrategy implements EntityStrategy<DustSweepContext> {

    /**
     * The renderer id under which the client-side outline preview is registered.
     * 客户端轮廓预览注册所用的渲染器ID。
     *
     * @see com.leaf.skiller.client.renderer.AllStrategyRenderers
     * @see com.leaf.skiller.client.renderer.StrategyRenderers#register(ResourceLocation, com.leaf.skiller.foundation.renderer.StrategyRenderer)
     * @since 1.0.0
     */
    public static final ResourceLocation RENDERER_ID = Skiller.modLoc("dust_sweep_outline");

    /**
     * Checks whether collection is possible in the given context.
     * 检查在给定上下文中是否可以进行收集。
     * <p>
     * Collection requires the casting player to be alive; the client-side
     * preview calls this every frame, so no server-only assumptions are made.
     * 收集要求施法玩家存活；客户端预览每帧都会调用此方法，
     * 因此不做任何仅限服务端的假设。
     * </p>
     *
     * @param context  The Dust Sweep context / 扫尘上下文
     * @param instance The skill instance / 技能实例
     * @return true if the player is alive / 玩家存活时返回 true
     * @see com.leaf.skiller.foundation.strategy.SkillStrategy
     * @since 1.0.0
     */
    @Override
    public boolean canCollect(DustSweepContext context, ISkillInstance<DustSweepContext> instance) {
        return context.getPlayer().isAlive();
    }

    /**
     * Collects all alive dropped item entities within the context radius.
     * 收集上下文半径内存活的所有掉落物实体。
     * <p>
     * The search box is the player's bounding box inflated by the configured
     * radius; only {@link ItemEntity}s that are still alive are added to the
     * result set.
     * 搜索范围为玩家包围盒向外扩展配置的半径；
     * 仅将仍然存活的 {@link ItemEntity} 加入结果集合。
     * </p>
     *
     * @param set      The set to collect matching entities into / 用于收集匹配实体的集合
     * @param context  The Dust Sweep context providing the radius / 提供半径的扫尘上下文
     * @param instance The skill instance / 技能实例
     * @see com.leaf.skiller.foundation.strategy.SkillStrategy
     * @since 1.0.0
     */
    @Override
    public void collect(Set<Entity> set, DustSweepContext context, ISkillInstance<DustSweepContext> instance) {
        var player = context.getPlayer();
        set.addAll(player.level().getEntitiesOfClass(
                ItemEntity.class,
                player.getBoundingBox().inflate(context.getRadius()),
                ItemEntity::isAlive
        ));
    }

    /**
     * Returns the renderer id of the client-side outline preview.
     * 返回客户端轮廓预览的渲染器ID。
     *
     * @return The registered renderer id / 已注册的渲染器ID
     * @see #RENDERER_ID
     * @since 1.0.0
     */
    @Override
    public ResourceLocation getRendererId() {
        return RENDERER_ID;
    }
}
