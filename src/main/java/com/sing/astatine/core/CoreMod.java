package com.sing.astatine.core;
import com.sing.astatine.Configuration;
import com.sing.astatine.utils.Utils;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("astatine")
@IFMLLoadingPlugin.SortingIndex(2333)
@IFMLLoadingPlugin.TransformerExclusions({"com.sing.astatine.core","com.sing.astatine.Configuration"})
public class CoreMod implements IFMLLoadingPlugin, IEarlyMixinLoader {

    private static final Set<String> FASTRANDOM_LIST =new ObjectArraySet<>(new String[]{
            "net.minecraft.client.gui.FontRenderer",
            "net.minecraft.client.renderer.EntityRenderer",
            "net.minecraft.client.audio.MusicTicker",
            "net.minecraft.client.renderer.entity.RenderEnderman",
            "net.minecraft.client.gui.GuiEnchantment",
            "net.minecraft.client.audio.SoundEventAccessor",
            "net.minecraft.client.network.NetHandlerPlayClient",
            "net.minecraft.client.particle.Particle",
            "net.minecraft.client.particle.ParticleManager",
            "net.minecraft.entity.Entity",
            "net.minecraft.server.MinecraftServer",
            "net.minecraft.entity.passive.EntitySquid"
    });

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{ASMTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public List<String> getMixinConfigs() {
        Configuration.init();
        final ArrayList<String> mixins = new ArrayList<>();
        if (Configuration.languageSelector) mixins.add("mixins.multilang.json");
        if (Configuration.fastLang) mixins.add("mixins.fastlang.json");
        if (Configuration.fastRandom) mixins.add("mixins.fastrandom.json");
        if (Configuration.starCount !=-1) mixins.add("mixins.faststar.json");
        if(Configuration.starShrinkingFreq!=0&&Configuration.starShrinkingAmplitude!=0)mixins.add("mixins.starshrinking.json");
        return mixins;
    }
    private static final Set<String> NEW_GUI_REPLACE=new ObjectArraySet<>(new String[]{
            "net.minecraft.client.gui.GuiMainMenu",
            "net.minecraft.client.gui.GuiOptions"
    });
    public static class ASMTransformer implements IClassTransformer {
        @Override
        public byte[] transform(String name, String transformedName, byte[] basicClass) {
            if(transformedName.startsWith("org.objectweb.asm"))
                throw new RuntimeException("???");
            switch (transformedName) {
                case "net.minecraft.client.resources.Locale": {
                    if (Configuration.forceAsciiFont) {
                        final ClassASM asm = ClassASM.get(basicClass);
                        asm.methodByName("func_135024_b", "checkUnicode").breaks();
                        return asm.toBytes();
                    }
                    break;
                }
                case "net.minecraft.entity.player.EntityPlayer": {
                    if (!(Configuration.creativeEating || Configuration.alwaysEatable)) break;
                    final ClassASM asm = ClassASM.get(basicClass);
                    final MethodASM method = asm.methodByName("func_71043_e", "canEat");
                    if (Configuration.alwaysEatable) {
                        method.breaks(true);
                    }
                    else {
                        InstructionList list = new InstructionList();
                        list.invokeVirtualThis(asm.name(), CoreModCore.mayDeobfuscated("func_184812_l_", "isCreative"), "()Z");
                        LabelNode node = list.jumpIf0();
                        list.return1();
                        list.label(node);
                        method.instructions().insertAfterHead(list);
                    }
                    return asm.toBytes();
                }
                case "net.minecraft.item.ItemFood":{
                    if(Configuration.creativeEating){
                        final ClassASM asm = ClassASM.get(basicClass);
                        final MethodASM method = asm.methodByName("func_77654_b","onItemUseFinish");
                        final InstructionList instructions = method.instructions();
                        final int isCreative = method.local("isCreative", "I", instructions.find(INodeMatcher.labels()), instructions.findLast(INodeMatcher.labels()));
                        InstructionList start=new InstructionList();
                        start.constant(false);
                        start.storeI(isCreative);
                        instructions.insertAfterHead(start);
                        InstructionList list=new InstructionList();
                        list.dupe();
                        list.invokeVirtual("net/minecraft/entity/player/EntityPlayer", CoreModCore.mayDeobfuscated("func_184812_l_", "isCreative"),"()Z");
                        list.storeI(isCreative);
                        InstructionList shrinkCheck=new InstructionList();
                        shrinkCheck.loadI(isCreative);
                        shrinkCheck.jumpIfNot0(instructions.findLastNth(INodeMatcher.labels(),1));
                        instructions.insertBefore(instructions.find(node -> node.getOpcode() == Opcodes.ASTORE && ((VarInsnNode) node).var == 4),list);
                        instructions.insert(instructions.findLastNth(INodeMatcher.labels(),2),shrinkCheck);
                        return asm.toBytes();
                    }
                    break;
                }
                case "net.minecraft.client.gui.GuiLanguage$List": {
                    if (!Configuration.fastLang ||Configuration.languageSelector) {
                        break;
                    }
                    final ClassASM asm = ClassASM.get(basicClass);
                    final MethodASM method = asm.methodByName("func_148144_a", "elementClicked");
                    final InstructionList instructions = method.instructions();
                    for (AbstractInsnNode i : instructions) {
                        if (i instanceof MethodInsnNode) {
                            final MethodInsnNode methodNode = (MethodInsnNode) i;
                            // Remove Forge resource reload
                            if (methodNode.owner.equals("net/minecraftforge/fml/client/FMLClientHandler"))
                                instructions.remove(methodNode);
                                // Replace Minecraft resource reload(saveOptions()V)
                            else
                                if (methodNode.getOpcode() == Opcodes.INVOKEVIRTUAL && methodNode.desc.equals("()V")) {
                                final InstructionList list = new InstructionList();
                                list.loadThis();
                                list.field("net/minecraft/client/gui/GuiLanguage$List", "this$0", "Lnet/minecraft/client/gui/GuiLanguage;");
                                list.invokeStatic("net/minecraft/client/gui/GuiLanguage", "access$000", "(Lnet/minecraft/client/gui/GuiLanguage;)Lnet/minecraft/client/resources/LanguageManager;");
                                list.invokeStatic("net/minecraft/client/Minecraft", CoreModCore.mayDeobfuscated("func_71410_x", "getMinecraft"), "()Lnet/minecraft/client/Minecraft;");
                                list.invokeVirtual("net/minecraft/client/Minecraft", CoreModCore.mayDeobfuscated("func_110442_L", "getResourceManager"), "()Lnet/minecraft/client/resources/IResourceManager;");
                                list.invokeVirtual("net/minecraft/client/resources/LanguageManager", CoreModCore.mayDeobfuscated("func_110549_a", "onResourceManagerReload"), "(Lnet/minecraft/client/resources/IResourceManager;)V");
                                String ofClassName = null;
                                if (Utils.hasClass("net.optifine.Lang")) {
                                    ofClassName = "net.optifine.Lang";
                                } else if (Utils.hasClass("Lang")) {
                                    ofClassName = "Lang";
                                }
                                if (ofClassName != null) {
                                    instructions.invokeStatic(ofClassName, "resourceReloaded", "()V");
                                }
                                instructions.insert(methodNode, list);
                                instructions.remove(methodNode.getPrevious().getPrevious().getPrevious());
                                instructions.remove(methodNode.getPrevious().getPrevious());
                                instructions.remove(methodNode.getPrevious());
                                instructions.remove(methodNode);
                            }
                        }
                    }
                    return asm.toBytes();
                }
            }
            if(Configuration.fastRandom&&FASTRANDOM_LIST.contains(transformedName)){
                final ClassASM asm = ClassASM.get(basicClass);
                final MethodASM method = asm.constructor();
                final InstructionList instructions = method.instructions();
                for (AbstractInsnNode node : instructions) {
                    if(node.getOpcode()==Opcodes.INVOKEVIRTUAL&&
                            ((MethodInsnNode)node).name.equals("setSeed")
                    ){
                        instructions.insert(node, InstructionList.of(new InsnNode(Opcodes.POP2)));
                        instructions.remove(node);
                    } else if(node.getOpcode()==Opcodes.NEW&&((TypeInsnNode) node).desc.equals("java/util/Random")){
                        InstructionList list=new InstructionList();
                        ThreadLocalRandom.current();
                        list.invokeStatic("java/util/concurrent/ThreadLocalRandom","current","()Ljava/util/concurrent/ThreadLocalRandom;");
                        instructions.insertBefore(node,list);
                        //noinspection DataFlowIssue
                        while(!(node.getOpcode()==Opcodes.INVOKESPECIAL&&((MethodInsnNode)node).owner.equals("java/util/Random"))){
                            final AbstractInsnNode next = node.getNext();
                            instructions.remove(node);
                            node=next;
                        }
                        instructions.remove(node);
                    }
                }
                return asm.toBytes();
            }else if(Configuration.languageSelector&&NEW_GUI_REPLACE.contains(transformedName)){
                final ClassASM asm = ClassASM.get(basicClass);
                final MethodASM method = asm.methodByName("func_146284_a","actionPerformed");
                final InstructionList instructions = method.instructions();
                for (AbstractInsnNode instruction : instructions) {
                    if(instruction instanceof TypeInsnNode){
                        final TypeInsnNode insn = (TypeInsnNode) instruction;
                        if(insn.desc.equals("net/minecraft/client/gui/GuiLanguage"))
                            insn.desc="com/sing/astatine/client/gui/multilang/GuiScreenLanguages";
                    }else if(instruction instanceof MethodInsnNode){
                        final MethodInsnNode insn = (MethodInsnNode) instruction;
                        if(insn.owner.equals("net/minecraft/client/gui/GuiLanguage"))
                            insn.owner="com/sing/astatine/client/gui/multilang/GuiScreenLanguages";
                    }
                }
                return asm.toBytes();
            }
            return basicClass;
        }
    }
}
