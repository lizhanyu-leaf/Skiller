package com.leaf.skiller.foundation.context;

import com.leaf.skiller.foundation.skill.config.impl.SkillColorContext;
import com.leaf.skiller.util.ColorConfig;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class AutoSkillContext implements SkillColorContext {

    private final Player player;
    private final ColorConfig config;
    private final CompoundTag data;
    private final List<Pair<String, Object>> parameters;

    public AutoSkillContext(Player player, ColorConfig config, CompoundTag data, List<Pair<String, Object>> parameters) {
            this.player = player;
            this.config = config;
            this.data = data;
            this.parameters = parameters;
    }

    /**
     * 返回解析后的完整配置：entryMap 中每个键都已按
     * “实例数据优先，缺失回退等级默认”解析完毕，直接按键读取即可。
     */
    public CompoundTag getData() {
        return data;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public ColorConfig color() {
        return config;
    }
    /**
     * 参数列表由工厂在 create 时从 entryMap 的键与解析后的实际值填出，
     * 不含玩家、颜色以及 customizer 写入的瞬态键（如连锁采集的 target）。
     */
    @Override
    public List<Pair<String, Object>> parameters() {
        return parameters;
    }
}
