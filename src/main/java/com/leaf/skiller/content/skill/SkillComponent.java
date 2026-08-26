package com.leaf.skiller.content.skill;

import com.leaf.skiller.foundation.skill.SkillBundle;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
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
 * @see SkillBundle
 * @see com.leaf.skiller.foundation.skill.ItemSkill
 * @since 1.0.0
 */
public class SkillComponent {
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
     * The internal map of skill bindings, mapping integer keys to skill bundles.
 * 技能绑定的内部映射，将整数键映射到技能束。
     * <p>
     * Each key typically represents a slot ID or skill identifier, with the associated
     * SkillBundle containing the skill instance and related data.
     * 每个键通常代表槽位 ID 或技能标识符，关联的 SkillBundle 包含技能实例和相关数据。
     * </p>
     */
    private final Map<Integer, SkillBundle> bindings;

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
     *                               如果绑定为 null 则抛出异常
     * @since 1.0.0
     */
    public SkillComponent(Map<Integer, SkillBundle> bindings) {
        this.bindings = bindings;
    }

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
     *         整数键到技能束的映射
     * @since 1.0.0
     */
    public Map<Integer, SkillBundle> bindings() {
        return bindings;
    }
}
