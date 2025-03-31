package com.sing.astatine.mixin.chunkcache;

import com.sing.astatine.utils.ICachedChunkProvider;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public class MixinChunk {
    @Shadow
    @Final
    private World world;
    @Shadow
    @Final
    public int x;
    @Shadow
    @Final
    public int z;
    @Inject(method = "onLoad",at=@At("TAIL"))
    void onLoad(CallbackInfo ci){
        if(!world.isRemote) {
            final WorldServer worldserver=((WorldServer)world);
            final long time = worldserver.getTotalWorldTime();
            ((ICachedChunkProvider) world.getChunkProvider()).astatine$getCache().store(time, (Chunk) (Object) this);
        }
    }
    @Inject(method = "onUnload",at=@At("TAIL"))
    void onUnload(CallbackInfo ci){
        if(!world.isRemote) {
            final WorldServer worldserver=((WorldServer)world);
            final long time = worldserver.getTotalWorldTime();
            ((ICachedChunkProvider) world.getChunkProvider()).astatine$getCache().cache(time, (Chunk) (Object) this);
        }
    }
}
