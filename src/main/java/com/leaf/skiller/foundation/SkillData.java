package com.leaf.skiller.foundation;

import com.leaf.skiller.Skiller;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * A record containing all data needed to represent and recreate a skill instance.
 * 包含表示和重新创建技能实例所需的所有数据的记录。
 * <p>
 * SkillData serves as the serialized representation of skill instances,
 * containing the skill ID, factory ID, and all instance-specific NBT data.
 * SkillData作为技能实例的序列化表示，包含技能ID、工厂ID和所有实例特定的NBT数据。
 * </p>
 * <p>
 * This record is used for persistent storage and network transmission of
 * skill instances, enabling save/load functionality and client-server sync.
 * 此记录用于技能实例的持久存储和网络传输，支持保存/加载功能和客户端-服务器同步。
 * </p>
 * <p>
 * The format consists of a skill identifier, the factory identifier for
 * recreation, and the NBT compound tag containing all instance data.
 * 格式包括技能标识符、用于重新创建的工厂标识符，以及包含所有实例数据的NBT复合标签。
 * </p>
 *
 * @param skillId   The unique identifier of the skill this data represents
 *                  此数据表示的技能的唯一标识符
 * @param factoryId The identifier of the factory that can recreate this skill instance
 *                  可以重新创建此技能实例的工厂的标识符
 * @param nbt       The NBT data containing all instance-specific information
 *                  包含所有实例特定信息的NBT数据
 *
 * @since 1.0.0
 * @see ISkillInstance
 * @see SkillInstanceFactory
 * @see CompoundTag
 */
public record SkillData(ResourceLocation skillId, ResourceLocation factoryId, CompoundTag nbt) {

    /**
     * Parses a SkillData from its string representation.
     * 从其字符串表示解析SkillData。
     * <p>
     * The expected format is: "skillId{nbtData}" where nbtData must contain
     * a "Factory" string field.
     * 预期格式为："skillId{nbtData}"，其中nbtData必须包含"Factory"字符串字段。
     * </p>
     * <p>
     * This method is used for deserializing skill data from storage or network.
     * 此方法用于从存储或网络反序列化技能数据。
     * </p>
     * <p>
     * Returns null if the input is invalid or parsing fails.
     * 如果输入无效或解析失败，则返回null。
     * </p>
     *
     * @param input The string representation to parse
     *              要解析的字符串表示
     *
     * @return A SkillData parsed from the input, or null if parsing fails
     *         从输入解析的SkillData；如果解析失败则返回null
     *
     * @since 1.0.0
     * @see #toString()
     * @see ISkillInstance#fromData(SkillData)
     */
    public static SkillData fromString(String input) {
        int brace = input.indexOf('{');
        if (brace == -1) return null;

        String skillId = input.substring(0, brace);
        String nbtStr = input.substring(brace);

        try {
            CompoundTag tag = TagParser.parseTag(nbtStr);
            ResourceLocation factory = ResourceLocation.parse(tag.getString("Factory"));
            return new SkillData(ResourceLocation.parse(skillId), factory, tag);
        } catch (Exception e) {
            Skiller.LOGGER.error("Failed to parse skill data: {}", input, e);
            return null;
        }
    }

    /**
     * Returns the string representation of this skill data.
     * 返回此技能数据的字符串表示。
     * <p>
     * The format is: "skillId{nbtData}" where nbtData includes all instance
     * information including the factory identifier.
     * 格式为："skillId{nbtData}"，其中nbtData包括所有实例信息，包括工厂标识符。
     * </p>
     * <p>
     * This representation is suitable for storage and transmission.
     * 此表示形式适用于存储和传输。
     * </p>
     *
     * @return A string that can be parsed back into SkillData via fromString
     *         可以通过fromString解析回SkillData的字符串
     *
     * @since 1.0.0
     * @see #fromString(String)
     */
    @Override
    public @NotNull String toString() {
        return skillId.toString() + nbt.toString();
    }
}
