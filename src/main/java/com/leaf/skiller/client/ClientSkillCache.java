package com.leaf.skiller.client;

import com.leaf.skiller.AllKeys;
import com.leaf.skiller.client.renderer.StrategyRenderers;
import com.leaf.skiller.content.packet.KeyPressedPacket;
import com.leaf.skiller.content.packet.SkillTogglePacket;
import com.leaf.skiller.foundation.provider.SkillProviders;
import com.leaf.skiller.foundation.skill.SkillBundle;
import com.leaf.skiller.util.KeyCooldown;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Client-side cache for managing skill system state and key bindings.
 * 客户端缓存，用于管理系统技能状态和按键绑定。
 * <p>
 * This class maintains the client-side state of the skill system including:
 * 该类维护技能系统的客户端状态，包括：
 * <ul>
 * <li>Cached key indices for skill key bindings - 技能按键绑定的缓存键索引</li>
 * <li>Key press states for each skill key - 每个技能键的按键状态</li>
 * <li>Active skill bundle for the current player - 当前玩家的活动技能包</li>
 * <li>Overall enable/disable state of the skill system - 技能系统的总体启用/禁用状态</li>
 * </ul>
 *
 * @see SkillBundle
 * @see SkillProviders
 * @since 1.0.0
 */
public class ClientSkillCache {
    /**
     * Set of cached key indices for skill key bindings.
     技能按键绑定的缓存键索引集合。
     * <p>
     * Stores the indices of skill keys that are currently active and being monitored.
     存储当前活动且正在被监控的技能键索引。
     * </p>
     *
     * @see AllKeys#SKILL_KEYS
     * @since 1.0.0
     */
    private static Set<Integer> cacheKeys = new HashSet<>();

    /**
     * Map tracking the pressed state of each skill key.
     跟踪每个技能键按下状态的映射。
     * <p>
     * Key: Integer index of the skill key
 * 键：技能键的整数索引
     * <br>
     * Value: Boolean indicating whether the key is currently pressed (true) or released (false)
 * 值：布尔值，表示键当前是否被按下（true）或释放（false）
     * </p>
     *
     * @see AllKeys#SKILL_KEYS
     * @since 1.0.0
     */
    private static final Map<Integer, Boolean> pressed = new HashMap<>();

    /**
     * The active skill bundle containing all skills available to the current player.
 * 包含当前玩家可用所有技能的活动技能包。
     * <p>
     * This bundle is populated when the skill system is enabled and cleared when disabled.
 * 当技能系统启用时填充此包，禁用时清除。
     * </p>
     *
     * @see SkillBundle
     * @see #enable(Minecraft, Player)
     * @see #disable(Minecraft, Player)
     * @since 1.0.0
     */
    public static SkillBundle skills;

    /**
     * Flag indicating whether the skill system is currently enabled.
 * 指示技能系统当前是否启用的标志。
     * <p>
     * When true, the system monitors key inputs and renders skill effects.
 * 为true时，系统监控按键输入并渲染技能效果。
     * When false, all skill-related functionality is suspended.
 * 为false时，所有相关技能功能被暂停。
     * </p>
     *
     * @see #isEnable()
     * @see #enable(Minecraft, Player)
     * @see #disable(Minecraft, Player)
     * @since 1.0.0
     */
    private static boolean enable = false;

    /**
     * Checks whether the skill system is currently enabled.
 * 检查技能系统当前是否已启用。
     * <p>
     * Returns true if the skill system is active and monitoring key inputs.
 * 如果技能系统处于活动状态并正在监控按键输入，则返回true。
     * Returns false if the system is disabled and skill functionality is suspended.
 * 如果系统被禁用且技能功能被暂停，则返回false。
     * </p>
     *
     * @return true if the skill system is enabled, false otherwise - 如果技能系统已启用则返回true，否则返回false
     * @see #enable(Minecraft, Player)
     * @see #disable(Minecraft, Player)
     * @since 1.0.0
     */
    public static boolean isEnable() {
        return enable;
    }

    /**
     * Handles keyboard input for skill key bindings and system toggle keys.
 * 处理技能按键绑定和系统切换键的键盘输入。
     * <p>
     * This method is called each frame to process key input events. It performs two main functions:
 * 此方法每帧调用一次以处理按键输入事件。它执行两个主要功能：
     * <ul>
     * <li>Monitors all cached skill keys and sends state changes to the server when keys are pressed or released
 * - 监控所有缓存的技能键，当按键被按下或释放时将状态更改发送到服务器</li>
     * <li>Checks for enable/disable key presses to toggle the skill system on/off
 * - 检查启用/禁用按键以切换技能系统的开/关状态</li>
     * </ul>
     * <p>
     * State changes are synchronized with the server via network packets to ensure multiplayer consistency.
 * 状态更改通过网络数据包与服务器同步，以确保多人游戏的一致性。
     * </p>
     *
     * @see KeyPressedPacket
     * @see SkillTogglePacket
     * @see KeyCooldown#canToggle()
     * @see #enable(Minecraft, Player)
     * @see #disable(Minecraft, Player)
     * @since 1.0.0
     */
    public static void onKeyInput() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Monitor skill key state changes and send updates to server
        // 监控技能键状态变化并向服务器发送更新

