package com.sing.astatine.mixin.fastrandom;

import net.minecraft.world.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Mixin({Explosion.class})
public class MixinExplosion {
    @Redirect(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;DDDFZZ)V", at = @At(value = "NEW", target = "()Ljava/util/Random;", remap = false))
    private Random useThreadLocalRandom() {
        return ThreadLocalRandom.current();
    }
}
