package com.leaf.skiller.content.skill.chainmine;

import com.leaf.skiller.client.renderer.AllStrategyRenderers;
import com.leaf.skiller.content.strategy.BlockStrategy;
import com.leaf.skiller.foundation.context.AutoSkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

/**
 * Strategy for the Chain Mine skill: collects same-block chains around the target position.
 * 连锁采集技能的策略：收集目标位置周围的同类方块链。
 * <p>
 * The chain starts from the target position (the right-clicked block on the server,
 * or the crosshair block in the client preview) and expands through 6-directional
 * adjacency over blocks of the same {@link Block} instance, bounded by the resolved
 * {@link ChainMineSkill#KEY_RANGE range} sphere and {@link ChainMineSkill#MAX_CHAIN}
 * cap. Only blocks present in the {@link ChainMineSkill#KEY_WHITELIST whitelist} are
 * accepted as the starting block.
 * 连锁从目标位置开始（服务端为被右键的方块，客户端预览为准星指向的方块），
 * 沿 6 方向邻接扩展到与起点相同的 {@link Block} 实例，
 * 以解析后的 {@link ChainMineSkill#KEY_RANGE 范围} 球形和
 * {@link ChainMineSkill#MAX_CHAIN} 上限为界。
 * 只有 {@link ChainMineSkill#KEY_WHITELIST 白名单} 中存在的方块才能作为起点。
 * </p>
 * <p>
 * The same {@link #collect(Set, AutoSkillContext, ISkillInstance)} result feeds two
 * consumers: the server-side release (chain-breaking the blocks and spawning the
 * drops at the player's feet) and the client-side {@code BlockOutlineRenderer}
 * preview (outlining the blocks while the skill key is held), guaranteeing the
 * preview always matches the effect.
 * 同一个 {@link #collect(Set, AutoSkillContext, ISkillInstance)} 结果供给两个消费方：
 * 服务端释放（连锁破坏方块并把掉落物生成在玩家脚下）和客户端
 * {@code BlockOutlineRenderer} 预览（按住技能键时对方块描边），
 * 保证预览与实际效果一致。
 * </p>
 *
 * @see BlockStrategy
 * @see ChainMineSkill
 * @see AutoSkillContext
 * @see Block
 * @since 1.0.0
 * @author Leaf
 */
public class ChainMineStrategy implements BlockStrategy<AutoSkillContext> {

    /**
     * Checks whether chaining is possible in the given context.
     * 检查在给定上下文中是否可以进行连锁。
     * <p>
     * Requires the casting player to be alive and a resolved target position whose
     * block is whitelisted and actually breakable (a non-negative destroy speed, so
     * unbreakable blocks such as bedrock never start a chain). The client-side
     * preview calls this every frame, so no server-only assumptions are made.
     * 要求施法玩家存活，且解析出的目标位置上的方块在白名单中且确实可破坏
     * （破坏速度非负，因此基岩等不可破坏方块不会触发连锁）。
     * 客户端预览每帧都会调用此方法，因此不做任何仅限服务端的假设。
     * </p>
     *
     * @param context  The resolved Chain Mine context / 解析后的连锁采集上下文
     * @param instance The skill instance / 技能实例
     * @return true if the chain can start from the target / 目标可以作为连锁起点时返回 true
     * @see ChainMineSkill#targetOf(AutoSkillContext)
     * @see ChainMineSkill#isWhitelisted(AutoSkillContext, BlockState)
     * @since 1.0.0
     */
    @Override
    public boolean canCollect(AutoSkillContext context, ISkillInstance<AutoSkillContext> instance) {
        if (!context.player().isAlive()) return false;
        BlockPos target = ChainMineSkill.targetOf(context);
        if (target == null) return false;

        Level level = context.player().level();
        BlockState state = level.getBlockState(target);
        return state.getDestroySpeed(level, target) >= 0.0F
                && ChainMineSkill.isWhitelisted(context, state);
    }

    /**
     * Collects the chain of same-type blocks around the target position.
     * 收集目标位置周围的同类方块链。
     * <p>
     * Breadth-first search over 6-directional neighbours starting at the target,
     * keeping only blocks of the same {@link Block} instance, within the range
     * sphere centered on the target and at most {@link ChainMineSkill#MAX_CHAIN}
     * blocks in total.
     * 从目标出发沿 6 方向邻接做广度优先搜索，
     * 仅保留相同 {@link Block} 实例的方块，
     * 限制在以目标为中心的范围球形内，且总数不超过 {@link ChainMineSkill#MAX_CHAIN}。
     * </p>
     *
     * @param set      The set to collect matching positions into / 用于收集匹配位置的集合
     * @param context  The resolved context providing range and target / 提供范围与目标的解析后上下文
     * @param instance The skill instance / 技能实例
     * @see ChainMineSkill#KEY_RANGE
     * @see ChainMineSkill#MAX_CHAIN
     * @since 1.0.0
     */
    @Override
    public void collect(Set<BlockPos> set, AutoSkillContext context, ISkillInstance<AutoSkillContext> instance) {
        BlockPos target = ChainMineSkill.targetOf(context);
        if (target == null) return;

        Level level = context.player().level();
        Block targetBlock = level.getBlockState(target).getBlock();
        double range = context.getData().getDouble(ChainMineSkill.KEY_RANGE);
        double rangeSqr = range * range;

        set.add(target);
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(target);
        // 6-directional BFS, bounded by the range sphere and the chain cap.
        // 6 方向广度优先搜索，受范围球形与连锁上限约束。
        while (!queue.isEmpty() && set.size() < ChainMineSkill.MAX_CHAIN) {
            BlockPos current = queue.poll();
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (set.contains(next)) continue;
                if (next.distSqr(target) > rangeSqr) continue;
                BlockState state = level.getBlockState(next);
                if (state.getBlock() != targetBlock) continue;
                set.add(next);
                queue.add(next);
                if (set.size() >= ChainMineSkill.MAX_CHAIN) break;
            }
        }
    }

    /**
     * Returns the renderer id of the client-side block outline preview.
     * 返回客户端方块轮廓预览的渲染器ID。
     *
     * @return The registered renderer id / 已注册的渲染器ID
     * @see AllStrategyRenderers#BLOCK
     * @since 1.0.0
     */
    @Override
    public ResourceLocation getRendererId() {
        return AllStrategyRenderers.BLOCK.getId();
    }
}
