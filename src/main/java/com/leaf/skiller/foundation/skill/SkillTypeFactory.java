package com.leaf.skiller.foundation.skill;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and managing skill type instances.
 * 用于创建和管理技能类型实例的工厂
 * <p>
 * This factory provides a centralized way to obtain SkillType instances,
 * ensuring that the same ResourceLocation always maps to the same SkillType instance.
 * Internally uses a concurrent hash map for thread-safe access.
 * 该工厂提供了获取 SkillType 实例的集中方式，确保相同的 ResourceLocation 始终映射到同一个 SkillType 实例。
 * 内部使用并发哈希表实现线程安全访问
 * </p>
 *
 * @see SkillType
 * @see ResourceLocation
 * @since 1.0.0
 * @author Leaf
 */
public class SkillTypeFactory {
    /**
     * Concurrent map storing skill type instances by their resource location.
     * 按资源位置存储技能类型实例的并发映射表
     * <p>
     * Uses ConcurrentHashMap to ensure thread-safe access and prevent duplicate instances.
     * 使用 ConcurrentHashMap 确保线程安全访问并防止重复实例
     * </p>
     *
     * @since 1.0.0
     */
    private static final Map<ResourceLocation, SkillType> TYPES = new ConcurrentHashMap<>();

    /**
     * Returns a SkillType instance for the given resource location.
     * 返回给定资源位置的 SkillType 实例
     * <p>
     * This method ensures that the same ResourceLocation always returns the same
     * SkillType instance (singleton pattern per ID). If the instance doesn't exist,
     * it will be created and cached.
     * 此方法确保相同的 ResourceLocation 始终返回同一个 SkillType 实例（每个 ID 的单例模式）。
     * 如果实例不存在，将创建并缓存它
     * </p>
     *
     * @param id The resource location identifying the skill type
     *           标识技能类型的资源位置
     * @return The SkillType instance for the given resource location
     *         给定资源位置的 SkillType 实例
     * @throws NullPointerException If the resource location is null
     *                              如果资源位置为 null
     * @see SkillType
     * @see ResourceLocation
     * @since 1.0.0
     */
    public static SkillType of(ResourceLocation id) {
        return TYPES.computeIfAbsent(id, HashSkillType::new);
    }

    /**
     * Implementation of SkillType using hash-based equality.
     * 使用基于哈希的相等性比较的 SkillType 实现
     * <p>
     * This inner class provides a concrete implementation of SkillType where
     * equality and hash codes are based solely on the resource location ID.
     * 此内部类提供了 SkillType 的具体实现，其中相等性和哈希码仅基于资源位置 ID
     * </p>
     *
     * @see SkillType
     * @see ResourceLocation
     * @since 1.0.0
     */
    public static class HashSkillType implements SkillType {
        /**
         * The resource location that uniquely identifies this skill type.
         * 唯一标识此技能类型的资源位置
         *
         * @since 1.0.0
         */
        private final ResourceLocation id;

        /**
         * Constructs a new HashSkillType with the given resource location.
         * 使用给定的资源位置构造新的 HashSkillType
         *
         * @param id The resource location identifying this skill type
         *           标识此技能类型的资源位置
         * @throws NullPointerException If the resource location is null
         *                              如果资源位置为 null
         * @since 1.0.0
         */
        private HashSkillType(ResourceLocation id) {
            this.id = id;
        }

        /**
         * Returns the resource location that identifies this skill type.
         * 返回标识此技能类型的资源位置
         *
         * @return The resource location ID
         *         资源位置 ID
         * @see SkillType#getId()
         * @since 1.0.0
         */
        @Override
        public ResourceLocation getId() {
            return id;
        }

        /**
         * Returns the hash code for this skill type.
         * 返回此技能类型的哈希码
         * <p>
         * The hash code is based solely on the resource location ID.
         * 哈希码仅基于资源位置 ID
         * </p>
         *
         * @return The hash code value
         *         哈希码值
         * @since 1.0.0
         */
        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}
