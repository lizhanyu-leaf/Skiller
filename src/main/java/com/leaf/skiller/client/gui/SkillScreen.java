package com.leaf.skiller.client.gui;

import com.leaf.skiller.AllDataComponents;
import com.leaf.skiller.AllKeys;
import com.leaf.skiller.content.skill.SkillComponent;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.SkillType;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The skill screen: a tree of trigger types and their skills on the left
 * ({@code #select-list}), and the selected skill's info text on the right
 * ({@code #content-area}), driven by {@code assets/skiller/ui/skill.xml}.
 * 技能界面：左侧为触发类型与技能构成的树（{@code #select-list}），
 * 右侧为所选技能的详情（{@code #content-area}），
 * 由 {@code assets/skiller/ui/skill.xml} 驱动。
 * <p>
 * <b>Selection:</b> only skill children are selectable (skill-type parents are
 * headers). The selected entry is marked with {@code >>}; navigation works via
 * W/S, arrow keys, the space bar (down only) and the ▲/▼ controls at the
 * bottom of the select block.
 * <b>选中：</b>只有技能子节点可选中（触发类型是纯标题）。选中项以 {@code >>}
 * 标记；W/S、方向键、空格（仅向下）以及 Select 块底部的 ▲/▼ 控件均可导航。
 * </p>
 *
 * @see ModularUIScreen
 * @see com.leaf.skiller.client.tooltip.SkillTooltipHandler
 * @since 1.0.0
 * @author Leaf
 */
public class SkillScreen extends ModularUIScreen {

    private static final ResourceLocation UI_XML = ResourceLocation.parse("skiller:ui/skill.xml");
    private static final String INFO_KEY_SUFFIX = ".info";
    private static final String NO_INFO_KEY = "skiller.screen.no_info";
    private static final String SELECTED_MARK = ">> ";
    private static final String UNSELECTED_PAD = "   ";

    /** The held item whose skills are displayed. 展示其技能的手持物品 */
    private final ItemStack stack;
    /** The subtitle label from the xml, shown per selected skill. 副标题 label，随选中技能更新 */
    private UIElement subtitleLabel;

    /** Selectable entries in display order (flattened across type groups). 按显示顺序扁平化的可选中条目 */
    private final List<SkillEntry> entries = new ArrayList<>();
    /** Text elements per entry, for selection-mark refresh. 每个条目的文本元素，用于刷新选中标记 */
    private final Map<SkillEntry, TextElement> entryElements = new LinkedHashMap<>();
    private int selectedIndex = 0;

    private SkillScreen(ModularUI modularUI, ItemStack stack) {
        super(modularUI, Component.translatable("skiller.screen.title"));
        this.stack = stack;
    }

    /**
     * Loads the UI definition and opens the screen on the client.
     * 加载 UI 定义并在客户端打开界面。
     *
     * @param stack The held item whose skills should be displayed
     *              展示其技能的手持物品
     */
    public static void open(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        UI ui = loadUI();
        mc.setScreen(new SkillScreen(ModularUI.of(ui), stack));
    }

    /**
     * Parses {@code assets/skiller/ui/skill.xml} into a {@link UI}.
     * 将 {@code assets/skiller/ui/skill.xml} 解析为 {@link UI}。
     */
    private static UI loadUI() {
        try {
            var xml = XmlUtils.loadXml(UI_XML);
            if (xml != null) {
                return UI.of(xml);
            }
            throw new NullPointerException();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + UI_XML, e);
        }
    }

    @Override
    public void init() {
        super.init();
        subtitleLabel = modularUI.getElementById("subtitle-label");
        populate();
    }

    /**
     * Creates a text element whose height adapts to its content — without this,
     * stacked text elements in a column container would overlap.
     * 创建高度随内容自适应的文本元素——列容器中堆叠的文本元素
     * 若无自适应尺寸会互相重叠。
     */
    private static TextElement textElement(Component text) {
        return new TextElement()
                .setText(text)
                .textStyle(style -> style.adaptiveHeight(true).adaptiveWidth(true));
    }

    /**
     * Builds the type/skill tree into {@code #select-list}, wires the ▲/▼
     * navigation and selects the first entry.
     * 在 {@code #select-list} 中构建 类型/技能 树，接好 ▲/▼ 导航并选中第一个条目。
     */
    private void populate() {
        UIElement selectList = modularUI.getElementById("select-list");
        UIElement contentArea = modularUI.getElementById("content-area");
        // 清掉 xml 里的占位 "Content" label
        assert contentArea != null;
        for (UIElement child : List.copyOf(contentArea.getChildren())) {
            contentArea.removeChild(child);
        }
        entries.clear();
        entryElements.clear();
        selectedIndex = 0;

        SkillComponent component = stack.get(AllDataComponents.SKILL_COMPONENT);
        if (component == null || component.bindings().isEmpty()) {
            contentArea.addChild(textElement(Component.translatable(NO_INFO_KEY)
                    .withStyle(ChatFormatting.DARK_GRAY)));
            return;
        }

        // 按触发类型分组；LinkedHashMap 保持绑定顺序
        Map<SkillType, List<SkillEntry>> tree = new LinkedHashMap<>();
        for (var binding : component.bindings().entrySet()) {
            int keyIndex = binding.getKey();
            Component keyName = keyIndex >= 0 && keyIndex < AllKeys.SKILL_KEYS.length
                    ? AllKeys.SKILL_KEYS[keyIndex].getTranslatedKeyMessage()
                    : Component.literal("?" + keyIndex);
            for (ISkillInstance<?> instance : binding.getValue().getAllData()) {
                tree.computeIfAbsent(instance.skill().getType(), k -> new ArrayList<>())
                        .add(new SkillEntry(keyName, instance));
            }
        }

        for (var entry : tree.entrySet()) {
            SkillType type = entry.getKey();
            ResourceLocation typeId = type.getId();
            // 父节点：触发方式（纯标题，不可选中，不参与导航）
            assert selectList != null;
            selectList.addChild(textElement(Component.translatable(
                    "skillType." + typeId.getNamespace() + "." + typeId.getPath())
                    .withStyle(ChatFormatting.YELLOW)));
            // 子节点：[按键] 名称 等级（可选中）
            for (SkillEntry skillEntry : entry.getValue()) {
                TextElement child = textElement(entryText(skillEntry, false));
                child.addEventListener(UIEvents.CLICK,
                        event -> select(entries.indexOf(skillEntry)));
                selectList.addChild(child);
                entries.add(skillEntry);
                entryElements.put(skillEntry, child);
            }
        }

        wireNavControls(contentArea);

        // 默认选中第一个条目
        if (!entries.isEmpty()) {
            refreshSelection();
        }
    }

    /**
     * Wires the ▲/▼ controls and the position indicator at the bottom of the
     * select block.
     * 接好 Select 块底部的 ▲/▼ 控件与位置指示器。
     */
    private void wireNavControls(UIElement contentArea) {
        UIElement up = modularUI.getElementById("nav-up");
        UIElement down = modularUI.getElementById("nav-down");
        if (up != null) {
            up.addEventListener(UIEvents.CLICK, event -> moveSelection(-1));
        }
        if (down != null) {
            down.addEventListener(UIEvents.CLICK, event -> moveSelection(1));
        }
    }

    /**
     * Keyboard navigation: W/↑ move up, S/↓ and space move down — all identical
     * to the ▲/▼ controls.
     * 键盘导航：W/↑ 向上，S/↓ 与空格向下——与 ▲/▼ 控件效果一致。
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) return true;
        switch (keyCode) {
            case GLFW.GLFW_KEY_W, GLFW.GLFW_KEY_UP -> {
                moveSelection(-1);
                return true;
            }
            case GLFW.GLFW_KEY_S, GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_SPACE -> {
                moveSelection(1);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * Moves the selection by the given step, clamped to the entry range.
     * 按给定步长移动选中项，并限制在条目范围内。
     */
    private void moveSelection(int delta) {
        if (entries.isEmpty()) return;
        select(Mth.clamp(selectedIndex + delta, 0, entries.size() - 1));
    }

    /**
     * Selects the entry at the given index: refreshes the {@code >>} marks,
     * the position indicator and the info area.
     * 选中指定索引的条目：刷新 {@code >>} 标记、位置指示器与详情区。
     */
    private void select(int index) {
        if (index < 0 || index >= entries.size()) return;
        selectedIndex = index;
        refreshSelection();
    }

    /**
     * Refreshes every entry's {@code >>} mark, the position indicator and the
     * info area for the current selection.
     * 按当前选中刷新所有条目的 {@code >>} 标记、位置指示器与详情区。
     */
    private void refreshSelection() {
        UIElement contentArea = modularUI.getElementById("content-area");
        for (var elementEntry : entryElements.entrySet()) {
            int index = entries.indexOf(elementEntry.getKey());
            elementEntry.getValue().setText(entryText(elementEntry.getKey(), index == selectedIndex));
        }
        UIElement indicator = modularUI.getElementById("nav-indicator");
        if (indicator instanceof TextElement textElement && !entries.isEmpty()) {
            textElement.setText(Component.literal(
                    (selectedIndex + 1) + "/" + entries.size()));
        }
        if (!entries.isEmpty()) {
            assert contentArea != null;
            showInfo(contentArea, entries.get(selectedIndex));
        }
    }

    /**
     * Builds one entry's display text: {@code >>} prefix when selected,
     * blank padding otherwise, followed by {@code [key] name level}.
     * 构建条目显示文本：选中时前缀 {@code >>}，否则空白对齐，
     * 后接 {@code [按键] 名称 等级}。
     */
    private static Component entryText(SkillEntry entry, boolean selected) {
        ResourceLocation id = entry.instance().skill().getId();
        Component name = Component.translatable(
                "skill." + id.getNamespace() + "." + id.getPath());
        Component level = Component.translatable(
                "enchantment.level." + entry.instance().level());

        var prefix = Component.literal(selected ? SELECTED_MARK : UNSELECTED_PAD)
                .withStyle(selected ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY);
        return prefix
                .append("[").append(entry.keyName()).append("] ")
                .append(name).append(" ")
                .append(level);
    }

    /**
     * Shows {@code skill.<modid>.<skillid>.info} in the content area when the
     * translation exists; {@code \n} splits the text into one element per line.
     * 若翻译键存在，在内容区显示 {@code skill.<modid>.<skillid>.info}；
     * 翻译中的 {@code \n} 拆分为逐行的元素。
     */
    private void showInfo(UIElement contentArea, SkillEntry entry) {
        for (UIElement child : List.copyOf(contentArea.getChildren())) {
            contentArea.removeChild(child);
        }

        ResourceLocation id = entry.instance().skill().getId();

        // 副标题：skill.<modid>.<skillid>.subtitle 存在则显示，否则禁用隐藏
        if (subtitleLabel != null) {
            String subtitleKey = "skill." + id.getNamespace() + "." + id.getPath() + ".subtitle";
            if (I18n.exists(subtitleKey)) {
                subtitleLabel.setDisplay(true);
                if (subtitleLabel instanceof TextElement textElement) {
                    textElement.setText(Component.translatable(subtitleKey));
                }
            } else {
                subtitleLabel.setDisplay(false);
            }
        }

        String key = "skill." + id.getNamespace() + "." + id.getPath() + INFO_KEY_SUFFIX;
        if (!I18n.exists(key)) {
            contentArea.addChild(textElement(Component.translatable(NO_INFO_KEY)
                    .withStyle(ChatFormatting.DARK_GRAY)));
            return;
        }

        String text = Component.translatable(key).getString();
        for (String line : text.split("\n", -1)) {
            contentArea.addChild(textElement(Component.literal(line)));
        }
    }

    /**
     * One selectable skill instance with the key it is bound to.
     * 一个可选中的技能实例及其绑定的按键。
     */
    private record SkillEntry(Component keyName, ISkillInstance<?> instance) {}
}
