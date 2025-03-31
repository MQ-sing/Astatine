package com.sing.astatine;

import com.sing.astatine.utils.Utils;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class ConfigurationRuntime {
    public static String[] languageList;
    public static final double maxTileEntityRenderDistanceSquared =Configuration.maxTileEntityRenderDistance*Configuration.maxTileEntityRenderDistance;
    public static final double maxEntityRenderDistanceSquared=Configuration.maxEntityRenderDistance*Configuration.maxEntityRenderDistance;
    static {
        if (Configuration.Lang.languageSelector) {
            final File file = Paths.get(Launch.minecraftHome.getAbsolutePath(), "config", "astatine_langlist.txt").toFile();
            try {
                Utils.createFile(file);
                languageList = Files.readAllLines(file.toPath(), StandardCharsets.US_ASCII).toArray(new String[0]);
            } catch (Exception e) {
                System.out.println("Cannot load languages file!exception:" + e);
            }
            final int i = ArrayUtils.indexOf(languageList, "en_us");
            if(i==-1){
                final String[] newList = new String[languageList.length + 1];
                newList[languageList.length]="en_us";
                System.arraycopy(languageList,0,newList,0,languageList.length);
                languageList=newList;
            }else if(i!=languageList.length-1){
                languageList[i]=languageList[languageList.length-1];
                languageList[languageList.length-1]="en_us";
            }
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                try {
                    Files.write(file.toPath(), Arrays.asList(languageList));
                } catch (IOException e) {
                    System.out.println("Cannot save languages file!exception:" + e);
                }
            }));
        }
    }
}
