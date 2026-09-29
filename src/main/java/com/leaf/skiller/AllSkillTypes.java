package com.leaf.skiller;

import com.leaf.skiller.foundation.skill.SkillType;
import com.leaf.skiller.foundation.skill.SkillTypeFactory;

/**
 * Enumeration holder of all built-in skill types provided by the Skiller mod.
 * Skiller 模组提供的所有内置技能类型的持有类。
 * <p>
 * A skill type classifies skills by their trigger style. Server-side trigger
 * handlers (e.g. the right-click-block trigger) release all skills whose type
 * matches the triggering interaction, so new trigger styles only require a new
 * type constant plus a trigger handler.
 * 技能类型按触发方式对技能分类。服务端触发处理器（例如方块右键触发）
 * 会释放类型与触发交互匹配的所有技能，
 * 因此新增触发方式只需要新增一个类型常量和对应的触发处理器。
 * </p>
 *
 * @see SkillType
 * @see SkillTypeFactory
 * @see com.leaf.skiller.server.SkillTriggers
 * @since 1.0.0
 * @author Leaf
 */
public final class AllSkillTypes {

    /**
     * Skill type for skills triggered by right-clicking a block.
     * 通过右键点击方块触发的技能类型。
     * <p>
     * Skills registered under this type are released by
     * {@code SkillTriggers#onRightClickBlock} whenever the player right-clicks
     * a block while the skill system is enabled.
     * 当玩家在技能系统启用时右键点击方块，
     * 注册到此类型下的技能由 SkillTriggers#onRightClickBlock 释放。
     * </p>
     *
     * @see com.leaf.skiller.server.SkillTriggers
     * @since 1.0.0
     */
    public static final SkillType RIGHT_CLICK_BLOCK =
            SkillTypeFactory.of(Skiller.modLoc("right_click_block"));

    /**
     * Skill type for skills triggered by pressing a bound skill key.
     * 通过按下绑定的技能键触发的技能类型。
     * <p>
     * Skills registered under this type are released on the key-down edge by
     * {@code PlayerPressedKeys} (through the unified {@code SkillReleaser}) —
     * once per press, not continuously while held. Because enabling the skill
     * system and syncing the component are asynchronous, the very first press
     * that enables the system carries only the placeholder component and
     * releases nothing; subsequent presses fire normally.
     * 注册到此类型下的技能在按键按下沿由 PlayerPressedKeys
     * （通过统一的 SkillReleaser）释放——每次按下一次，而非按住期间持续触发。
     * 由于启用技能系统与同步组件是异步的，启用系统的第一次按下
     * 只携带占位组件、不会释放任何技能；后续按下正常触发。
     * </p>
     *
     * @see com.leaf.skiller.server.PlayerPressedKeys
     * @see com.leaf.skiller.util.SkillReleaser
     * @since 1.0.0
     */
    public static final SkillType KEY_PRESSED =
            SkillTypeFactory.of(Skiller.modLoc("key_pressed"));

    private AllSkillTypes() {
        throw new AssertionError("This class should not be instantiated");
    }
}
