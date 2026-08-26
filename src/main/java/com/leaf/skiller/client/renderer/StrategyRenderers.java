package com.leaf.skiller.client.renderer;

import com.leaf.skiller.client.ClientSkillCache;
import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.renderer.StrategyRenderer;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.StrategySkill;
import com.leaf.skiller.foundation.strategy.SkillStrategy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager for strategy skill renderers in the client-side rendering pipeline.
 * 客户端渲染管线中策略技能渲染器的管理器。
 * <p>
 * This class manages the registration, scheduling, and execution of strategy skill renderers.
 * Strategy skills are skills that have visual effects rendered in the 3D world, such as
 * indicators, projectiles, or area-of-effect displays.
 * 此类管理策略技能渲染器的注册、调度和执行。策略技能是指在3D世界中渲染视觉效果的技能，
 * 例如指示器、投射物或区域效果显示。
 * </p>
 * <p>
 * The class maintains two main data structures:
 * 该类维护两个主要数据结构：
 * <ul>
 * <li>A registry of all available strategy renderers indexed by their resource location ID
 * - 由其资源位置ID索引的所有可用策略渲染器的注册表</li>
 * <li>A mapping of render stages to lists of active renderers for efficient rendering
 * - 渲染阶段到活动渲染器列表的映射，以实现高效渲染</li>
 * </ul>
 *
 * @see StrategyRenderer
 * @see StrategySkill
 * @see RenderLevelStageEvent
 * @since 1.0.0
 */
public class StrategyRenderers {
    /**
     * Registry of all available strategy renderers indexed by their resource location IDs.
 * 由其资源位置ID索引的所有可用策略渲染器的注册表。
     * <p>
     * Renderers are registered during mod initialization and are retrieved based on the
 * strategy's renderer ID when scheduling skill rendering.
 * 渲染器在模组初始化期间注册，并在调度技能渲染时根据策略的渲染器ID进行检索。
     * </p>
     *
     * @see StrategyRenderer
     * @see #register(ResourceLocation, StrategyRenderer)
     * @see #getRenderer(SkillStrategy)
     * @since 1.0.0
     */
    public static final Map<ResourceLocation, StrategyRenderer<?>> RENDERERS = new HashMap<>();

    /**
     * Mapping of render stages to lists of active renderer functions.
 * 渲染阶段到活动渲染器函数列表的映射。
     * <p>
     * This map is populated during the scheduling phase and used during rendering to efficiently
 * execute only the renderers for the current render stage. This optimizes performance by
 * avoiding unnecessary render calls.
 * 此映射在调度阶段填充，并在渲染期间使用，仅执行当前渲染阶段的渲染器，从而优化性能。
     * </p>
     * <p>
     * Key: RenderLevelStageEvent.Stage (e.g., AFTER_PARTICLES, AFTER_TRANSLUCENT_BLOCKS)
 * 键：RenderLevelStageEvent.Stage（例如 AFTER_PARTICLES、AFTER_TRANSLUCENT_BLOCKS）
     * <br>
     * Value: List of Renderer functions to execute for that stage
 * 值：该阶段要执行的Renderer函数列表
     * </p>
     *
     * @see Renderer
     * @see RenderLevelStageEvent.Stage
     * @see #schedule()
     * @see #render(RenderLevelStageEvent, Minecraft, ClientLevel, PoseStack, Camera, MultiBufferSource)
     * @since 1.0.0
     */
    private static final Map<RenderLevelStageEvent.Stage, List<Renderer>> STAGE_RENDERERS = new HashMap<>();

    /**
     * Registers a strategy renderer with the specified resource location ID.
 * 使用指定的资源位置ID注册策略渲染器。
     * <p>
     * This method should be called during mod initialization to register custom strategy renderers.
 * The renderer can later be retrieved by its ID during the scheduling phase.
 * 此方法应在模组初始化期间调用以注册自定义策略渲染器。
 * 渲染器稍后可在调度阶段通过其ID检索。
     * </p>
     * <p>
     * Each renderer corresponds to a specific type of strategy skill and defines how that skill
 * should be visually represented in the 3D world.
 * 每个渲染器对应于特定类型的策略技能，并定义该技能在3D世界中应如何可视化呈现。
     * </p>
     *
     * @param id The unique resource location ID for this renderer (e.g., "mymod:fireball") - 此渲染器的唯一资源位置ID（例如 "mymod:fireball"）
     * @param renderer The strategy renderer instance to register - 要注册的策略渲染器实例
     * @see StrategyRenderer
     * @see ResourceLocation
     * @see #getRenderer(SkillStrategy)
     * @since 1.0.0
     */
    public static void register(ResourceLocation id, StrategyRenderer<?> renderer) {
        RENDERERS.put(id, renderer);
    }

