package com.leaf.skiller.client.renderer.entity;

import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.renderer.StrategyRenderer;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.content.strategy.EntityStrategy;
import com.leaf.skiller.foundation.skill.StrategySkill;
import com.leaf.skiller.foundation.skill.config.SkillContextEnvironment;
import com.leaf.skiller.foundation.skill.config.impl.SkillColorContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Renderer for drawing colored outlines around entities using the strategy skill system.
 * 使用策略技能系统为实体绘制彩色轮廓的渲染器。
 * <p>
 * EntityOutlineRenderer provides a mechanism to visually highlight entities in the game world
 * with customizable colored outlines. This is particularly useful for skills that target,
 * track, or manipulate specific entities.
 * EntityOutlineRenderer 提供了一种机制，可以在游戏世界中用可定制的彩色轮廓来可视化高亮显示实体。
 * 这对于针对、跟踪或操控特定实体的技能特别有用。
 * </p>
 * <p>
 * The rendering process:
 * 渲染过程：
 * </p>
 * <ol>
 *     <li>Filter entities based on strategy criteria using shouldRender method
 *     使用 shouldRender 方法根据策略标准过滤实体</li>
 *     <li>Calculate affected entities using the EntityStrategy.calculate method
 *     使用 EntityStrategy.calculate 方法计算受影响的实体</li>
 *     <li>Set outline color based on skill configuration
 *     根据技能配置设置轮廓颜色</li>
 *     <li>Render entity models with outline effect using proper transformations
 *     使用适当的变换用轮廓效果渲染实体模型</li>
 *     <li>Handle entity pose, scale, and rotation correctly
 *     正确处理实体姿势、缩放和旋转</li>
 * </ol>
 * @see EntityStrategy
 * @see StrategyRenderer
 * @see LivingEntityRenderer
 * @since 1.0.0
 * @author Leaf
 */
public class EntityOutlineRenderer implements StrategyRenderer<SkillContext> {
    public static Set<Entity> glowingEntities = new HashSet<>();

    /**
     * Gets the rendering context for entity outline rendering.
     * 获取实体轮廓渲染的渲染上下文。
     * <p>
     * This method extracts the necessary context information for entity selection and rendering.
     * If the required context (such as entity information) is not available, it returns empty.
     * 此方法提取实体选择和渲染所需的上下文信息。如果所需的上下文（如实体信息）不可用，则返回空。
     * </p>
     *
     * @param mc The Minecraft client instance
     *           Minecraft客户端实例
     * @param level The client level being rendered
     *              正在渲染的客户端层级
     * @param player The player entity for context
     *               用于上下文的玩家实体
     * @param instance The skill instance containing configuration and level
     *                 包含配置和等级的技能实例
     * @return Optional containing the rendering context, or empty if context cannot be created
     *         包含渲染上下文的Optional，如果无法创建上下文则返回空
     * @since 1.0.0
     */
    @Override
    public Optional<SkillContext> getContext(Minecraft mc, net.minecraft.client.multiplayer.ClientLevel level,
                                  net.minecraft.world.entity.player.Player player,
                                  ISkillInstance<SkillContext> instance) {
        var context = instance.skill().getFactory()
                .create(SkillContextEnvironment.noEvent(player, level), instance);
        return Optional.ofNullable(context);
    }

    /**
     * Renders entity outlines based on the strategy and configuration.
     * 根据策略和配置渲染实体轮廓。
     * <p>
     * This is the main rendering method that:
     * 这是主要渲染方法，它：
     * </p>
     * <ul>
     *     <li>Determines which entities should be rendered using the strategy
     *     使用策略确定应该渲染哪些实体</li>
     *     <li>Calculates the set of affected entities
     *     计算受影响的实体集合</li>
     *     <li>Sets up outline rendering with the configured color
     *     使用配置的颜色设置轮廓渲染</li>
     *     <li>Renders each entity with proper transformations and model rendering
     *     使用适当的变换和模型渲染渲染每个实体</li>
     * </ul>
     *
     * @param context The skill context providing environment and entity information
     *                提供环境和实体信息的技能上下文
     * @param instance The skill instance containing configuration and level information
     *                 包含配置和等级信息的技能实例
     * @param mc The Minecraft client instance
     *           Minecraft客户端实例
     * @param poseStack The pose stack for rendering transformations
     *                  渲染变换的位姿栈
     * @param camera The camera for the current view
     *              当前视图的相机
     * @param buffer The multi-buffer source for vertex output
     *              顶点输出的多缓冲区源
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    @Override
    public void render(SkillContext context, ISkillInstance<SkillContext> instance, Minecraft mc,
                      PoseStack poseStack, Camera camera, MultiBufferSource buffer) {
        var level = mc.level;
        if (level == null) return;

        // Check if you should render and calculate entities
        // 检查是否应该渲染并计算实体
        var skill = instance.skill().getSkill();
        if (!(skill instanceof StrategySkill<?,?> strategySkill)) return;
        var strategy = (EntityStrategy<SkillContext>) strategySkill.strategy();

        if (!strategy.canCollect(context, instance)) return;

        Set<Entity> entities = new HashSet<>();
        strategy.collect(entities, context, instance);

        // Set up outline buffer source
        // 设置轮廓缓冲区源
        OutlineBufferSource outlineBuffer = mc.renderBuffers().outlineBufferSource();
        if (context instanceof SkillColorContext colorContext) {
            var config = colorContext.getColor();
            outlineBuffer.setColor(
                    (int) (config.red() * 255),
                    (int) (config.green() * 255),
                    (int) (config.blue() * 255),
                    (int) (config.alpha() * 255)
            );
        }

        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(true);

        for (Entity entity : entities) {
            // Get player for visibility checks
            // 获取玩家进行可见性检查
            var player = mc.player;

            poseStack.pushPose();

            // Position transformation
            // 位置变换
            poseStack.translate(
                    entity.getX(),
                    entity.getY(),
                    entity.getZ()
            );

            // Mark entity as glowing
            // 标记实体为发光
            glowingEntities.add(entity);

            renderEntity(entity, partialTick, poseStack, outlineBuffer, camera);

            poseStack.popPose();
        }

        // End outline batch
        // 结束轮廓批次
        outlineBuffer.endOutlineBatch();
    }

    public void renderEntity(Entity entity, float partialTick, PoseStack poseStack,
                             MultiBufferSource buffer, Camera camera) {
        double d0 = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double d1 = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double d2 = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        float f = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());

        Minecraft.getInstance().getEntityRenderDispatcher().render(
                entity,
                d0 - camera.getPosition().x(),
                d1 - camera.getPosition().y(),
                d2 - camera.getPosition().z(),
                f,
                partialTick,
                poseStack,
                buffer,
                Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(entity, partialTick)
        );
    }

    /**
     * Returns the render stage for entity outline rendering.
     * 返回实体轮廓渲染的渲染阶段。
     * <p>
     * Entity outlines are typically rendered after translucent blocks to ensure
     * they are visible on top of other world elements.
     * 实体轮廓通常在半透明块之后渲染，以确保它们在其他世界元素之上可见。
     * </p>
     *
     * @return AFTER_TRANSLUCENT_BLOCKS render stage
     *         AFTER_TRANSLUCENT_BLOCKS 渲染阶段
     * @since 1.0.0
     */
    @Override
    public RenderLevelStageEvent.Stage getStage() {
        return RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS;
    }
}