package com.sing.astatine.core;

import net.minecraft.launchwrapper.Launch;

public class CoreModCore {
    public static boolean isRuntimeDeobfuscated=(Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    public static String mayDeobfuscated(String srg,String deobfuscatedName){
        return isRuntimeDeobfuscated?deobfuscatedName:srg;
    }
    public static String getDescReturnType(String desc){
        return desc.substring(desc.lastIndexOf(')')+1);
    }
    public static int getTypeOffset(String type){
        switch (type.charAt(0)){
            case'Z':case 'I':return 0;
            case 'J':return 1;
            case 'F':return 2;
            case 'D':return 3;
            case 'L':return 4;
            case 'V':return 5;
        }
        throw new IllegalArgumentException("Unknown type '"+type+"'");
    }
    public static int doTypeOffset(int code,String type){
        return code+getTypeOffset(type);
    }
}
