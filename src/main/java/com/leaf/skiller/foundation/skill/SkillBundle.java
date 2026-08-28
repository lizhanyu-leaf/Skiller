package com.leaf.skiller.foundation.skill;

import com.leaf.skiller.foundation.OwnedBySkills;
import com.leaf.skiller.foundation.SkillData;
import com.leaf.skiller.foundation.SkillResource;
import com.leaf.skiller.foundation.context.SkillContext;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A container that bundles multiple skills together for management and execution.
 * 技能容器，将多个技能捆绑在一起进行管理和执行
 * <p>
 * SkillBundle provides a centralized way to manage multiple skills of different types,
 * supporting serialization, network transmission, and resource consumption.
 * SkillBundle 提供了一种集中管理多种不同类型技能的方式，支持序列化、网络传输和资源消耗
 * </p>
 *
 * @see OwnedBySkills
 * @see SkillType
 * @see ItemSkill
 * @see ISkillInstance
 * @since 1.0.0
 * @author Leaf
 */
public class SkillBundle implements OwnedBySkills {
    /**
     * Codec for serializing and deserializing SkillBundle to/from a list of strings.
     * 用于将 SkillBundle 与字符串列表之间进行序列化和反序列化的编解码器
     * <p>
     * This codec enables saving skills to NBT data and loading them back.
     * 该编解码器支持将技能保存到 NBT 数据并重新加载
     * </p>
     *
     * @see com.mojang.serialization.Codec
     * @since 1.0.0
     */
    public static final Codec<SkillBundle> CODEC = Codec.list(Codec.STRING).xmap(
            SkillBundle::fromStrings,
            bundle -> getStrings(bundle.getAllData())
    );

    /**
     * Stream codec for network transmission of SkillBundle data.
     * 用于 SkillBundle 数据网络传输的流编解码器
     * <p>
     * Enables efficient packet-based transmission over Minecraft's network protocol.
     * 支持通过 Minecraft 网络协议进行高效的数据包传输
     * </p>
     *
     * @see net.minecraft.network.codec.StreamCodec
     * @since 1.0.0
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillBundle> STREAM_CODEC =
            StreamCodec.of(
                    (buf, bundle) -> {
                        List<String> strings = getStrings(bundle.getAllData());
                        buf.writeVarInt(strings.size());
                        strings.forEach(buf::writeUtf);
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        List<String> strings = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            strings.add(buf.readUtf());
                        }
                        return SkillBundle.fromStrings(strings);
                    }
            );

    /**
     * Map of skill types to their corresponding item skills.
     * 技能类型到对应物品技能的映射表
     * <p>
     * Organizes skills by their type for quick lookup and management.
     * 按技能类型组织技能，便于快速查找和管理
     * </p>
     *
     * @since 1.0.0
     */
    private final Map<SkillType, List<ItemSkillRegistration<?>>> skills;

    /**
     * Map of skill types to their corresponding skill instances.
     * 技能类型到对应技能实例的映射表
     * <p>
     * Stores the actual data and state for each skill instance.
     * 存储每个技能实例的实际数据和状态
     * </p>
     *
     * @since 1.0.0
     */
    private final Map<SkillType, List<ISkillInstance<?>>> skillData;

    /**
     * Empty skill bundle constant representing no skills.
     * 表示无技能的空技能包常量
     * <p>
     * Use this constant when you need an empty bundle instead of creating a new instance.
     * 当需要空技能包时使用此常量，而不是创建新实例
     * </p>
     *
     * @since 1.0.0
     */
    public static final SkillBundle EMPTY = new SkillBundle();

    /**
     * Default constructor creating an empty skill bundle.
     * 默认构造器，创建一个空的技能包
     * <p>
     * Creates a bundle with no skills. Use this when initializing without any skill data.
     * 创建一个不包含任何技能的技能包。在不需要任何技能数据初始化时使用
     * </p>
     *
     * @since 1.0.0
     */
    public SkillBundle() {
        this.skills = Collections.emptyMap();
        this.skillData = Collections.emptyMap();
    }

    /**
     * Constructor creating a skill bundle from a list of skill instances.
     * 从技能实例列表创建技能包的构造器
     * <p>
     * Groups the provided skill instances by their skill type for organized management.
     * 将提供的技能实例按其技能类型分组，以便组织管理
     * </p>
     *
     * @param skills List of skill instances to include in this bundle, or null/empty for empty bundle
     *               要包含在此技能包中的技能实例列表，传入 null 或空列表将创建空技能包
     * @throws ClassCastException If skill instances have incompatible types
     *                             如果技能实例具有不兼容的类型
     * @see ISkillInstance
     * @see SkillType
     * @since 1.0.0
     */
    public SkillBundle(List<ISkillInstance<?>> skills) {
        if (skills == null || skills.isEmpty()) {
            this.skills = Collections.emptyMap();
            this.skillData = Collections.emptyMap();
        } else {
            var pair = groupSkills(skills);
            this.skills  = pair.getFirst();
            this.skillData = pair.getSecond();
        }
    }

