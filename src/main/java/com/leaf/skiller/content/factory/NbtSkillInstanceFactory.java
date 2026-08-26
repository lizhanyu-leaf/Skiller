package com.leaf.skiller.content.factory;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.foundation.SkillData;
import com.leaf.skiller.foundation.SkillInstanceFactory;
import com.leaf.skiller.foundation.SkillResource;
import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.ItemSkill;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

/**
 * Abstract factory for creating skill instances from NBT-based serialized data.
 * 用于从基于 NBT 的序列化数据创建技能实例的抽象工厂。
 * <p>
 * This class provides a framework for implementing skill instance factories that
 * can serialize and deserialize skill instances using NBT data format. It handles
 * the common pattern of storing skill definitions, resource types, and levels in
 * NBT tags, allowing subclasses to focus on the specific instance creation logic.
 * 此类为实现可以使用 NBT 数据格式序列化和反序列化技能实例的技能实例工厂提供框架。
 * 它处理将技能定义、资源类型和等级存储在 NBT 标签中的常见模式，
 * 允许子类专注于特定的实例创建逻辑。
 * </p>
 * <p>
 * The factory uses configurable NBT keys for resource and level storage, enabling
 * customization of the serialization format while maintaining a consistent
 * deserialization process across all implementations.
 * 工厂使用可配置的 NBT 键进行资源和等级存储，允许自定义序列化格式，
 * 同时在所有实现中保持一致的反序列化过程。
 * </p>
 *
 * @param <T> the skill context type, must extend SkillContext
 *            技能上下文类型，必须扩展 SkillContext
 * @param <I> the skill instance type, must extend ISkillInstance
 *            技能实例类型，必须扩展 ISkillInstance
 * @see SkillInstanceFactory
 * @see ISkillInstance
 * @see SkillData
 * @since 1.0.0
 */
