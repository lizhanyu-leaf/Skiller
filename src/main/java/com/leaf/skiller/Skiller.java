package com.leaf.skiller;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The main class for the Skiller mod, serving as the central entry point for initialization.
 * Skiller 模组的主类，作为初始化的中心入口点。
 * <p>
 * This class handles mod initialization, event registration, and provides utility methods
 * for creating mod-specific resource locations. It follows NeoForge modding conventions.
 * 此类处理模组初始化、事件注册，并提供用于创建模组特定资源位置的实用方法。
 * 它遵循 NeoForge 模组约定。
 * </p>
 * <p>
 * The mod identifier "skiller" is used throughout the codebase for namespacing
 * all mod resources, registries, and configurations.
 * 模组标识符 "skiller" 在整个代码库中用于为所有模组资源、注册表和配置命名。
 * </p>
 *
 * @author Leaf
 * @version 1.0.0
 * @see ModContainer
 * @see IEventBus
 * @see Config
 * @see AllDataComponents
 * @see AllSkillInstanceFactories
 * @see AllSkillResources
 * @see AllSkillProviders
 * @since 1.0.0
 */
// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Skiller.MOD_ID)
public class Skiller {
    /**
     * The unique mod identifier used for namespacing all mod resources.
     * 用于所有模组资源命名空间的唯一模组标识符。
     * <p>
     * This constant is used throughout the codebase to prefix resource locations,
     * registry keys, and configuration files.
     * 此常量在整个代码库中用于前缀资源位置、注册表键和配置文件。
     * </p>
     */
    public static final String MOD_ID = "skiller";

    /**
     * The current version of the Skiller mod.
     * Skiller 模组的当前版本。
     * <p>
     * Used for packet versioning, compatibility checks, and display purposes.
     * 用于数据包版本控制、兼容性检查和显示目的。
     * </p>
     */
    public static final String VERSION = "1.0.0";

    /**
     * The logger instance for the Skiller mod, used for outputting mod-specific log messages.
     * Skiller 模组的日志记录器实例，用于输出模组特定的日志消息。
     * <p>
     * This logger outputs to the Minecraft game log with the "Skiller" category,
     * making it easy to filter and identify mod-related messages.
     * 此日志记录器以 "Skiller" 类别输出到 Minecraft 游戏日志，
     * 便于过滤和识别模组相关消息。
     * </p>
     */
    public static final Logger LOGGER = LogManager.getLogger("Skiller");

    /**
     * Constructs the Skiller mod instance and initializes all mod components.
     * 构造 Skiller 模组实例并初始化所有模组组件。
     * <p>
     * This constructor is called automatically by NeoForge during mod loading.
     * It performs the following initialization steps:
     * 此构造函数在模组加载期间由 NeoForge 自动调用。
     * 它执行以下初始化步骤：
     * </p>
     * <ul>
     * <li>Registers all data components used by the mod
     * 注册模组使用的所有数据组件</li>
     * <li>Registers the mod configuration system
     * 注册模组配置系统</li>
     * <li>Adds an event listener for registry registration
     * 添加注册表注册的事件监听器</li>
     * </ul>
     *
     * @param modEventBus The NeoForge mod event bus for registering mod events
     *                    用于注册模组事件的 NeoForge 模组事件总线
     * @param modContainer The mod container containing metadata and configuration
     *                     包含元数据和配置的模组容器
     * @see AllDataComponents
     * @see Config
     * @see #onRegister(RegisterEvent)
     * @since 1.0.0
     */
    public Skiller(IEventBus modEventBus, ModContainer modContainer) {
        AllDataComponents.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(Skiller::onRegister);
    }

    /**
     * Handles the registration event for all mod registries.
     * 处理所有模组注册表的注册事件。
     * <p>
     * This method is called during mod initialization to register all custom
     * registries and their contents. It ensures all skill-related resources,
     * providers, and factories are properly registered.
     * 此方法在模组初始化期间调用，用于注册所有自定义注册表及其内容。
     * 它确保所有技能相关资源、提供者和工厂正确注册。
     * </p>
     *
     * @param event The register event providing access to registries
     *              提供注册表访问权限的注册事件
     * @see RegisterEvent
     * @see AllSkillInstanceFactories
     * @see AllSkillResources
     * @see AllSkillProviders
     * @since 1.0.0
     */
    public static void onRegister(final RegisterEvent event) {
        AllSkillInstanceFactories.register();
        AllSkillResources.register();
        AllSkillProviders.register();
    }

    /**
     * Creates a resource location scoped to this mod.
     * 创建作用域于此模组的资源位置。
     * <p>
     * This utility method prefixes all resource paths with the mod ID,
     * ensuring proper namespacing and avoiding conflicts with other mods.
     * The returned resource location can be used for textures, models,
     * registry keys, and any other mod resource.
     * 此实用方法使用模组 ID 前缀所有资源路径，确保适当的命名空间
     * 并避免与其他模组冲突。返回的资源位置可用于纹理、模型、
     * 注册表键和任何其他模组资源。
     * </p>
     * <p>
     * Example usage: modLoc("textures/item/sword.png") returns "skiller:textures/item/sword.png"
     * 使用示例：modLoc("textures/item/sword.png") 返回 "skiller:textures/item/sword.png"
     * </p>
     *
     * @param path The resource path (without mod ID prefix)
     *             资源路径（不带模组 ID 前缀）
     * @return A namespaced resource location with the mod ID as the namespace
     *         以模组 ID 作为命名空间的命名空间资源位置
     * @see ResourceLocation
     * @see #MOD_ID
     * @since 1.0.0
     */
    public static ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
