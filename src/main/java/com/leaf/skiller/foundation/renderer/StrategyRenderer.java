package com.leaf.skiller.foundation.renderer;

import com.leaf.skiller.foundation.context.SkillContext;
import com.leaf.skiller.foundation.skill.ISkillInstance;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Optional;

public interface StrategyRenderer<C extends SkillContext> {
    Optional<C> getContext(Minecraft mc, ClientLevel level, Player player, ISkillInstance<C> instance);
    void render(C context, ISkillInstance<C> instance, Minecraft mc, PoseStack stack, Camera camera, MultiBufferSource buffer);
    RenderLevelStageEvent.Stage getStage();

    default void render(Minecraft mc, ISkillInstance<C> instance, ClientLevel level, PoseStack poseStack, Camera camera, MultiBufferSource buffer) {
        Optional<C> optional = getContext(mc, level, mc.player, instance);
        if (optional.isEmpty()) return;
        render(optional.get(), instance, mc, poseStack, camera, buffer);
    }
}
