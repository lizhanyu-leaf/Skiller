package com.leaf.skiller.server;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.*;

/**
 * Server-side key state tracker for monitoring player key press states.
 * 服务端按键状态跟踪器，用于监控玩家按键状态。
 * <p>
 * This class maintains a real-time mapping of which keys each player currently has pressed,
 * allowing the server to track key states independent of client-side state.
 * This is essential for skill systems that rely on key combinations or continuous key presses.
 * 该类维护每个玩家当前按下的按键的实时映射，允许服务端独立于客户端状态跟踪按键状态。
 * 这对于依赖按键组合或持续按键的技能系统至关重要。
 * <p>
 * The key states are synchronized from the client through network packets and are
 * automatically cleaned up when players disconnect from the server to prevent memory leaks.
 * 按键状态通过网络数据包从客户端同步，并在玩家断开与服务器的连接时自动清理以防止内存泄漏。
 * <p>
 * Key indices are integer values representing specific keyboard or mouse buttons as defined
 * by the input system. The same key index should be used consistently between client and server.
 * 按键索引是表示特定键盘或鼠标按钮的整数值，由输入系统定义。
 * 相同的按键索引应在客户端和服务端之间保持一致。
 *
 * @see ServerSkillCache
 * @see com.leaf.skiller.content.packet.KeyPressedPacket
 * @see com.leaf.skiller.client.ClientEvents
 * @since 1.0.0
 */
@EventBusSubscriber
public class PlayerPressedKeys {
    /**
     * Thread-safe map storing player UUIDs mapped to their currently pressed key indices.
     * 线程安全的映射表，存储玩家UUID到其当前按下的按键索引的映射。
     * <p>
     * Each player has a set of integer key indices representing all keys currently pressed
     * by that player. The set is dynamically updated as keys are pressed and released.
     * 每个玩家都有一个整数按键索引集合，表示该玩家当前按下的所有按键。
     * 该集合在按键按下和释放时动态更新。
     * <p>
     * The map uses HashMap for storage and HashSet for key sets, providing O(1) average
     * time complexity for add, remove, and contains operations.
     * 该映射表使用HashMap进行存储，使用HashSet作为按键集合，为添加、删除和包含操作提供O(1)的平均时间复杂度。
     * <p>
     * Key states are automatically cleaned up when players disconnect through the
     * {@link #onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent)} event handler.
     * 按键状态在玩家断开连接时通过 {@link #onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent)} 事件处理器自动清理。
     */
    private static final Map<UUID, Set<Integer>> PRESSED_KEYS = new HashMap<>();

    /**
     * Updates the pressed state of a specific key for a player.
     * 更新玩家特定按键的按下状态。
     * <p>
     * This method is called to synchronize key state changes from the client to the server.
     * When a key is pressed or released on the client side, this method updates the server's
     * tracking to maintain consistency with the client's input state.
     * 此方法被调用以将按键状态变化从客户端同步到服务端。
     * 当客户端按下或释放按键时，此方法更新服务端的跟踪以与客户端的输入状态保持一致。
     * <p>
     * If the player does not yet have an entry in the key state map, one is automatically
     * created. This ensures that the method can be called safely for any player without
     * requiring prior initialization.
     * 如果玩家在按键状态映射表中尚无条目，则会自动创建一个。
     * 这确保可以安全地为任何玩家调用此方法而无需预先初始化。
     * <p>
     * The method uses a compute-if-absent pattern to efficiently handle both existing
     * and new player entries, minimizing synchronization overhead.
     * 该方法使用计算-如果-不存在模式来高效处理现有和新的玩家条目，最小化同步开销。
     *
     * @param player The server player entity whose key state is being updated.
     *               正在更新按键状态的服务端玩家实体。
     * @param keyIndex The integer index representing the specific key being updated.
     *                 Must be consistent with the client-side key indexing system.
     *                 表示正在更新的特定按键的整数索引。
     *                 必须与客户端按键索引系统保持一致。
     * @param pressed {@code true} if the key is currently pressed, {@code false} if released.
     *                如果按键当前被按下则为 {@code true}，如果释放则为 {@code false}。
     * @see #isPressed(ServerPlayer, int)
     * @see #getPressedKeys(ServerPlayer)
     * @since 1.0.0
     */
    public static void setKeyPressed(ServerPlayer player, int keyIndex, boolean pressed) {
        Set<Integer> keys = PRESSED_KEYS.computeIfAbsent(player.getUUID(), k -> new HashSet<>());
        if (pressed) {
            keys.add(keyIndex);
        } else {
            keys.remove(keyIndex);
        }
    }