public abstract class NbtSkillInstanceFactory<T extends SkillContext, I extends ISkillInstance<T>>
        implements SkillInstanceFactory<T, I> {

    /**
     * The NBT key used to store the resource registry identifier in serialized data.
     * 用于在序列化数据中存储资源注册表标识符的 NBT 键。
     * <p>
     * This field defines the key under which the resource type's registry location
     * is stored in the NBT data during serialization. The same key is used during
     * deserialization to retrieve the resource type from the registry.
     * 此字段定义在序列化期间资源类型的注册表位置存储在 NBT 数据中的键。
     * 在反序列化期间使用相同的键从注册表中检索资源类型。
     * </p>
     */
    private final String resourceRegistryKey;

    /**
     * The NBT key used to store the skill level in serialized data.
     * 用于在序列化数据中存储技能等级的 NBT 键。
     * <p>
     * This field defines the key under which the skill's level value is stored
     * in the NBT data during serialization. The same key is used during
     * deserialization to retrieve the level value.
     * 此字段定义在序列化期间技能的等级值存储在 NBT 数据中的键。
     * 在反序列化期间使用相同的键检索等级值。
     * </p>
     */
    private final String levelKey;

    /**
     * Constructs a new NbtSkillInstanceFactory with the specified NBT keys.
     * 使用指定的 NBT 键构造新的 NbtSkillInstanceFactory。
     * <p>
     * This constructor initializes the factory with custom keys for storing resource
     * type and level information in NBT data. These keys must be consistent between
     * serialization and deserialization to ensure proper data reconstruction.
     * 此构造函数使用用于在 NBT 数据中存储资源类型和等级信息的自定义键初始化工厂。
     * 这些键在序列化和反序列化之间必须保持一致，以确保正确的数据重建。
     * </p>
     *
     * @param resourceRegistryKey the NBT key for storing resource registry location
     *                            用于存储资源注册表位置的 NBT 键
     * @param levelKey            the NBT key for storing skill level
     *                            用于存储技能等级的 NBT 键
     * @throws NullPointerException if either key is null
     *                               如果任一键为 null 则抛出异常
     * @since 1.0.0
     */
    protected NbtSkillInstanceFactory(String resourceRegistryKey, String levelKey) {
        this.resourceRegistryKey = resourceRegistryKey;
        this.levelKey = levelKey;
    }

    /**
     * Creates a skill instance from serialized skill data.
     * 从序列化的技能数据创建技能实例。
     * <p>
     * This method implements the deserialization process for skill instances,
     * extracting and validating all required components from the provided data:
     * 此方法实现技能实例的反序列化过程，从提供的数据中提取和验证所有必需的组件：
     * </p>
     * <ol>
     *   <li>Retrieves the skill definition from the registry using the skill ID
     *   <li>使用技能 ID 从注册表中检索技能定义</li>
     *   <li>Parses the resource type from NBT and retrieves it from the registry
     *   <li>从 NBT 解析资源类型并从注册表中检索</li>
     *   <li>Extracts the skill level from NBT
     *   <li>从 NBT 提取技能等级</li>
     *   <li>Delegates to the abstract {@link #create(ItemSkill, SkillResource, int, CompoundTag)} method
     *   <li>委托给抽象的 {@link #create(ItemSkill, SkillResource, int, CompoundTag)} 方法</li>
     * </ol>
     * <p>
     * If any required component cannot be found or is invalid, this method returns null.
     * 如果任何必需的组件无法找到或无效，此方法返回 null。
     * </p>
     *
     * @param data the serialized skill data containing skill ID, factory ID, and NBT data
     *             包含技能 ID、工厂 ID 和 NBT 数据的序列化技能数据
     * @return a new skill instance created from the data, or null if creation failed
     *         从数据创建的新技能实例，如果创建失败则返回 null
     * @throws NullPointerException if data is null
     *                               如果 data 为 null 则抛出异常
     * @see SkillData
     * @see SkillerBuiltInRegistries
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    @Override
    public I createFromData(SkillData data) {
        CompoundTag nbt = data.nbt();

        // 1. 从注册表获取 skill
        // Retrieve skill from registry
        ItemSkill<T> skill = (ItemSkill<T>) SkillerBuiltInRegistries.SKILLS.get(data.skillId());
        if (skill == null) return null;

        // 2. 从 NBT 获取 resource
        // Retrieve resource from NBT
        SkillResource resource = SkillerBuiltInRegistries.SKILL_RESOURCES
                .get(ResourceLocation.parse(nbt.getString(resourceRegistryKey)));
        if (resource == null) return null;

        // 3. 从 NBT 获取 level
        // Retrieve level from NBT
        int level = nbt.getInt(levelKey);

        return create(skill, resource, level, nbt);
    }

    /**
     * Serializes a skill instance into skill data format.
     * 将技能实例序列化为技能数据格式。
     * <p>
     * This method implements the serialization process for skill instances,
     * extracting all necessary information from the instance and packaging it
     * into a {@link SkillData} object for persistence or network transmission:
     * 此方法实现技能实例的序列化过程，从实例中提取所有必要信息并将其打包到
     * {@link SkillData} 对象中，用于持久化或网络传输：
     * </p>
     * <ol>
     *   <li>Copies the instance's NBT data to avoid mutation
     *   <li>复制实例的 NBT 数据以避免变异</li>
     *   <li>Stores the resource type's registry location using the configured key
     *   <li>使用配置的键存储资源类型的注册表位置</li>
     *   <li>Stores the skill level using the configured key
     *   <li>使用配置的键存储技能等级</li>
     *   <li>Creates SkillData with skill ID, factory ID, and modified NBT
     *   <li>使用技能 ID、工厂 ID 和修改后的 NBT 创建 SkillData</li>
     * </ol>
     *
     * @param instance the skill instance to serialize
     *                 要序列化的技能实例
     * @return a SkillData object containing all serialized instance information
     *         包含所有序列化实例信息的 SkillData 对象
     * @throws NullPointerException if instance is null
     *                               如果 instance 为 null 则抛出异常
     * @see SkillData
     * @see SkillerBuiltInRegistries
     * @since 1.0.0
     */
    @Override
    public SkillData toData(I instance) {
        CompoundTag nbt = instance.data().copy();
        nbt.putString(resourceRegistryKey, instance.getResource().key().location().toString());
        nbt.putInt(levelKey, instance.level());

        return new SkillData(
                SkillerBuiltInRegistries.SKILLS.getKey(instance.skill()),
                SkillerBuiltInRegistries.SKILL_FACTORIES.getKey(this),
                nbt
        );
    }

    /**
     * Creates a new skill instance with the specified parameters.
     * 使用指定参数创建新的技能实例。
     * <p>
     * This abstract method must be implemented by subclasses to provide the
     * specific logic for creating skill instances. It is called by the
     * {@link #createFromData(SkillData)} method after all components have been
     * extracted and validated from the serialized data.
     * 此抽象方法必须由子类实现以提供创建技能实例的特定逻辑。
     * 在从序列化数据中提取和验证所有组件后，由 {@link #createFromData(SkillData)} 方法调用。
     * </p>
     * <p>
     * Implementations should construct and return a new instance of their specific
     * skill instance class, using the provided parameters to initialize all
     * necessary properties and state.
     * 实现应构造并返回其特定技能实例类的新实例，使用提供的参数初始化所有必需的属性和状态。
     * </p>
     *
     * @param skill    the skill definition for this instance
     *                 此实例的技能定义
     * @param resource the resource type for this instance
     *                 此实例的资源类型
     * @param level    the skill level for this instance
     *                 此实例的技能等级
     * @param nbt      the NBT data containing additional instance properties
     *                 包含其他实例属性的 NBT 数据
     * @return a new skill instance configured with the provided parameters
     *         使用提供的参数配置的新技能实例
     * @see ISkillInstance
     * @see ItemSkill
     * @see SkillResource
     * @see CompoundTag
     * @since 1.0.0
     */
    protected abstract I create(ItemSkill<T> skill, SkillResource resource, int level, CompoundTag nbt);
}
