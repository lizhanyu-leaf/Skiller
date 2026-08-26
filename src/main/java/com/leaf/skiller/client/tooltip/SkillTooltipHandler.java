package com.leaf.skiller.client.tooltip;

import com.leaf.skiller.AllDataComponents;
import com.leaf.skiller.AllKeys;
import com.leaf.skiller.content.skill.SkillComponent;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

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
 * </ul>
 *
 * @see AllDataComponents#SKILL_COMPONENT
 * @see ISkillInstance
 * @see SkillComponent
 * @since 1.0.0
 */
public class SkillTooltipHandler {

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
     * <li><b>Alt key not held:</b> Adds a single gray line prompting the player to hold Alt for details
 * - <b>未按住Alt键：</b>添加单行灰色文本，提示玩家按住Alt键查看详细信息</li>
     * <li><b>Alt key held:</b> Adds a header line and lists all skills with their key bindings,
 *       names, levels, and types in a formatted manner
 * - <b>按住Alt键：</b>添加标题行并以格式化方式列出所有技能及其按键绑定、名称、等级和类型</li>
     * </ul>
     * </p>
     *
     * @param event The item tooltip event containing the item stack and tooltip list to modify - 包含要修改的物品堆和工具提示列表的物品工具提示事件
     * @see ItemTooltipEvent
     * @see ItemStack
     * @see SkillComponent
     * @see AllDataComponents#SKILL_COMPONENT
     * @see Screen#hasAltDown()
     * @see #fromSkill(Component, ISkillInstance)
     * @since 1.0.0
     */
    public static void addTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        SkillComponent component = stack.get(AllDataComponents.SKILL_COMPONENT);
        if (component == null) return;

        // Show brief hint when Alt is not held
        // 未按住Alt键时显示简短提示
        if (!Screen.hasAltDown()) {
            event.getToolTip().add(
                    1,
                    Component.translatable("tooltip.skiller.skill_tips")
                            .withStyle(ChatFormatting.GRAY)
            );
        }
        // Show detailed skill information when Alt is held
        // 按住Alt键时显示详细技能信息
        else {
            event.getToolTip().add(
                    1,
                    Component.translatable("tooltip.skiller.skill")
            );
            int index = 1;
            // Iterate through all skill bindings and add them to the tooltip
            // 遍历所有技能绑定并将它们添加到工具提示中
            for (int i : component.bindings().keySet()) {
                KeyMapping key = AllKeys.SKILL_KEYS[i];
                for (ISkillInstance<?> skill : component.bindings().get(i).getAllData())
                    event.getToolTip().add(index++,
                            fromSkill(key.getTranslatedKeyMessage(), skill));
            }
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
     * @param key The key binding component to display (e.g., the key name) - 要显示的按键绑定组件（例如键名）
     * @param skill The skill instance containing the skill data to display - 包含要显示的技能数据的技能实例
     * @return A formatted text component displaying the skill information with key, name, level, and type - 显示技能信息（包括键、名称、等级和类型）的格式化文本组件
     * @see ISkillInstance
     * @see Component
     * @see ResourceLocation
     * @since 1.0.0
     */
    public static Component fromSkill(Component key, ISkillInstance<?> skill) {
        ResourceLocation id = skill.skill().getId();
        ResourceLocation typeId = skill.skill().getType().getId();
        return Component.literal("  [")
                .append(key)
                .append("] ")
                .append(Component.translatable("skill." + id.getNamespace() + "." + id.getPath()))
                .append(" ")
                .append(Component.translatable("enchantment.level." + skill.level()))
                .append(" - ")
                .append(Component.translatable("skillType." + typeId.getNamespace() + "." + typeId.getPath()));
    }
}
