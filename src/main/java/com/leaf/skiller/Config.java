package com.leaf.skiller;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Central configuration class for the Skiller mod, managing all mod settings and options.
 * Skiller 模组的中央配置类，管理所有模组设置和选项。
 * <p>
 * This class provides a structured way to define and manage mod configuration
 * values using NeoForge's configuration system. It supports both client-side
 * and common configuration categories.
 * 此类提供了一种结构化的方式来使用 NeoForge 的配置系统定义和管理
 * 模组配置值。它支持客户端和通用配置类别。
 * </p>
 * <p>
 * Configuration values are automatically synchronized between server and clients,
 * and can be modified at runtime through configuration files or in-game menus.
 * 配置值在服务器和客户端之间自动同步，可以通过配置文件或游戏内菜单
 * 在运行时修改。
 * </p>
 *
 * @author Skiller Development Team
 * @author Skiller 开发团队
 * @version 1.0.0
 * @see EventBusSubscriber
 * @see ModConfigSpec
 * @see Common
 * @since 1.0.0
 */
// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
@SuppressWarnings("removal")
@EventBusSubscriber(modid = Skiller.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    /**
     * The builder used to construct the configuration specification.
     * 用于构建配置规范的构建器。
     * <p>
     * This builder creates the structure and validation rules for all
     * configuration values defined in this class.
     * 此构建器为此类中定义的所有配置值创建结构和验证规则。
     * </p>
     */
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * The common configuration instance containing all shared settings.
     * 包含所有共享设置的通用配置实例。
     * <p>
     * Common configuration applies to both client and server sides,
     * and is synchronized across the network when playing on multiplayer servers.
     * 通用配置适用于客户端和服务器端，在多人游戏服务器上通过网络同步。
     * </p>
     */
    private static final Common COMMON;

    /**
     * The final configuration specification that defines all mod configuration.
     * 定义所有模组配置的最终配置规范。
     * <p>
     * This specification is registered with the mod container and used
     * to load and validate configuration values from disk.
     * 此规范向模组容器注册，用于从磁盘加载和验证配置值。
     * </p>
     */
    public static final ModConfigSpec SPEC;

    /**
     * Static initialization block that builds the configuration structure.
     * 构建配置结构的静态初始化块。
     * <p>
     * This block creates the configuration specification by instantiating
     * the Common configuration class and extracting the built specification.
     * 此块通过实例化 Common 配置类并提取构建的规范来创建配置规范。
     * </p>
     */
    static {
        var pair = BUILDER.configure(Common::new);
        COMMON = pair.getKey();
        SPEC = pair.getRight();
    }

    /**
     * Common configuration category containing settings that apply to both client and server.
     * 包含适用于客户端和服务器的设置的通用配置类别。
     * <p>
     * Values in this category are synchronized in multiplayer environments
     * and can be modified by server administrators.
     * 此类别中的值在多人游戏环境中同步，可由服务器管理员修改。
     * </p>
     * <p>
     * Subclasses can define additional configuration categories (Client, Server)
     * to segregate settings by their intended scope.
     * 子类可以定义其他配置类别（Client、Server）以按预期范围分隔设置。
     * </p>
     *
     * @author Skiller Development Team
     * @author Skiller 开发团队
     * @version 1.0.0
     * @see ModConfigSpec.Builder
     * @since 1.0.0
     */
    public static class Common {

        /**
         * Constructs the Common configuration with all shared mod settings.
         * 使用所有共享模组设置构造通用配置。
         * <p>
         * This constructor receives the configuration builder and defines
         * all configuration values that should be available on both
         * client and server sides.
         * 此构造函数接收配置构建器并定义应在客户端和服务器端
         * 都可用的所有配置值。
         * </p>
         *
         * @param builder The configuration builder for defining configuration values
         *                用于定义配置值的配置构建器
         * @since 1.0.0
         */
        private Common(ModConfigSpec.Builder builder) {
        }
    }

    /**
     * Event handler called when the configuration is loaded or reloaded.
     * 配置加载或重新加载时调用的事件处理器。
     * <p>
     * This method is invoked after configuration values are loaded from disk
     * or modified at runtime. It can be used to update in-game systems
     * to reflect the new configuration values.
     * 此方法在配置值从磁盘加载或在运行时修改后调用。
     * 它可用于更新游戏内系统以反映新的配置值。
     * </p>
     * <p>
     * Note: This event fires on both the client and server sides, so care
     * should be taken to only perform side-appropriate operations.
     * 注意：此事件在客户端和服务器端都会触发，因此应注意
     * 仅执行适合端的操作。
     * </p>
     *
     * @param event The configuration event containing loading information
     *              包含加载信息的配置事件
     * @see ModConfigEvent
     * @since 1.0.0
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }
}
