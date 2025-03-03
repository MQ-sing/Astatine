package com.sing.astatine;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.sing.astatine.utils.Utils;
import net.minecraft.launchwrapper.Launch;
import org.jetbrains.annotations.Nls;

import java.io.File;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class Configuration {
    // Used in CoreMod.
    @ConfigComment("Forces ASCII font rendering for all texts, " +
            "fixing a bug where Minecraft always uses Unicode rendering " +
            "if text contains any Unicode characters (ignoring language settings).")
    public static boolean forceAsciiFont = true;

    @ConfigComment({
            "Optimizes language file loading by:",
            "- Skipping redundant resource reloads when switching languages",
            "- Replacing regex-based .lang file parsing with a manual parser"
    })
    public static boolean fastLang = true;
    @ConfigComment({
            "Disable a forge's extension for .lang files,",
            "It will cause the file be read over twice(1 for Forge's,1 for Vanilla),and worse performance when using fastLang",
            "Close it when you find some issues on translation keys",
            "Requires fastLang to be opened."
    })
    public static boolean noForgeLangExtension = false;


    @ConfigComment("[Experimental] Enables a new language selector UI. " +
            "Warning: May cause instability!")
    public static boolean languageSelector = false;

    @ConfigComment({"[Experimental] Replaces Vanilla's java.util.Random " +
            "with ThreadLocalRandom in specific classes for better thread performance.",
            "Behaves strangely most of the time."})
    public static boolean fastRandom = false;

    @ConfigComment({
            "Controls the number of stars rendered in the sky:",
            "-1: Vanilla behavior (default 1500 stars)",
            "=0: No stars rendered",
            ">0: Custom star count",
            "Note: Uses a faster RNG during star generation"
    })
    public static int starCount = -1;
    @ConfigComment({
            "Controls the size of stars,required starCount not to be -1.",
            "Vanilla: 0.15"
    })
    public static double starSizeBase = 0.15;

    @ConfigComment({
            "Controls the difference of size of stars,required starCount not to be -1.",
            "Vanilla: 0.1"
    })
    public static double starSizeDiff = 0.1;
    @ConfigComment({
            "Controls the random seed to use while generating stars.One seed,one unique star layout.",
            "Set to 0 to generate a new seed everytime you enter the game.",
            "Requires starCount not to be -1."
    })
    public static long starSeed = 23333;

    @ConfigComment({
            "Let the stars shrinking using a sin function,This controls the shrinking frequency.",
            "Use it with starShrinkingAmplitude!",
            "A bug(maybe?) causes this feature invalid in Vanilla."
    })
    public static float starShrinkingFreq = 0;
    @ConfigComment({
            "Let the stars shrinking using a sin function,This controls the shrinking amplitude.",
            "Use it with starShrinkingFreq!"
    })
    public static float starShrinkingAmplitude = 0;
    @ConfigComment({
            "Controls the basic value of stars' brightness.",
            "Vanilla: 0.5"
    })
    public static float starBrightness=0.5F;

    @ConfigComment("Allows players to eat food anytime, including in Creative mode")
    public static boolean alwaysEatable = false;

    @ConfigComment("Enables Creative mode players to eat food without consuming the item")
    public static boolean creativeEating = true;

    public static void init() {
        final File configFile = new File(Launch.minecraftHome, "config/astatine.properties");
        final Properties props = new Properties();
        StringBuilder configContent = new StringBuilder("# Astatine Mod Configure File\n\n");

        try {
            if(!Utils.createFile(configFile)) {
                props.load(Files.newReader(configFile, StandardCharsets.UTF_8));
            }
            for (Field field : Configuration.class.getDeclaredFields()) {
                String key = field.getName();
                String valueStr = props.getProperty(key);
                Object fieldValue;
                try {
                    fieldValue = parseValue(field.getType(), valueStr);
                    Configuration.class.getField(key).set(null, fieldValue);
                    //NPE when valueStr is null
                } catch (Exception e) {
                    fieldValue = field.get(null);
                    System.out.println("Cannot parse configure value '" + key + "', the default value '" + fieldValue + "' would be used");
                }
                for (String s : field.getAnnotation(ConfigComment.class).value()) {
                    configContent
                            .append('#')
                            .append(' ')
                            .append(s)
                            .append('\n');
                }
                configContent
                        .append("# type:")
                        .append(field.getType().getSimpleName())
                        .append('\n')
                        .append(field.getName())
                        .append('=')
                        .append(fieldValue)
                        .append('\n')
                        .append('\n');
            }
            Files.write(configContent.toString(), configFile, StandardCharsets.UTF_8);

        } catch (Exception e) {
            System.out.println("[Astatine] Unable to load configure file!Error: " + e.getMessage());
        }
    }

    private static Object parseValue(Class<?> type, String valueStr) {
        Preconditions.checkNotNull(valueStr);
        if (type == Boolean.TYPE) return Boolean.parseBoolean(valueStr);
        if (type == Integer.TYPE) return Integer.parseInt(valueStr);
        if (type == Double.TYPE) return Double.parseDouble(valueStr);
        if (type == Long.TYPE) return Long.parseLong(valueStr);
        if (type == Float.TYPE) return Float.parseFloat(valueStr);
        throw new IllegalArgumentException("Unknown configure type: " + type);
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ConfigComment {
        @Nls String[] value();
    }
}
