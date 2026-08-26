package com.leaf.skiller;

import com.leaf.skiller.content.packet.KeyPressedPacket;
import com.leaf.skiller.content.packet.SkillTogglePacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Central registry for all network packets used by the Skiller mod.
 * Skiller 模组使用的所有网络数据包的中央注册表。
 * <p>
 * This class manages the registration of all custom network packets that
 * facilitate communication between the client and server. Network packets
 * are essential for multiplayer functionality and client-server synchronization.
 * 此类管理所有自定义网络数据包的注册，促进客户端和服务器之间的通信。
 * 网络数据包对于多人游戏功能和客户端-服务器同步至关重要。
 * </p>
 * <p>
 * Packets registered here use NeoForge's modern payload system, which
 * provides type-safe network communication with automatic version handling.
 * 此处注册的数据包使用 NeoForge 的现代负载系统，提供类型安全的
 * 网络通信和自动版本处理。
 * </p>
 *
 * @author Skiller Development Team
 * @author Skiller 开发团队
 * @version 1.0.0
 * @see RegisterPayloadHandlersEvent
 * @see KeyPressedPacket
 * @see SkillTogglePacket
 * @since 1.0.0
 */
@EventBusSubscriber
public final class AllPackets {
    /**
     * Private constructor to prevent instantiation of this utility class.
     * 私有构造函数，防止此实用类的实例化。
     * <p>
     * This class only contains static members for packet registration and
     * should never be instantiated. All methods are accessed statically.
     * 此类仅包含用于数据包注册的静态成员，永远不应被实例化。
     * 所有方法都是静态访问的。
     * </p>
     *
     * @throws UnsupportedOperationException Always thrown if construction is attempted
     *                                          如果尝试构造则始终抛出
     * @since 1.0.0
     */
    private AllPackets() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Event handler that registers all network packet handlers with the NeoForge network system.
     * 事件处理器，向 NeoForge 网络系统注册所有网络数据包处理器。
     * <p>
     * This method is called automatically during mod initialization to set up
     * network communication channels. It registers packet types, their
     * stream codecs for serialization, and their handler methods.
     * 此方法在模组初始化期间自动调用以设置网络通信通道。
     * 它注册数据包类型、用于序列化的流编解码器及其处理方法。
     * </p>
     * <p>
     * Registered packets:
     * 注册的数据包：
     * <ul>
     * <li>KeyPressedPacket - Sent from client to server when a skill key is pressed
     * 当按下技能键时从客户端发送到服务器</li>
     * <li>SkillTogglePacket - Sent from client to server to toggle skill activation state
     * 从客户端发送到服务器以切换技能激活状态</li>
     * </ul>
     * </p>
     *
     * @param event The payload handler registration event
     *              负载处理器注册事件
     * @see RegisterPayloadHandlersEvent
     * @see KeyPressedPacket
     * @see SkillTogglePacket
     * @since 1.0.0
     */
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar(Skiller.VERSION)
                .playToServer(
                        KeyPressedPacket.TYPE,
                        KeyPressedPacket.STREAM_CODEC,
                        KeyPressedPacket::handle
                )
                .playToServer(
                        SkillTogglePacket.TYPE,
                        SkillTogglePacket.STREAM_CODEC,
                        SkillTogglePacket::handle
                )
        ;
    }
}
