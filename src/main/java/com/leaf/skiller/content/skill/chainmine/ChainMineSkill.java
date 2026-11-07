package com.leaf.skiller.content.skill.chainmine;

import com.leaf.skiller.foundation.Consumable;
import com.leaf.skiller.foundation.context.AutoSkillContext;
import com.leaf.skiller.foundation.context.AutoSkillContextFactory;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.StrategySkill;
import com.leaf.skiller.foundation.strategy.SkillStrategy;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * The Chain Mine skill: chain-breaks same-type blocks around the clicked block and
 * gathers the drops at the player's feet.
 * 连锁采集技能：连锁破坏被点击方块周围的同类方块，并把掉落物集中到玩家脚下。
 * <p>
 * Uses the generic {@link AutoSkillContext}: all config values (range, whitelist,
 * outline color) are resolved by its factory from the instance data (with
 * level-based fallbacks), so this class only reads resolved values from
 * {@link AutoSkillContext#getData()} — it never touches the raw instance data
 * or knows how defaults work.
 * 使用通用的 {@link AutoSkillContext}：所有配置值（范围、白名单、轮廓颜色）
 * 由其工厂从实例数据解析（缺失回退等级默认），
 * 因此此类只从 {@link AutoSkillContext#getData()} 读取解析后的值——
 * 从不接触原始实例数据，也不关心默认值的规则。
 * </p>
 * <p>
 * Triggered by right-clicking a whitelisted block — released by the server-side
 * trigger for {@link com.leaf.skiller.AllSkillTypes#RIGHT_CLICK_BLOCK} skills
 * ({@link com.leaf.skiller.server.SkillTriggers}). Holding the bound skill key
 * previews the affected blocks via {@link com.leaf.skiller.content.skill.chainmine.ChainMineStrategy}
 * and the block outline renderer, with the crosshair block standing in as the
 * preview target.
 * 通过右键白名单中的方块触发——由服务端针对
 * {@link com.leaf.skiller.AllSkillTypes#RIGHT_CLICK_BLOCK} 类型技能的触发器
 * （{@link com.leaf.skiller.server.SkillTriggers}）释放。
 * 按住绑定的技能键时，通过 {@link com.leaf.skiller.content.skill.chainmine.ChainMineStrategy}
 * 与方块轮廓渲染器预览受影响的方块，此时准星指向的方块充当预览目标。
 * </p>
 *
 * @see StrategySkill
 * @see ChainMineStrategy
 * @see AutoSkillContext
 * @see com.leaf.skiller.foundation.context.AutoSkillContextFactory
 * @see com.leaf.skiller.AllSkills
 * @since 1.0.0
 * @author Leaf
 */
public class ChainMineSkill implements StrategySkill<BlockPos, AutoSkillContext> {

    /**
     * Config key of the chain range (blocks), resolved by the context factory.
     * 连锁范围（格）的配置键，由上下文工厂解析。
     */
    public static final String KEY_RANGE = "range";

    /**
     * Config key of the whitelist block tag id (e.g. {@value #DEFAULT_WHITELIST}).
     * 白名单方块标签ID的配置键（例如 {@value #DEFAULT_WHITELIST}）。
     */
    public static final String KEY_WHITELIST = "whitelist";

    /**
     * Default whitelist block tag, used when the instance data has none.
     * Backed by {@code data/skiller/tags/block/chain_mine_whitelist.json}.
     * 实例数据没有白名单时使用的默认方块标签，
     * 内容来自 {@code data/skiller/tags/block/chain_mine_whitelist.json}。
     */
    public static final String DEFAULT_WHITELIST = "skiller:chain_mine_whitelist";

    /**
     * Transient key of the chain target position ({@link BlockPos#asLong()}), stashed
     * into the resolved data by {@link #TARGET_CUSTOMIZER} — never stored on the instance.
     * 连锁目标位置（{@link BlockPos#asLong()}）的瞬态配置键，由 {@link #TARGET_CUSTOMIZER}
     * 写入解析后的数据——从不持久化到实例上。
     */
    public static final String KEY_TARGET = "target";

    /**
     * Hard cap on chain size, independent of the configured range, so an oversized
     * range can never freeze the server with one click.
     * 连锁数量的硬上限，与配置的范围无关，
     * 防止过大的范围用一次点击卡死服务端。
     */
    public static final int MAX_CHAIN = 64;

    /**
     * Reach of the crosshair pick used for the client-side preview target.
     * 客户端预览目标所用的准星拾取距离。
     */
    private static final double PREVIEW_REACH = 4.5D;

    /**
     * Stashes the chain target into the resolved context data.
     * 将连锁目标写入解析后的上下文数据。
     * <p>
     * On the server release path the target is the right-clicked block carried by the
     * trigger event. On the client preview path there is no trigger event, so the
     * block under the crosshair stands in as the preview target. The value is written
     * into the per-release resolved {@link AutoSkillContext#getData() data}, so it is
     * never persisted back to the instance.
     * 服务端释放路径的目标为触发事件携带的被右键方块；
     * 客户端预览路径没有触发事件，以准星下的方块作为预览目标。
     * 该值写入每次释放的解析后 {@link AutoSkillContext#getData() 数据}，
     * 因此不会持久化回实例。
     * </p>
     *
     * @see ChainMineSkill#KEY_TARGET
     * @see ChainMineStrategy#collect(Set, AutoSkillContext, ISkillInstance)
     * @since 1.0.0
     */
    public static final AutoSkillContextFactory.ContextCustomizer TARGET_CUSTOMIZER = (env, instance, context) -> {
        BlockPos target = null;
        Object event = env.getEvent();
        if (event instanceof PlayerInteractEvent.RightClickBlock click) {
            // Server release: the clicked block is the chain origin.
            // 服务端释放：被点击的方块即连锁起点。
            target = click.getPos();
        } else if (event == null) {
            // Client preview: fall back to the block under the crosshair.
            // 客户端预览：回退为准星指向的方块。
            HitResult hit = env.getPlayer().pick(PREVIEW_REACH, 1.0F, false);
            if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
                target = blockHit.getBlockPos();
            }
        }
        if (target != null) {
            context.getData().putLong(KEY_TARGET, target.asLong());
        }
    };

    /**
     * The registry key of the strategy used by this skill.
     * 此技能所用策略的注册表键。
     */
    private final ResourceKey<SkillStrategy<?, ?>> strategyKey;

    /**
     * Creates the Chain Mine skill bound to the given strategy key.
     * 创建绑定到给定策略键的连锁采集技能。
     *
     * @param strategyKey The registry key of the {@link ChainMineStrategy}
     *                    {@link ChainMineStrategy} 的注册表键
     * @see com.leaf.skiller.AllSkills
     * @since 1.0.0
     */
    public ChainMineSkill(ResourceKey<SkillStrategy<?, ?>> strategyKey) {
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
     * Releases the skill: chain-breaks the collected blocks and spawns the drops at
     * the player's feet.
     * 释放技能：连锁破坏收集到的方块，并把掉落物生成在玩家脚下。
     * <p>
     * Drops are computed per block through the block's loot table (so tool
     * enchantments apply), but are popped at the player's feet instead of the block
     * position; the block itself is removed without its own drop pass.
     * 掉落按方块通过其战利品表计算（工具附魔生效），
     * 但生成位置改为玩家脚下而非方块原位；
     * 方块移除时不再走自身的掉落流程。
     * </p>
     * <p>
     * TODO: experience drops (vanilla mining grants both).
     * TODO: 经验掉落（原版挖掘两者都会产生）。
     * </p>
     *
     * @param context  The resolved Chain Mine context / 解析后的连锁采集上下文
     * @param instance The skill instance being released / 正在释放的技能实例
     * @see ChainMineStrategy#collect(Set, AutoSkillContext, ISkillInstance)
     * @see Block#getDrops(BlockState, ServerLevel, BlockPos,
     *      net.minecraft.world.level.block.entity.BlockEntity,
     *      net.minecraft.world.entity.Entity, ItemStack)
     * @see Block#popResource(Level, BlockPos, ItemStack)
     * @since 1.0.0
     */
    @Override
    public void release(AutoSkillContext context, ISkillInstance<AutoSkillContext> instance) {
        ServerLevel level = (ServerLevel) context.player().level();

        SkillStrategy<BlockPos, AutoSkillContext> mine = strategy();
        Set<BlockPos> targets = new HashSet<>();
        if (!mine.canCollect(context, instance)) return;
        mine.collect(targets, context, instance);

        Player player = context.player();
        BlockPos feet = player.blockPosition();
        ItemStack tool = player.getMainHandItem();

        for (BlockPos pos : targets) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;
            // Compute the drops with the block's loot table (tool enchants apply),
            // then pop them at the player's feet instead of the block position.
            // 通过方块的战利品表计算掉落（工具附魔生效），
            // 然后把掉落物生成在玩家脚下而非方块原位。
            for (ItemStack drop : Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, tool)) {
                if (!drop.isEmpty()) Block.popResource(level, feet, drop);
            }
            // Remove the block without its own drop pass (dropBlock=false).
            // 移除方块且不再走自身的掉落流程（dropBlock=false）。
            level.destroyBlock(pos, false, player);
        }
    }

    /**
     * Consumes the skill's resource cost. Chain Mine currently has no cost
     * (it is registered with the EMPTY resource), so this is a no-op.
     * 消耗技能的资源成本。连锁采集当前没有成本（以 EMPTY 资源注册），
     * 因此此方法为空操作。
     *
     * @param context    The resolved Chain Mine context / 解析后的连锁采集上下文
     * @param consumable The consumable accumulating the cost / 累积成本的可消耗项
     * @param instance   The skill instance / 技能实例
     * @since 1.0.0
     */
    @Override
    public void consumeResource(AutoSkillContext context, Consumable consumable, ISkillInstance<AutoSkillContext> instance) {
        SkillStrategy<BlockPos, AutoSkillContext> mine = strategy();
        Set<BlockPos> targets = new HashSet<>();
        if (!mine.canCollect(context, instance)) return;
        mine.collect(targets, context, instance);
        consumable.consume(targets.size());
    }

    /**
     * Checks whether the given block may start (and is part of) a chain.
     * 检查给定方块是否可以作为连锁起点（即是否在白名单标签中）。
     * <p>
     * The whitelist is a block tag id resolved from the context data and matched
     * with {@link BlockState#is(TagKey)}; block tags are synced to the client, so the
     * preview always matches the server. Unparsable or unknown tag ids simply
     * never match anything.
     * 白名单是从上下文数据解析的方块标签ID，经 {@link BlockState#is(TagKey)} 匹配；
     * 方块标签会同步到客户端，因此预览与服务端一致。
     * 无法解析或未知的标签ID不会匹配任何方块。
     * </p>
     *
     * @param context The resolved Chain Mine context / 解析后的连锁采集上下文
     * @param block   The block to check / 要检查的方块
     * @return true if the block is in the whitelist tag / 方块在白名单标签中时返回 true
     * @see ChainMineSkill#DEFAULT_WHITELIST
     * @since 1.0.0
     */
    public static boolean isWhitelisted(AutoSkillContext context, BlockState block) {
        ResourceLocation tagId = ResourceLocation.tryParse(context.getData().getString(KEY_WHITELIST));
        return tagId != null && block.is(BlockTags.create(tagId));
    }

    /**
     * Returns the chain target position resolved by {@link #TARGET_CUSTOMIZER},
     * or null when this context carries no target (e.g. a preview with nothing
     * under the crosshair).
     * 返回由 {@link #TARGET_CUSTOMIZER} 解析出的连锁目标位置；
     * 当上下文没有目标时返回 null（例如准星下没有方块的预览）。
     *
     * @param context The resolved Chain Mine context / 解析后的连锁采集上下文
     * @return The target position, or null if absent / 目标位置，不存在时返回 null
     * @see ChainMineSkill#KEY_TARGET
     * @since 1.0.0
     */
    public static BlockPos targetOf(AutoSkillContext context) {
        CompoundTag data = context.getData();
        if (!data.contains(KEY_TARGET, Tag.TAG_LONG)) return null;
        return BlockPos.of(data.getLong(KEY_TARGET));
    }
}
