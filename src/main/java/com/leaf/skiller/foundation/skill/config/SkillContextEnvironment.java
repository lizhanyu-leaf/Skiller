package com.leaf.skiller.foundation.skill.config;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 技能上下文环境
 * 包含创建上下文所需的所有信息
 * Container for all information needed to create context
 */
public class SkillContextEnvironment {
    private final Player player;
    private final Level level;
    private Class<?> eventClass;
    private final SkillTriggerEvent triggerEvent;  // 触发事件
    private Map<String, Object> extraData;   // 额外数据

    public SkillContextEnvironment(Player player, Level level, @Nullable Event triggerEvent, Map<String, Object> extraData) {
        this.player = player;
        this.level = level;
        this.triggerEvent = new SkillTriggerEvent() {
            public <T> T getAs(Class<T> eventType) {
                return eventType.cast(triggerEvent);
            }
        };
        if (triggerEvent != null) {
            this.eventClass = triggerEvent.getClass();
        }
        this.extraData = extraData;
    }

    public SkillContextEnvironment(Player player, Level level, Event triggerEvent) {
        this(player, level, triggerEvent, new HashMap<>());
    }

    // 基本访问器
    public Player getPlayer() { return player; }
    public Level getLevel() { return level; }
    public SkillTriggerEvent getTriggerEvent() { return triggerEvent; }
    public Object getEvent() {
        return triggerEvent.getAs(eventClass);
    }
    public <T> T getExtraData(String key, Class<T> clazz) {
        return clazz.cast(extraData.get(key));
    }

    public SkillContextEnvironment extraData(String key, Object o) {
        extraData.put(key, o);
        return this;
    }

    public static SkillContextEnvironment noEvent(Player player, Level level) {
        return new SkillContextEnvironment(player, level, null);
    }

    public static SkillContextEnvironment withEvent(Player player, Level level, Event event) {
        return new SkillContextEnvironment(player, level, event);
    }
}
