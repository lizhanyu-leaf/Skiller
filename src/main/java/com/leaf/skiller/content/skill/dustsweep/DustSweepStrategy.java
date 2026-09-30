package com.leaf.skiller.content.skill.dustsweep;

import com.leaf.skiller.Skiller;
import com.leaf.skiller.content.strategy.EntityStrategy;
import com.leaf.skiller.foundation.context.AutoSkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.Set;

/**
 * Strategy for the Dust Sweep skill: collects dropped item entities around the player.
 * 扫尘技能的策略：收集玩家周围的掉落物实体。
 * <p>
 * The radius comes from the resolved {@link AutoSkillContext} built by
 * {@link com.leaf.skiller.foundation.context.AutoSkillContextFactory}, so the
 * strategy itself holds no config — it reads the resolved value from
 * {@link AutoSkillContext#getData()}.
 * 半径来自由 {@link com.leaf.skiller.foundation.context.AutoSkillContextFactory}
 * 构建的解析后 {@link AutoSkillContext}，策略自身不持有任何配置——
 * 直接从 {@link AutoSkillContext#getData()} 读取解析后的值。
 * </p>
 * <p>
 * The same {@link #collect(Set, AutoSkillContext, ISkillInstance)} result feeds
 * two consumers: the server-side release (attracting the items) and the
 * client-side {@code EntityOutlineRenderer} preview (outlining the items while
 * the skill key is held), guaranteeing the preview always matches the effect.
 * 同一个 {@link #collect(Set, AutoSkillContext, ISkillInstance)} 结果供给两个消费方：
 * 服务端释放（吸取物品）和客户端 EntityOutlineRenderer 预览
 * （按住技能键时对物品描边），保证预览与实际效果一致。
 * </p>
 *
 * @see EntityStrategy
 * @see DustSweepSkill
 * @see AutoSkillContext
 * @see ItemEntity
 * @since 1.0.0
 * @author Leaf
 */
public class DustSweepStrategy implements EntityStrategy<AutoSkillContext> {

    /**
     * The renderer id under which the client-side outline preview is registered.
     * 客户端轮廓预览注册所用的渲染器ID。
     *
     * @see com.leaf.skiller.client.renderer.AllStrategyRenderers
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
     * @param context  The resolved Dust Sweep context / 解析后的扫尘上下文
     * @param instance The skill instance / 技能实例
     * @return true if the player is alive / 玩家存活时返回 true
     * @see com.leaf.skiller.foundation.strategy.SkillStrategy
     * @since 1.0.0
     */
    @Override
    public boolean canCollect(AutoSkillContext context, ISkillInstance<AutoSkillContext> instance) {
        return context.getPlayer().isAlive();
    }

    /**
     * Collects all alive dropped item entities within the context radius.
     * 收集上下文半径内存活的所有掉落物实体。
     *
     * @param set      The set to collect matching entities into / 用于收集匹配实体的集合
     * @param context  The resolved context providing the radius / 提供半径的解析后上下文
     * @param instance The skill instance / 技能实例
     * @see com.leaf.skiller.foundation.strategy.SkillStrategy
     * @since 1.0.0
     */
    @Override
    public void collect(Set<Entity> set, AutoSkillContext context, ISkillInstance<AutoSkillContext> instance) {
        var player = context.getPlayer();
        double radius = context.getData().getDouble(DustSweepSkill.KEY_RADIUS);
        set.addAll(player.level().getEntitiesOfClass(
                ItemEntity.class,
                player.getBoundingBox().inflate(radius),
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
