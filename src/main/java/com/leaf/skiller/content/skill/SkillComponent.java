package com.leaf.skiller.content.skill;

import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.SkillBundle;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A component that manages skill bindings for entities or items.
 * 管理实体或物品技能绑定的组件。
 * <p>
 * This class maintains a mapping of integer keys to {@link SkillBundle} objects,
 * representing the association between slots or identifiers and their corresponding skills.
 * 此类维护整数键到 {@link SkillBundle} 对象的映射，表示槽位或标识符与其对应技能之间的关联。
 * </p>
 * <p>
 * The component provides serialization support through both {@link Codec} for data persistence
 * and {@link StreamCodec} for network transmission, enabling seamless integration with
 * Minecraft's data systems.
 * 该组件通过用于数据持久化的 {@link Codec} 和用于网络传输的 {@link StreamCodec} 提供序列化支持，
 * 实现与 Minecraft 数据系统的无缝集成。
 * </p>
 *
 * @param bindings The internal map of skill bindings, mapping integer keys to skill bundles.
 *                 技能绑定的内部映射，将整数键映射到技能束。
 *                 <p>
 *                 Each key typically represents a slot ID or skill identifier, with the associated
 *                 SkillBundle containing the skill instance and related data.
 *                 每个键通常代表槽位 ID 或技能标识符，关联的 SkillBundle 包含技能实例和相关数据。
 *                 </p>
 * @see SkillBundle
 * @see com.leaf.skiller.foundation.skill.ItemSkill
 * @since 1.0.0
 */
public record SkillComponent(Map<Integer, SkillBundle> bindings) {
    /**
     * Codec for serializing and deserializing SkillComponent objects.
     * 用于序列化和反序列化 SkillComponent 对象的编解码器。
     * <p>
     * This codec handles conversion between SkillComponent and a map representation,
     * using string keys during serialization for compatibility with JSON format.
     * 该编解码器处理 SkillComponent 与映射表示之间的转换，在序列化期间使用字符串键以兼容 JSON 格式。
     * </p>
     */
    public static final Codec<SkillComponent> CODEC = Codec.unboundedMap(
            Codec.STRING, SkillBundle.CODEC
    ).xmap(
            map -> new SkillComponent(
                    map.entrySet().stream().collect(Collectors.toMap(
                            e -> Integer.parseInt(e.getKey()), Map.Entry::getValue
                    ))
            ),
            component -> component.bindings.entrySet().stream().collect(Collectors.toMap(
                    e -> String.valueOf(e.getKey()), Map.Entry::getValue
            ))
    );

    /**
     * Stream codec for network serialization of SkillComponent objects.
     * 用于 SkillComponent 对象网络序列化的流编解码器。
     * <p>
     * This codec enables efficient binary serialization for network packet transmission,
     * writing the size of bindings followed by each key-value pair.
     * 该编解码器实现高效的二进制序列化用于网络数据包传输，写入绑定大小后跟每个键值对。
     * </p>
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillComponent> STREAM_CODEC =
            StreamCodec.of(
                    (buf, component) -> {
                        buf.writeVarInt(component.bindings.size());
                        for (var entry : component.bindings.entrySet()) {
                            buf.writeVarInt(entry.getKey());
                            SkillBundle.STREAM_CODEC.encode(buf, entry.getValue());
                        }
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        Map<Integer, SkillBundle> bindings = new HashMap<>();
                        for (int i = 0; i < size; i++) {
                            int key = buf.readVarInt();
                            SkillBundle bundle = SkillBundle.STREAM_CODEC.decode(buf);
                            bindings.put(key, bundle);
                        }
                        return new SkillComponent(bindings);
                    }
            );

    /**
     * Constructs a new SkillComponent with the specified bindings.
     * 使用指定的绑定构造新的 SkillComponent。
     * <p>
     * This constructor initializes the component with a pre-populated map of skill bindings.
     * The provided map is directly assigned, so callers should ensure it is not modified externally.
     * 此构造函数使用预填充的技能绑定映射初始化组件。提供的映射被直接赋值，因此调用者应确保其不会被外部修改。
     * </p>
     *
     * @param bindings the map of integer keys to skill bundles
     *                 整数键到技能束的映射
     * @throws NullPointerException if bindings is null
     *                              如果绑定为 null 则抛出异常
     * @since 1.0.0
     */
    public SkillComponent {}

    /**
     * Empty skill component constant representing no skill bindings.
     * 表示无技能绑定的空技能组件常量。
     * <p>
     * Use this constant when you need an empty component instead of creating a new instance.
     * The bindings map of this constant is immutable.
     * 当需要空组件时使用此常量，而不是创建新实例。此常量的绑定映射是不可变的。
     * </p>
     *
     * @since 1.0.0
     */
    public static final SkillComponent EMPTY = new SkillComponent(Map.of());

    /**
     * Returns the immutable map of skill bindings.
     * 返回技能绑定的不可变映射。
     * <p>
     * The returned map provides access to all skill bindings managed by this component.
     * Modifications to the returned map may not be reflected in the component.
     * 返回的映射提供对此组件管理的所有技能绑定的访问。对返回映射的修改可能不会反映在组件中。
     * </p>
     *
     * @return the map of integer keys to skill bundles
     * 整数键到技能束的映射
     * @since 1.0.0
     */
    @Override
    public Map<Integer, SkillBundle> bindings() {
        return bindings;
    }

    /**
     * Returns a list of all skill instances across every key binding in this component.
     * 返回此组件中所有按键绑定下的全部技能实例列表。
     * <p>
     * This method flattens all bundles from all key bindings into a single list.
     * The returned list is newly allocated and can be safely modified.
     * 此方法将所有按键绑定的所有技能束展平为单个列表。
     * 返回的列表是新分配的，可以安全地修改。
     * </p>
     *
     * @return A list containing all skill instances from all bindings, or an empty list if none exist
     * 包含所有绑定下所有技能实例的列表，如果不存在则返回空列表
     * @see SkillBundle#getAllData()
     * @since 1.0.0
     */
    public List<ISkillInstance<?>> getAllData() {
        return bindings.values().stream()
                .flatMap(bundle -> bundle.getAllData().stream())
                .collect(Collectors.toList());
    }
}
