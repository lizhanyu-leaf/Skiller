package com.leaf.skiller.client.renderer.block;

import com.leaf.skiller.Skiller;
import com.leaf.skiller.client.ClientSkillCache;
import com.leaf.skiller.client.renderer.type.AllRenderTypes;
import com.leaf.skiller.content.skill.SkillComponent;
import com.leaf.skiller.content.strategy.BlockStrategy;
import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.renderer.StrategyRenderer;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.SkillBundle;
import com.leaf.skiller.foundation.skill.StrategySkill;
import com.leaf.skiller.foundation.skill.config.impl.SkillColorContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.*;

/**
 * Renderer for drawing colored outlines around blocks using the strategy skill system.
 * 使用策略技能系统为方块绘制彩色轮廓的渲染器。
 *
 * <p>与 {@code EntityOutlineRenderer} 同构：不写 {@code getContext}（上下文由
 * {@code instance.skill().getFactory().create(...)} 在框架侧产生），
 * {@link #render} 只认 {@link BlockStrategy}，颜色从
 *
 * <h2>缓冲冲刷</h2>
 * <p>新接口给的是原版 {@link MultiBufferSource}，没有人替我们 {@code endBatch}，
 * 必须自己按 RenderType 冲刷（{@link #flush}）；否则症状是"算完了却什么都看不到"。</p>
 *
 * @see BlockStrategy
 * @see StrategyRenderer
 * @since 1.0.0
 */
public class BlockOutlineRenderer implements StrategyRenderer<SkillContext> {

    /** 穿透层的 alpha 系数 */
    private static final float TRANSPARENT_ALPHA_FACTOR = 0.3F;

    /**
     * Renders block outlines based on the strategy and configuration.
     * 根据策略和配置渲染方块轮廓。
     */
    @SuppressWarnings("unchecked")
    @Override
    public void render(SkillContext context, ISkillInstance<SkillContext> instance, Minecraft mc,
                       PoseStack poseStack, Camera camera, MultiBufferSource buffer) {
        ClientLevel level = mc.level;
        if (level == null || context == null) return;

        // 只处理方块策略
        var skill = instance.skill().getSkill();
        if (!(skill instanceof StrategySkill<?, ?> strategySkill)) return;
        if (!(strategySkill.strategy() instanceof BlockStrategy)) return;
        var strategy = (BlockStrategy<SkillContext>) strategySkill.strategy();

        if (!strategy.canCollect(context, instance)) return;

        Set<BlockPos> positions = new HashSet<>();
        strategy.collect(positions, context, instance);
        if (positions.isEmpty()) return;

        // 颜色：与 EntityOutlineRenderer 一样从 SkillColorContext 取
        // 颜色：与 EntityOutlineRenderer 一致——从 SkillColorContext 取；
        // 取不到就保持默认白色不透明（等价于"不设色"）。
        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;
        float a = 1.0F;
        if (context instanceof SkillColorContext colorContext) {
            var config = colorContext.color();
            r = config.red();
            g = config.green();
            b = config.blue();
            a = config.alpha();
        }

        // 必须做相机位移：世界坐标 → 相机相对坐标
        var camPos = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        draw(level, positions, poseStack, buffer, r, g, b, a);

        poseStack.popPose();

        flush(buffer);
    }

    private static Integer slotOf(ISkillInstance<?> instance) {
        SkillComponent component = ClientSkillCache.skills;
        if (component == null || instance == null) {
            return null;
        }
        for (Map.Entry<Integer, SkillBundle> binding : component.bindings().entrySet()) {
            SkillBundle bundle = binding.getValue();
            if (bundle == null) {
                continue;
            }
            for (ISkillInstance<?> candidate : bundle.getAllData()) {
                if (candidate == instance) {
                    return binding.getKey();
                }
            }
        }
        return null;
    }

    // =====================================================================
    //  绘制：两层描边 + 冲刷
    // =====================================================================

    /** 画两层：不透明层（受深度测试）+ 半透明穿透层。 */
    private static void draw(ClientLevel level, Set<BlockPos> positions, PoseStack poseStack,
                             MultiBufferSource buffer, float r, float g, float b, float a) {
        VertexConsumer solid = buffer.getBuffer(RenderType.LINES);
        renderOutline(level, positions, poseStack, solid, r, g, b, a);

        VertexConsumer transparent = buffer.getBuffer(AllRenderTypes.LINES_TRANSPARENT);
        renderOutline(level, positions, poseStack, transparent, r, g, b, a * TRANSPARENT_ALPHA_FACTOR);
    }

    /** 必须自己冲刷：新接口没人替我们 endBatch。 */
    private static void flush(MultiBufferSource buffer) {
        if (buffer instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(RenderType.LINES);
            bufferSource.endBatch(AllRenderTypes.LINES_TRANSPARENT);
        }
    }

    @Override
    public RenderLevelStageEvent.Stage getStage() {
        return RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS;
    }

    @Override
    public ResourceLocation getId() {
        return Skiller.modLoc("block_outline_renderer");
    }

    // =====================================================================
    //  几何引擎（原 OutlineRenderer，已内联）
    // =====================================================================

