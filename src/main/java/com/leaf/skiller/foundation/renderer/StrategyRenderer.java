package com.leaf.skiller.foundation.renderer;

import com.leaf.skiller.client.ClientSkillCache;
import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.leaf.skiller.foundation.skill.config.SkillContextEnvironment;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Optional;

public interface StrategyRenderer<C extends SkillContext> {

    default Optional<C> getContext(Minecraft mc, ClientLevel level, Player player, ISkillInstance<C> instance) {
        if (!ClientSkillCache.isInstanceKeyPressed(instance)) return Optional.empty();

        var context = instance.skill().getFactory()
                .create(SkillContextEnvironment.noEvent(player, level), instance);
        return Optional.ofNullable(context);
    }

    void render(C context, ISkillInstance<C> instance, Minecraft mc, PoseStack stack, Camera camera, MultiBufferSource buffer);
    RenderLevelStageEvent.Stage getStage();
    ResourceLocation getId();

    default void render(Minecraft mc, ISkillInstance<C> instance, ClientLevel level, PoseStack poseStack, Camera camera, MultiBufferSource buffer) {
        Optional<C> optional = getContext(mc, level, mc.player, instance);
        if (optional.isEmpty()) return;
        render(optional.get(), instance, mc, poseStack, camera, buffer);
    }
}
