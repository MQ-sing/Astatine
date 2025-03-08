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
        stateB=s1^s0^(s1>>17)^(s0>>26);
        return stateB+s0;
    }
    @Override
    public void setSeed(long seed){
        stateB=seed^0xBEEFC418C31AE1CDL;
        stateA=~stateB+0xCC;
    }
    protected int next(int bits) {
        return (int)(xorshift128plus() >>> (64-bits));
    }
}