        // TODO : 同样地，这里只应该发送技能需要的按键状态的更改
        // TODO : 这里需要使用 SkillProviders的 TODO 内容
        for (int idx : cacheKeys) {
            if (idx >= AllKeys.SKILL_KEYS.length) continue;
            boolean state = AllKeys.SKILL_KEYS[idx].isDown();
            if (pressed.get(idx) != state) {
                pressed.put(idx, state);
                PacketDistributor.sendToServer(new KeyPressedPacket(idx, state));
            }
        }

        // Handle enable/disable toggle keys
        // 处理启用/禁用切换键
        if (AllKeys.ENABLE_SKILL.isDown()) {
            if (!KeyCooldown.canToggle()) return;
            mc.player.displayClientMessage(Component.translatable("message.skiller.enabled"), true);
            ClientSkillCache.enable(mc, mc.player);
        }
        else if (AllKeys.DISABLE_SKILL.isDown()) {
            if (!KeyCooldown.canToggle()) return;
            mc.player.displayClientMessage(Component.translatable("message.skiller.disabled"), true);
            ClientSkillCache.disable(mc, mc.player);
        }
    }

    /**
     * Enables the skill system for the specified player.
 * 为指定玩家启用技能系统。
     * <p>
     * When called, this method performs the following initialization steps:
 * 调用时，此方法执行以下初始化步骤：
     * <ul>
     * <li>Sets the enable flag to true
 * - 将启用标志设置为true</li>
     * <li>Collects and caches all skill key bindings for the player
 * - 收集并缓存玩家的所有技能按键绑定</li>
     * <li>Collects and stores all skills available to the player in the skill bundle
 * - 收集并在技能包中存储玩家可用的所有技能</li>
     * <li>Schedules rendering for all strategy skills
 * - 安排所有策略技能的渲染</li>
     * <li>Sends a packet to the server to notify that the skill system has been enabled
 * - 向服务器发送数据包以通知技能系统已启用</li>
     * </ul>
     * <p>
     * This method is idempotent - calling it when already enabled has no effect.
 * 此方法是幂等的 - 在已启用时调用它无效。
     * </p>
     *
     * @param mc The Minecraft client instance - Minecraft客户端实例
     * @param player The player for whom to enable the skill system - 要为其启用技能系统的玩家
     * @see SkillProviders#collectAllKeys(Player)
     * @see SkillProviders#collectAllSkills(Player)
     * @see StrategyRenderers#schedule()
     * @see SkillTogglePacket
     * @see #disable(Minecraft, Player)
     * @since 1.0.0
     */
    public static void enable(Minecraft mc, Player player) {
        if (!enable) {
            enable = true;
            cacheKeys = SkillProviders.collectAllKeys(player);
            skills = SkillProviders.collectAllSkills(player);
            StrategyRenderers.schedule();

            PacketDistributor.sendToServer(new SkillTogglePacket(true));
        }
    }

    /**
     * Disables the skill system for the specified player.
 * 为指定玩家禁用技能系统。
     * <p>
     * When called, this method performs the following cleanup steps:
 * 调用时，此方法执行以下清理步骤：
     * <ul>
     * <li>Sets the enable flag to false
 * - 将启用标志设置为false</li>
     * <li>Clears all cached skill key bindings
 * - 清除所有缓存的技能按键绑定</li>
     * <li>Resets the skill bundle to empty, effectively removing all skills
 * - 将技能包重置为空，实际上移除了所有技能</li>
     * <li>Disables rendering for all strategy skills
 * - 禁用所有策略技能的渲染</li>
     * <li>Sends a packet to the server to notify that the skill system has been disabled
 * - 向服务器发送数据包以通知技能系统已禁用</li>
     * </ul>
     * <p>
     * This method is idempotent - calling it when already disabled has no effect.
 * 此方法是幂等的 - 在已禁用时调用它无效。
     * </p>
     *
     * @param mc The Minecraft client instance - Minecraft客户端实例
     * @param player The player for whom to disable the skill system - 要为其禁用技能系统的玩家
     * @see SkillProviders#collectAllKeys(Player)
     * @see SkillProviders#collectAllSkills(Player)
     * @see StrategyRenderers#disable()
     * @see SkillTogglePacket
     * @see #enable(Minecraft, Player)
     * @since 1.0.0
     */
    public static void disable(Minecraft mc, Player player) {
        if (enable) {
            enable = false;

            // Clear the cache / 清空缓存
            cacheKeys.clear();
            skills = SkillBundle.EMPTY;
            StrategyRenderers.disable();

            PacketDistributor.sendToServer(new SkillTogglePacket(false));
        }
    }
}