    /**
     * Returns a map of skill types to their corresponding item skills.
     * 返回技能类型到对应物品技能的映射表
     * <p>
     * This method is part of the OwnedBySkills interface implementation.
     * 此方法是 OwnedBySkills 接口实现的一部分
     * </p>
     *
     * @return Unmodifiable map of skill types to lists of item skills
     *         技能类型到物品技能列表的不可修改映射表
     * @see OwnedBySkills#skills()
     * @see SkillType
     * @see ItemSkill
     * @since 1.0.0
     */
    @Override
    public Map<SkillType, List<ItemSkillRegistration<?>>> skills() {
        return skills;
    }

    /**
     * Releases all skills of the specified type, consuming resources and executing effects.
     * 释放指定类型的所有技能，消耗资源并执行效果
     * <p>
     * This method checks if all required resources are available before releasing skills.
     * In creative mode, resources are not consumed. All resources must be available
     * for any skill to be released.
     * 此方法在释放技能之前检查所有所需资源是否可用。在创造模式下不会消耗资源。
     * 所有资源必须可用才能释放任何技能
     * </p>
     *
     * @param <T> The type of skill context
     *            技能上下文的类型
     * @param type The skill type to release
     *             要释放的技能类型
     * @param context The skill context containing player and environment information
     *                包含玩家和环境信息的技能上下文
     * @return true if skills were successfully released, false if resources were insufficient
     *         如果技能成功释放则返回 true，如果资源不足则返回 false
     * @throws ClassCastException If the context type is incompatible with the skill instances
     *                             如果上下文类型与技能实例不兼容
     * @see OwnedBySkills#releaseSkills(SkillType, SkillContext)
     * @see SkillResource
     * @see SkillContext
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends SkillContext> boolean releaseSkills(SkillType type, T context) {
        List<ISkillInstance<T>> skills = (List<ISkillInstance<T>>) (List<?>) skillData.get(type);
        Player player = context.getPlayer();

        if (!player.isCreative()) {
            Map<ResourceKey<SkillResource>, SkillResource.DelayConsumable> consumables =
                    new HashMap<>();
            for (ISkillInstance<T> instance : skills) {
                consumables.computeIfAbsent(instance.getResource().key(),
                        key -> instance.getResource().getDelayConsumable(player));
                instance.consumeResource(context, consumables.get(instance.getResource().key()));
            }

            for (SkillResource.DelayConsumable consumable : consumables.values()) {
                if (!consumable.canConsume()) return false;
            }

            for (SkillResource.DelayConsumable consumable : consumables.values()) {
                consumable.apply();
            }
        }

        for (ISkillInstance<T> instance : skills) {
            instance.release(context);
        }

        return true;
    }

    /**
     * Returns a modifiable list of all skill instances in this bundle.
     * 返回此技能包中所有技能实例的可修改列表
     * <p>
     * This method collects all skill instances across all types into a single list.
     * The returned list is a new ArrayList and can be safely modified.
     * 此方法将所有类型的所有技能实例收集到一个列表中。
     * 返回的列表是一个新的 ArrayList，可以安全地修改
     * </p>
     *
     * @return A modifiable list containing all skill instances, or empty list if none exist
     *         包含所有技能实例的可修改列表，如果不存在则返回空列表
     * @see ISkillInstance
     * @since 1.0.0
     */
    public List<ISkillInstance<?>> getAllData() {
        if (skillData.isEmpty()) {
            return new ArrayList<>();
        }
        List<ISkillInstance<?>> all = new ArrayList<>();
        for (List<ISkillInstance<?>> dataList : skillData.values()) {
            all.addAll(dataList);
        }
        return all;  // 返回可修改的新列表
    }

    // ========== Object 方法重写 ==========

    /**
     * Checks if this skill bundle is equal to another object.
     * 检查此技能包是否等于另一个对象
     * <p>
     * Two bundles are equal if they contain the same skill instances with the same data.
     * 如果两个技能包包含具有相同数据的相同技能实例，则它们相等
     * </p>
     *
     * @param o The object to compare with
     *          要比较的对象
     * @return true if the objects are equal skill bundles, false otherwise
     *         如果对象是相等的技能包则返回 true，否则返回 false
     * @since 1.0.0
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkillBundle that)) return false;
        // 比较 DataSkill 列表（包含 NBT 和 cost）
        return getAllData().equals(that.getAllData());
    }

    /**
     * Returns the hash code for this skill bundle.
     * 返回此技能包的哈希码
     * <p>
     * The hash code is based on all skill instances in the bundle.
     * 哈希码基于技能包中的所有技能实例
     * </p>
     *
     * @return The hash code value
     *         哈希码值
     * @since 1.0.0
     */
    @Override
    public int hashCode() {
        return getAllData().hashCode();
    }

