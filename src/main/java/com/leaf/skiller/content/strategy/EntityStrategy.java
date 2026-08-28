package com.leaf.skiller.content.strategy;

import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.strategy.SkillStrategy;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Strategy interface for entity-based skills that interact with game entities.
 * 基于实体的技能策略接口，与游戏实体进行交互。
 * <p>
 * EntityStrategy extends SkillStrategy to provide specialized functionality for skills
 * that target, manipulate, or visualize entities in the game world.
 * EntityStrategy 扩展了 SkillStrategy，为针对、操控或可视化游戏世界中实体的技能提供专门功能。
 * </p>
 * <p>
 * This strategy type enables:
 * 此策略类型支持：
 * </p>
 * <ul>
 *     <li>Entity selection and filtering based on skill criteria
 *     基于技能标准的实体选择和过滤</li>
 *     <li>Entity visualization with outlines or highlights
 *     使用轮廓或高亮显示实体可视化</li>
 *     <li>Entity manipulation and interaction
 *     实体操控和交互</li>
 *     <li>Area-of-effect entity operations
 *     区域效应实体操作</li>
 * </ul>
 *
 * @param <C> The skill context type, must extend {@linkplain SkillContext}
 *            技能上下文类型，必须扩展 {@linkplain SkillContext}
 * @see SkillStrategy
 * @see Entity
 * @see LivingEntity
 * @see ISkillInstance
 * @since 1.0.0
 * @author Leaf
 */
public interface EntityStrategy<C extends SkillContext> extends SkillStrategy<Entity, C> {}