    /**
     * Retrieves a strategy renderer for the specified skill strategy.
 * 检索指定技能策略的策略渲染器。
     * <p>
     * This method looks up the appropriate renderer based on the strategy's renderer ID.
 * The renderer ID is obtained from the strategy's {@link SkillStrategy#getRendererId()} method.
 * 此方法根据策略的渲染器ID查找适当的渲染器。
 * 渲染器ID从策略的 {@link SkillStrategy#getRendererId()} 方法获取。
     * </p>
     * <p>
     * The method uses unchecked casting to return the renderer with the correct context type.
 * It is the caller's responsibility to ensure the strategy has a registered renderer.
 * 此方法使用未检查的强制转换来返回具有正确上下文类型的渲染器。
 * 调用者负责确保策略具有已注册的渲染器。
     * </p>
     *
     * @param <T> The type of skill context required by the renderer - 渲染器所需的技能上下文类型
     * @param strategy The skill strategy for which to retrieve the renderer - 要检索渲染器的技能策略
     * @return The strategy renderer instance for the specified strategy, or null if not registered - 指定策略的策略渲染器实例，如果未注册则返回null
     * @see SkillStrategy
     * @see SkillStrategy#getRendererId()
     * @see StrategyRenderer
     * @see #register(ResourceLocation, StrategyRenderer)
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    public static <T extends SkillContext> StrategyRenderer<T> getRenderer(SkillStrategy<?, T> strategy) {
        return (StrategyRenderer<T>) RENDERERS.get(strategy.getRendererId());
    }

    /**
     * Schedules all active strategy skills for rendering.
 * 安排所有活动策略技能进行渲染。
     * <p>
     * This method builds the rendering schedule by iterating through all active skills and
 * organizing them by their render stage. This optimizes the rendering process by ensuring
 * each renderer is only called during its appropriate stage.
 * 此方法通过遍历所有活动技能并按渲染阶段组织它们来构建渲染计划。
 * 这通过确保每个渲染器仅在适当的阶段被调用，从而优化了渲染过程。
     * </p>
     * <p>
     * The scheduling process:
 * 调度过程：
     * <ol>
     * <li>Clears any previous rendering schedule
 * - 清除任何先前的渲染计划</li>
     * <li>Iterates through all skills in the client skill cache
 * - 遍历客户端技能缓存中的所有技能</li>
     * <li>Filters for only StrategySkill instances (skills with visual effects)
 * - 仅过滤StrategySkill实例（具有视觉效果的技能）</li>
     * <li>Retrieves the appropriate renderer for each strategy skill
 * - 检索每个策略技能的适当渲染器</li>
     * <li>Creates a renderer function bound to the skill instance
 * - 创建绑定到技能实例的渲染器函数</li>
     * <li>Groups renderers by their render stage for efficient execution
 * - 按渲染阶段分组渲染器以实现高效执行</li>
     * </ol>
     * </p>
     * <p>
     * This method should be called whenever the set of active skills changes, such as when
 * the skill system is enabled or when the player gains/loses skills.
 * 每当活动技能集发生变化时（例如启用技能系统或玩家获得/失去技能时），都应调用此方法。
     * </p>
     *
     * @see ClientSkillCache#skills
     * @see StrategySkill
     * @see SkillStrategy
     * @see StrategyRenderer#getStage()
     * @see #disable()
     * @since 1.0.0
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void schedule() {
        // Clear previous schedule / 清除先前的计划
        STAGE_RENDERERS.clear();

        // Iterate through all active skills and schedule their renderers
        // 遍历所有活动技能并安排它们的渲染器
        ClientSkillCache.skills.getAllData().forEach(instance -> {
            // Only process strategy skills (skills with visual effects)
            // 仅处理策略技能（具有视觉效果的技能）
            if (!(instance.skill() instanceof StrategySkill<?,?> strategySkill)) return;

            // Get the renderer for this strategy
            // 获取此策略的渲染器
            var renderer = getRenderer(strategySkill.strategy());

            // Create a lambda that binds the skill instance to the renderer
            // 创建一个将技能实例绑定到渲染器的lambda表达式
            STAGE_RENDERERS.computeIfAbsent(renderer.getStage(), key -> new ArrayList<>())
                    .add((mc, level, stack, camera, buffer) ->
                            renderer.render(mc, (ISkillInstance) instance, level, stack, camera, buffer));
        });
    }

    /**
     * Disables all strategy skill rendering by clearing the render schedule.
 * 通过清除渲染计划来禁用所有策略技能渲染。
     * <p>
     * This method removes all scheduled renderers from all render stages, effectively
 * stopping all strategy skill visual effects until {@link #schedule()} is called again.
 * 此方法从所有渲染阶段移除所有计划的渲染器，有效地停止所有策略技能视觉效果，
 * 直到再次调用 {@link #schedule()}。
     * </p>
     * <p>
     * This is typically called when the skill system is disabled or when the player disconnects
 * from a server.
 * 这通常在技能系统被禁用或玩家与服务器断开连接时调用。
     * </p>
     *
     * @see #schedule()
     * @see ClientSkillCache#disable
     * @since 1.0.0
     */
    public static void disable() {
        STAGE_RENDERERS.clear();
    }