    /**
     * Checks whether a specific key is currently pressed for a player.
     * 检查玩家的特定按键当前是否被按下。
     * <p>
     * This method provides a simple boolean check for a single key's state,
     * useful for conditional logic that depends on specific key inputs.
     * It returns {@code false} for players who have no key state data (e.g., not yet initialized).
     * 此方法为单个按键的状态提供简单的布尔检查，适用于依赖于特定按键输入的条件逻辑。
     * 对于没有按键状态数据的玩家（例如，尚未初始化），它返回 {@code false}。
     * <p>
     * The method uses getOrDefault with an empty set to handle cases where the player
     * has no key state entry, providing safe access without null checks.
     * 该方法使用getOrDefault和空集合来处理玩家没有按键状态条目的情况，
     * 提供安全访问而无需空值检查。
     * <p>
     * This method is commonly used in skill activation logic, combo detection systems,
     * and other gameplay mechanics that respond to specific key presses.
     * 此方法通常用于技能激活逻辑、连击检测系统以及其他响应特定按键的游戏机制。
     *
     * @param player The server player entity whose key state is being checked.
     *               正在检查按键状态的服务端玩家实体。
     * @param keyIndex The integer index representing the specific key to check.
     *                 Must match the index used when the key state was set.
     *                 表示要检查的特定按键的整数索引。
     *                 必须与设置按键状态时使用的索引匹配。
     * @return {@code true} if the specified key is currently pressed for the player,
     *         {@code false} if the key is not pressed or the player has no key state data.
     *         如果玩家的指定按键当前被按下，则返回 {@code true}；
     *         如果按键未按下或玩家没有按键状态数据，则返回 {@code false}。
     * @see #setKeyPressed(ServerPlayer, int, boolean)
     * @see #getPressedKeys(ServerPlayer)
     * @since 1.0.0
     */
    public static boolean isPressed(ServerPlayer player, int keyIndex) {
        return PRESSED_KEYS.getOrDefault(player.getUUID(), Set.of()).contains(keyIndex);
    }

    /**
     * Retrieves all keys currently pressed for a player.
     * 检索玩家当前按下的所有按键。
     * <p>
     * This method returns the complete set of key indices that are currently pressed
     * for the specified player, providing comprehensive access to the player's current
     * input state. This is useful for multi-key combinations, chord detection, or complex
     * input handling that requires knowledge of all pressed keys simultaneously.
     * 此方法返回指定玩家当前按下的完整按键索引集合，提供对玩家当前输入状态的全面访问。
     * 这对于多键组合、和弦检测或需要同时了解所有按下按键的复杂输入处理很有用。
     * <p>
     * The returned set is immutable (Set.of()) for players with no key state data,
     * preventing modification of the internal state through the returned reference.
     * For players with key state data, the returned set is a live view of the internal data.
     * 对于没有按键状态数据的玩家，返回的集合是不可变的（Set.of()），
     * 防止通过返回的引用修改内部状态。对于有按键状态数据的玩家，返回的集合是内部数据的实时视图。
     * <p>
     * If you need to modify the returned set or maintain a snapshot, create a new
     * HashSet using the returned set as the constructor argument.
     * 如果需要修改返回的集合或维护快照，请使用返回的集合作为构造函数参数创建新的HashSet。
     *
     * @param player The server player entity whose pressed keys are being retrieved.
     *               正在检索按下按键的服务端玩家实体。
     * @return An immutable or mutable set of integer key indices representing all keys
     *         currently pressed by the player. Returns an empty set if the player has
     *         no key state data or no keys are currently pressed.
     *         表示玩家当前按下的所有按键的整数索引的不可变或可变集合。
     *         如果玩家没有按键状态数据或当前没有按下任何按键，则返回空集合。
     * @see #setKeyPressed(ServerPlayer, int, boolean)
     * @see #isPressed(ServerPlayer, int)
     * @since 1.0.0
     */
    public static Set<Integer> getPressedKeys(ServerPlayer player) {
        return PRESSED_KEYS.getOrDefault(player.getUUID(), Set.of());
    }

