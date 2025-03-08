package com.sing.astatine.mixin.weather;

import com.sing.astatine.utils.Utils;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRain;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(ParticleRain.class)
public abstract class MixinParticleRain extends Particle {
    protected MixinParticleRain(World worldIn, double posXIn, double posYIn, double posZIn) {
        super(worldIn, posXIn, posYIn, posZIn);
    }
    @Redirect(method = "<init>",at=@At(value = "INVOKE",target = "Ljava/lang/Math;random()D"))
    private double threadLocalRandom(){
        return ThreadLocalRandom.current().nextDouble();
    }
    /**
     * @author MQ-sing
     * @reason main logic
     */
    @Overwrite
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.motionY -= this.particleGravity;
        this.move(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;


        if (this.particleMaxAge-- <= 0)
        {
            this.setExpired();
        }

        if (this.onGround)
        {
            if (Utils.random() < 0.5D)
            {
                this.setExpired();
            }

            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }

        BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
        IBlockState iblockstate = this.world.getBlockState(blockpos);
        Material material = iblockstate.getMaterial();

        if (material.isLiquid() || material.isSolid())
        {
            double d0;

            if (iblockstate.getBlock() instanceof BlockLiquid)
            {
                d0 = 1.0F - BlockLiquid.getLiquidHeightPercent(iblockstate.getValue(BlockLiquid.LEVEL));
            }
            else
            {
                d0 = iblockstate.getBoundingBox(this.world, blockpos).maxY;
            }

            double d1 = (double) MathHelper.floor(this.posY) + d0;

            if (this.posY < d1)
            {
                this.setExpired();
            }
        }
    }
//    @Override
//    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
//    {
//        float f = (float)this.particleTextureIndexX / 16.0F;
//        float f1 = f + 0.0624375F;
//        float f2 = (float)this.particleTextureIndexY / 16.0F;
//        float f3 = f2 + 0.0624375F;
//        float f4 = 0.1F * this.particleScale;
//
//        if (this.particleTexture != null)
//        {
//            f = this.particleTexture.getMinU();
//            f1 = this.particleTexture.getMaxU();
//            f2 = this.particleTexture.getMinV();
//            f3 = this.particleTexture.getMaxV();
//        }
//
//        float f5 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
//        float f6 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
//        float f7 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
//        int j = 13;
//        int k = 13;
//        Vec3d[] avec3d = new Vec3d[] {
//                new Vec3d(-rotationX * f4 - rotationXY * f4, -rotationZ * f4, -rotationYZ * f4 - rotationXZ * f4),
//                new Vec3d(-rotationX * f4 + rotationXY * f4, rotationZ * f4, -rotationYZ * f4 + rotationXZ * f4),
//                new Vec3d(rotationX * f4 + rotationXY * f4, rotationZ * f4, rotationYZ * f4 + rotationXZ * f4),
//                new Vec3d(rotationX * f4 - rotationXY * f4, -rotationZ * f4, rotationYZ * f4 - rotationXZ * f4)};
//
//        if (this.particleAngle != 0.0F)
//        {
//            float f8 = (this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks)*0.5F;
//            float f9 = MathHelper.cos(f8);
//            final float fsin = MathHelper.sin(f8);
//            float f10 = fsin * (float)cameraViewDir.x;
//            float f11 = fsin * (float)cameraViewDir.y;
//            float f12 = fsin * (float)cameraViewDir.z;
//            Vec3d vec3d = new Vec3d(f10, f11, f12);
//
//            for (int l = 0; l < 4; ++l)
//            {
//                avec3d[l] = vec3d
//                        .scale(2.0D * avec3d[l]
//                                .dotProduct(vec3d))
//                        .add(avec3d[l]
//                                .scale((double)(f9 * f9) -
//                                        vec3d.dotProduct(vec3d)))
//                        .add(vec3d
//                                .crossProduct(avec3d[l]).scale(2.0F * f9));
//            }
//        }
//
//        buffer.pos((double)f5 + avec3d[0].x, (double)f6 + avec3d[0].y, (double)f7 + avec3d[0].z).tex(f1, f3).lightmap(j, k).endVertex();
//        buffer.pos((double)f5 + avec3d[1].x, (double)f6 + avec3d[1].y, (double)f7 + avec3d[1].z).tex(f1, f2).lightmap(j, k).endVertex();
//        buffer.pos((double)f5 + avec3d[2].x, (double)f6 + avec3d[2].y, (double)f7 + avec3d[2].z).tex(f, f2).lightmap(j, k).endVertex();
//        buffer.pos((double)f5 + avec3d[3].x, (double)f6 + avec3d[3].y, (double)f7 + avec3d[3].z).tex(f, f3).lightmap(j, k).endVertex();
//    }
}
