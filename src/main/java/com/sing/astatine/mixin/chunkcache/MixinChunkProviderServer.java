package com.sing.astatine.mixin.chunkcache;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.sing.astatine.Configuration;
import com.sing.astatine.utils.ICachedChunkProvider;
import com.sing.astatine.utils.ChunkCache;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkProviderServer.class)
public abstract class MixinChunkProviderServer implements ICachedChunkProvider {
    @Shadow
    public final Long2ObjectMap<Chunk> loadedChunks = new Long2ObjectOpenHashMap<Chunk>(8192);
    @Shadow
    @Final
    public WorldServer world;
    @Unique
    private ChunkCache astatine$cache;
    @Unique
    private long astatine$total;
    @Unique
    private long astatine$hit;
    @Unique
    private long astatine$lastTidy;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo i) {
        astatine$cache = new ChunkCache(Configuration.ChunkCache.maxCacheSize, Configuration.ChunkCache.minReservedChunks, Configuration.ChunkCache.minLoadedTimeToRetain, Configuration.ChunkCache.maxIdleTimeToPurge);
        astatine$lastTidy = 0;
    }

    @Inject(method = "loadChunk(IILjava/lang/Runnable;)Lnet/minecraft/world/chunk/Chunk;", at = @At(
            target = "Lnet/minecraft/world/gen/ChunkProviderServer;getLoadedChunk(II)Lnet/minecraft/world/chunk/Chunk;",
            value = "INVOKE_ASSIGN"))
    void findInCache(int x, int z, Runnable runnable, CallbackInfoReturnable<Chunk> cir, @Local(ordinal = 0) LocalRef<Chunk> chunk1) {
        if (chunk1.get() != null) return;
        long pos = ChunkPos.asLong(x, z);
        final Chunk cached = astatine$cache.get(pos);
        if (Configuration.ChunkCache.debug) ++astatine$total;
        if (cached != null) {
            if (Configuration.ChunkCache.debug) ++astatine$hit;
            cached.markLoaded(true);
            if (Configuration.ChunkCache.debug) {
                if (Math.random() > 0.92)
                    System.out.println("Cache hit #" + astatine$hit + ", " + astatine$total + " loadings in total." + astatine$cache.size() + " chunks cached. rate: " + Math.round((float) astatine$hit / astatine$total * 100F) + "%");
                loadedChunks.put(pos, cached);
            }
            chunk1.set(cached);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfoReturnable<Boolean> cir) {
        final long time = world.getTotalWorldTime();
        if (time - astatine$lastTidy > Configuration.ChunkCache.cleanupInterval) {
            astatine$cache.tide(time);
            astatine$lastTidy = time;
        }
    }

    @Override
    public ChunkCache astatine$getCache() {
        return astatine$cache;
    }
}
