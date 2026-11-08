package com.leaf.skiller.client.tooltip;

import com.leaf.skiller.AllDataComponents;
import com.leaf.skiller.AllKeys;
import com.leaf.skiller.content.skill.SkillComponent;
import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.SkillBundle;
import com.leaf.skiller.foundation.skill.config.SkillContextEnvironment;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handler for adding skill-related information to item tooltips.
 * 用于向物品工具提示添加技能相关信息的处理程序。
 * <p>
 * This class provides functionality to inspect items and augment their tooltips with skill data.
 * When an item has associated skills, this handler adds appropriate information to help players
 * understand which skills are bound to which keys.
 * 此类提供检查物品并使用技能数据增强其工具提示的功能。
 * 当物品具有关联的技能时，此处理程序会添加适当的信息以帮助玩家了解哪些技能绑定到哪些键。
 * </p>
 * <p>
 * Tooltip behavior varies based on keyboard input:
 * 工具提示行为因键盘输入而异：
 * <ul>
 * <li>Without Alt key: Shows a brief hint that skill information is available
 * - 不按Alt键：显示简短提示，表明有技能信息可用</li>
 * <li>With Alt key: Displays detailed skill information including key bindings, names, levels, and types
 * - 按住Alt键：显示详细的技能信息，包括按键绑定、名称、等级和类型</li>
 * <li>With Alt key and Up/Down arrows: Moves a ">>" cursor between skill entries; the detail of
 *       the selected skill (with its parameters) is shown below the whole list
 * - 按住Alt键并按上/下方向键：在技能条目之间移动">>"光标，所选技能的详情（含参数）显示在整个列表下方</li>
 * </ul>
 *
 * @see AllDataComponents#SKILL_COMPONENT
 * @see ISkillInstance
 * @see SkillComponent
 * @since 1.0.0
 */
public class SkillTooltipHandler {

    /**
     * Color of informational lines: the Alt hint, the detail line and parameter lines.
     * 信息行（Alt提示、详情行、参数行）的颜色。
     */
    private static final ChatFormatting INFO_COLOR = ChatFormatting.GRAY;

    /**
     * Color of the ">>" cursor marking the selected skill entry.
     * ">>"光标（标记选中技能条目）的颜色。
     */
    private static final ChatFormatting CURSOR_COLOR = ChatFormatting.YELLOW;

    /**
     * Color of segments wrapped in {@code _xxx_} markers inside descriptions.
     * 描述中被{@code _xxx_}标记包裹片段的颜色。
     */
    private static final ChatFormatting HIGHLIGHT_COLOR = ChatFormatting.WHITE;

    /**
     * Index of the skill entry the ">>" cursor currently points to.
     * Persists across frames because {@link ItemTooltipEvent} fires every frame while hovering.
     * ">>"光标当前指向的技能条目索引。
     * 由于悬停时{@link ItemTooltipEvent}每帧都会触发，该索引需跨帧保存。
     */
    private static int cursorIndex = 0;

    /**
     * Up/Down key states from the previous frame, used for edge detection so that each physical
     * key press moves the cursor exactly one entry instead of scrolling every frame.
     * 上一帧的上/下方向键状态，用于边沿检测，使每次物理按键只移动一格光标，而不是每帧连续滚动。
     */
    private static boolean prevUp = false;
    private static boolean prevDown = false;

    /**
     * The item stack hovered on the previous frame. The cursor resets to the first entry
     * when the player moves the mouse onto a different item.
     * 上一帧悬停的物品堆。当玩家将鼠标移到其他物品上时，光标会重置到第一个条目。
     */
    private static ItemStack lastStack = null;

