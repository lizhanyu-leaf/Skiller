package com.leaf.skiller.foundation.skill.config;

import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;

public interface SkillContextFactory<C extends SkillContext> {

    /**
     * 从Instance数据创建上下文
     */
    C create(SkillContextEnvironment env, ISkillInstance<C> instance);

    /**
     * 从环境创建上下文
     * 支持复杂场景的上下文创建
     */
    C createDefault(SkillContextEnvironment env, int level);
}
