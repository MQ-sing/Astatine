package com.sing.astatine.mixin.stars;

import com.sing.astatine.Configuration;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(World.class)
public abstract class MixinWorld {
    @Shadow public abstract float getCelestialAngle(float partialTicks);

    @Unique
    private static final float astatine$FREQ =(float)(Math.PI * Configuration.StarTwinkling.frequency);
    @Unique
    private float astatine$totalTime;
    /**
     * @author MQ-sing
     * @reason the main feature
     */
    @SideOnly(Side.CLIENT)
    @Overwrite(remap=false)
    public float getStarBrightnessBody(float partialTicks)
    {
        astatine$totalTime+=partialTicks/16;
        float angle = MathHelper.clamp(1.0F - (MathHelper.cos(getCelestialAngle(partialTicks) * ((float)Math.PI * 2F)) * 4.0F + 0.25F),0,1);
        if(Configuration.StarTwinkling.timeAttenuation !=0&&angle==0)return 0;
        float f1 =MathHelper.sin(astatine$totalTime * astatine$FREQ) * Configuration.StarTwinkling.amplitude;
        return (Configuration.StarTwinkling.base +f1)*(1+(angle*angle)*Configuration.StarTwinkling.timeAttenuation)/(1+Configuration.StarTwinkling.timeAttenuation);
    }
}
