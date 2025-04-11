package com.sing.astatine.utils;

import com.sing.astatine.Configuration;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    public static String replaceFormatSpecifiers(String input) {
        int i = input.indexOf('%');
        if (i == -1) return input;
        StringBuilder result = new StringBuilder(input.length());
        int length = input.length();
        for (int last=0;;last=i,i=input.indexOf('%',i)) {
            if(i==-1){
                result.append(input, last, input.length());
                break;
            }
            result.append(input, last, i);
            result.append('%');
            if (++i >= length) break;
            char current=input.charAt(i);
            if(current == '%'){
                ++i;continue;
            }
            // most cases
            if (isConversionChar(current)) {
                result.append('s');
                ++i;
                continue;
            }
            int locationStart = i;
            while (i < length && Character.isDigit(input.charAt(i))) i++;
            if (input.charAt(i) == '$') {
                result.append(input,locationStart,++i);
            }
            while (i < length && !isConversionChar(input.charAt(i))) ++i;
            result.append('s');
            ++i;
        }
        return result.toString();
    }

    private static boolean isConversionChar(char c) {
        switch (Character.toLowerCase(c)) {
            case 'b':
            case 'h':
            case 's':
            case 'c':
            case 'd':
            case 'o':
            case 'x':
            case 'e':
            case 'f':
            case 'g':
            case 'a':
            case 't':
            case 'n':
                return true;
            default:
                return false;
        }
    }

    public static boolean hasClass(String name) {
        try {
            Class.forName(name, false, Thread.currentThread().getContextClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static boolean startsWithIgnoreCase(String text, String find) {
        return text.regionMatches(true, 0, find, 0, find.length());
    }

    public static boolean anyMatch(String toFind, String... target) {
        for (String s : target) {
            if (startsWithIgnoreCase(s, toFind)) return true;
        }
        return false;
    }

    public static boolean createFile(File file) throws IOException {
        if (file.exists()) return false;
        if (!file.getParentFile().mkdirs()) throw new IOException("Unable to create parent dirs!");
        return file.createNewFile();
    }

    public static double random() {
        return ThreadLocalRandom.current().nextDouble();
    }

    @SuppressWarnings("unused")
    public static void drawGameTime(Minecraft mc) {
        if (mc.world == null || mc.gameSettings.showDebugInfo) return;
        final long time = mc.world.getWorldTime() + 6000;
        long totalMinutes = (time / 1000) * 60;
        totalMinutes += (time % 1000) * 60 / 1000;
        int hours = (int) (totalMinutes / 60) % 24;
        int minutes = (int) (totalMinutes % 60);
        String text = String.format("%02d:%02d", hours, minutes);
        mc.fontRenderer.drawString(text, Configuration.WorldTime.timeDisplayXOffset, Configuration.WorldTime.timeDisplayYOffset, 0xFFFFFF);
    }

    public static long chunkPos(@NotNull Chunk chunk) {
        return ChunkPos.asLong(chunk.x, chunk.z);
    }

    public static int doCompare(int... results) {
        for (int result : results) {
            if (result != 0) return result;
        }
        return 0;
    }

    @SuppressWarnings("unused")
    public static boolean addItemToInventory(InventoryPlayer inventory, ItemStack item) {
        boolean hasSucceed = false;
        final int maxStackSize = item.getMaxStackSize();
        while (item.getCount() > maxStackSize) {
            final ItemStack stack = item.copy();
            stack.setCount(maxStackSize);
            item.shrink(maxStackSize);
            if (!inventory.addItemStackToInventory(stack)) return hasSucceed;
            hasSucceed = true;
        }
        if (item.getCount() > 0) {
            hasSucceed |= inventory.addItemStackToInventory(item);
        }
        return hasSucceed;
    }

    public static long splitMix64(long value) {
        long z = (value + (0x9E3779B97F4A7C15L));
        z = (z ^ (z >>> 30)) * (0xBF58476D1CE4E5B9L);
        z = (z ^ (z >>> 27)) * (0x94D049BB133111EBL);
        return z ^ (z >>> 31);
    }
}
