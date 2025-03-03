package com.sing.astatine.utils;

public class XORShiftRandom {
    long stateA;
    long stateB;
    private long xorshift128plus() {
        long s1 = stateA;
        long s0 = stateB;
        stateA=s0;
        s1^=(s1<<23);
        stateB=s1^s0^(s1>>17)^(s0>>26);
        return stateB+s0;
    }
    public void setSeed(long seedA,long seedB){
        stateA = seedA;
        stateB=seedB;
    }

    public double nextDouble() {
        long value = xorshift128plus();
        return ((double) (value & 0x7FFFFFFFFFFFFFFFL)) / 0x7FFFFFFFFFFFFFFFL;
    }

    public float nextFloat() {
        long value = xorshift128plus();
        return ((float) (value & 0x7FFFFFFFFFFFFFFFL)) / 0x7FFFFFFFFFFFFFFFL;
    }
}
