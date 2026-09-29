package com.leaf.skiller.client.renderer;

import com.leaf.skiller.client.renderer.entity.EntityOutlineRenderer;
import com.leaf.skiller.content.skill.dustsweep.DustSweepStrategy;

/**
 * Registration holder for all client-side strategy renderers of the Skiller mod.
 * Skiller 模组所有客户端策略渲染器的注册持有类。
 * <p>
 * Renderers are registered under the {@link com.leaf.skiller.foundation.strategy.SkillStrategy#getRendererId()}
 * ids referenced by strategies, so scheduling can look them up per skill. This
 * class is client-only and must only be loaded on the physical client (it is
 * referenced from {@link com.leaf.skiller.client.ClientEvents}, which is
 * annotated with {@code Dist.CLIENT}).
 * 渲染器按策略引用的 {@link com.leaf.skiller.foundation.strategy.SkillStrategy#getRendererId()}
 * ID 注册，调度时即可按技能查找。此类仅限客户端，
 * 只能在物理客户端加载（由标注了 {@code Dist.CLIENT} 的
 * {@link com.leaf.skiller.client.ClientEvents} 引用）。
 * </p>
 *
 * @see StrategyRenderers
 * @see EntityOutlineRenderer
 * @see DustSweepStrategy#RENDERER_ID
 * @since 1.0.0
 * @author Leaf
 */
public class AllStrategyRenderers {

    /**
     * The shared entity outline renderer used by all entity-preview strategies.
     * 所有实体预览策略共用的实体轮廓渲染器。
     */
    public static final EntityOutlineRenderer ENTITY = new EntityOutlineRenderer();

    /**
     * Registers all strategy renderers under the renderer ids referenced by
     * their strategies. Called once from {@code ClientEvents}' static
     * initializer, which only loads on the client.
     * 将所有策略渲染器按其策略引用的渲染器ID注册。
     * 由 {@code ClientEvents} 的静态初始化块调用一次，该类仅在客户端加载。
     *
     * @see com.leaf.skiller.client.ClientEvents
     * @see DustSweepStrategy#RENDERER_ID
     * @since 1.0.0
     */
    public static void register() {
        StrategyRenderers.register(DustSweepStrategy.RENDERER_ID, ENTITY);
    }

    private AllStrategyRenderers() {
        throw new AssertionError("This class should not be instantiated");
    }
}