    /**
     * Executes all strategy skill renderers for the specified render stage.
 * 执行指定渲染阶段的所有策略技能渲染器。
     * <p>
     * This method is called during the world rendering pipeline to render skill visual effects.
 * It retrieves the list of renderers for the current render stage and executes each one with
 * the provided rendering context.
 * 此方法在世界渲染管线期间被调用以渲染技能视觉效果。
 * 它检索当前渲染阶段的渲染器列表，并使用提供的渲染上下文执行每个渲染器。
     * </p>
     * <p>
     * The method provides all necessary rendering context to each renderer:
 * 该方法为每个渲染器提供所有必要的渲染上下文：
     * <ul>
     * <li>Minecraft client instance - for accessing game state
 * - Minecraft客户端实例 - 用于访问游戏状态</li>
     * <li>Client level - for accessing world data
 * - 客户端级别 - 用于访问世界数据</li>
     * <li>PoseStack - for transformation matrices
 * - PoseStack - 用于变换矩阵</li>
     * <li>Camera - for view frustum and perspective
 * - Camera - 用于视锥体和透视</li>
     * <li>MultiBufferSource - for vertex buffer management
 * - MultiBufferSource - 用于顶点缓冲区管理</li>
     * </ul>
     * </p>
     * <p>
     * If no renderers are registered for the current stage, the method does nothing.
 * 如果当前阶段没有注册渲染器，则该方法不执行任何操作。
     * </p>
     *
     * @param event The render level stage event containing the current stage information - 包含当前阶段信息的渲染级别阶段事件
     * @param mc The Minecraft client instance - Minecraft客户端实例
     * @param level The client level being rendered - 正在渲染的客户端级别
     * @param stack The pose stack for rendering transformations - 渲染变换的位姿栈
     * @param camera The camera for the current view - 当前视图的相机
     * @param buffer The multi-buffer source for vertex output - 顶点输出的多缓冲区源
     * @see RenderLevelStageEvent
     * @see RenderLevelStageEvent#getStage()
     * @see Renderer#render(Minecraft, ClientLevel, PoseStack, Camera, MultiBufferSource)
     * @see #schedule()
     * @since 1.0.0
     */
    public static void render(RenderLevelStageEvent event, Minecraft mc, ClientLevel level,
                              PoseStack stack, Camera camera, MultiBufferSource buffer) {
        // Get the list of renderers for this stage and execute each one
        // 获取此阶段的渲染器列表并执行每个渲染器
        STAGE_RENDERERS.get(event.getStage())
                .forEach(renderer -> renderer.render(mc, level, stack, camera, buffer));
    }

    /**
     * Functional interface for rendering a strategy skill in the 3D world.
 * 用于在3D世界中渲染策略技能的函数式接口。
     * <p>
     * This interface encapsulates a single rendering operation bound to a specific skill instance.
 * It is created during the scheduling phase to bind the generic renderer to a concrete skill
 * instance, allowing efficient rendering without type checking during the render loop.
 * 此接口封装了绑定到特定技能实例的单个渲染操作。
 * 它在调度期间创建，将通用渲染器绑定到具体的技能实例，从而在渲染循环中实现高效渲染而无需类型检查。
     * </p>
     * <p>
     * Each Renderer function is bound to:
 * 每个Renderer函数绑定到：
     * <ul>
     * <li>A specific strategy skill instance (with its level and context data)
 * - 特定的策略技能实例（及其等级和上下文数据）</li>
     * <li>A specific renderer implementation (e.g., fireball renderer, aura renderer)
 * - 特定的渲染器实现（例如火球渲染器、光环渲染器）</li>
     * </ul>
     * </p>
     *
     * @see StrategyRenderer
     * @see #schedule()
     * @see #render(RenderLevelStageEvent, Minecraft, ClientLevel, PoseStack, Camera, MultiBufferSource)
     * @since 1.0.0
     */
    private interface Renderer {
        /**
         * Renders the strategy skill with the provided rendering context.
 * 使用提供的渲染上下文渲染策略技能。
         * <p>
         * This method is called during the world rendering pipeline to execute the actual
         * rendering of the skill's visual effects.
         * 此方法在世界渲染管线期间被调用，以执行技能视觉效果的实际渲染。
         * </p>
         *
         * @param mc The Minecraft client instance - Minecraft客户端实例
         * @param level The client level being rendered - 正在渲染的客户端级别
         * @param stack The pose stack for rendering transformations - 渲染变换的位姿栈
         * @param camera The camera for the current view - 当前视图的相机
         * @param buffer The multi-buffer source for vertex output - 顶点输出的多缓冲区源
         */
        void render(Minecraft mc, ClientLevel level, PoseStack stack, Camera camera, MultiBufferSource buffer);
    }
}
