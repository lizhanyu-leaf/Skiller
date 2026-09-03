package com.leaf.skiller.content.packet;

import com.leaf.skiller.Skiller;
import com.leaf.skiller.server.ServerSkillCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * A network packet that communicates skill system enable/disable toggle requests from client to server.
 * 网络数据包，用于从客户端向服务器通信技能系统启用/禁用切换请求
 *
 * <p>This packet is used when a player wants to toggle the entire skill system on or off
 * 当玩家想要切换整个技能系统的开关状态时使用此数据包
 *
 * <p>The packet contains a boolean flag indicating whether to enable or disable the skill system
 * 该数据包包含一个布尔标志，指示是启用还是禁用技能系统
 *
 * <p>Unlike key press packets which track continuous input, this is a discrete toggle action
 * 与跟踪连续输入的按键数据包不同，这是一个离散的切换操作
 *
 * @param enable True to enable the skill system, false to disable it
 *               为 true 时启用技能系统，为 false 时禁用技能系统
 * @see KeyPressedPacket KeyPressedPacket - Packet for tracking continuous key press states
 * @see ServerSkillCache ServerSkillCache - Server-side skill state manager
 * @see CustomPacketPayload CustomPacketPayload - Base interface for custom network packets
 * @since 1.0.0
 * @version 1.0.0
 * @author Skiller Mod Team
 */
public record SkillTogglePacket(boolean enable) implements CustomPacketPayload {
    /**
     * The unique identifier type for this packet payload.
     * 此数据包负载的唯一标识符类型
     *
     * <p>Used by the networking system to route and identify packets during transmission
     * 网络系统在传输过程中使用它来路由和识别数据包
     *
     * <p>The resource location "skill_toggle" uniquely identifies this packet type within the mod's namespace
     * 资源位置 "skill_toggle" 在模组的命名空间内唯一标识此数据包类型
     *
     * @see CustomPacketPayload.Type CustomPacketPayload.Type - Base type for packet identifiers
     * @see Skiller#modLoc(String) Skiller.modLoc - Creates mod-scoped resource locations
     * @since 1.0.0
     */
    public static final Type<SkillTogglePacket> TYPE =
            new Type<>(Skiller.modLoc("skill_toggle"));

    /**
     * Stream codec for serializing and deserializing SkillTogglePacket instances.
     * 用于序列化和反序列化 SkillTogglePacket 实例的流编解码器
     *
     * <p>This codec handles the encoding of the enable/disable boolean flag to/from byte buffers
     * 此编解码器处理启用/禁用布尔标志到/从字节缓冲区的编码
     *
     * <p>The encoder writes the enable state as a single boolean value
     * 编码器将启用状态写入为单个布尔值
     *
     * <p>The decoder reads the boolean value to reconstruct the packet with the toggle state
     * 解码器读取布尔值以重构包含切换状态的数据包
     *
     * <p>This codec is simpler than KeyPressedPacket's codec as it only handles a single boolean value
     * 此编解码器比 KeyPressedPacket 的编解码器更简单，因为它只处理单个布尔值
     *
     * @see StreamCodec StreamCodec - Interface for bidirectional stream encoding/decoding
     * @see RegistryFriendlyByteBuf RegistryFriendlyByteBuf - Network byte buffer with registry context
     * @since 1.0.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillTogglePacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        buf.writeBoolean(packet.enable);
                    },
                    buf -> new SkillTogglePacket(buf.readBoolean())
            );


    /**
     * Returns the packet type identifier for routing this packet through the network.
     * 返回用于通过网络路由此数据包的数据包类型标识符
     *
     * <p>This method is required by the CustomPacketPayload interface for packet identification
     * CustomPacketPayload 接口需要此方法用于数据包识别
     *
     * <p>The returned type is used by the networking system to ensure packets are routed to the correct handlers
     * 网络系统使用返回的类型来确保数据包被路由到正确的处理程序
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
     * Handles the skill toggle packet on the server side by updating the player's skill system state.
     * 在服务器端处理技能切换数据包，通过更新玩家的技能系统状态
     *
     * <p>This method is called on the network thread when a skill toggle packet is received
     * 当接收到技能切换数据包时，在网络线程上调用此方法
     *
     * <p>The actual skill state update is queued to run on the main server thread to ensure thread safety
     * 实际的技能状态更新被排队到主服务器线程上运行，以确保线程安全
     *
     * <p>The ServerSkillCache processes the toggle request and updates all related skill functionality
     * ServerSkillCache 处理切换请求并更新所有相关的技能功能
     *
     * <p>Use of enqueueWork() prevents race conditions and ensures proper synchronization with game state
     * 使用 enqueueWork() 可以防止竞争条件，并确保与游戏状态的正确同步
     *
     * @param packet The skill toggle packet containing the enable/disable flag
     *               包含启用/禁用标志的技能切换数据包
     * @param context The payload context providing access to the player and network information
     *                提供玩家和网络信息的负载上下文
     * @see IPayloadContext IPayloadContext - Context interface for packet handling
     * @see ServerSkillCache#onToggle(ServerPlayer, boolean) ServerSkillCache.onToggle - Processes skill toggle requests
     * @see ServerPlayer ServerPlayer - Server-side player entity
     * @since 1.0.0
     */
    public static void handle(SkillTogglePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ServerSkillCache.onToggle(player, packet.enable);
        });
    }
}
