package com.leaf.skiller.foundation.context;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public interface SkillContext {
    Player player();

    /**
     * 返回该技能可显示的参数列表。
     * <p>
     * 键为蛇形命名的配置键（如 {@code range}、{@code pull_strength}），
     * 值为解析后的实际值；玩家与颜色等环境字段不算参数。
     * 实现应按配置顺序返回，客户端 tooltip 会把键翻译成
     * {@code config.<namespace>.<key>} 后以“键: 值”逐行显示。
     * </p>
     *
     * @return 参数键值对列表，默认无参数
     */
    default List<Pair<String, Object>> parameters() {
        return List.of();
    }
}