package com.sing.astatine.utils;

import java.util.Random;
public class XORShiftRandom extends Random {
    long stateA;
    long stateB;
    private long xorshift128plus() {
        long s1 = stateA;
        long s0 = stateB;
        stateA=s0;
        s1^=(s1<<23);
        stateB=s1^s0^(s1>>>18)^(s0>>>5);
        return stateB+s0;
    }
    @Override
    public void setSeed(long seed){
        stateA=Utils.splitMix64(seed);
        stateB=Utils.splitMix64(stateA);
    }
    protected int next(int bits) {
        return (int)(xorshift128plus() >>> (64-bits));
    }
    public double nextOffset(){
        return nextDouble()*2-1;
    }
}