    /**
     * Removes all key state data for a player, cleaning up resources.
     * 移除玩家的所有按键状态数据，清理资源。
     * <p>
     * This method completely removes the player's entry from the key state tracking system,
     * clearing all pressed key information. This is typically called when a player disconnects
     * from the server or when the key state system needs to be reset for a specific player.
     * 此方法完全将玩家的条目从按键状态跟踪系统中移除，清除所有按下按键信息。
     * 这通常在玩家与服务器断开连接或需要为特定玩家重置按键状态系统时调用。
     * <p>
     * The cleanup is essential for preventing memory leaks, as the key state map would
     * otherwise accumulate entries for disconnected players over time.
     * 这种清理对于防止内存泄漏至关重要，否则按键状态映射表会随着时间的推移累积断开连接的玩家条目。
     * <p>
     * After this method is called, {@link #isPressed(ServerPlayer, int)} will return {@code false}
     * and {@link #getPressedKeys(ServerPlayer)} will return an empty set for the player
     * until new key state data is added via {@link #setKeyPressed(ServerPlayer, int, boolean)}.
     * 在调用此方法后，对于该玩家，{@link #isPressed(ServerPlayer, int)} 将返回 {@code false}，
     * {@link #getPressedKeys(ServerPlayer)} 将返回空集合，直到通过 {@link #setKeyPressed(ServerPlayer, int, boolean)} 添加新的按键状态数据。
     *
     * @param player The server player entity whose key state data is to be removed.
     *               要移除按键状态数据的服务端玩家实体。
     * @see #onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent)
     * @see #setKeyPressed(ServerPlayer, int, boolean)
     * @since 1.0.0
     */
    public static void remove(ServerPlayer player) {
        PRESSED_KEYS.remove(player.getUUID());
    }

    /**
     * Event handler for player disconnection, automatically cleaning up key state data.
     * 玩家断开连接的事件处理器，自动清理按键状态数据。
     * <p>
     * This method is automatically called by the NeoForge event bus when a player
     * logs out from the server. It ensures that key state data is properly cleaned up
     * without requiring manual cleanup calls, preventing memory leaks and data inconsistencies.
     * 当玩家从服务器登出时，此方法由NeoForge事件总线自动调用。
     * 它确保按键状态数据被正确清理而无需手动清理调用，防止内存泄漏和数据不一致。
     * <p>
     * The method is annotated with {@link SubscribeEvent} and {@link EventBusSubscriber}
     * to register it with the NeoForge event system, making it a passive listener that
     * responds to player logout events.
     * 该方法使用 {@link SubscribeEvent} 和 {@link EventBusSubscriber} 注解在NeoForge事件系统中注册，
     * 使其成为响应玩家登出事件的被动监听器。
     * <p>
     * This automatic cleanup ensures that the key state tracking system remains efficient
     * and accurate even with players frequently joining and leaving the server.
     * 这种自动清理确保即使玩家频繁加入和离开服务器，按键状态跟踪系统仍保持高效和准确。
     *
     * @param event The player logout event containing the player entity that is disconnecting.
     *              包含正在断开连接的玩家实体的玩家登出事件。
     * @see PlayerEvent.PlayerLoggedOutEvent
     * @see #remove(ServerPlayer)
     * @see EventBusSubscriber
     * @see SubscribeEvent
     * @since 1.0.0
     */
    // 玩家离开时清理
    @SubscribeEvent
    public static void onPlayerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        remove((ServerPlayer) event.getEntity());
    }
}
