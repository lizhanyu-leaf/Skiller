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
        // Defensive: a null map would break extraData(...) later.
        // 防御：null 映射会在此后的 extraData(...) 调用中引发 NPE。
        this.extraData = extraData != null ? extraData : new HashMap<>();
    }

    public SkillContextEnvironment(Player player, Level level, Event triggerEvent) {
        this(player, level, triggerEvent, new HashMap<>());
    }

    // 基本访问器
    public Player getPlayer() { return player; }
    public Level getLevel() { return level; }
    public SkillTriggerEvent getTriggerEvent() { return triggerEvent; }
    public Object getEvent() {
        // Returns null when this environment was created without a trigger event,
        // instead of throwing NPE on the null event class.
        // 当环境创建时没有触发事件时返回 null，而不是因空的イベント类抛出 NPE。
        if (eventClass == null) return null;
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
