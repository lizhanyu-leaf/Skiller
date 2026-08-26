package com.leaf.skiller.mixin;

import com.leaf.skiller.Skiller;
import com.leaf.skiller.api.registry.SkillerBuiltInRegistries;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {
    static {
        SkillerBuiltInRegistries.init();
    }

    @WrapOperation(method = "validate", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;forEach(Ljava/util/function/Consumer;)V"))
    private static <T extends Registry<?>> void skiller$ourRegistriesAreNotEmpty(Registry<T> instance, Consumer<T> consumer, Operation<Void> original) {
        Consumer<T> callback = (t) -> {
            if (!t.key().location().getNamespace().equals(Skiller.MOD_ID))
                consumer.accept(t);
        };

        original.call(instance, callback);
    }
}
