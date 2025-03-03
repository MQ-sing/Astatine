package com.sing.astatine.core;

import net.minecraft.launchwrapper.Launch;

public class CoreModCore {
    public static boolean isRuntimeDeobfuscated=(Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    public static String mayDeobfuscated(String srg,String deobfuscatedName){
        return isRuntimeDeobfuscated?deobfuscatedName:srg;
    }
}
