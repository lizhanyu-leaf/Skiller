package com.leaf.skiller.mixin;

import com.leaf.skiller.client.renderer.entity.EntityOutlineRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "shouldEntityAppearGlowing", at = @At("TAIL"), cancellable = true)
    private void entityGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (EntityOutlineRenderer.glowingEntities.contains(entity)) {
            EntityOutlineRenderer.glowingEntities.remove(entity);
            cir.setReturnValue(true);
        }
    }
}
