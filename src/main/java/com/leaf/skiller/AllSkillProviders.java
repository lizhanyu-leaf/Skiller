package com.leaf.skiller;

import com.leaf.skiller.content.provider.HeldItemSkillProvider;
import com.leaf.skiller.foundation.provider.SkillProviders;

/**
 * Central registry for all skill providers used by the Skiller mod.
 * Skiller 模组使用的所有技能提供者的中央注册表。
 * <p>
 * This class manages the registration of skill providers, which are responsible
 * for determining what skills are available in different contexts. Providers
 * enable skills based on conditions like held items, equipment, or player state.
 * 此类管理技能提供者的注册，技能提供者负责确定在不同上下文中
 * 可用哪些技能。提供者根据手持物品、装备或玩家状态等条件启用技能。
 * </p>
 * <p>
 * Skill providers implement logic that checks game state and returns
 * appropriate skills that the player can use. Multiple providers can be
 * registered to support different skill acquisition methods.
 * 技能提供者实现检查游戏状态并返回玩家可使用的适当技能的逻辑。
 * 可以注册多个提供者以支持不同的技能获取方法。
 * </p>
 *
 * @author Skiller Development Team
 * @author Skiller 开发团队
 * @version 1.0.0
 * @see SkillProviders
 * @see HeldItemSkillProvider
 * @since 1.0.0
 */
public class AllSkillProviders {

    /**
     * Registers all skill providers with the skill system.
     * 向技能系统注册所有技能提供者。
     * <p>
     * This method is called during mod initialization to ensure all skill
     * providers are properly registered before gameplay begins. The actual
     * registration happens in the static initializer.
     * 此方法在模组初始化期间调用，确保所有技能提供者在游戏开始前
     * 正确注册。实际注册发生在静态初始化块中。
     * </p>
     * <p>
     * This method is intentionally empty as registration occurs during
     * class initialization. It exists for API consistency with other
     * registry classes.
     * 此方法有意留空，因为注册在类初始化期间发生。
     * 它的存在是为了与其他注册表类保持 API 一致性。
     * </p>
     *
     * @see SkillProviders
     * @since 1.0.0
     */
    public static void register() {}

    /**
     * Static initialization block that registers all built-in skill providers.
     * 注册所有内置技能提供者的静态初始化块。
     * <p>
     * This block runs once when the class is loaded, ensuring all skill
     * providers are registered before any gameplay occurs. Providers are
     * registered with the central SkillProviders registry.
     * 此块在类加载时运行一次，确保所有技能提供者在任何游戏发生之前
     * 注册。提供者向中央 SkillProviders 注册表注册。
     * </p>
     * <p>
     * Registered providers:
     * 注册的提供者：
     * <ul>
     * <li>HeldItemSkillProvider - Provides skills based on the player's currently held item
     * 根据玩家当前持有的物品提供技能</li>
     * </ul>
     * </p>
     *
     * @see SkillProviders
     * @see HeldItemSkillProvider
     * @since 1.0.0
     */
    static {
        SkillProviders.register(new HeldItemSkillProvider());
    }
}
