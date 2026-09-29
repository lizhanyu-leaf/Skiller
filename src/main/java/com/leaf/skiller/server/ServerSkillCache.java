package com.leaf.skiller.server;

import com.leaf.skiller.content.skill.SkillComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side skill cache management class for storing and managing player skill components.
 * 服务端技能缓存管理类，用于存储和管理玩家的技能组件。
 * <p>
 * This class maintains a mapping of player UUIDs to their corresponding skill components,
 * providing centralized access to player skill data on the server side.
 * 该类维护玩家UUID到其对应技能组件的映射，在服务端提供对玩家技能数据的集中访问。
 * <p>
 * The cache is populated when skills are enabled for a player and cleared when disabled,
 * ensuring that skill data is only stored for players who actively have skills enabled.
 * 缓存在为玩家启用技能时填充，在禁用时清除，确保仅为主动启用技能的玩家存储技能数据。
 *
 * @see SkillComponent
 * @see SkillProviders
 * @see com.leaf.skiller.client.ClientSkillCache
 * @since 1.0.0
 */
@EventBusSubscriber
public class ServerSkillCache {

    /**
     * Map storing player UUIDs mapped to their skill components.
     * 存储玩家UUID到其技能组件的映射。
     * <p>
     * All mutations happen on the main server thread (packet handlers use
     * enqueueWork), so a plain HashMap is sufficient here.
     * 所有修改都发生在服务端主线程（数据包处理器使用 enqueueWork），
     * 因此这里使用普通 HashMap 即可。
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
    private static final Map<UUID, SkillComponent> SKILL_COMPONENTS = new HashMap<>();

    /**
     * Callbacks waiting for the player's component sync to arrive.
     * 等待玩家组件同步到达的回调列表。
     * <p>
     * Enabling the system and receiving the client's component are asynchronous,
     * so logic that must observe the real component (e.g. a KEY_PRESSED edge
     * fired by the enabling press) can park a callback here via
     * {@link #onSynced(ServerPlayer, Runnable)}. Callbacks are drained and
     * run by {@link #onSync(ServerPlayer, SkillComponent)} when the component
     * arrives, and are discarded on disable or disconnect.
     * 启用系统与收到客户端组件是异步的，
     * 因此必须依赖真实组件的逻辑（例如启用按键触发的 KEY_PRESSED 沿）
     * 可以通过 {@link #onSynced(ServerPlayer, Runnable)} 在此挂起回调。
     * 组件到达时由 {@link #onSync(ServerPlayer, SkillComponent)} 取出并执行，
     * 禁用或断开连接时丢弃。
     * </p>
     */
    private static final Map<UUID, List<Runnable>> PENDING_SYNC_CALLBACKS = new HashMap<>();

    /**
     * Runs the callback immediately if the player's component is already
     * synced, otherwise parks it until the sync arrives.
     * 若玩家的组件已同步则立即执行回调，否则挂起直到同步到达。
     * <p>
     * Use this for actions that need the real skill data right now but may be
     * triggered before the client has answered the sync request (the enabling
     * press only carries the {@link SkillComponent#EMPTY} placeholder).
     * 适用于此刻就需要真实技能数据、但可能早于客户端应答同步请求而触发
     * 的动作（启用系统的按下只携带 {@link SkillComponent#EMPTY} 占位）。
     * </p>
     * <p>
     * Callbacks run on the main server thread either way; a parked callback is
     * silently dropped if the player disables or disconnects before the sync.
     * 两种情况下回调都在服务端主线程执行；
     * 若玩家在同步前禁用或断开连接，挂起的回调会被静默丢弃。
     * </p>
     *
     * @param player   The server player the callback depends on / 回调依赖的服务端玩家
     * @param callback The action to run once the component is available / 组件就绪后执行的动作
     * @see #onSync(ServerPlayer, SkillComponent)
     * @since 1.0.0
     */
    public static void onSynced(ServerPlayer player, Runnable callback) {
        PENDING_SYNC_CALLBACKS.computeIfAbsent(player.getUUID(), k -> new ArrayList<>())
                .add(callback);
    }

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
     *               为 {@code true} 时启用技能并缓存玩家的技能组件，
     *               为 {@code false} 时禁用技能并将玩家从缓存中移除。
     * @see SkillProviders#collectAllSkills
     * @see #isEnableSkill(ServerPlayer)
     * @see #getComponent(ServerPlayer)
     * @since 1.0.0
     */
    public static void onToggle(ServerPlayer player, boolean enable) {
        if (enable) {
            // Enabled marker only. The actual skill data is collected on the
            // client (single source of truth) and arrives via onSync.
            // 仅作启用标记。实际技能数据由客户端收集（唯一事实来源），
            // 并通过 onSync 到达。
            SKILL_COMPONENTS.put(player.getUUID(), SkillComponent.EMPTY);
        }
        else {
            SKILL_COMPONENTS.remove(player.getUUID());
            // A disable invalidates anything parked for the sync.
            // 禁用会使所有为同步暂存的内容失效。
            PENDING_SYNC_CALLBACKS.remove(player.getUUID());
        }
    }

