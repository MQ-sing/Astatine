package com.sing.astatine.utils;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class ChunkCache {
    static class Element implements Comparable<Element>{
        public final Chunk value;
        public long lastLoaded;
        public long timeKept=0;

        public Element(Chunk value, long lastLoaded) {
            this.value = value;
            this.lastLoaded = lastLoaded;
        }
        public Element updateTime(long currentTime){
            timeKept+=currentTime-lastLoaded;
            lastLoaded=currentTime;
            return this;
        }

        @Override
        public int compareTo(@NotNull ChunkCache.Element o) {
            return Utils.doCompare(Long.compare(lastLoaded,o.lastLoaded), Integer.compare(this.hashCode(),o.hashCode()));
        }
    }
    private final Long2LongOpenHashMap toCache=new Long2LongOpenHashMap();
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
        toCache.putIfAbsent(key,time);
    }
    public void cache(long currentTime,Chunk target){
        final long key = Utils.chunkPos(target);
        final long time = toCache.remove(key);
        long keepTime = currentTime - time;
        final Element previous = cache.get(key);
        if(previous==null) {
            if(keepTime<minimumTime)return;
            final Element element = new Element(target, time).updateTime(currentTime);
            cache.put(key, element);
            keepTimeChunks.add(element);
        }else{
            previous.updateTime(currentTime);
            keepTimeChunks.remove(previous);
            if(previous.timeKept < minimumTime)return;
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
            if(element.lastLoaded - currentTime > maxIdleTime) {
                it.remove();
                cache.remove(Utils.chunkPos(element.value));
            }
        }
    }
    public int size(){
        return cache.size();
    }
}