    /**
     * Returns a string representation of this skill bundle.
     * 返回此技能包的字符串表示
     * <p>
     * Useful for debugging and logging purposes.
     * 适用于调试和日志记录
     * </p>
     *
     * @return A string representation of the skill bundle
     *         技能包的字符串表示
     * @since 1.0.0
     */
    @Override
    public String toString() {
        return "SkillsComponent{" +
                "skills=" + getStrings(getAllData()) +
                '}';
    }

    /**
     * Groups skill instances by their skill type.
     * 按技能类型对技能实例进行分组
     * <p>
     * This private utility method organizes skills into two maps: one for item skills
     * and one for skill instances, both keyed by skill type.
     * 此私有工具方法将技能组织到两个映射表中：一个用于物品技能，一个用于技能实例，
     * 两者都以技能类型为键
     * </p>
     *
     * @param skillData List of skill instances to group
     *                  要分组的技能实例列表
     * @return A pair containing the skills map and the skill data map
     *         包含技能映射表和技能数据映射表的对组
     * @see SkillType
     * @see ItemSkill
     * @see ISkillInstance
     * @since 1.0.0
     */
    private static Pair<Map<SkillType, List<ItemSkillRegistration<?>>>, Map<SkillType, List<ISkillInstance<?>>>>
            groupSkills(List<ISkillInstance<?>> skillData) {
        Map<SkillType, List<ItemSkillRegistration<?>>> skillResult          = new HashMap<>();
        Map<SkillType, List<ISkillInstance<?>>> skillDataResult = new HashMap<>();

        for (ISkillInstance<?> data : skillData) {
            ItemSkillRegistration<?> skill = data.skill();
            skillResult.computeIfAbsent(skill.getType(), k -> new ArrayList<>()).add(skill);
            skillDataResult.computeIfAbsent(skill.getType(), k -> new ArrayList<>()).add(data);
        }

        return Pair.of(skillResult, skillDataResult);
    }

    /**
     * Creates a skill bundle from a list of skill ID strings.
     * 从技能 ID 字符串列表创建技能包
     * <p>
     * This is a utility method for deserializing skill bundles from string representation.
     * Useful when loading saved skill data.
     * 这是一个用于从字符串表示反序列化技能包的工具方法。适用于加载保存的技能数据
     * </p>
     *
     * @param skillIds List of skill ID strings to convert into skill instances
     *                 要转换为技能实例的技能 ID 字符串列表
     * @return A new skill bundle containing the converted skill instances
     *         包含转换后的技能实例的新技能包
     * @see SkillData#fromString(String)
     * @see ISkillInstance#fromData(SkillData)
     * @since 1.0.0
     */
    public static SkillBundle fromStrings(List<String> skillIds) {
        return new SkillBundle(getSkills(skillIds));
    }

    /**
     * Converts a list of skill instances into a list of string representations.
     * 将技能实例列表转换为字符串表示列表
     * <p>
     * This is the inverse operation of {@link #getSkills(List)}.
     * Used for serializing skill data to storage or network transmission.
     * 这是 {@link #getSkills(List)} 的逆操作。用于将技能数据序列化以存储或网络传输
     * </p>
    *
     * @param skills List of skill instances to convert, or null for empty list
     *               要转换的技能实例列表，传入 null 返回空列表
     * @return List of string representations of the skill instances
     *         技能实例的字符串表示列表
     * @see ISkillInstance#toData()
     * @see SkillData#toString()
     * @since 1.0.0
     */
    public static List<String> getStrings(List<ISkillInstance<?>> skills) {
        if (skills == null) return Collections.emptyList();
        return skills.stream()
                .map(ISkillInstance::toData)
                .filter(Objects::nonNull)
                .map(SkillData::toString)
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of skill ID strings into skill instances.
     * 将技能 ID 字符串列表转换为技能实例
     * <p>
     * This is the inverse operation of {@link #getStrings(List)}.
     * Used for deserializing skill data from storage or network transmission.
     * 这是 {@link #getStrings(List)} 的逆操作。用于从存储或网络传输反序列化技能数据
     * </p>
     *
     * @param strings List of skill ID strings to convert, or null for empty list
     *                要转换的技能 ID 字符串列表，传入 null 返回空列表
     * @return List of skill instances created from the string representations
     *         从字符串表示创建的技能实例列表
     * @see SkillData#fromString(String)
     * @see ISkillInstance#fromData(SkillData)
     * @since 1.0.0
     */
    public static List<ISkillInstance<?>> getSkills(List<String> strings) {
        if (strings == null) return Collections.emptyList();
        return strings.stream()
                .map(SkillData::fromString)
                .filter(Objects::nonNull)
                .map(ISkillInstance::fromData)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
