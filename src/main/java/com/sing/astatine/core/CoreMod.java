package com.sing.astatine.core;

import com.sing.astatine.Configuration;
import com.sing.astatine.utils.Utils;
import com.sing.astatine.utils.config.ConfigurationLoader;
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
@IFMLLoadingPlugin.TransformerExclusions({"com.sing.astatine.core", "com.sing.astatine.Configuration"})
public class CoreMod implements IFMLLoadingPlugin, IEarlyMixinLoader {

    private static final Set<String> FASTRANDOM_LIST = new ObjectArraySet<>(new String[]{
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
    private static final Set<String> NEW_GUI_REPLACE = new ObjectArraySet<>(new String[]{
            "net.minecraft.client.gui.GuiMainMenu",
            "net.minecraft.client.gui.GuiOptions"
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
    private static void mixinIf(ArrayList<String> list,String name,boolean flag){
       if(flag)list.add(name);
    }
    @Override
    public List<String> getMixinConfigs() {
        ConfigurationLoader.init(Configuration.class);
        final ArrayList<String> mixins = new ArrayList<>();
        mixinIf(mixins,"mixins.multilang.json",         Configuration.Lang.languageSelector);
        mixinIf(mixins,"mixins.fastlang.json",          Configuration.Lang.fastLang);
        mixinIf(mixins,"mixins.fastrandom.json",        Configuration.Random.fastRandom);
        mixinIf(mixins,"mixins.faststar.json",          Configuration.StarGen.enabled);
        mixinIf(mixins,"mixins.star_twinkling.json",    Configuration.StarTwinkling.enabled);
        mixinIf(mixins,"mixins.weather.json",           Configuration.forceWeatherParticleUseConstLight);
        mixinIf(mixins,"mixins.renderskip.json",Configuration.maxEntityRenderDistance!=-1||Configuration.maxTileEntityRenderDistance!=-1);
        if(Configuration.ChunkCache.enabled){
            if(Configuration.ChunkCache.maxCacheSize <16)throw new IllegalStateException("maxCacheSize must greater than 16");
            mixins.add("mixins.chunkcache.json");
        }
        return mixins;
    }
    public static class ASMTransformer implements IClassTransformer {
        @Override
        public byte[] transform(String name, String transformedName, byte[] basicClass) {
            switch (transformedName) {
                case "net.minecraft.client.resources.Locale": {
                    if (Configuration.Lang.forceAsciiFont) {
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
                    } else {
                        InstructionList list = new InstructionList();
                        list.invokeVirtualThis(asm.name(), CoreModCore.mayDeobfuscated("func_184812_l_", "isCreative"), "()Z");
                        LabelNode node = list.jumpIf0();
                        list.return1();
                        list.label(node);
                        method.instructions().insertAfterHead(list);
                    }
                    return asm.toBytes();
                }
                case "net.minecraft.item.ItemFood": {
                    if (Configuration.creativeEating) {
                        final ClassASM asm = ClassASM.get(basicClass);
                        final MethodASM method = asm.methodByName("func_77654_b", "onItemUseFinish");
                        final InstructionList instructions = method.instructions();
                        final int isCreative = method.local("isCreative", "I", instructions.find(INodeMatcher.labels()), instructions.findLast(INodeMatcher.labels()));
                        InstructionList start = new InstructionList();
                        start.constant(false);
                        start.storeI(isCreative);
                        instructions.insertAfterHead(start);
                        InstructionList list = new InstructionList();
                        list.dupe();
                        list.invokeVirtual("net/minecraft/entity/player/EntityPlayer", CoreModCore.mayDeobfuscated("func_184812_l_", "isCreative"), "()Z");
                        list.storeI(isCreative);
                        InstructionList shrinkCheck = new InstructionList();
                        shrinkCheck.loadI(isCreative);
                        shrinkCheck.jumpIfNot0(instructions.findLastNth(INodeMatcher.labels(), 1));
                        instructions.insertBefore(instructions.find(node -> node.getOpcode() == Opcodes.ASTORE && ((VarInsnNode) node).var == 4), list);
                        instructions.insert(instructions.findLastNth(INodeMatcher.labels(), 2), shrinkCheck);
                        return asm.toBytes();
                    }
                    break;
                }
                case "net.minecraft.client.gui.GuiLanguage$List": {
                    if (!Configuration.Lang.fastLang || Configuration.Lang.languageSelector) {
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
                            else if (methodNode.getOpcode() == Opcodes.INVOKEVIRTUAL && methodNode.desc.equals("()V")) {
                                final InstructionList list = new InstructionList();
                                list.loadThis();
                                list.field("net/minecraft/client/gui/GuiLanguage$List", "this$0", "Lnet/minecraft/client/gui/GuiLanguage;");
                                list.invokeStatic("net/minecraft/client/gui/GuiLanguage", "access$000", "(Lnet/minecraft/client/gui/GuiLanguage;)Lnet/minecraft/client/resources/LanguageManager;");
                                list.invokeStatic("net/minecraft/client/Minecraft", CoreModCore.mayDeobfuscated("func_71410_x", "getMinecraft"), "()Lnet/minecraft/client/Minecraft;");
                                list.invokeVirtual("net/minecraft/client/Minecraft", CoreModCore.mayDeobfuscated("func_110442_L", "getResourceManager"), "()Lnet/minecraft/client/resources/IResourceManager;");
                                list.invokeVirtual("net/minecraft/client/resources/LanguageManager", CoreModCore.mayDeobfuscated("func_110549_a", "onResourceManagerReload"), "(Lnet/minecraft/client/resources/IResourceManager;)V");
                                String ofClassName = null;
                                if (Utils.hasClass("net.optifine.Lang")) {
                                    ofClassName = "net/optifine/Lang";
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
                case "net.minecraft.client.multiplayer.WorldClient":
                case "net.minecraft.world.WorldServer":{
                    if (Configuration.WorldTime.worldTimeAdvancementInterval<=1) {
                        break;
                    }
                    final ClassASM asm = ClassASM.get(basicClass);
                    final String counter = asm.field("astatine$worldTimeFactorCounter", "I", 0);
                    final MethodASM method = asm.methodByName("func_72835_b", "tick");
                    final InstructionList instructions = method.instructions();
                    final JumpInsnNode node = (JumpInsnNode) instructions.findLast(INodeMatcher.ldc("doDaylightCycle")).getNext().getNext();
                    final InstructionList list = new InstructionList();
                    list.loadThis();
                    list.field(asm.name(),counter,"I");
                    list.constant(1);
                    list.add(Opcodes.IADD);
                    list.dupe();
                    list.loadThis();
                    list.swap();
                    list.setField(asm.name(),counter,"I");
                    list.staticVar("com/sing/astatine/Configuration","worldTimeFactor","I");
                    list.doJump(Opcodes.IF_ICMPLT, node.label);
                    list.loadThis();
                    list.constant(0);
                    list.setField(asm.name(),counter,"I");
                    instructions.insert(node,list);
                    return asm.toBytes();
                }
                case "net.minecraftforge.client.GuiIngameForge":{
                    if (!Configuration.WorldTime.showGameTime) {
                        break;
                    }
                    final ClassASM asm = ClassASM.get(basicClass);
                    final MethodASM method = asm.methodByName("func_175180_a", "renderGameOverlay");
                    final InstructionList instructions = method.instructions();
                    final InstructionList list = new InstructionList();
                    list.getFieldThis(asm.name(),CoreModCore.mayDeobfuscated("field_73839_d","mc"),"Lnet/minecraft/client/Minecraft;");
                    list.invokeStatic("com/sing/astatine/utils/Utils","drawGameTime","(Lnet/minecraft/client/Minecraft;)V");
                    instructions.insert(instructions.find(INodeMatcher.invokes("renderFPSGraph")),list);

                    return asm.toBytes();
                }
                case "net.minecraft.stats.StatisticsManager":{
                    if(!Configuration.disableStats)break;
                    final ClassASM asm = ClassASM.get(basicClass);
                    asm.methodByName("func_150871_b", "increaseStat").breaks();
                    return asm.toBytes();
                }
                case "net.minecraft.entity.item.EntityItem":{
                    if(Configuration.forceItemEntityMerge) {
                        final ClassASM asm = ClassASM.get(basicClass);
                        final InstructionList instructions = asm.methodByName("func_70289_a", "combineItems").instructions();
                        final MethodInsnNode invokeGetMaxStackSize = instructions.find(INodeMatcher.invokes(CoreModCore.mayDeobfuscated("func_77976_d", "getMaxStackSize")));
                        final InstructionList list = new InstructionList();
                        list.pop();
                        list.constant(Integer.MAX_VALUE);
                        instructions.replace(invokeGetMaxStackSize, list);
                        asm.methodByName("func_70100_b_","onCollideWithPlayer").instructions()
                                .redirect(null,CoreModCore.mayDeobfuscated("func_70441_a","addItemStackToInventory"),null,"com/sing/astatine/utils/Utils","addItemToInventory","(Lnet/minecraft/entity/player/InventoryPlayer;Lnet/minecraft/item/ItemStack;)Z",Opcodes.INVOKESTATIC);
                        return asm.toBytes();
                    }
                }
                case "net.minecraft.item.ItemStack":{
                    if(!Configuration.enableExtendedStackStorage)break;
                    final ClassASM asm = ClassASM.get(basicClass);
                    asm.constructor("(Lnet/minecraft/nbt/NBTTagCompound;)V")
                            .instructions()
                            .redirect(null,CoreModCore.mayDeobfuscated("func_74771_c","getByte"),null,null,CoreModCore.mayDeobfuscated("func_74762_e","getInteger"),"(Ljava/lang/String;)I",-1);
                    final InstructionList instructions = asm.methodByName("func_77955_b", "writeToNBT")
                            .instructions();
                    instructions.redirect(null,CoreModCore.mayDeobfuscated("func_74774_a","setByte"),null,null,CoreModCore.mayDeobfuscated("func_74768_a","setInteger"),"(Ljava/lang/String;I)V",-1);
                    instructions.remove(instructions.find(INodeMatcher.opcode(Opcodes.I2B)));
                    return asm.toBytes();
                }
            }
            if (Configuration.Random.fastRandom && FASTRANDOM_LIST.contains(transformedName)) {
                final ClassASM asm = ClassASM.get(basicClass);
                final MethodASM method = asm.constructor(null);
                final InstructionList instructions = method.instructions();
                for (AbstractInsnNode node : instructions) {
                    if (node.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                            ((MethodInsnNode) node).name.equals("setSeed")
                    ) {
                        instructions.replace(node, new InsnNode(Opcodes.POP2));
                    } else if (node.getOpcode() == Opcodes.NEW && ((TypeInsnNode) node).desc.equals("java/util/Random")) {
                        InstructionList list = new InstructionList();
                        ThreadLocalRandom.current();
                        list.invokeStatic("java/util/concurrent/ThreadLocalRandom", "current", "()Ljava/util/concurrent/ThreadLocalRandom;");
                        instructions.insertBefore(node, list);
                        //noinspection DataFlowIssue
                        while (!(node.getOpcode() == Opcodes.INVOKESPECIAL && ((MethodInsnNode) node).owner.equals("java/util/Random"))) {
                            final AbstractInsnNode next = node.getNext();
                            instructions.remove(node);
                            node = next;
                        }
                        instructions.remove(node);
                    }
                }
                return asm.toBytes();
            } else if (Configuration.Lang.languageSelector && NEW_GUI_REPLACE.contains(transformedName)) {
                final ClassASM asm = ClassASM.get(basicClass);
                final MethodASM method = asm.methodByName("func_146284_a", "actionPerformed");
                final InstructionList instructions = method.instructions();
                for (AbstractInsnNode instruction : instructions) {
                    if (instruction instanceof TypeInsnNode) {
                        final TypeInsnNode insn = (TypeInsnNode) instruction;
                        if (insn.desc.equals("net/minecraft/client/gui/GuiLanguage"))
                            insn.desc = "com/sing/astatine/client/gui/multilang/GuiScreenLanguages";
                    } else if (instruction instanceof MethodInsnNode) {
                        final MethodInsnNode insn = (MethodInsnNode) instruction;
                        if (insn.owner.equals("net/minecraft/client/gui/GuiLanguage"))
                            insn.owner = "com/sing/astatine/client/gui/multilang/GuiScreenLanguages";
                    }
                }
                return asm.toBytes();
            }
            return basicClass;
        }
    }
}
