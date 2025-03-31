package com.sing.astatine.utils.config;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.sing.astatine.utils.Utils;
import net.minecraft.launchwrapper.Launch;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class ConfigurationLoader {
    private static boolean checkAnnotation(AnnotatedElement e, StringBuilder builder){
        if (e.isAnnotationPresent(Hidden.class)) return true;
        if (e.isAnnotationPresent(Experimental.class)) builder.append("# [Experimental]\n");
        final ConfigComment commentAnnotation = e.getAnnotation(ConfigComment.class);
        if (commentAnnotation != null) for (String s : commentAnnotation.value()) {
            builder.append('#').append(' ').append(s).append('\n');
        }
        return false;
    }
    private static void initFor(Properties props, StringBuilder configContent, String baseGroup, Class<?> clazz,boolean hidden) throws IllegalAccessException {
        for (Field field : clazz.getDeclaredFields()) {
            if(field.isAnnotationPresent(Ignore.class))continue;
            boolean currentHidden=checkAnnotation(field,configContent);
            String groupName = getGroupName(baseGroup,field);
            String key = groupName + field.getName();
            String valueStr = props.getProperty(key);
            Object fieldValue;
            try {
                fieldValue = parseValue(field.getType(), valueStr);
                field.set(null,fieldValue);
                //NPE when valueStr is null
            } catch (Exception e) {
                fieldValue = field.get(null);
                System.out.println("Cannot parse configure value '" + key + "', the default value '" + fieldValue + "' would be used");
            }
            if(hidden||currentHidden)continue;
            configContent
                    .append("# type:")
                    .append(field.getType().getSimpleName())
                    .append('\n')
                    .append(key)
                    .append('=')
                    .append(fieldValue)
                    .append('\n')
                    .append('\n');
        }
        for (Class<?> declaredClass : clazz.getDeclaredClasses()) {
            if(declaredClass.isAnnotationPresent(Ignore.class))continue;
            final String groupName = getGroupName(baseGroup, declaredClass);
            boolean currentHidden=hidden||checkAnnotation(declaredClass,configContent);
            if (!currentHidden) {
                configContent.append("#group: ").append(groupName).append('\n');
                configContent.append('\n');
            }
            initFor(props, configContent, groupName+".", declaredClass,currentHidden);
            if(!currentHidden) configContent.append("# --------------------\n\n");
        }
    }

    private static @NotNull String getGroupName(String baseGroup, AnnotatedElement e) {
        Group groupAnnotation = e.getAnnotation(Group.class);
        return baseGroup + (groupAnnotation == null ? "" : groupAnnotation.value());
    }

    public static void init(Class<?> clazz) {
        final File configFile = new File(Launch.minecraftHome, "config/astatine.properties");
        final Properties props = new Properties();
        StringBuilder configContent = new StringBuilder("# Astatine Mod Configure File\n\n");

        try {
            if (!Utils.createFile(configFile)) {
                props.load(Files.newReader(configFile, StandardCharsets.US_ASCII));
            }
            initFor(props, configContent, "", clazz,false);
            Files.write(configContent.toString(), configFile, StandardCharsets.US_ASCII);
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


}
