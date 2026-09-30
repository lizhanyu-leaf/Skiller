package com.leaf.skiller.mixin;

import com.leaf.skiller.SkillerLDLibPlugin;
import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AccessorRegistries.class)
public class AccessorRegistriesMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private static void onInit(CallbackInfo ci) {
        SkillerLDLibPlugin.loadAccessor();
    }
}
