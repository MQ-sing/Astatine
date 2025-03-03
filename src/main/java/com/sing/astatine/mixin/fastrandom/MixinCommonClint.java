package com.sing.astatine.mixin.fastrandom;

import net.minecraft.item.Item;
import net.minecraft.server.network.NetHandlerLoginServer;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.util.datafix.fixes.ZombieProfToType;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Mixin({Item.class, NetHandlerLoginServer.class, TileEntityDispenser.class, TileEntityEnchantmentTable.class, MathHelper.class, ZombieProfToType.class})
public class MixinCommonClint {
    @Redirect(method = "<clinit>", at = @At(value = "NEW", target = "()Ljava/util/Random;", remap = false))
    private static Random useThreadLocalRandom() {
        return ThreadLocalRandom.current();
    }
}
