package com.sing.astatine.mixin.stars;

import com.sing.astatine.Configuration;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(World.class)
public abstract class MixinWorld {
    @Unique
    private static final float astatine$FREQ =(float)(Math.PI * Configuration.starShrinkingFreq);
    @Unique
    private float astatine$totalTime;
    /**
     * @author MQ-sing
     * @reason re-enable this feature.
     */
    @SideOnly(Side.CLIENT)
    @Overwrite(remap=false)
    public float getStarBrightnessBody(float partialTicks)
    {
        astatine$totalTime+=partialTicks/16;
        float f1 = (MathHelper.sin(astatine$totalTime * astatine$FREQ) * Configuration.starShrinkingAmplitude);
        return MathHelper.clamp(Configuration.starBrightness+f1,0,1);
    }
}