    /**
     * Stores the skill component sent by the client for an enabled player.
     * 存储客户端为已启用技能系统的玩家发送的技能组件。
     * <p>
     * The client is the single source of truth for skill collection, so the
     * server simply persists what it receives. Data is only accepted while
     * the player's skill system is enabled; a stray packet arriving after a
     * disable cannot resurrect cached state.
     * 客户端是技能收集的唯一事实来源，因此服务端只需持久化收到的数据。
     * 仅当玩家的技能系统处于启用状态时才接受数据；
     * 禁用之后到达的杂散数据包无法恢复缓存状态。
     * </p>
     *
     * @param player The server player whose component is being synchronized.
     *               正在同步技能组件的服务端玩家。
     * @param component The merged component collected on the client, or null to ignore.
     *                  客户端收集的合并组件，null 则忽略。
     * @see #onToggle(ServerPlayer, boolean)
     * @see #getComponent(ServerPlayer)
     * @since 1.0.0
     */
    public static void onSync(ServerPlayer player, SkillComponent component) {
        if (component == null) return;
        // Only accept data for players whose skill system is enabled.
        // 仅接受技能系统已启用的玩家的数据。
        if (!SKILL_COMPONENTS.containsKey(player.getUUID())) return;
        SKILL_COMPONENTS.put(player.getUUID(), component);

        // The real component is in place: drain and run everything that was
        // waiting for it (the callbacks themselves re-validate any conditions
        // they care about, e.g. whether the key is still held).
        // 真实组件已就位：取出并执行所有等待它的回调
        //（回调自身会重新校验各自关心的条件，例如按键是否仍被按住）。
        List<Runnable> callbacks = PENDING_SYNC_CALLBACKS.remove(player.getUUID());
        if (callbacks == null) return;
        callbacks.forEach(Runnable::run);
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
     * Use this method before calling {@link #getComponent(ServerPlayer)} to avoid
     * potential exceptions when accessing skill data for players without skills enabled.
     * 在调用 {@link #getComponent(ServerPlayer)} 之前使用此方法，
     * 可避免在访问未启用技能的玩家的技能数据时出现潜在异常。
     *
     * @param player The server player entity to check for skill system status.
     *               要检查技能系统状态的服务端玩家实体。
     * @return {@code true} if the player has skills enabled and their data is cached,
     *         {@code false} if the player's skill system is disabled or not present in cache.
     *         如果玩家已启用技能且其数据已缓存，则返回 {@code true}；
     *         如果玩家的技能系统已禁用或不存在于缓存中，则返回 {@code false}。
     * @see #onToggle(ServerPlayer, boolean)
     * @see #getComponent(ServerPlayer)
     * @since 1.0.0
     */
    public static boolean isEnableSkill(ServerPlayer player) {
        return SKILL_COMPONENTS.containsKey(player.getUUID());
    }

    /**
     * Retrieves the skill bundle for a player who has skills enabled.
     * 检索已启用技能的玩家的技能组件。
     * <p>
     * This method returns the complete skill bundle containing all skills available
     * to the player, including their current states, levels, and other skill-related data.
     * The bundle is dynamically populated from all available skill providers when enabled.
     * 此方法返回包含玩家所有可用技能的完整技能组件，包括其当前状态、等级和其他技能相关数据。
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
     * 返回的技能组件是对缓存数据的实时引用。对包的修改将影响缓存状态。
     * 对于只读访问，请考虑创建副本。
     *
     * @param player The server player entity whose skill bundle is to be retrieved.
     *               要检索技能组件的服务端玩家实体。
     * @return The skill bundle containing all skills and their current states for the player.
     *         包含玩家所有技能及其当前状态的技能组件。
     * @throws NullPointerException if the player does not have skills enabled or is not in cache.
     *                             如果玩家未启用技能或不存在于缓存中。
     * @see SkillComponent
     * @see SkillProviders#collectAllSkills
     * @see #isEnableSkill(ServerPlayer)
     * @see #onToggle(ServerPlayer, boolean)
     * @since 1.0.0
     */
    public static SkillComponent getComponent(ServerPlayer player) {
        SkillComponent component = SKILL_COMPONENTS.get(player.getUUID());
        if (component == null)
            throw new IllegalStateException(
                    "Player has not enabled the skill system: " + player.getGameProfile().getName());
        return component;
    }

    /**
     * Removes all cached skill data for a player, cleaning up resources.
     * 移除玩家的所有缓存技能数据，清理资源。
     * <p>
     * Called automatically when a player disconnects so that entries do not
     * linger for players who leave with the skill system still enabled.
     * 在玩家断开连接时自动调用，避免技能系统仍启用的玩家离开后其条目残留。
     * </p>
     *
     * @param player The player whose cached data should be removed.
     *               要移除缓存数据的玩家。
     * @see #onToggle(ServerPlayer, boolean)
     * @since 1.0.0
     */
    public static void remove(ServerPlayer player) {
        SKILL_COMPONENTS.remove(player.getUUID());
        PENDING_SYNC_CALLBACKS.remove(player.getUUID());
    }

    /**
     * Event handler for player disconnection, cleaning up cached skill data.
     * 玩家断开连接的事件处理器，清理缓存的技能数据。
     *
     * @param event The player logout event.
     *              玩家登出事件。
     * @see PlayerEvent.PlayerLoggedOutEvent
     * @see #remove(ServerPlayer)
     * @since 1.0.0
     */
    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            remove(serverPlayer);
        }
    }
}
