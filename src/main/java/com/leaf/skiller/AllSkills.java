package com.leaf.skiller;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.api.registry.SkillerRegistries;
import com.leaf.skiller.content.skill.dustsweep.DustSweepSkill;
import com.leaf.skiller.content.skill.dustsweep.DustSweepStrategy;
import com.leaf.skiller.foundation.context.AutoSkillContext;
import com.leaf.skiller.foundation.context.AutoSkillContextFactory;
import com.leaf.skiller.foundation.skill.ItemSkill;
import com.leaf.skiller.foundation.skill.ItemSkillRegistration;
import com.leaf.skiller.foundation.skill.SkillType;
import com.leaf.skiller.foundation.skill.config.SkillContextFactory;
import com.leaf.skiller.foundation.strategy.SkillStrategy;
import com.leaf.skiller.util.ColorConfig;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Registry of all built-in skills of the Skiller mod.
 * Skiller 模组所有内置技能的注册表。
 * <p>
 * Each enum constant IS one skill: it derives its registry id from its own
 * name ({@code DUST_SWEEP} → {@code skiller:dust_sweep}), creates and
 * configures its {@link AutoSkillContextFactory} through the {@code create}
 * config lambda, and registers its strategy, context factory and skill
 * registration in one pass when {@link #register()} is called during
 * {@code RegisterEvent}.
 * 每个枚举常量就是一个技能：它从自身名称派生注册ID
 * （{@code DUST_SWEEP} → {@code skiller:dust_sweep}），
 * 通过 {@code create} 配置 lambda 创建并配置自己的 {@link AutoSkillContextFactory}，
 * 并在 {@code RegisterEvent} 期间调用 {@link #register()} 时
 * 一次性注册策略、上下文工厂和技能注册项。
 * </p>
 * <p>
 * Adding a new skill is therefore: add an enum constant, a strategy class, a
 * skill class — and optionally extend {@link AllSkillTypes} with a trigger.
 * 因此新增一个技能只需要：加一个枚举常量、一个策略类、一个技能类——
 * 需要新触发方式时再扩展 {@link AllSkillTypes}。
 * </p>
 *
 * @see AllSkillTypes
 * @see ItemSkillRegistration
 * @see AutoSkillContextFactory
 * @see Skiller#onRegister(net.neoforged.neoforge.registries.RegisterEvent)
 * @since 1.0.0
 * @author Leaf
 */
public enum AllSkills {

    /**
     * The Dust Sweep skill: attracts nearby dropped items toward the player.
     * 扫尘技能：将附近的掉落物吸引到玩家身边。
     */
    DUST_SWEEP(
            AllSkillTypes.RIGHT_CLICK_BLOCK,
            DustSweepStrategy::new,
            DustSweepSkill::new,
            create -> create.createFromConfig()
                    .addDouble(DustSweepSkill.KEY_RADIUS,
                            level -> 3.0 + level,
                            d -> Mth.clamp(d, 1.0, 16.0))
                    .addDouble(DustSweepSkill.KEY_PULL_STRENGTH,
                            level -> 0.6 + 0.1 * level,
                            d -> Mth.clamp(d, 0.1, 3.0))
                    .addDefaultColor(new ColorConfig(1F, 1F, 1F, 1F))
                    .end()
    );

    /**
     * The trigger classification of this skill.
     * 此技能的触发分类。
     */
    private final SkillType type;

    /**
     * The registry id, derived from the constant name.
     * 由常量名派生的注册表ID。
     */
    private final ResourceLocation id;

    /**
     * The registry key of this skill's strategy.
     * 此技能策略的注册表键。
     */
    private final ResourceKey<SkillStrategy<?, ?>> strategyKey;

    /**
     * The strategy instance of this skill.
     * 此技能的策略实例。
     */
    private final SkillStrategy<?, ?> strategy;

    /**
     * The registry key of this skill's context factory.
     * 此技能上下文工厂的注册表键。
     * <p>
     * Declared with the concrete context type so {@link ItemSkillRegistration}
     * keeps its generic type-safety end to end; the raw cast only erases the
     * type argument towards the wildcard registry.
     * 使用具体上下文类型声明，使 {@link ItemSkillRegistration} 端到端保持泛型类型安全；
     * raw 转换只是向通配符注册表擦除了类型参数。
     * </p>
     */
    private final ResourceKey<SkillContextFactory<AutoSkillContext>> contextFactoryKey;

    /**
     * The configured context factory created by the {@code create} lambda.
     * 由 {@code create} lambda 创建并配置好的上下文工厂。
     */
    private final AutoSkillContextFactory contextFactory;

    /**
     * The complete registration binding type, factory key and skill logic.
     * 绑定类型、工厂键与技能逻辑的完整注册项。
     */
    private final ItemSkillRegistration<AutoSkillContext> registration;

    /**
     * Creates one skill: derives all registry keys from the constant name,
     * creates the strategy and context factory, and builds the registration.
     * 创建一个技能：从常量名派生所有注册表键，
     * 创建策略与上下文工厂，构建注册项。
     *
     * @param type            The trigger classification / 触发分类
     * @param strategyFactory Creates the strategy instance / 创建策略实例
     * @param skillFactory    Builds the skill logic from the strategy key / 由策略键构建技能逻辑
     * @param create          Receives a fresh {@link AutoSkillContextFactory}, configures it
     *                        and returns it (must end with {@code end()})
     *                        接收全新的 {@link AutoSkillContextFactory}，完成配置后返回
     *                        （必须以 {@code end()} 结尾）
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    AllSkills(SkillType type,
              Supplier<SkillStrategy<?, ?>> strategyFactory,
              Function<ResourceKey<SkillStrategy<?, ?>>, ItemSkill<AutoSkillContext>> skillFactory,
              Function<AutoSkillContextFactory, AutoSkillContextFactory> create) {
        this.type = type;
        this.id = Skiller.modLoc(name().toLowerCase(Locale.ROOT));
        this.strategyKey = ResourceKey.create(SkillerRegistries.STRATEGY, id);
        this.contextFactoryKey =
                (ResourceKey) ResourceKey.create(SkillerRegistries.CONTEXT_FACTORY, id);
        this.strategy = strategyFactory.get();
        // create：new 一个 AutoSkillContextFactory 并应用技能的配置
        this.contextFactory = create.apply(new AutoSkillContextFactory());
        this.registration = new ItemSkillRegistration<>(
                id, type, contextFactoryKey, skillFactory.apply(strategyKey));
    }

    static {
        for (AllSkills skill : values()) {
            Registry.register(SkillerBuiltInRegistries.STRATEGIES,
                    skill.strategyKey.location(), skill.strategy);
            Registry.register(SkillerBuiltInRegistries.CONTEXT_FACTORIES,
                    skill.contextFactoryKey.location(), skill.contextFactory);
            Registry.register(SkillerBuiltInRegistries.SKILLS,
                    skill.id, skill.registration);
        }
    }

    /**
     * Registers every skill: strategy, context factory and registration.
     * 注册所有技能：策略、上下文工厂与注册项。
     * <p>
     * Called during {@code RegisterEvent}, while the registries are still
     * writable. The wildcard-typed registries cannot accept the concrete-typed
     * keys directly, so the resource-location overloads of
     * {@link Registry#register} are used.
     * 在 {@code RegisterEvent} 期间调用，此时注册表仍可写。
     * 通配符类型的注册表无法直接接受具体类型的键，
     * 因此使用 {@link Registry#register} 的 ResourceLocation 重载。
     * </p>
     *
     * @see Skiller#onRegister(net.neoforged.neoforge.registries.RegisterEvent)
     * @since 1.0.0
     */
    public static void register() {

    }

    /**
     * Returns the trigger classification of this skill.
     * 返回此技能的触发分类。
     */
    public SkillType getType() {
        return type;
    }

    /**
     * Returns the registry id of this skill.
     * 返回此技能的注册表ID。
     */
    public ResourceLocation getId() {
        return id;
    }

    /**
     * Returns the registry key of this skill's strategy.
     * 返回此技能策略的注册表键。
     */
    public ResourceKey<SkillStrategy<?, ?>> getStrategyKey() {
        return strategyKey;
    }

    /**
     * Returns the complete registration of this skill.
     * 返回此技能的完整注册项。
     */
    public ItemSkillRegistration<AutoSkillContext> getRegistration() {
        return registration;
    }
}
