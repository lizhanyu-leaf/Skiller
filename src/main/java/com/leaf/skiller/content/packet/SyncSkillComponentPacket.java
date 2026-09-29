package com.leaf.skiller.content.packet;

import com.leaf.skiller.Skiller;
import com.leaf.skiller.content.skill.SkillComponent;
import com.leaf.skiller.server.ServerSkillCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * A network packet that transmits the player's merged skill component from client to server.
 * 网络数据包，将玩家合并后的技能组件从客户端发送到服务端。
 *
 * <p>The client is the single source of truth for skill collection: it gathers skills from
 * all providers once, merges them into a {@link SkillComponent}, and sends the result here.
 * The server stores exactly what it receives instead of running its own collection, which
 * eliminates any drift between the two sides.
 * 客户端是技能收集的唯一事实来源：它从所有提供者收集一次技能，合并为一个
 * {@link SkillComponent}，并将结果通过此数据包发送。服务端存储收到的数据，
 * 而不是自行收集，从而消除两端之间的不同步。
 *
 * @param skillComponent The merged skill component containing all skills with their key bindings
 *                       包含所有技能及其按键绑定的合并技能组件
 * @see SkillComponent SkillComponent - The payload being synchronized
 * @see ServerSkillCache ServerSkillCache - Server-side storage for the received component
 * @see SkillSyncRequestPacket SkillSyncRequestPacket - The server request this packet answers
 * @see CustomPacketPayload CustomPacketPayload - Base interface for custom network packets
 * @since 1.0.0
 * @author Leaf
 */
public record SyncSkillComponentPacket(SkillComponent skillComponent) implements CustomPacketPayload {
    /**
     * The unique identifier type for this packet payload.
     * 此数据包负载的唯一标识符类型。
     *
     * @see CustomPacketPayload.Type CustomPacketPayload.Type - Base type for packet identifiers
     * @since 1.0.0
     */
    public static final Type<SyncSkillComponentPacket> TYPE =
            new Type<>(Skiller.modLoc("sync_skill_component"));

    /**
     * Stream codec delegating to {@link SkillComponent#STREAM_CODEC}.
     * 委托给 {@link SkillComponent#STREAM_CODEC} 的流编解码器。
     *
     * @see StreamCodec StreamCodec - Interface for bidirectional stream encoding/decoding
     * @since 1.0.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSkillComponentPacket> STREAM_CODEC =
            StreamCodec.composite(
                    SkillComponent.STREAM_CODEC,
                    SyncSkillComponentPacket::skillComponent,
                    SyncSkillComponentPacket::new
            );

    /**
     * Returns the packet type identifier for routing this packet through the network.
     * 返回用于通过网络路由此数据包的数据包类型标识符。
     *
     * @return The packet type identifier that uniquely identifies this packet class
     *         唯一标识此数据包类的数据包类型标识符
     * @see CustomPacketPayload#type() CustomPacketPayload#type() - Interface method requirement
     * @since 1.0.0
     */
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handles the sync packet on the server side by storing the received component.
     * 在服务器端处理同步数据包，存储接收到的技能组件。
     *
     * <p>The update is queued on the main server thread via enqueueWork() for thread safety.
     * The component is only accepted for players whose skill system is currently enabled,
     * so a stray packet cannot resurrect state after a disable.
     * 更新通过 enqueueWork() 排队到服务端主线程以保证线程安全。
     * 仅当玩家的技能系统当前已启用时才接受该组件，
     * 这样杂散的数据包不会在禁用后恢复状态。
     *
     * @param packet The packet containing the merged skill component
     *               包含合并技能组件的数据包
     * @param context The payload context providing access to the player and network information
     *                提供玩家和网络信息的负载上下文
     * @see IPayloadContext IPayloadContext - Context interface for packet handling
     * @see ServerSkillCache#onSync(ServerPlayer, SkillComponent) ServerSkillCache.onSync - Stores the component
     * @since 1.0.0
     */
    public static void handle(SyncSkillComponentPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ServerSkillCache.onSync(player, packet.skillComponent());
        });
    }
}
