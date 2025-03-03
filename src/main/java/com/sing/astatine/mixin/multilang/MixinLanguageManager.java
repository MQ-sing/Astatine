package com.sing.astatine.mixin.multilang;

import com.google.common.collect.Lists;
import com.sing.astatine.ConfigurationRuntime;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.Locale;
import net.minecraft.util.text.translation.LanguageMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;

@Mixin(LanguageManager.class)
public abstract class MixinLanguageManager {
    @Shadow
    @Final
    protected static Locale CURRENT_LOCALE;
    @Shadow
    private String currentLanguage;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(CallbackInfo ci) {
        if (ConfigurationRuntime.languageList.length == 1&& !currentLanguage.equals("en_us")) {
            final String[] newList = new String[ConfigurationRuntime.languageList.length + 1];
            newList[0]=currentLanguage;
            System.arraycopy(ConfigurationRuntime.languageList,0,newList,1,ConfigurationRuntime.languageList.length);
            ConfigurationRuntime.languageList=newList;
        }
    }

    /**
     * @author MQ-sing
     * @reason let it support multiple languages loading
     */
    @Overwrite
    public void onResourceManagerReload(IResourceManager resourceManager) {
        final ArrayList<String> list = Lists.newArrayList(ConfigurationRuntime.languageList);
        Collections.reverse(list);
        CURRENT_LOCALE.loadLocaleDataFiles(resourceManager, list);
        LanguageMap.replaceWith(CURRENT_LOCALE.properties);
    }
}