    /** 合并相邻方块为 AABB，用 VoxelShape OR 合并后 optimize 自动去除内部边。 */
    private static void renderOutline(Level level, Set<BlockPos> positions, PoseStack poseStack,
                                      VertexConsumer consumer, float r, float g, float b, float a) {
        if (positions.isEmpty()) return;

        List<AABB> boxes = mergeBlocks(positions, level);
        if (boxes.isEmpty()) return;

        VoxelShape combined = combineToShape(boxes);
        PoseStack.Pose pose = poseStack.last();
        Set<Edge> rendered = new HashSet<>();

        combined.forAllEdges((x1, y1, z1, x2, y2, z2) ->
                addEdge(pose, consumer, rendered, x1, y1, z1, x2, y2, z2, r, g, b, a));
    }

    /** 合并相邻方块为若干 AABB（空气过滤用原坐标）。 */
    private static List<AABB> mergeBlocks(Set<BlockPos> positions, Level level) {
        Set<BlockPos> remaining = new HashSet<>();
        for (BlockPos pos : positions) {
            if (!level.getBlockState(pos).isAir()) {
                remaining.add(pos.immutable());
            }
        }

        List<AABB> boxes = new ArrayList<>();
        while (!remaining.isEmpty()) {
            BlockPos start = remaining.iterator().next();
            remaining.remove(start);

            List<BlockPos> line = expandLine(start, remaining, Direction.Axis.Z);
            List<BlockPos> plane = expandPlane(line, remaining, Direction.Axis.X);
            List<BlockPos> volume = expandPlane(plane, remaining, Direction.Axis.Y);

            volume.forEach(remaining::remove);
            boxes.add(createAABB(volume));
        }
        return boxes;
    }

    private static List<BlockPos> expandLine(BlockPos start, Set<BlockPos> remaining, Direction.Axis axis) {
        List<BlockPos> line = new ArrayList<>();
        line.add(start);

        Direction posDir = getDirection(axis, true);
        Direction negDir = getDirection(axis, false);

        BlockPos current = start;
        while (true) {
            BlockPos next = current.relative(posDir);
            if (!remaining.contains(next)) break;
            line.add(next);
            current = next;
        }

        current = start;
        while (true) {
            BlockPos next = current.relative(negDir);
            if (!remaining.contains(next)) break;
            line.addFirst(next);
            current = next;
        }
        return line;
    }

    private static List<BlockPos> expandPlane(List<BlockPos> line, Set<BlockPos> remaining, Direction.Axis axis) {
        List<BlockPos> result = new ArrayList<>(line);
        Direction posDir = getDirection(axis, true);

        int layer = 1;
        while (true) {
            List<BlockPos> nextLayer = new ArrayList<>();
            for (BlockPos pos : line) {
                BlockPos next = pos.relative(posDir, layer);
                if (!remaining.contains(next)) return result;
                nextLayer.add(next);
            }
            result.addAll(nextLayer);
            layer++;
        }
    }

    private static AABB createAABB(List<BlockPos> positions) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : positions) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
    }

    private static VoxelShape combineToShape(List<AABB> boxes) {
        VoxelShape result = Shapes.empty();
        for (AABB box : boxes) {
            result = Shapes.joinUnoptimized(
                    result,
                    Shapes.create(box.inflate(0.005D)),  // 膨胀防 Z-fighting
                    BooleanOp.OR
            );
        }
        return result.optimize();  // 自动去除内部边
    }

    private record Edge(double x1, double y1, double z1, double x2, double y2, double z2) {
        Edge normalized() {
            if (x1 < x2) return this;
            if (x1 > x2) return new Edge(x2, y2, z2, x1, y1, z1);
            if (y1 < y2) return this;
            if (y1 > y2) return new Edge(x2, y2, z2, x1, y1, z1);
            if (z1 < z2) return this;
            if (z1 > z2) return new Edge(x2, y2, z2, x1, y1, z1);
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Edge that)) return false;
            Edge a = this.normalized();
            Edge b = that.normalized();
            return a.x1 == b.x1 && a.y1 == b.y1 && a.z1 == b.z1
                    && a.x2 == b.x2 && a.y2 == b.y2 && a.z2 == b.z2;
        }

        @Override
        public int hashCode() {
            Edge n = normalized();
            return Objects.hash(n.x1, n.y1, n.z1, n.x2, n.y2, n.z2);
        }
    }

    private static void addEdge(PoseStack.Pose pose, VertexConsumer consumer, Set<Edge> rendered,
                                double x1, double y1, double z1, double x2, double y2, double z2,
                                float r, float g, float b, float a) {
        Edge edge = new Edge(x1, y1, z1, x2, y2, z2);
        if (rendered.add(edge)) {
            renderEdge(pose, consumer, x1, y1, z1, x2, y2, z2, r, g, b, a);
        }
    }

    private static void renderEdge(PoseStack.Pose pose, VertexConsumer consumer,
                                   double x1, double y1, double z1, double x2, double y2, double z2,
                                   float r, float g, float b, float a) {
        float dx = (float) (x2 - x1);
        float dy = (float) (y2 - y1);
        float dz = (float) (z2 - z1);
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0) {
            dx /= len;
            dy /= len;
            dz /= len;
        }

        consumer.addVertex(pose, (float) x1, (float) y1, (float) z1)
                .setColor(r, g, b, a)
                .setNormal(pose, dx, dy, dz);

        consumer.addVertex(pose, (float) x2, (float) y2, (float) z2)
                .setColor(r, g, b, a)
                .setNormal(pose, dx, dy, dz);
    }

    private static Direction getDirection(Direction.Axis axis, boolean positive) {
        return switch (axis) {
            case X -> positive ? Direction.EAST : Direction.WEST;
            case Y -> positive ? Direction.UP : Direction.DOWN;
            case Z -> positive ? Direction.SOUTH : Direction.NORTH;
        };
    }
}