    /**
     * Adds skill information to the item tooltip based on the Alt key state.
     * 根据Alt键状态向物品工具提示添加技能信息。
     * <p>
     * This method examines the item stack to determine if it has associated skill data.
     * If no skill component is present, the method returns without modifying the tooltip.
     * 此方法检查物品堆以确定其是否具有关联的技能数据。
     * 如果不存在技能组件，则该方法返回而不修改工具提示。
     * </p>
     * <p>
     * Tooltip behavior:
     * 工具提示行为：
     * <ul>
     * <li><b>Alt key not held:</b> Adds a single gray line prompting the player to hold Alt for details;
     *       also resets the cursor state
     * - <b>未按住Alt键：</b>添加单行灰色文本，提示玩家按住Alt键查看详细信息；同时重置光标状态</li>
     * <li><b>Alt key held:</b> Adds a header line and lists all skills with their key bindings,
     *       names, levels, and types in a formatted manner, the selected one marked with the
     *       ">>" cursor (movable with Up/Down arrows); the selected skill's detail block is
     *       appended below the whole list
     * - <b>按住Alt键：</b>添加标题行并以格式化方式列出所有技能及其按键绑定、名称、等级和类型，
     *       被选中的条目带">>"光标标记（可用上/下方向键移动）；所选技能的详情块追加在整个列表下方</li>
     * </ul>
     * </p>
     *
     * @param event The item tooltip event containing the item stack and tooltip list to modify - 包含要修改的物品堆和工具提示列表的物品工具提示事件
     * @see ItemTooltipEvent
     * @see ItemStack
     * @see SkillComponent
     * @see AllDataComponents#SKILL_COMPONENT
     * @see Screen#hasAltDown()
     * @see #fromSkill(Component, Component, ISkillInstance)
     * @see #fromDetail(ISkillInstance)
     * @since 1.0.0
     */
    public static void addTooltip(ItemTooltipEvent event) {
        // Track arrow-key edges EVERY frame — even when not hovering a skill item (the method
        // early-returns below) — otherwise the stale previous-frame state swallows the first
        // press made after hovering something else.
        // 每帧都跟踪方向键边沿——即使没有悬停技能物品（下方会提前返回）——
        // 否则过期的上一帧状态会吞掉悬停其他物品之后做出的第一次按键。
        long window = Minecraft.getInstance().getWindow().getWindow();
        boolean up = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_UP);
        boolean down = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_DOWN);
        boolean upPressed = up && !prevUp;
        boolean downPressed = down && !prevDown;
        prevUp = up;
        prevDown = down;

        ItemStack stack = event.getItemStack();
        SkillComponent component = stack.get(AllDataComponents.SKILL_COMPONENT);
        if (component == null) return;

        // Show brief hint when Alt is not held (and reset the cursor state).
        // 未按住Alt键时显示简短提示（同时重置光标状态）。
        if (!Screen.hasAltDown()) {
            cursorIndex = 0;
            lastStack = null;
            event.getToolTip().add(
                    1,
                    Component.translatable("tooltip.skiller.skill_tips")
                            .withStyle(INFO_COLOR)
            );
            return;
        }

        // Collect all (key, skill) entries first so the cursor knows the list bounds.
        // Sorted by slot index because the bindings map is a HashMap with unstable order.
        // 先收集所有（按键, 技能）条目，让光标知道列表边界。
        // 由于bindings是HashMap顺序不稳定，需按槽位索引排序。
        record SkillEntry(Component key, ISkillInstance<?> skill) {}
        List<Map.Entry<Integer, SkillBundle>> sorted = new ArrayList<>(component.bindings().entrySet());
        sorted.sort(Map.Entry.comparingByKey());
        List<SkillEntry> entries = new ArrayList<>();
        for (Map.Entry<Integer, SkillBundle> entry : sorted) {
            int i = entry.getKey();
            // Guard against binding keys outside the registered key array.
            // 防止绑定键超出已注册按键数组的范围。
            if (i < 0 || i >= AllKeys.SKILL_KEYS.length) continue;
            KeyMapping key = AllKeys.SKILL_KEYS[i];
            for (ISkillInstance<?> skill : entry.getValue().getAllData())
                entries.add(new SkillEntry(key.getTranslatedKeyMessage(), skill));
        }

        // Reset the cursor when a different item is hovered.
        // 悬停其他物品时重置光标。
        if (stack != lastStack) {
            cursorIndex = 0;
            lastStack = stack;
        }

        // Move the cursor one entry per physical key press (edges computed above).
        // 每次物理按键移动一格光标（边沿已在上方计算）。
        if (upPressed) cursorIndex--;
        if (downPressed) cursorIndex++;

        // Clamp the cursor into bounds (also covers an empty or shrunken skill list).
        // 将光标限制在有效范围内（同时处理空列表或列表缩短的情况）。
        cursorIndex = Mth.clamp(cursorIndex, 0, entries.size() - 1);

        event.getToolTip().add(
                1,
                Component.translatable("tooltip.skiller.skill")
        );
        int index = 2;
        // Iterate through all skill entries; the selected one gets the ">>" cursor prefix.
        // 遍历所有技能条目；被选中的条目带">>"光标前缀。
        for (int e = 0; e < entries.size(); e++) {
            SkillEntry entry = entries.get(e);
            boolean selected = e == cursorIndex;
            event.getToolTip().add(index++,
                    fromSkill(
                            selected
                                    ? Component.literal(">> ").withStyle(CURSOR_COLOR)
                                    : Component.literal("   "),
                            entry.key(),
                            entry.skill()
                    ));
        }
        // The detail always sits below the whole skill list, not below the selected entry.
        // 详情固定显示在整个技能列表下方，而不是所选条目下方。
        if (!entries.isEmpty()) {
            event.getToolTip().add(index++,
                    fromDetail(entries.get(cursorIndex).skill()).withStyle(INFO_COLOR));
            // Parameters are only shown while the detail is shown, right below it.
            // 参数只在显示详情时、紧跟详情下方显示。
            index = addParameters(event.getToolTip(), index, entries.get(cursorIndex).skill());
        }
    }

    /**
     * Creates a formatted text component displaying skill information.
     * 创建显示技能信息的格式化文本组件。
     * <p>
     * This method constructs a rich text component that displays a skill in the following format:
     * 此方法构建一个丰富的文本组件，按以下格式显示技能：
     * </p>
     * <pre>
     *   [Key] Skill Name Level - Skill Type
     *   [键] 技能名称 等级 - 技能类型
     * </pre>
     * <p>
     * The format includes:
     * 格式包括：
     * <ul>
     * <li>A leading prefix segment, used as either an indent or the ">>" cursor
     * - 前导前缀，用作缩进或">>"光标</li>
     * <li>Key binding in brackets (e.g., [K])
     * - 方括号中的按键绑定（例如 [K]）</li>
     * <li>Localized skill name (e.g., "Fireball")
     * - 本地化的技能名称（例如 "Fireball"）</li>
     * <li>Skill level as an enchantment-style number (e.g., I, II, III)
     * - 附魔样式的技能等级数字（例如 I、II、III）</li>
     * <li>Localized skill type (e.g., "Active", "Passive")
     * - 本地化的技能类型（例如 "Active"、"Passive"）</li>
     * </ul>
     * </p>
     * <p>
     * All text is localized using translation keys based on the skill and type IDs.
     * 所有文本都使用基于技能和类型ID的翻译键进行本地化。
     * </p>
     *
     * @param prefix The leading prefix segment, e.g. an indent or the ">>" cursor - 前导前缀，例如缩进或">>"光标
     * @param key The key binding component to display (e.g., the key name) - 要显示的按键绑定组件（例如键名）
     * @param skill The skill instance containing the skill data to display - 包含要显示的技能数据的技能实例
     * @return A formatted text component displaying the skill information with prefix, key, name, level, and type - 显示技能信息（包括前缀、键、名称、等级和类型）的格式化文本组件
     * @see ISkillInstance
     * @see Component
     * @see ResourceLocation
     * @since 1.0.0
     */
    public static Component fromSkill(Component prefix, Component key, ISkillInstance<?> skill) {
        ResourceLocation id = skill.skill().getId();
        ResourceLocation typeId = skill.skill().getType().getId();
        return Component.literal("")
                .append(prefix)
                .append("[")
                .append(key)
                .append("] ")
                .append(Component.translatable("skill." + id.getNamespace() + "." + id.getPath()))
                .append(" ")
                .append(Component.translatable("enchantment.level." + skill.level()))
                .append(" - ")
                .append(Component.translatable("skillType." + typeId.getNamespace() + "." + typeId.getPath()));
    }

    public static boolean hasDesc(ISkillInstance<?> skill) {
        ResourceLocation id = skill.skill().getId();
        String descKey = "skill." + id.getNamespace() + "." + id.getPath() + ".desc";
        return Language.getInstance().has(descKey);
    }

    /**
     * Creates a formatted text component displaying the detail line of a skill.
     * 创建显示技能详情行的格式化文本组件。
     * <p>
     * The detail content is resolved from the translation key {@code skill.<namespace>.<path>.desc},
     * derived from the skill's ID. If that key has no translation value, the line falls back to
     * the {@code tooltip.skiller.detail_none} placeholder.
     * 详情内容由技能ID派生的翻译键{@code skill.<namespace>.<path>.desc}解析。
     * 如果该键没有翻译值，则回退显示{@code tooltip.skiller.detail_none}占位文本。
     * </p>
     * <p>
     * In the description value, segments wrapped in underscores ({@code _xxx_}) are rendered in
     * {@link #HIGHLIGHT_COLOR} via {@link #fromMarkedText(String)}.
     * 描述值中被下划线包裹的片段（{@code _xxx_}）会经{@link #fromMarkedText(String)}
     * 以{@link #HIGHLIGHT_COLOR}颜色渲染。
     * </p>
     * <pre>
     *   Details: The skill's description - 详情：技能的描述内容
     *   Details: None (when untranslated) - 详情：暂无（未翻译时）
     * </pre>
     *
     * @param skill The skill instance whose detail is to be displayed - 要显示详情的技能实例
     * @return A formatted text component containing the "Details:" title and its content - 包含"详情："标题及其内容的格式化文本组件
     * @see #hasDesc(ISkillInstance)
     * @see #fromMarkedText(String)
     * @see #fromSkill(Component, Component, ISkillInstance)
     * @since 1.0.0
     */
    public static MutableComponent fromDetail(ISkillInstance<?> skill) {
        ResourceLocation id = skill.skill().getId();
        String descKey = "skill." + id.getNamespace() + "." + id.getPath() + ".desc";
        // Use the description translation when present; otherwise show the "none" placeholder.
        // 有描述翻译时使用之；否则显示"暂无"占位文本。
        // The translation value is resolved here so that _xxx_ markers can be styled.
        // 在此处解析翻译值，以便渲染_xxx_标记。
        Component content = hasDesc(skill)
                ? Component.literal("\n").append(fromMarkedText(Language.getInstance().getOrDefault(descKey)))
                : Component.translatable("tooltip.skiller.detail_none");
        return Component.translatable("tooltip.skiller.detail", content);
    }

    /**
     * Converts marked text into a styled component: segments wrapped in underscores
     * ({@code _xxx_}) are rendered in {@link #HIGHLIGHT_COLOR}, while the rest keeps the
     * style inherited from its parent component.
     * 将带标记的文本转换为带样式组件：被下划线包裹的片段（{@code _xxx_}）以{@link #HIGHLIGHT_COLOR}渲染，
     * 其余部分继承父组件的样式。
     * <p>
     * Underscores must come in pairs around a segment. A single unmatched underscore
     * (including the segment after it) is kept literally, so ordinary prose containing
     * underscores still renders unchanged.
     * 下划线必须成对包裹片段。单个未成对的下划线（连同其后的内容）按原样保留，
     * 因此含下划线的普通文本不会受影响。
     * </p>
     *
     * @param text The raw (already translated) text possibly containing {@code _xxx_} markers - 原始（已翻译）文本，可能含{@code _xxx_}标记
     * @return A component with the marked segments highlighted - 标记片段已高亮的组件
     * @see #HIGHLIGHT_COLOR
     * @since 1.0.0
     */
    public static MutableComponent fromMarkedText(String text) {
        MutableComponent result = Component.empty();
        int start = 0;
        // Split on underscore pairs; an unmatched trailing underscore stays literal.
        // 按成对下划线切分；未成对的下划线按原样保留。
        while (start < text.length()) {
            int open = text.indexOf('_', start);
            if (open < 0) {
                result.append(text.substring(start));
                break;
            }
            int close = text.indexOf('_', open + 1);
            if (close < 0) {
                result.append(text.substring(start));
                break;
            }
            // 先追加标记之前的普通片段，否则非_xxx_文本会全部丢失。
            if (open > start) result.append(text.substring(start, open));
            result.append(Component.literal(text.substring(open + 1, close))
                    .withStyle(HIGHLIGHT_COLOR));
            start = close + 1;
        }
        return result;
    }

    /**
     * Appends the skill's parameter lines below the detail line, e.g. "范围: 3".
     * 在详情行下方追加技能的参数行，例如"范围: 3"。
     * <p>
     * A fresh context is built through the skill's context factory (with an
     * event-less environment, the same way the client-side preview does it), and
     * each {@code parameters()} entry renders as one line: the snake_case key is
     * translated via {@code config.skiller.<key>} when a translation exists,
     * otherwise the raw key is shown.
     * 通过技能自己的上下文工厂构建一份新上下文（无事件环境，与客户端预览相同），
     * {@code parameters()} 的每一项渲染为一行：蛇形键在存在翻译时经
     * {@code config.skiller.<key>} 翻译，否则显示原始键名。
     * </p>
     *
     * @param tooltip The tooltip list to append to / 要追加的tooltip列表
     * @param index   The insert position / 插入位置
     * @param skill   The selected skill instance / 被选中的技能实例
     * @param <T>     The skill context type / 技能上下文类型
     * @return The next free insert position / 下一个可插入位置
     * @see SkillContext#parameters()
     * @since 1.0.0
     */
    private static <T extends SkillContext> int addParameters(List<Component> tooltip, int index, ISkillInstance<T> skill) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return index;
        SkillContext context = skill.skill().getFactory()
                .create(SkillContextEnvironment.noEvent(mc.player, mc.level), skill);
        if (context == null) return index;
        for (Pair<String, Object> parameter : context.parameters()) {
            tooltip.add(index++, parameterLine(parameter).withStyle(INFO_COLOR));
        }
        return index;
    }

    /**
     * Renders one parameter as a "name: value" component; integral doubles are
     * displayed without a trailing ".0" (3.0 → "3").
     * 将单个参数渲染为"名称: 值"组件；整数值的 double 不带".0"显示（3.0 → "3"）。
     *
     * @param parameter The parameter key-value pair / 参数键值对
     * @return The rendered line component / 渲染出的行组件
     * @see #addParameters(List, int, ISkillInstance)
     * @since 1.0.0
     */
    private static MutableComponent parameterLine(Pair<String, Object> parameter) {
        String name = parameter.getFirst();
        String translationKey = "config.skiller." + name;
        Component nameComponent = Language.getInstance().has(translationKey)
                ? Component.translatable(translationKey)
                : Component.literal(name);
        Object value = parameter.getSecond();
        String valueText;
        if (value instanceof Double d && !d.isInfinite() && !d.isNaN() && d == Math.floor(d)) {
            valueText = String.valueOf(d.longValue());
        } else {
            valueText = String.valueOf(value);
        }
        return Component.literal("")
                .append(nameComponent)
                .append(": ")
                .append(Component.literal(valueText));
    }
}
