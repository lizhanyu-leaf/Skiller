package com.leaf.skiller.foundation.strategy;

import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface SkillStrategy<T, C extends SkillContext> {
    void collect(Set<T> set, C context, ISkillInstance<C> instance);
    boolean canCollect(C context, ISkillInstance<C> instance);

    ResourceLocation getRendererId();
}
