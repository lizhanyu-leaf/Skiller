package com.leaf.skiller.foundation.context;

import com.leaf.skiller.foundation.skill.config.impl.SkillColorContext;
import com.leaf.skiller.util.ColorConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class AutoSkillContext implements SkillColorContext {
    private final Player player;
    private final ColorConfig config;
    private final CompoundTag data;

    public AutoSkillContext(Player player, ColorConfig config, CompoundTag data) {
        this.player = player;
        this.config = config;
        this.data = data;
    }

    /**
     * 返回解析后的完整配置：entryMap 中每个键都已按
     * “实例数据优先，缺失回退等级默认”解析完毕，直接按键读取即可。
     */
    public CompoundTag getData() {
        return data;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public ColorConfig getColor() {
        return config;
    }
}
