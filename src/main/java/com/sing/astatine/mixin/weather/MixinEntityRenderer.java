package com.sing.astatine.mixin.weather;

import com.sing.astatine.utils.XORShiftRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {
    @Final
    @Shadow
    private static ResourceLocation SNOW_TEXTURES;
    @Unique
    private final double[] astatine$rainXCoords1 = new double[1024];
    @Unique
    private final double[] astatine$rainYCoords1 = new double[1024];
    @Shadow
    private int rendererUpdateCount;
    @Final
    @Shadow
    private Minecraft mc;
    @Shadow
    private float[] rainXCoords = new float[1024];
    @Shadow
    private float[] rainYCoords = new float[1024];
    @Final
    @Shadow
    private Random random;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void halfCoords(Minecraft mcIn, IResourceManager resourceManagerIn, CallbackInfo ci) {
        rainXCoords = null;
        rainYCoords = null;
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                double f = j - 16;
                double f1 = i - 16;
                double f2 = Math.sqrt(f * f + f1 * f1) * 2;
                astatine$rainXCoords1[i << 5 | j] = -f1 / f2 + 0.5;
                astatine$rainYCoords1[i << 5 | j] = f / f2 + 0.5;
            }
        }
        random = new XORShiftRandom();
    }

    @Shadow
    public abstract void enableLightmap();

    @Shadow
    public abstract void disableLightmap();

    /**
     * @author
     * @reason
     */
    @Overwrite
    protected void renderRainSnow(float partialTicks) {
        net.minecraftforge.client.IRenderHandler renderer = this.mc.world.provider.getWeatherRenderer();
        if (renderer != null) {
            renderer.render(partialTicks, this.mc.world, mc);
            return;
        }

        float f = this.mc.world.getRainStrength(partialTicks);

        if (!(f > 0.0F)) {
            return;
        }
        this.enableLightmap();
        Entity entity = this.mc.getRenderViewEntity();
        World world = this.mc.world;
        int i = MathHelper.floor(entity.posX);
        int j = MathHelper.floor(entity.posY);
        int k = MathHelper.floor(entity.posZ);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.disableCull();
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        GlStateManager.enableBlend();
//        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//        GlStateManager.alphaFunc(516, 0.1F);
//        GlStateManager.enableAlpha();
        double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
        double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
        double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
        int l = MathHelper.floor(d1);
        int i1 = 5;

        if (this.mc.gameSettings.fancyGraphics) {
            i1 = 10;
        }

        float f1 = (float) this.rendererUpdateCount + partialTicks;
        bufferbuilder.setTranslation(-d0, -d1, -d2);
        GlStateManager.color(0.5F, 0.5F, 0.7F, 0.1F);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int k1 = k - i1; k1 <= k + i1; ++k1) {
            for (int l1 = i - i1; l1 <= i + i1; ++l1) {
                int i2 = (k1 - k + 16) * 32 + l1 - i + 16;
                double d3 = this.astatine$rainXCoords1[i2];
                double d4 = this.astatine$rainYCoords1[i2];
                blockpos$mutableblockpos.setPos(l1, 0, k1);
                Biome biome = world.getBiome(blockpos$mutableblockpos);

                if (!biome.canRain() && !biome.getEnableSnow()) {
                    continue;
                }
                int j2 = world.getPrecipitationHeight(blockpos$mutableblockpos).getY();
                int k2 = Math.max(j - i1, j2);
                int l2 = Math.max(j + i1, j2);
                int i3 = Math.max(j2, l);
                this.random.setSeed((long) l1 * l1 * 3121 + l1 * 45238971L ^ (long) k1 * k1 * 418711 + k1 * 13761L);
                blockpos$mutableblockpos.setPos(l1, k2, k1);
                float f2 = biome.getTemperature(blockpos$mutableblockpos);
                if (world.getBiomeProvider().getTemperatureAtHeight(f2, j2) >= 0.15F) {

                    double d5 = -((double) (this.rendererUpdateCount + l1 * l1 * 3121 + l1 * 45238971 + k1 * k1 * 418711 + k1 * 13761 & 31) + (double) partialTicks) / 32.0D * (3.0D + random.nextDouble());
                    double d6 = (double) ((float) l1 + 0.5F) - entity.posX;
                    double d7 = (double) ((float) k1 + 0.5F) - entity.posZ;
                    float f4 = (float) (((1.0F - (d6 * d6 + d7 * d7) / i1 * i1) * 0.5F + 0.5F) * f);
                    blockpos$mutableblockpos.setPos(l1, i3, k1);
                    int j3 = 15 << 20;
                    int k3 = j3 >> 16 & 65535;
                    int l3 = 0;
                    bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                    bufferbuilder.pos((double) l1 - d3 + 1D, l2, (double) k1 - d4 + 1D).color(1.0F, 1.0F, 1.0F, 1).endVertex();
                    bufferbuilder.pos((double) l1 + d3, l2, (double) k1 + d4).color(1.0F, 1.0F, 1.0F, 1).endVertex();
                    bufferbuilder.pos((double) l1 + d3, k2, (double) k1 + d4).color(1.0F, 1.0F, 1.0F, 1).endVertex();
                    bufferbuilder.pos((double) l1 - d3 + 1D, k2, (double) k1 - d4 + 1D).color(1.0F, 1.0F, 1.0F, 1).endVertex();
                    tessellator.draw();
                } else {
                    double d8 = -((float) (this.rendererUpdateCount & 511) + partialTicks) / 512.0F;
                    double d9 = random.nextDouble() + (double) f1 * 0.01D * (double) ((float) random.nextGaussian());
                    double d10 = random.nextDouble() + (double) (f1 * (float) random.nextGaussian()) * 0.001D;
                    double d11 = (double) ((float) l1 + 0.5F) - entity.posX;
                    double d12 = (double) ((float) k1 + 0.5F) - entity.posZ;
                    float f5 = (float) (((1.0F - ((d11 * d11 + d12 * d12) / ((float) i1 * i1))) * 0.3F + 0.5F) * f);
                    blockpos$mutableblockpos.setPos(l1, i3, k1);
                    int j4 = 216;
                    int k4 = 60;
                    this.mc.getTextureManager().bindTexture(SNOW_TEXTURES);
                    bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                    bufferbuilder.pos((double) l1 - d3 + 1, l2, (double) k1 - d4 + 1).tex(0.0D + d9, (double) k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                    bufferbuilder.pos((double) l1 + d3, l2, (double) k1 + d4).tex(1.0D + d9, (double) k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                    bufferbuilder.pos((double) l1 + d3, k2, (double) k1 + d4).tex(1.0D + d9, (double) l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                    bufferbuilder.pos((double) l1 - d3 + 1, k2, (double) k1 - d4 + 1).tex(0.0D + d9, (double) l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                    tessellator.draw();
                }
            }
        }
        bufferbuilder.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
//        GlStateManager.disableAlpha();
//        GlStateManager.alphaFunc(516, 0.1F);
        this.disableLightmap();
    }
}
