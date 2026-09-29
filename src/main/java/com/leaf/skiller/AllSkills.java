package com.leaf.skiller;

import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.leaf.skiller.api.registry.SkillerRegistries;
import com.leaf.skiller.content.skill.dustsweep.DustSweepContext;
import com.leaf.skiller.content.skill.dustsweep.DustSweepContextFactory;
import com.leaf.skiller.content.skill.dustsweep.DustSweepSkill;
import com.leaf.skiller.content.skill.dustsweep.DustSweepStrategy;
import com.leaf.skiller.foundation.skill.ItemSkillRegistration;
import com.leaf.skiller.foundation.skill.config.SkillContextFactory;
import com.leaf.skiller.foundation.strategy.SkillStrategy;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Registration holder for all built-in skills of the Skiller mod.
 * Skiller 模组所有内置技能的注册持有类。
 * <p>
 * Each skill binds together, at registration time, the three pieces the system
 * needs: its {@link com.leaf.skiller.foundation.skill.SkillType} (trigger
 * classification), its context factory (how the execution context is built from
 * instance config + environment), and the skill logic itself. Like
 * {@link AllSkillInstanceFactories}, registration happens in the static
 * initializer (triggered by {@link #register()} during {@code RegisterEvent},
 * while the registries are still writable); the {@code register()} method body
 * is intentionally empty.
 * 每个技能在注册时绑定系统所需的三个部分：
 * {@link com.leaf.skiller.foundation.skill.SkillType}（触发分类）、
 * 上下文工厂（如何从实例配置和环境构建执行上下文）以及技能逻辑本身。
 * 与 {@link AllSkillInstanceFactories} 相同，注册发生在静态初始化块中
 * （由 {@code RegisterEvent} 期间的 {@link #register()} 触发，此时注册表仍可写）；
 * {@code register()} 方法体有意留空。
 * </p>
 *
 * @see AllSkillTypes
 * @see ItemSkillRegistration
 * @see DustSweepSkill
 * @see Skiller#onRegister(net.neoforged.neoforge.registries.RegisterEvent)
 * @since 1.0.0
 * @author Leaf
 */
public final class AllSkills {

    /**
     * Registry id of the Dust Sweep skill.
     * 扫尘技能的注册表ID。
     */
    private static final ResourceLocation DUST_SWEEP_ID = Skiller.modLoc("dust_sweep");

    /**
     * Registry key of the Dust Sweep skill in the skills registry.
     * 扫尘技能在技能注册表中的注册表键。
     */
    public static final ResourceKey<ItemSkillRegistration<?>> DUST_SWEEP_KEY =
            ResourceKey.create(SkillerRegistries.SKILL, DUST_SWEEP_ID);

    /**
     * Registry key of the Dust Sweep strategy in the strategies registry.
     * 扫尘策略在策略注册表中的注册表键。
     */
    public static final ResourceKey<SkillStrategy<?, ?>> DUST_SWEEP_STRATEGY_KEY =
            ResourceKey.create(SkillerRegistries.STRATEGY, Skiller.modLoc("dust_sweep"));

    /**
     * Registry key of the Dust Sweep context factory.
     * 扫尘上下文工厂的注册表键。
     * <p>
     * Declared with the concrete context type so {@link ItemSkillRegistration}
     * keeps its generic type-safety end to end; the raw cast at registration
     * only erases the type argument towards the wildcard registry.
     * 使用具体上下文类型声明，使 {@link ItemSkillRegistration} 端到端保持泛型类型安全；
     * 注册时的 raw 转换只是向通配符注册表擦除了类型参数。
     * </p>
     */
    public static final ResourceKey<SkillContextFactory<DustSweepContext>> DUST_SWEEP_CONTEXT_FACTORY_KEY =
            (ResourceKey) ResourceKey.create(SkillerRegistries.CONTEXT_FACTORY, Skiller.modLoc("dust_sweep"));

    /**
     * The Dust Sweep skill registration: right-click-block type, its context
     * factory and the skill logic.
     * 扫尘技能注册：方块右键类型、其上下文工厂以及技能逻辑。
     */
    public static final ItemSkillRegistration<DustSweepContext> DUST_SWEEP =
            new ItemSkillRegistration<>(
                    AllSkillTypes.RIGHT_CLICK_BLOCK,
                    DUST_SWEEP_CONTEXT_FACTORY_KEY,
                    new DustSweepSkill(DUST_SWEEP_STRATEGY_KEY)
            );

    /**
     * Static initializer registering the strategy, the context factory and the
     * skill into their registries. The resource-location overloads of
     * {@link Registry#register} are used on purpose: the wildcard-typed
     * registries cannot accept the concrete-typed keys directly.
     * 静态初始化块，将策略、上下文工厂和技能注册到各自注册表。
     * 有意使用 {@link Registry#register} 的 ResourceLocation 重载：
     * 通配符类型的注册表无法直接接受具体类型的键。
     */
    static {
        Registry.register(SkillerBuiltInRegistries.STRATEGIES, DUST_SWEEP_STRATEGY_KEY.location(), new DustSweepStrategy());
        Registry.register(SkillerBuiltInRegistries.CONTEXT_FACTORIES, DUST_SWEEP_CONTEXT_FACTORY_KEY.location(), new DustSweepContextFactory());
        Registry.register(SkillerBuiltInRegistries.SKILLS, DUST_SWEEP_ID, DUST_SWEEP);
    }

    /**
     * Triggers class loading (and therefore registration) of this holder.
     * 触发此持有类的类加载（进而触发注册）。
     * <p>
     * Called during {@code RegisterEvent}, like the other {@code All*} holders.
     * 与其他 {@code All*} 持有类一样，在 {@code RegisterEvent} 期间调用。
     * </p>
     *
     * @see Skiller#onRegister(net.neoforged.neoforge.registries.RegisterEvent)
     * @since 1.0.0
     */
    public static void register() {}

    private AllSkills() {
        throw new AssertionError("This class should not be instantiated");
    }
}
