package com.leaf.skiller.foundation.skill.config;

/**
 * 技能触发事件
 * 封装各种可能触发技能的事件
 * Encapsulates various events that might trigger skills
 */
public interface SkillTriggerEvent {
    <T> T getAs(Class<T> eventType);
}
