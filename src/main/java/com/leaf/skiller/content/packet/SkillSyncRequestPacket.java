package com.leaf.skiller.content.packet;

import com.leaf.skiller.Skiller;
import com.leaf.skiller.client.ClientSkillCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * A server-to-client packet requesting the client to apply an enable/disable
 * decision and (when enabling) sync its merged skill component back.
 * 服务端发往客户端的数据包，请求客户端执行启用/禁用决定，
 * 并在启用时把合并后的技能组件回传给服务端。
 *
 * <p>The server decides the skill system's timing from {@code PlayerPressedKeys}:
 * any held skill key enables it, no held skill key disables it. On enable the
 * server only sets a placeholder; the actual {@code SkillComponent} arrives
 * through {@link SyncSkillComponentPacket} once the client answers this request.
 * 服务端根据 PlayerPressedKeys 决定技能系统的时机：
 * 任意技能键被按住即启用，没有任何技能键被按住即禁用。
 * 启用时服务端仅放置占位数据；真正的 SkillComponent 由客户端响应本请求后
 * 通过 {@link SyncSkillComponentPacket} 到达。
 *
 * @param enable {@code true} to request enable + component sync,
 *               {@code false} to request a local disable
 *               为 {@code true} 时请求启用并同步组件，
 *               为 {@code false} 时请求本地禁用
 * @see ClientSkillCache ClientSkillCache - Applies the decision client-side
 * @see SyncSkillComponentPacket SyncSkillComponentPacket - The answering payload
 * @since 1.0.0
 * @author Leaf
 */
public record SkillSyncRequestPacket(boolean enable) implements CustomPacketPayload {
    /**
     * The unique identifier type for this packet payload.
     * 此数据包负载的唯一标识符类型。
     *
     * @since 1.0.0
     */
    public static final Type<SkillSyncRequestPacket> TYPE =
            new Type<>(Skiller.modLoc("skill_sync_request"));

    /**
     * Stream codec serializing the single enable flag.
     * 序列化单个启用标志的流编解码器。
     *
     * @since 1.0.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillSyncRequestPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> buf.writeBoolean(packet.enable),
                    buf -> new SkillSyncRequestPacket(buf.readBoolean())
            );

    /**
     * Returns the packet type identifier for routing this packet through the network.
     * 返回用于通过网络路由此数据包的数据包类型标识符。
     *
     * @return The packet type identifier that uniquely identifies this packet class
     *         唯一标识此数据包类的数据包类型标识符
     * @since 1.0.0
     */
    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handles the sync request on the client thread.
     * 在客户端线程处理同步请求。
     * <p>
     * The client applies the enable/disable locally; on enable it also collects
     * its skills and answers with {@link SyncSkillComponentPacket}. Only executed
     * on the physical client, so referencing client-only classes here is safe.
     * 客户端在本地执行启用/禁用；启用时还会收集自身技能并以
     * {@link SyncSkillComponentPacket} 应答。仅在物理客户端执行，
     * 因此此处引用仅客户端的类是安全的。
     * </p>
     *
     * @param packet The sync request packet / 同步请求数据包
     * @param context The payload context / 负载上下文
     * @see ClientSkillCache#handleSyncRequest(boolean)
     * @since 1.0.0
     */
    public static void handle(SkillSyncRequestPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientSkillCache.handleSyncRequest(packet.enable));
    }
}
