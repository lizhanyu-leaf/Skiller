package com.leaf.skiller.client.tooltip;

import com.leaf.skiller.AllDataComponents;
import com.leaf.skiller.content.skill.SkillComponent;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SkillTooltipHandler {

    public static boolean enable = true;

    // ========== 替代 LerpedFloat ==========
    static float holdKeyProgress = 0;
    static float prevHoldKeyProgress = 0;

    static ItemStack hoveredStack = ItemStack.EMPTY;
    static ItemStack trackingStack = ItemStack.EMPTY;
    static boolean deferTick = false;

    static final List<Consumer<ItemStack>> hoveredStackCallbacks = new ArrayList<>();

    public static final String HOLD_TO_SHOW_SKILL = "skiller.tooltip.hold_to_show_skill";

    // ========== Tick ==========

    public static void tick() {
        deferTick = true;
    }

    public static void deferredTick() {
        deferTick = false;
        Minecraft mc = Minecraft.getInstance();

        if (hoveredStack.isEmpty() || trackingStack.isEmpty()) {
            trackingStack = ItemStack.EMPTY;
            holdKeyProgress = 0;
            prevHoldKeyProgress = 0;
            return;
        }

        // 只判断 Alt：tooltip 本身只在屏幕打开时存在（mc.screen != null），
        // 旧移植代码里的 currentScreen == null 条件与该前提矛盾，导致进度
        // 永远走衰减分支、进度条永远不出现。
        // Only gate on Alt: a tooltip only exists while a screen is open
        // (mc.screen != null), so the ported currentScreen == null condition
        // contradicted its own premise and kept the progress at zero forever.
        if (RenderSystem.isOnRenderThread() && isAltDown()) {
            if (holdKeyProgress >= 1) {
                // 进度满 → 打开技能界面
//                com.leaf.skiller.client.gui.SkillScreen.open(trackingStack);
                holdKeyProgress = 0;
                prevHoldKeyProgress = 0;
                return;
            }
            prevHoldKeyProgress = holdKeyProgress;
            holdKeyProgress = Math.min(1,
                    holdKeyProgress + Math.max(.25f, holdKeyProgress) * .25f);
        } else {
            prevHoldKeyProgress = holdKeyProgress;
            holdKeyProgress = Math.max(0, holdKeyProgress - .05f);
        }

        hoveredStack = ItemStack.EMPTY;
    }

    private static boolean isAltDown() {
        return Screen.hasAltDown();
    }

    // ========== Tooltip 注入 ==========

    public static void addToTooltip(List<Component> toolTip, ItemStack stack) {
        if (!enable) return;

        updateHovered(stack);

        if (deferTick) deferredTick();

        // 内容比较而非引用比较：部分 GUI 每帧重建 stack 实例，
        // 引用比较会导致进度条时有时无。
        // Compare by content, not identity: some screens rebuild the stack
        // instance every frame, which would flicker the progress bar.
        if (!ItemStack.isSameItemSameComponents(trackingStack, stack)) return;

        // 替代 AnimationTickHolder.getPartialTicksUI()
        float partialTicks = Minecraft.getInstance()
                .getTimer()
                .getGameTimeDeltaPartialTick(true);

        // 替代 LerpedFloat.getValue(partialTicks)
        float smoothProgress = Mth.lerp(partialTicks, prevHoldKeyProgress, holdKeyProgress);

        Component component = makeProgressBar(Math.min(1, smoothProgress * 8 / 7f));

        if (toolTip.size() < 2) toolTip.add(component);
        else toolTip.add(1, component);
    }

    protected static void updateHovered(ItemStack stack) {
        ItemStack prevStack = trackingStack;
        hoveredStack = ItemStack.EMPTY;

        if (stack.isEmpty()) return;

        SkillComponent component = stack.get(AllDataComponents.SKILL_COMPONENT);
        if (component == null || component.bindings() == null || component.bindings().isEmpty()) return;

        if (prevStack.isEmpty() || !prevStack.is(stack.getItem())) {
            holdKeyProgress = 0;
            prevHoldKeyProgress = 0;
        }

        hoveredStack = stack;
        trackingStack = stack;

        for (Consumer<ItemStack> callback : hoveredStackCallbacks)
            callback.accept(hoveredStack.copy());
    }

    // ========== 进度条 ==========

    private static Component makeProgressBar(float progress) {
        MutableComponent holdAlt = Component.translatable(HOLD_TO_SHOW_SKILL,
                        Component.literal("Alt").withStyle(ChatFormatting.GRAY))
                .withStyle(ChatFormatting.DARK_GRAY);

        Font font = Minecraft.getInstance().font;
        float charWidth = font.width("|");
        float tipWidth = font.width(holdAlt);

        int total = (int) (tipWidth / charWidth);
        int current = (int) (progress * total);

        if (progress > 0) {
            StringBuilder bars = new StringBuilder();
            bars.append(ChatFormatting.GRAY).append("|".repeat(current));
            if (progress < 1)
                bars.append(ChatFormatting.DARK_GRAY).append("|".repeat(total - current));
            return Component.literal(bars.toString());
        }

        return holdAlt;
    }

    // ========== 回调 ==========

    public synchronized static void registerHoveredStackCallback(Consumer<ItemStack> consumer) {
        hoveredStackCallbacks.add(consumer);
    }

    public synchronized static void removeHoveredStackCallback(Consumer<ItemStack> consumer) {
        hoveredStackCallbacks.remove(consumer);
    }
}