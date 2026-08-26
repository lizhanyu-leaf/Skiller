package com.leaf.skiller.server;

import com.leaf.skiller.foundation.provider.SkillProviders;
import com.leaf.skiller.foundation.skill.SkillBundle;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side skill cache management class for storing and managing player skill bundles.
 * 服务端技能缓存管理类，用于存储和管理玩家的技能包。
 * <p>
 * This class maintains a mapping of player UUIDs to their corresponding skill bundles,
 * providing centralized access to player skill data on the server side.
 * 该类维护玩家UUID到其对应技能包的映射，在服务端提供对玩家技能数据的集中访问。
 * <p>
 * The cache is populated when skills are enabled for a player and cleared when disabled,
 * ensuring that skill data is only stored for players who actively have skills enabled.
 * 缓存在为玩家启用技能时填充，在禁用时清除，确保仅为主动启用技能的玩家存储技能数据。
 *
 * @see SkillBundle
 * @see SkillProviders
 * @see com.leaf.skiller.client.ClientSkillCache
 * @since 1.0.0
 */
public class ServerSkillCache {
    /**
     * Thread-safe map storing player UUIDs mapped to their skill bundles.
     * 线程安全的映射表，存储玩家UUID到其技能包的映射。
     * <p>
     * This map acts as the central cache for all active player skill data on the server.
     * Each entry represents a player who currently has skills enabled.
     * 该映射表作为服务端所有活跃玩家技能数据的中央缓存。
     * 每个条目代表一个当前已启用技能的玩家。
     * <p>
     * The map is modified when players toggle their skill system on or off,
     * and is accessed frequently during gameplay to retrieve skill data.
     * 该映射表在玩家开启或关闭技能系统时被修改，并在游戏过程中频繁访问以获取技能数据。
     */
    private static final Map<UUID, SkillBundle> SKILL_BUNDLES = new HashMap<>();

    /**
     * Handles skill system toggle events for players, updating the cache accordingly.
     * 处理玩家的技能系统切换事件，相应地更新缓存。
     * <p>
     * When a player enables their skill system, this method collects all available skills
     * for that player and stores them in the cache. When disabled, the player's skill data
     * is removed from the cache to free resources.
     * 当玩家启用其技能系统时，此方法收集该玩家的所有可用技能并将它们存储在缓存中。
     * 当禁用时，玩家的技能数据将从缓存中移出以释放资源。
     * <p>
     * This method should be called whenever a player toggles their skill system on or off,
     * typically through a UI interaction or command.
     * 每当玩家开启或关闭其技能系统时都应调用此方法，通常通过UI交互或命令触发。
     *
     * @param player The server player entity whose skill system is being toggled.
     *               正在切换技能系统的服务端玩家实体。
     * @param enable {@code true} to enable skills and cache the player's skill bundle,
     *               {@code false} to disable skills and remove the player from cache.
     *               为 {@code true} 时启用技能并缓存玩家的技能包，
     *               为 {@code false} 时禁用技能并将玩家从缓存中移除。
     * @see SkillProviders#collectAllSkills(ServerPlayer)
     * @see #isEnableSkill(ServerPlayer)
     * @see #getBundle(ServerPlayer)
     * @since 1.0.0
     */
    public static void onToggle(ServerPlayer player, boolean enable) {
        if (enable) {
            SKILL_BUNDLES.put(player.getUUID(), SkillProviders.collectAllSkills(player));
        }
        else {
            SKILL_BUNDLES.remove(player.getUUID());
        }
    }

    /**
     * Checks whether a player currently has their skill system enabled.
     * 检查玩家当前是否启用了技能系统。
     * <p>
     * This method provides a quick way to determine if a player is participating
     * in the skill system without needing to access their actual skill data.
     * It simply checks for the presence of the player's UUID in the cache.
     * 此方法提供了一种快速方式来确定玩家是否参与技能系统，而无需访问其实际技能数据。
     * 它仅检查玩家UUID是否存在于缓存中。
     * <p>
     * Use this method before calling {@link #getBundle(ServerPlayer)} to avoid
     * potential exceptions when accessing skill data for players without skills enabled.
     * 在调用 {@link #getBundle(ServerPlayer)} 之前使用此方法，
     * 可避免在访问未启用技能的玩家的技能数据时出现潜在异常。
     *
     * @param player The server player entity to check for skill system status.
     *               要检查技能系统状态的服务端玩家实体。
     * @return {@code true} if the player has skills enabled and their data is cached,
     *         {@code false} if the player's skill system is disabled or not present in cache.
     *         如果玩家已启用技能且其数据已缓存，则返回 {@code true}；
     *         如果玩家的技能系统已禁用或不存在于缓存中，则返回 {@code false}。
     * @see #onToggle(ServerPlayer, boolean)
     * @see #getBundle(ServerPlayer)
     * @since 1.0.0
     */
    public static boolean isEnableSkill(ServerPlayer player) {
        return SKILL_BUNDLES.containsKey(player.getUUID());
    }

    /**
     * Retrieves the skill bundle for a player who has skills enabled.
     * 检索已启用技能的玩家的技能包。
     * <p>
     * This method returns the complete skill bundle containing all skills available
     * to the player, including their current states, levels, and other skill-related data.
     * The bundle is dynamically populated from all available skill providers when enabled.
     * 此方法返回包含玩家所有可用技能的完整技能包，包括其当前状态、等级和其他技能相关数据。
     * 该包在启用时从所有可用的技能提供者动态填充。
     * <p>
     * <b>Important:</b> This method will throw a {@link NullPointerException} if called
     * for a player who does not have skills enabled. Always check {@link #isEnableSkill(ServerPlayer)}
     * before calling this method to ensure the player's skill data is available.
     * <b>重要：</b> 如果为未启用技能的玩家调用此方法，将抛出 {@link NullPointerException}。
     * 在调用此方法之前始终检查 {@link #isEnableSkill(ServerPlayer)}，以确保玩家的技能数据可用。
     * <p>
     * The returned skill bundle is a live reference to the cached data. Modifications to
     * the bundle will affect the cached state. For read-only access, consider creating a copy.
     * 返回的技能包是对缓存数据的实时引用。对包的修改将影响缓存状态。
     * 对于只读访问，请考虑创建副本。
     *
     * @param player The server player entity whose skill bundle is to be retrieved.
     *               要检索技能包的服务端玩家实体。
     * @return The skill bundle containing all skills and their current states for the player.
     *         包含玩家所有技能及其当前状态的技能包。
     * @throws NullPointerException if the player does not have skills enabled or is not in cache.
     *                             如果玩家未启用技能或不存在于缓存中。
     * @see SkillBundle
     * @see SkillProviders#collectAllSkills(ServerPlayer)
     * @see #isEnableSkill(ServerPlayer)
     * @see #onToggle(ServerPlayer, boolean)
     * @since 1.0.0
     */
    public static SkillBundle getBundle(ServerPlayer player) {
        if (!SKILL_BUNDLES.containsKey(player.getUUID()))
            throw new NullPointerException();
        return SKILL_BUNDLES.get(player.getUUID());
    }
}
