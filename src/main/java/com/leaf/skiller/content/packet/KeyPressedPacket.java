package com.leaf.skiller.content.packet;

import com.leaf.skiller.Skiller;
import com.leaf.skiller.server.PlayerPressedKeys;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * A network packet that communicates key press state changes from client to server.
 * 网络数据包，用于从客户端向服务器通信按键状态变化
 *
 * <p>This packet is used to synchronize the pressed/released state of skill keys
 * 该数据包用于同步技能按键的按下/释放状态
 *
 * <p>Each key press is tracked by its index and boolean pressed state
 * 每个按键通过其索引和布尔按下状态进行跟踪
 *
 * @param keyIndex The unique index identifier of the skill key being pressed/released
 *                 被按下/释放的技能按键的唯一索引标识符
 * @param pressed True if the key is currently pressed, false if it's being released
 *                如果按键当前被按下则为 true，如果正在释放则为 false
 * @see SkillTogglePacket SkillTogglePacket - Packet for toggling skill enable/disable state
 * @see PlayerPressedKeys PlayerPressedKeys - Server-side key press state manager
 * @see CustomPacketPayload CustomPacketPayload - Base interface for custom network packets
 * @since 1.0.0
 * @version 1.0.0
 * @author Skiller Mod Team
 */
public record KeyPressedPacket(int keyIndex, boolean pressed) implements CustomPacketPayload {
    /**
     * The unique identifier type for this packet payload.
     此数据包负载的唯一标识符类型
     *
     * <p>Used by the networking system to route and identify packets during transmission
     * 网络系统在传输过程中使用它来路由和识别数据包
     *
     * @see CustomPacketPayload.Type CustomPacketPayload.Type - Base type for packet identifiers
     * @since 1.0.0
     */
    public static final Type<KeyPressedPacket> TYPE =
            new Type<>(Skiller.modLoc("key_pressed"));

    /**
     * Stream codec for serializing and deserializing KeyPressedPacket instances.
     * 用于序列化和反序列化 KeyPressedPacket 实例的流编解码器
     *
     * <p>This codec handles the encoding of key index and pressed state to/from byte buffers
     * 此编解码器处理按键索引和按下状态到/从字节缓冲区的编码
     *
     * <p>The encoder writes the key index as a VarInt and the pressed state as a boolean
     * 编码器将按键索引写入为 VarInt，按下状态写入为布尔值
     *
     * <p>The decoder reads these values in the same order to reconstruct the packet
     * 解码器按相同顺序读取这些值以重构数据包
     *
     * @see StreamCodec StreamCodec - Interface for bidirectional stream encoding/decoding
     * @see RegistryFriendlyByteBuf RegistryFriendlyByteBuf - Network byte buffer with registry context
     * @since 1.0.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, KeyPressedPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        buf.writeVarInt(packet.keyIndex);
                        buf.writeBoolean(packet.pressed);
                    },
                    buf -> new KeyPressedPacket(buf.readVarInt(), buf.readBoolean())
            );

    /**
     * Returns the packet type identifier for routing this packet through the network.
     * 返回用于通过网络路由此数据包的数据包类型标识符
     *
     * <p>This method is required by the CustomPacketPayload interface for packet identification
     * CustomPacketPayload 接口需要此方法用于数据包识别
     *
     * @return The packet type identifier that uniquely identifies this packet class
     *         唯一标识此数据包类的数据包类型标识符
     * @see CustomPacketPayload#type() CustomPacketPayload#type() - Interface method requirement
     * @see Type Type - Packet type identifier class
     * @since 1.0.0
     */
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handles the key press packet on the server side by updating the player's key state.
     * 在服务器端处理按键数据包，通过更新玩家的按键状态
     *
     * <p>This method is called on the network thread when a key press packet is received
     * 当接收到按键数据包时，在网络线程上调用此方法
     *
     * <p>The actual key state update is queued to run on the main server thread to ensure thread safety
     * 实际的按键状态更新被排队到主服务器线程上运行，以确保线程安全
     *
     * <p>Use of enqueueWork() prevents race conditions and ensures proper synchronization with game state
     * 使用 enqueueWork() 可以防止竞争条件，并确保与游戏状态的正确同步
     *
     * @param packet The key press packet containing the key index and pressed state
     *               包含按键索引和按下状态的按键数据包
     * @param context The payload context providing access to the player and network information
     *                提供玩家和网络信息的负载上下文
     * @see IPayloadContext IPayloadContext - Context interface for packet handling
     * @see PlayerPressedKeys#setKeyPressed - Updates key state
     * @see ServerPlayer ServerPlayer - Server-side player entity
     * @since 1.0.0
     */
    public static void handle(KeyPressedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            PlayerPressedKeys.setKeyPressed(player, packet.keyIndex, packet.pressed);
        });
    }
}
