package com.sing.astatine.utils;

import com.google.common.base.Preconditions;
import com.sing.astatine.Configuration;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class ChunkCache {
    static class Element implements Comparable<Element>{
        public final Chunk value;
        public long timeLoaded;
        public long timeKept;

        public Element(Chunk value, long timeLoaded) {
            this.value = value;
            this.timeLoaded = timeLoaded;
        }

        @Override
        public int compareTo(@NotNull ChunkCache.Element o) {
            return Utils.doCompare(Long.compare(timeLoaded,o.timeLoaded), Integer.compare(this.hashCode(),o.hashCode()));
        }
    }
    private final Long2ObjectOpenHashMap<Element> toCache=new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<Element> cache = new Long2ObjectOpenHashMap<>();
    private final ObjectRBTreeSet<Element> keepTimeChunks = new ObjectRBTreeSet<>();
    private final int reservedCount;
    private final int maxSize;
    private final long minimumTime;
    private final long maxIdleTime;

    public ChunkCache(int maxSize,int reservedCount,int minimumTime,int maxIdleTime) {
        Preconditions.checkArgument(maxSize>2,"Max size must be greater than 2");
        Preconditions.checkArgument(maxSize-reservedCount>2,"reservedCount must be greater than maxSize for 2");
        this.maxSize = maxSize;
        this.reservedCount=reservedCount;
        this.minimumTime=minimumTime;
        this.maxIdleTime=maxIdleTime;
    }

    public Chunk get(long key) {
        final Element elem = cache.get(key);
        if(elem==null)return null;
        return elem.value;
    }
    public void store(long time, Chunk value) {
        final long key = ChunkPos.asLong(value.x, value.z);
        final Element elem = new Element(value, time);
        toCache.putIfAbsent(key,elem);
    }
    public void cache(long currentTime,Chunk target){
        final long key = Utils.chunkPos(target);
        final Element chunk = toCache.remove(key);
        if(chunk==null)return;
        long keepTime = currentTime - chunk.timeLoaded;
        final Element previous = cache.get(key);
        if(previous==null) {
            if(keepTime<minimumTime)return;
            chunk.timeKept =keepTime;
            cache.put(key, chunk);
            keepTimeChunks.add(chunk);
        }else{
            final long totalKept = previous.timeKept + keepTime;
            if(totalKept < minimumTime)return;
            keepTimeChunks.remove(previous);
            previous.timeKept = totalKept;
            keepTimeChunks.add(previous);
        }
        int overflowCount=cache.size()-maxSize;
        while(overflowCount-->0){
            final Element first = keepTimeChunks.first();
            keepTimeChunks.remove(first);
            cache.remove(Utils.chunkPos(first.value));
        }
    }
    public void tide(long currentTime){
        //TODO: add config options for the factor(2)
        int maxRemoves= (keepTimeChunks.size() - reservedCount)/2;
        Iterator<Element> it= keepTimeChunks.iterator();
        while(it.hasNext() && maxRemoves-->0){
            Element element=it.next();
            if(element.timeLoaded - currentTime> maxIdleTime) {
                it.remove();
                cache.remove(Utils.chunkPos(element.value));
            }
        }
    }
    public int size(){
        return cache.size();
    }
}
