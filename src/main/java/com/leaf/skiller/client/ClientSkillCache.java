package com.leaf.skiller.client;

import com.leaf.skiller.AllKeys;
import com.leaf.skiller.client.renderer.StrategyRenderers;
import com.leaf.skiller.content.packet.KeyPressedPacket;
import com.leaf.skiller.content.packet.SyncSkillComponentPacket;
import com.leaf.skiller.content.skill.SkillComponent;
import com.leaf.skiller.foundation.provider.SkillProviders;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

/**
 * Client-side cache for managing skill system state and key bindings.
 * 客户端缓存，用于管理系统技能状态和按键绑定。
 * <p>
 * The skill system's enable/disable timing is decided by the SERVER: any held
 * skill key enables it, no held skill key disables it. This class therefore has
 * two always-on duties plus two server-driven transitions:
 * 技能系统的启用/禁用时机由服务端决定：任意技能键被按住即启用，
 * 没有任何技能键被按住即禁用。因此此类有两个常驻职责和两个服务端驱动的转换：
 * <ul>
 * <li>Always monitor every skill key and send state changes to the server —
 * the server derives the toggle timing from them.
 * 始终监控所有技能键并把状态变化发送到服务端——服务端据此判断开关时机。</li>
 * <li>Always track the pressed state for client-side preview gating.
 * 始终跟踪按键按下状态，供客户端预览渲染的门控使用。</li>
 * <li>On a sync request (enable): collect skills, schedule renderers, and
 * answer with {@link SyncSkillComponentPacket} — the client is the single
 * source of truth for skill data.
 * 收到同步请求（启用）时：收集技能、调度渲染，并以
 * {@link SyncSkillComponentPacket} 应答——客户端是技能数据的唯一事实来源。</li>
 * <li>On a sync request (disable): clear the local cache and rendering.
 * 收到同步请求（禁用）时：清理本地缓存和渲染。</li>
 * </ul>
 *
 * @see com.leaf.skiller.content.packet.SkillSyncRequestPacket
 * @see KeyPressedPacket
 * @see com.leaf.skiller.server.PlayerPressedKeys
 * @since 1.0.0
 */
public class ClientSkillCache {

    /**
     * Map tracking the pressed state of each skill key.
     * 跟踪每个技能键按下状态的映射。
     * <p>
     * Key: index into {@link AllKeys#SKILL_KEYS}; value: whether the key is
     * currently held. This tracks REAL input and is intentionally NOT cleared on
     * disable — clearing it would swallow the next release packet, leaving the
     * server believing a key is still held.
     * 键：{@link AllKeys#SKILL_KEYS} 的索引；值：按键当前是否被按住。
     * 这里跟踪的是真实输入，禁用时有意不清除——
     * 清除会吞掉下一次松键数据包，使服务端误以为按键仍被按住。
     * </p>
     *
     * @see AllKeys#SKILL_KEYS
     * @since 1.0.0
     */
    private static final Map<Integer, Boolean> pressed = new HashMap<>();

    /**
     * The active skill component containing all skills available to the current player.
     * 包含当前玩家可用所有技能的活动技能组件。
     * <p>
     * Populated when a server sync request enables the system, cleared on disable.
     * 收到服务端同步请求启用时填充，禁用时清空。
     * </p>
     *
     * @see SkillComponent
     * @see #handleSyncRequest(boolean)
     * @since 1.0.0
     */
    public static SkillComponent skills;

    /**
     * Flag mirroring the server-side skill system state.
     * 镜像服务端技能系统状态的标志。
     *
     * @see #isEnable()
     * @see #handleSyncRequest(boolean)
     * @since 1.0.0
     */
    private static boolean enable = false;

    /**
     * Checks whether the skill system is currently enabled.
     * 检查技能系统当前是否已启用。
     *
     * @return true if the skill system is enabled, false otherwise
     *         如果技能系统已启用则返回 true，否则返回 false
     * @see #handleSyncRequest(boolean)
     * @since 1.0.0
     */
    public static boolean isEnable() {
        return enable;
    }

