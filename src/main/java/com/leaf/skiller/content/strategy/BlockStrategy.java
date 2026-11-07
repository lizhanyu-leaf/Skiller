package com.leaf.skiller.content.strategy;

import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.strategy.SkillStrategy;
import net.minecraft.core.BlockPos;

public interface BlockStrategy<C extends SkillContext> extends SkillStrategy<BlockPos, C> {}
