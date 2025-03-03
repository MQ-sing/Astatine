package com.sing.astatine.mixin.fastlang;

import com.sing.astatine.Configuration;
import com.sing.astatine.utils.Utils;
import net.minecraft.client.resources.Locale;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

@Mixin(Locale.class)
public abstract class MixinLocale {
    @Shadow
    public Map<String, String> properties;

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/regex/Pattern;compile(Ljava/lang/String;)Ljava/util/regex/Pattern;"))
    private static Pattern removeMatchPattern(String s) {
        return null;
    }


    /**
     * @author MQ-sing
     * @reason use explicit logic instead of a pattern match
     */
    @Overwrite
    private void loadLocaleData(InputStream inputStreamIn) throws IOException {
        if(!Configuration.noForgeLangExtension){
            inputStreamIn= FMLCommonHandler.instance().loadLanguage(properties, inputStreamIn);
            if(inputStreamIn==null)return;
        }
        for (String s : IOUtils.readLines(inputStreamIn, StandardCharsets.UTF_8)) {
            if (s.isEmpty() || s.charAt(0)=='#') continue;
            final int idx = s.indexOf('=');
            if(idx!=-1) {
                String s1 = s.substring(0, idx);
                String s2 = Utils.replaceFormatSpecifiers(s.substring(idx + 1));
                this.properties.put(s1, s2);
            }
        }
    }
}
