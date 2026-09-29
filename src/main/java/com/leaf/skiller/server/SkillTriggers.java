package com.leaf.skiller.server;

import com.leaf.skiller.AllSkillTypes;
import com.leaf.skiller.content.skill.SkillComponent;
import com.leaf.skiller.foundation.skill.SkillBundle;
import com.leaf.skiller.foundation.skill.config.SkillContextEnvironment;
import com.leaf.skiller.util.SkillReleaser;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Server-side trigger handlers that release skills from gameplay interactions.
 * 从游戏交互中释放技能的服务端触发处理器。
 * <p>
 * Each handler filters skills by their {@link com.leaf.skiller.foundation.skill.SkillType}
 * (the trigger classification) and releases them with an environment carrying
 * the triggering event, so context factories can read interaction details (such
 * as the clicked position) without the skills touching events themselves.
 * 每个处理器按技能的 {@link com.leaf.skiller.foundation.skill.SkillType}（触发分类）
 * 过滤技能，并携带触发事件构建环境后释放它们，
 * 使上下文工厂可以读取交互细节（如点击位置），
 * 而技能自身无需接触事件。
 * </p>
 * <p>
 * This handler only runs on the server side; key-press triggers are handled in
 * {@link com.leaf.skiller.content.packet.KeyPressedPacket} instead.
 * 此处理器仅在服务端运行；按键触发则由
 * {@link com.leaf.skiller.content.packet.KeyPressedPacket} 处理。
 * </p>
 *
 * @see AllSkillTypes#RIGHT_CLICK_BLOCK
 * @see ServerSkillCache
 * @see SkillBundle#releaseSkills(com.leaf.skiller.foundation.skill.SkillType, SkillContextEnvironment)
 * @since 1.0.0
 * @author Leaf
 */
@EventBusSubscriber
public class SkillTriggers {

    /**
     * Releases all right-click-block skills when the player right-clicks a block.
     * 当玩家右键点击方块时释放所有方块右键类型的技能。
     * <p>
     * Runs server-side only, on the main hand pass only (the offhand pass would
     * otherwise release the same skills twice per click), and only for players
     * whose skill system is enabled. The click is not cancelled — sweeping dust
     * does not conflict with normal block interaction.
     * 仅在服务端、仅处理主手（否则副手判定会使同一次点击重复释放技能），
     * 且仅针对技能系统已启用的玩家。不取消点击——
     * 扫尘与正常的方块交互并不冲突。
     * </p>
     *
     * @param event The right-click-block event / 方块右键事件
     * @see PlayerInteractEvent.RightClickBlock
     * @see AllSkillTypes#RIGHT_CLICK_BLOCK
     * @see ServerSkillCache#isEnableSkill(ServerPlayer)
     * @see ServerSkillCache#getComponent(ServerPlayer)
     * @since 1.0.0
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!ServerSkillCache.isEnableSkill(player)) return;

        SkillContextEnvironment env = SkillContextEnvironment.withEvent(player, player.level(), event);

        SkillReleaser releaser = new SkillReleaser(player);
        releaser.release(AllSkillTypes.RIGHT_CLICK_BLOCK, env);
    }
}
