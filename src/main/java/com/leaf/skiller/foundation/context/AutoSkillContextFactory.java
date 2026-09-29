package com.leaf.skiller.foundation.context;

import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.config.SkillContextEnvironment;
import com.leaf.skiller.foundation.skill.config.SkillContextFactory;
import com.leaf.skiller.util.ColorConfig;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class AutoSkillContextFactory implements SkillContextFactory<AutoSkillContext> {

    // end() 未配置颜色时的兜底键与兜底颜色
    public static final String DEFAULT_COLOR_KEY = "color";
    public static final ColorConfig DEFAULT_COLOR = ColorConfig.whiteConfig();

    Config config;

    public Config createFromConfig() {
        return new Config(this);
    }

    @Override
    public AutoSkillContext create(SkillContextEnvironment env, ISkillInstance<AutoSkillContext> instance) {
        CompoundTag data = instance.data();
        // 直接遍历 entryMap 聚合：键存在 → 读实例数据；不存在 → 回退到等级默认
        CompoundTag resolved = new CompoundTag();
        for (Map.Entry<String, GetterEntry<?>> entry : config.entryMap.entrySet()) {
            resolveAndPut(resolved, entry.getKey(), entry.getValue(), data, instance.level());
        }

        AutoSkillContext context = new AutoSkillContext(
                env.getPlayer(), resolveColor(data, instance.level()), resolved);
        // 创建完成后的回调：调用方可基于 env / instance / context 做最后调整
        if (config.customizer != null) {
            config.customizer.customize(env, instance, context);
        }
        return context;
    }

    @Override
    public AutoSkillContext createDefault(SkillContextEnvironment env, int level) {
        CompoundTag resolved = new CompoundTag();
        for (Map.Entry<String, GetterEntry<?>> entry : config.entryMap.entrySet()) {
            entry.getValue().putDefault(resolved, entry.getKey(), level);
        }
        ColorConfig color = config.colorEntry != null
                ? config.colorEntry.get(level)
                : DEFAULT_COLOR;
        return new AutoSkillContext(env.getPlayer(), color, resolved);
    }

    // 单个 entry 的解析与写回：实例数据有 → 读；没有 → 等级默认
    private static <T> void resolveAndPut(CompoundTag tag, String key,
                                          GetterEntry<T> entry, CompoundTag data, int level) {
        T value = data.contains(key) ? entry.get(data) : entry.get(level);
        entry.put(tag, key, value);
    }

    // 颜色解析：实例数据有 → 读；没有 → 等级默认
    private ColorConfig resolveColor(CompoundTag data, int level) {
        return data.contains(DEFAULT_COLOR_KEY)
                ? config.colorEntry.get(data)
                : config.colorEntry.get(level);
    }

    // 约定的颜色 NBT 结构：{r, g, b, a} 四个 float
    private static ColorConfig readColor(CompoundTag data, String key) {
        CompoundTag tag = data.getCompound(key);
        return new ColorConfig(
                tag.getFloat("r"),
                tag.getFloat("g"),
                tag.getFloat("b"),
                tag.getFloat("a")
        );
    }

    // 写回函数：CompoundTag 的 putX 返回 void，BiFunction 装不下，需要独立接口
    @FunctionalInterface
    public interface Putter<T> {
        void put(CompoundTag tag, String key, T value);
    }

    public record GetterEntry<T>(
            Function<CompoundTag, T> getter,
            Function<Integer, T> defaultGetter,
            Function<T, T> clamp,
            Putter<T> putter) {
        T get(CompoundTag data) {
            return clamp.apply(getter.apply(data));
        }

        T get(int level) {
            return clamp.apply(defaultGetter.apply(level));
        }

        void put(CompoundTag tag, String key, T value) {
            putter.put(tag, key, value);
        }

        void putDefault(CompoundTag tag, String key, int level) {
            putter.put(tag, key, get(level));
        }
    }

    // 创建上下文后的回调：环境、实例与构建好的上下文
    @FunctionalInterface
    public interface ContextCustomizer {
        void customize(SkillContextEnvironment env, ISkillInstance<AutoSkillContext> instance, AutoSkillContext context);
    }

    public static class Config {
        public final Map<String, GetterEntry<?>> entryMap = new HashMap<>();
        private final AutoSkillContextFactory factory;
        private GetterEntry<ColorConfig> colorEntry;
        private ContextCustomizer customizer;

        private void addEntry(String key, GetterEntry<?> getterEntry) {
            entryMap.put(key, getterEntry);
        }

        private Config(AutoSkillContextFactory factory) {
            this.factory = factory;
            factory.config = this;
        }

        private <T> Config addEntry(
                String key,
                BiFunction<CompoundTag, String, T> getter,
                Function<Integer, T> defaultGetter,
                Function<T, T> clamp,
                Putter<T> putter) {
            addEntry(key, new GetterEntry<>(
                    data -> getter.apply(data, key), defaultGetter, clamp, putter
            ));
            return this;
        }

        public Config addInt(
                String key,
                Function<Integer, Integer> defaultGetter,
                Function<Integer, Integer> clamp
        ) {
            return addEntry(key, CompoundTag::getInt, defaultGetter, clamp, CompoundTag::putInt);
        }

        public Config addFloat(
                String key,
                Function<Integer, Float> defaultGetter,
                Function<Float, Float> clamp
        ) {
            return addEntry(key, CompoundTag::getFloat, defaultGetter, clamp, CompoundTag::putFloat);
        }

        public Config addDouble(
                String key,
                Function<Integer, Double> defaultGetter,
                Function<Double, Double> clamp
        ) {
            return addEntry(key, CompoundTag::getDouble, defaultGetter, clamp, CompoundTag::putDouble);
        }

        public Config addLong(
                String key,
                Function<Integer, Long> defaultGetter,
                Function<Long, Long> clamp
        ) {
            return addEntry(key, CompoundTag::getLong, defaultGetter, clamp, CompoundTag::putLong);
        }

        public Config addString(
                String key,
                Function<Integer, String> defaultGetter,
                Function<String, String> clamp
        ) {
            return addEntry(key, CompoundTag::getString, defaultGetter, clamp, CompoundTag::putString);
        }

        public Config addCompoundTag(
                String key,
                Function<Integer, CompoundTag> defaultGetter,
                Function<CompoundTag, CompoundTag> clamp
        ) {
            return addEntry(key, CompoundTag::getCompound, defaultGetter, clamp, CompoundTag::put);
        }

        // 完全自定义的颜色 getter（颜色不写入 resolved，直接进上下文字段）
        public Config addColor(
                BiFunction<CompoundTag, String, ColorConfig> getter,
                Function<Integer, ColorConfig> defaultGetter
        ) {
            this.colorEntry = new GetterEntry<>(
                    data -> getter.apply(data, DEFAULT_COLOR_KEY),
                    defaultGetter,
                    Function.identity(),
                    (tag, key, value) -> { }
            );
            return this;
        }

        // 只填默认值；getter 按 {r, g, b, a} 的约定结构读取
        public Config addDefaultColor(ColorConfig defaultValue) {
            return addColor(AutoSkillContextFactory::readColor, level -> defaultValue);
        }

        // 注册创建完成后的回调
        public Config addCustomizer(ContextCustomizer customizer) {
            this.customizer = customizer;
            return this;
        }

        public AutoSkillContextFactory end() {
            // end 时没有配置颜色 → 兜底默认白色
            if (colorEntry == null) {
                addDefaultColor(DEFAULT_COLOR);
            }
            return factory;
        }
    }
}
