package com.leaf.skiller.client;

import com.leaf.skiller.client.renderer.StrategyRenderers;
import com.leaf.skiller.client.tooltip.SkillTooltipHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Event handler class for client-side events related to the skill system.
 * 技能系统相关客户端事件的事件处理程序类。
 * <p>
 * This class uses the NeoForge event bus to subscribe to and handle various client-side events.
 * 此类使用NeoForge事件总线订阅和处理各种客户端事件。
 * It manages the following event types:
 * 它管理以下事件类型：
 * <ul>
 * <li>Keyboard input events for skill key bindings - 技能按键绑定的键盘输入事件</li>
 * <li>Item tooltip events for displaying skill information - 显示技能信息的物品工具提示事件</li>
 * <li>World rendering events for skill visual effects - 技能视觉效果的世界渲染事件</li>
 * </ul>
 *
 * @see EventBusSubscriber
 * @see ClientSkillCache
 * @see SkillTooltipHandler
 * @see StrategyRenderers
 * @since 1.0.0
 */
@EventBusSubscriber
public class ClientEvents {

    /**
     * Event handler for keyboard input events.
 * 键盘输入事件的事件处理程序。
     * <p>
     * This method is subscribed to the NeoForge input event bus and is called whenever
 * a keyboard key is pressed or released. It delegates the handling of skill key inputs
 * to the ClientSkillCache for processing.
 * 此方法订阅了NeoForge输入事件总线，每当键盘按键被按下或释放时都会调用。
 * 它将技能键输入的处理委托给ClientSkillCache进行处理。
     * </p>
     *
     * @param event The keyboard input event containing information about the key state - 包含按键状态信息的键盘输入事件
     * @see InputEvent.Key
     * @see ClientSkillCache#onKeyInput()
     * @see SubscribeEvent
     * @since 1.0.0
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        ClientSkillCache.onKeyInput();
    }

    /**
     * Event handler for item tooltip modification events.
 * 物品工具提示修改事件的事件处理程序。
     * <p>
     * This method is called whenever an item tooltip is being constructed. It checks if the
 * item has associated skill data and adds appropriate skill information to the tooltip.
 * 此方法每当物品工具提示被构建时都会调用。它检查物品是否有关联的技能数据，
 * 并将适当的技能信息添加到工具提示中。
     * </p>
     * <p>
     * When the Alt key is held, detailed skill information including key bindings and skill
 * levels is displayed. Otherwise, a brief hint is shown.
 * 按住Alt键时，会显示详细的技能信息，包括按键绑定和技能等级。
 * 否则，显示简短的提示。
     * </p>
     *
     * @param event The item tooltip event containing the item stack and tooltip list - 包含物品堆和工具提示列表的物品工具提示事件
     * @see ItemTooltipEvent
     * @see SkillTooltipHandler#addTooltip(ItemTooltipEvent)
     * @see SubscribeEvent
     * @since 1.0.0
     */
    @SubscribeEvent
    public static void modifyTooltip(ItemTooltipEvent event) {
        SkillTooltipHandler.addTooltip(event);
    }

    /**
     * Event handler for world rendering stage events.
 * 世界渲染阶段事件的事件处理程序。
     * <p>
     * This method is called during the world rendering pipeline to render skill-related visual
 * effects. It delegates the actual rendering work to the StrategyRenderers system, which
 * handles all active strategy skills that have visual effects.
 * 此方法在世界渲染管线期间被调用，以渲染技能相关的视觉效果。
 * 它将实际的渲染工作委托给StrategyRenderers系统，该系统处理所有具有视觉效果的活动策略技能。
     * </p>
     * <p>
     * Different rendering stages (translucent, solid, etc.) are handled by the StrategyRenderers
 * system to ensure proper visual layering.
 * 不同的渲染阶段（半透明、实体等）由StrategyRenderers系统处理，以确保正确的视觉分层。
     * </p>
     *
     * @param event The render level stage event containing rendering context information - 包含渲染上下文信息的渲染级别阶段事件
     * @see RenderLevelStageEvent
     * @see StrategyRenderers#render
     * @see SubscribeEvent
     * @since 1.0.0
     */
    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        StrategyRenderers.render(
                event,
                mc,
                mc.level,
                event.getPoseStack(),
                event.getCamera(),
                mc.renderBuffers().bufferSource()
        );
    }
}
