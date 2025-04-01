package com.sing.astatine.mixin.renderskip;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.sing.astatine.ConfigurationRuntime;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
    @WrapWithCondition(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;render(Lnet/minecraft/tileentity/TileEntity;FI)V"))
    boolean shouldRenderTileEntity(TileEntityRendererDispatcher instance, TileEntity tile, float partialTicks, int destroyStage) {
        return tile.getPos().distanceSqToCenter(instance.entityX, instance.entityY, instance.entityZ) < ConfigurationRuntime.maxTileEntityRenderDistanceSquared;
    }
    @Redirect(method = "renderEntities",at= @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;DDD)Z"))
    boolean shouldRenderEntity(RenderManager instance, Entity entityIn, ICamera camera, double camX, double camY, double camZ){
        final double a = entityIn.posX-camX;
        final double b = entityIn.posY-camY;
        final double c = entityIn.posZ-camZ;
        return a*a+b*b+c*c<ConfigurationRuntime.maxEntityRenderDistanceSquared && instance.shouldRender(entityIn,camera,camX,camY,camZ);
    }
}