    /**
     * Checks whether a skill key bound to the given skill instance is currently held.
     * 检查绑定到给定技能实例的技能键当前是否被按住。
     * <p>
     * Used by client-side preview renderers (e.g. the entity outline renderer) to
     * gate rendering on the skill key, so a preview only shows while the key
     * trigger is active and never for other triggers such as block right-click.
     * 供客户端预览渲染器（例如实体轮廓渲染器）使用，以技能键作为渲染门控，
     * 使预览只在按键触发期间显示，而其他触发方式（如方块右键）不会显示预览。
     * </p>
     *
     * @param instance The skill instance to look up the binding for
     *                 要查询绑定的技能实例
     * @return true if any key bound to this skill is currently pressed
     *         如果绑定此技能的任意按键当前被按住则返回 true
     * @see #skills
     * @see #pressed
     * @since 1.0.0
     */
    public static boolean isInstanceKeyPressed(ISkillInstance<?> instance) {
        if (skills == null) return false;
        for (var entry : skills.bindings().entrySet()) {
            boolean bound = entry.getValue().getAllData().stream()
                    .anyMatch(i -> i.skill().equals(instance.skill()));
            if (bound && pressed.getOrDefault(entry.getKey(), false)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Monitors every skill key and sends state changes to the server.
     * 监控所有技能键并把状态变化发送到服务端。
     * <p>
     * Runs unconditionally — even while the skill system is disabled — because
     * the server decides the enable/disable timing from exactly these state
     * changes: any held skill key enables, no held skill key disables.
     * 无条件运行——即使技能系统处于禁用状态——
     * 因为服务端正是依据这些状态变化来决定启用/禁用时机：
     * 任意技能键被按住即启用，没有任何技能键被按住即禁用。
     * </p>
     * <p>
     * Only state <em>changes</em> are sent, so both the press and the release
     * edge reach the server exactly once.
     * 只发送状态<em>变化</em>，因此按下沿和松开沿都恰好到达服务端一次。
     * </p>
     *
     * @see KeyPressedPacket
     * @see com.leaf.skiller.server.PlayerPressedKeys#setKeyPressed
     * @since 1.0.0
     */
    public static void onKeyInput() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        for (int idx = 0; idx < AllKeys.SKILL_KEYS.length; idx++) {
            boolean state = AllKeys.SKILL_KEYS[idx].isDown();
            // Keys have no entry until their first state change; defaulting to
            // "not pressed" avoids unboxing a null on the very first press.
            // 按键在首次状态变化前没有条目；默认视为未按下，
            // 避免第一次按下时对 null 拆箱。
            boolean previous = pressed.getOrDefault(idx, false);
            if (previous != state) {
                pressed.put(idx, state);
                PacketDistributor.sendToServer(new KeyPressedPacket(idx, state));
            }
        }
    }

    /**
     * Applies a server-driven enable/disable decision on the client.
     * 在客户端执行服务端驱动的启用/禁用决定。
     * <p>
     * On enable: collects the player's skills locally (the client is the single
     * source of truth), schedules strategy rendering, and answers the server
     * with {@link SyncSkillComponentPacket} so the server stores exactly what
     * the client collected. On disable: clears the local cache and rendering.
     * 启用时：在本地收集玩家技能（客户端是唯一事实来源）、
     * 调度策略渲染，并以 {@link SyncSkillComponentPacket} 应答服务端，
     * 使服务端存储的正是客户端收集的数据。禁用时：清理本地缓存和渲染。
     * </p>
     * <p>
     * Both transitions are idempotent. The {@code pressed} map is intentionally
     * left untouched — it tracks real input and must survive toggles, otherwise
     * the next release edge would be swallowed.
     * 两个转换都是幂等的。有意不动 pressed 映射——
     * 它跟踪真实输入且必须跨开关保留，否则下一个松开沿会被吞掉。
     * </p>
     *
     * @param enableRequest {@code true} to enable and sync the component,
     *                      {@code false} to disable locally
     *                      为 {@code true} 时启用并同步组件，
     *                      为 {@code false} 时本地禁用
     * @see com.leaf.skiller.content.packet.SkillSyncRequestPacket
     * @see #pressed
     * @since 1.0.0
     */
    public static void handleSyncRequest(boolean enableRequest) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (enableRequest) {
            if (enable) return;
            enable = true;

            skills = SkillProviders.collectAllSkills(mc.player);
            StrategyRenderers.schedule();

            // Answer the server with the merged component; the server only
            // holds a placeholder until this arrives.
            // 将合并后的组件应答给服务端；在此到达之前服务端只有占位数据。
            PacketDistributor.sendToServer(new SyncSkillComponentPacket(skills));
        } else {
            if (!enable) return;
            enable = false;

            // Do NOT clear `pressed`: it tracks real input across toggles.
            // 不要清除 pressed：它跨开关跟踪真实输入。
            skills = SkillComponent.EMPTY;
            StrategyRenderers.disable();
        }
    }
}
