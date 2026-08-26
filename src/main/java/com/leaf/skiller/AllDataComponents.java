package com.leaf.skiller;

import com.leaf.skiller.content.skill.SkillComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.UnaryOperator;

/**
 * Central registry for all custom data components used by the Skiller mod.
 * Skiller 模组使用的所有自定义数据组件的中央注册表。
 * <p>
 * This class manages the registration of data components that can be attached
 * to items, entities, and other game objects. Data components are the modern
 * replacement for NBT tags in Minecraft 1.21+.
 * 此类管理可附加到物品、实体和其他游戏对象的数据组件的注册。
 * 数据组件是 Minecraft 1.21+ 中 NBT 标签的现代替代品。
 * </p>
 * <p>
 * All components registered here are automatically synchronized between
 * server and client when present on items in player inventories or the world.
 * 当玩家物品栏或世界中的物品存在时，此处注册的所有组件在服务器和
 * 客户端之间自动同步。
 * </p>
 *
 * @author Skiller Development Team
 * @author Skiller 开发团队
 * @version 1.0.0
 * @see DataComponentType
 * @see DeferredRegister
 * @see SkillComponent
 * @since 1.0.0
 */
public class AllDataComponents {
    /**
     * The deferred register for all Skiller data components.
     * 所有 Skiller 数据组件的延迟注册表。
     * <p>
     * Deferred registration allows components to be registered during the
     * appropriate registration event, ensuring proper integration with
     * Minecraft's registry system.
     * 延迟注册允许在适当的注册事件期间注册组件，确保与 Minecraft 的
     * 注册系统正确集成。
     * </p>
     */
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Skiller.MOD_ID);

    /**
     * The main skill component type that stores skill data on items.
     * 在物品上存储技能数据的主要技能组件类型。
     * <p>
     * This component can be attached to any item to give it skill-related
     * functionality. It includes codec support for persistent storage
     * (saved to disk) and stream codec support for network synchronization.
     * 此组件可以附加到任何物品以赋予其技能相关功能。它包含用于持久化
     * 存储（保存到磁盘）的编解码器支持和用于网络同步的流编解码器支持。
     * </p>
     * <p>
     * Items with this component can have skills that players can activate
     * using configured key bindings.
     * 带有此组件的物品可以拥有玩家可以使用配置的按键绑定激活的技能。
     * </p>
     */
    public static final DataComponentType<SkillComponent> SKILL_COMPONENT = register(
            "skill_component",
            builder -> builder
                    .persistent(SkillComponent.CODEC)
                    .networkSynchronized(SkillComponent.STREAM_CODEC)
    );

    /**
     * Registers a data component with the deferred register.
     * 使用延迟注册表注册数据组件。
     * <p>
     * This helper method encapsulates the boilerplate code needed to register
     * a data component. The builder parameter allows customization of the
     * component's behavior (persistence, network sync, etc.).
     * 此辅助方法封装了注册数据组件所需的样板代码。构建器参数允许
     * 自定义组件的行为（持久化、网络同步等）。
     * </p>
     *
     * @param <T> The type of data stored in the component
     *            组件中存储的数据类型
     * @param name The registry name for this component (without mod namespace)
     *             此组件的注册表名称（不带模组命名空间）
     * @param builder A function that configures the component builder
     *                配置组件构建器的函数
     * @return The registered data component type
     *         注册的数据组件类型
     * @see DataComponentType.Builder
     * @since 1.0.0
     */
    private static <T> DataComponentType<T> register(String name,
                                                     UnaryOperator<DataComponentType.Builder<T>> builder) {
        DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        DATA_COMPONENTS.register(name, () -> type);
        return type;
    }

    /**
     * Registers all data components with the mod event bus.
     * 向模组事件总线注册所有数据组件。
     * <p>
     * This method is called during mod initialization to hook into the
     * registration system. After this call, all components are available
     * for use in-game.
     * 此方法在模组初始化期间调用以挂钩到注册系统。
     * 此调用后，所有组件都可在游戏中使用。
     * </p>
     * <p>
     * <strong>Note:</strong> This method is marked as Internal and should
     * only be called by the main mod class during initialization.
     * <strong>注意：</strong>此方法标记为 Internal，应仅由主模组类在
     * 初始化期间调用。
     * </p>
     *
     * @param modEventBus The mod event bus to register components with
     *                    要向其注册组件的模组事件总线
     * @see IEventBus
     * @see DeferredRegister#register(IEventBus)
     * @since 1.0.0
     */
    @ApiStatus.Internal
    public static void register(IEventBus modEventBus) {
        DATA_COMPONENTS.register(modEventBus);
    }
}