package com.sing.astatine.mixin.faststar;

import com.sing.astatine.Configuration;
import com.sing.astatine.utils.XORShiftRandom;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
    @Unique
    private static final XORShiftRandom miscFixes$random =new XORShiftRandom();
    /**
     * @author MQ-sing
     * @reason main feature
     */
    @Overwrite
    private void renderStars(BufferBuilder bufferBuilderIn)
    {
        miscFixes$random.setSeed(Configuration.StarGen.seed);
        bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);

        for (int i = 0; i < Configuration.StarGen.count; ++i)
        {
            double d0 = miscFixes$random.nextFloat() * 2.0F - 1.0F;
            double d1 = miscFixes$random.nextFloat() * 2.0F - 1.0F;
            double d2 = miscFixes$random.nextFloat() * 2.0F - 1.0F;
            double d3 = Configuration.StarGen.baseSize + miscFixes$random.nextFloat() * Configuration.StarGen.sizeFluctuation;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;

            if (!(d4 < 1.0D) || !(d4 > 0.01D)) {
                continue;
            }
            d4 = 1.0D / Math.sqrt(d4);
            d0 = d0 * d4;
            d1 = d1 * d4;
            d2 = d2 * d4;
            double d5 = d0 * 100D;
            double d6 = d1 * 100D;
            double d7 = d2 * 100D;
            double d8 = Math.atan2(d0, d2);
            double d9 = Math.sin(d8);
            double d10 = Math.cos(d8);
            double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
            double d12 = Math.sin(d11);
            double d13 = Math.cos(d11);
            double d14 = miscFixes$random.nextDouble() * Math.PI * 2.0D;
            double d15 = Math.sin(d14);
            double d16 = Math.cos(d14);

            for (int j = 0; j < 4; ++j)
            {
                double d18 = (double)((j & 2) - 1) * d3;
                double d19 = (double)((j + 1 & 2) - 1) * d3;
                double d21 = d18 * d16 - d19 * d15;
                double d22 = d19 * d16 + d18 * d15;
                double d23 = d21 * d12;
                double d24 = -d21 * d13;
                double d25 = d24 * d9 - d22 * d10;
                double d26 = d22 * d9 + d24 * d10;
                final double starY = d6 + d23;
                bufferBuilderIn.pos(d5 + d25, starY, d7 + d26).endVertex();
            }
        }
    }
}
