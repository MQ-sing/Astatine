package com.sing.astatine.mixin.fastrandom;

import net.minecraft.client.particle.ParticleSpell;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Mixin({ParticleSpell.class, TileEntityEnchantmentTable.class})
public class MixinClientClint {
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "()Ljava/util/Random;", remap = false))
    private static Random useThreadLocalRandom() {
        return ThreadLocalRandom.current();
    }
}
