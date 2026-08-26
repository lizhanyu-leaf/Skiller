package com.leaf.skiller;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * Central registry for all key bindings used by the Skiller mod.
 * Skiller 模组使用的所有按键绑定的中央注册表。
 * <p>
 * This class defines and manages all keyboard shortcuts and key bindings
 * that players can use to interact with the skill system. Key bindings
 * are client-side only and can be customized in the game's controls menu.
 * 此类定义和管理玩家可用于与技能系统交互的所有键盘快捷键和按键绑定。
 * 按键绑定仅在客户端，可以在游戏的控制菜单中自定义。
 * </p>
 * <p>
 * All key mappings are automatically registered with Minecraft's key binding
 * system and appear in the controls menu under the "Skiller" category.
 * 所有按键映射自动注册到 Minecraft 的按键绑定系统，并显示在
 * 控制菜单的 "Skiller" 类别下。
 * </p>
 *
 * @author Skiller Development Team
 * @author Skiller 开发团队
 * @version 1.0.0
 * @see KeyMapping
 * @see RegisterKeyMappingsEvent
 * @since 1.0.0
 */
@EventBusSubscriber
public class AllKeys {
    /**
     * Array of skill activation keys that trigger different skills.
     * 触发不同技能的技能激活键数组。
     * <p>
     * These keys correspond to skill slots and allow players to quickly
     * activate skills attached to their held item. Each key can be bound
     * to a different skill slot by default.
     * 这些键对应技能槽位，允许玩家快速激活附加到持有物品的技能。
     * 默认情况下，每个键可以绑定到不同的技能槽位。
     * </p>
     * <p>
     * Default bindings:
     * 默认绑定：
     * <ul>
     * <li>Skill 0: Left Control (LCtrl) - 技能 0：左 Control</li>
     * <li>Skill 1: Left Shift (LShift) - 技能 1：左 Shift</li>
     * <li>Skill 2: Left Alt (LAlt) - 技能 2：左 Alt</li>
     * </ul>
     * </p>
     */
    public static final KeyMapping[] SKILL_KEYS = {
            new KeyMapping("key.skiller.skill_0", InputConstants.KEY_LCONTROL, "key.categories.skiller"),
            new KeyMapping("key.skiller.skill_1", InputConstants.KEY_LSHIFT, "key.categories.skiller"),
            new KeyMapping("key.skiller.skill_2", InputConstants.KEY_LALT, "key.categories.skiller"),
    };

    /**
     * Key binding for enabling or activating skills.
     * 用于启用或激活技能的按键绑定。
     * <p>
     * This key is used to toggle skill activation or enable specific skills
     * depending on the context. Players can press this key to activate
     * the skill system or enable individual skills.
     * 此键用于根据上下文切换技能激活或启用特定技能。
     * 玩家可以按此键激活技能系统或启用单个技能。
     * </p>
     * <p>
     * Default binding: R key (can be customized in controls menu)
     * 默认绑定：R 键（可在控制菜单中自定义）
     * </p>
     */
    public static final KeyMapping ENABLE_SKILL = new KeyMapping(
            "key.skiller.enable_skill",
            InputConstants.KEY_R,
            "key.categories.skiller");

    /**
     * Key binding for disabling or deactivating skills.
     * 用于禁用或停用技能的按键绑定。
     * <p>
     * This key is used to deactivate active skills or disable the skill
     * system temporarily. Pressing this key can cancel skill activations
     * or turn off specific skills.
     * 此键用于停用活动技能或临时禁用技能系统。
     * 按此键可以取消技能激活或关闭特定技能。
     * </p>
     * <p>
     * Default binding: Y key (can be customized in controls menu)
     * 默认绑定：Y 键（可在控制菜单中自定义）
     * </p>
     */
    public static final KeyMapping DISABLE_SKILL = new KeyMapping(
            "key.skiller.disable_skill",
            InputConstants.KEY_Y,
            "key.categories.skiller");

    /**
     * Event handler that registers all key mappings with Minecraft's key binding system.
     * 事件处理器，向 Minecraft 的按键绑定系统注册所有按键映射。
     * <p>
     * This method is called automatically during the client initialization phase.
     * It iterates through all defined key mappings and registers them so they
     * appear in the game's controls menu and respond to keyboard input.
     * 此方法在客户端初始化阶段自动调用。它遍历所有定义的按键映射
     * 并注册它们，使其出现在游戏的控制菜单中并响应键盘输入。
     * </p>
     * <p>
     * After registration, players can customize these key bindings through
     * the standard Minecraft controls menu.
     * 注册后，玩家可以通过标准 Minecraft 控制菜单自定义这些按键绑定。
     * </p>
     *
     * @param event The key mapping registration event
     *              按键映射注册事件
     * @see RegisterKeyMappingsEvent
     * @see KeyMapping
     * @since 1.0.0
     */
    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        for (KeyMapping key : SKILL_KEYS) {
            event.register(key);
        }
        event.register(ENABLE_SKILL);
        event.register(DISABLE_SKILL);
    }
}
