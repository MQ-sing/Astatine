package com.sing.astatine.core;

import com.google.common.collect.ImmutableMap;
import com.sing.astatine.Configuration;
import com.sing.astatine.utils.Utils;
import com.sing.astatine.utils.config.ConfigurationLoader;
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NonNls;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import javax.annotation.Nullable;
import java.util.*;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("astatine")
@IFMLLoadingPlugin.SortingIndex(2333)
@IFMLLoadingPlugin.TransformerExclusions({"com.sing"})
public class CoreMod implements IFMLLoadingPlugin, IEarlyMixinLoader {
    private static void processFastRandom(List<MethodASM> methods) {
        for (MethodASM method : methods) {
            final InstructionList instructions = method.instructions();
            for (AbstractInsnNode node : instructions) {
                if (node.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                        ((MethodInsnNode) node).name.equals("setSeed")
                ) {
                    instructions.replace(node, new InsnNode(Opcodes.POP2));
                } else if (node.getOpcode() == Opcodes.NEW && ((TypeInsnNode) node).desc.equals("java/util/Random")) {
                    //invokespecial constructor
                    instructions.remove(node.getNext().getNext());
                    //dup
                    instructions.remove(node.getNext());
                    //new random
                    instructions.replace(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/concurrent/ThreadLocalRandom", "current", "()Ljava/util/concurrent/ThreadLocalRandom;", false));
                }
            }
        }
    }

    private static void mixinIf(ArrayList<String> list, @NonNls String name, boolean flag) {
        if (flag) list.add(name);
    }

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
        ConfigurationLoader.init(Configuration.class);
        final ArrayList<String> mixins = new ArrayList<>();
        mixinIf(mixins, "mixins.multilang.json", Configuration.Lang.languageSelector);
        mixinIf(mixins, "mixins.fastlang.json", Configuration.Lang.fastLang);
        mixinIf(mixins, "mixins.faststar.json", Configuration.StarGen.enabled);
        mixinIf(mixins, "mixins.star_twinkling.json", Configuration.StarTwinkling.enabled);
        mixinIf(mixins, "mixins.weather.json", Configuration.forceWeatherParticleUseConstLight);
        mixinIf(mixins, "mixins.renderskip.json", Configuration.maxEntityRenderDistance != -1 || Configuration.maxTileEntityRenderDistance != -1);
        mixinIf(mixins, "mixins.misc.json", Configuration.Misc.collections);
        if (Configuration.ChunkCache.enabled) {
            if (Configuration.ChunkCache.maxCacheSize < 16)
                throw new IllegalStateException("maxCacheSize must greater than 16");
            mixins.add("mixins.chunkcache.json");
        }
        return mixins;
    }

    @FunctionalInterface
    private interface Processor {
        void process(ClassASM asm);
    }

    private static class ProcessorMap<P> extends Object2ObjectOpenHashMap<String, ArrayList<P>> {
        public void register(P processor, String clazz) {
            computeIfAbsent(clazz, k -> new ArrayList<>()).add(processor);
        }

        public void register(P processor, String... classes) {
            for (String aClass : classes) {
                register(processor, aClass);
            }
        }
    }

    public static class ASMTransformer implements IClassTransformer {
        private static final ProcessorMap<Processor> processors = new ProcessorMap<>();
        private static final Logger LOGGER = LogManager.getLogger(CoreMod.class);

        static {
            if (Configuration.Lang.forceAsciiFont) {
                processors.register(asm -> asm.methodByName("func_135024_b", "checkUnicode").breaks(), "net.minecraft.client.resources.Locale");
            }
            if (Configuration.creativeEating) {
                processors.register(asm -> asm.methodByName("func_71043_e", "canEat").insertAtHead(new InstructionList().invokeVirtualThis(asm.name(), CoreModCore.mayDeobfuscated("func_184812_l_", "isCreative"), "()Z")
                        .doJump(Opcodes.IFEQ, InstructionList::return1)), "net.minecraft.entity.player.EntityPlayer");
                processors.register(asm -> {
                    final MethodASM method = asm.methodByName("func_77654_b", "onItemUseFinish");
                    final InstructionList instructions = method.instructions();
                    final int isCreative = method.local("isCreative", "I", instructions.find(INodeMatcher.labels()), instructions.findLast(INodeMatcher.labels()));
                    InstructionList start = method.createList()
                            .constant(false)
                            .store(isCreative);
                    instructions.insertHead(start);
                    InstructionList list = method.createList()
                            .dupe()
                            .invokeVirtual("net/minecraft/entity/player/EntityPlayer", CoreModCore.mayDeobfuscated("func_184812_l_", "isCreative"), "()Z")
                            .store(isCreative);
                    InstructionList shrinkCheck = method.createList();
                    shrinkCheck.load(isCreative)
                            .doJump(Opcodes.IFNE,instructions.findLastNth(INodeMatcher.labels(), 1));
                    instructions.insertBefore(instructions.find(node -> node.getOpcode() == Opcodes.ASTORE && ((VarInsnNode) node).var == 4), list);
                    instructions.insert(instructions.findLastNth(INodeMatcher.labels(), 2), shrinkCheck);
                }, "net.minecraft.item.ItemFood");
            }
            if (Configuration.alwaysEatable) {
                processors.register(asm -> asm.methodByName("func_71043_e", "canEat").breaks(true), "net.minecraft.entity.player.EntityPlayer");
            }
            if (Configuration.Lang.fastLang) {
                processors.register(asm -> {
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
                                final String MINECRAFT = "net/minecraft/client/Minecraft";
                                final InstructionList list = new InstructionList()
                                        .getFieldThis(asm.name(), "this$0", "Lnet/minecraft/client/gui/GuiLanguage;")
                                        .invokeStatic("net/minecraft/client/gui/GuiLanguage", "access$000", "(Lnet/minecraft/client/gui/GuiLanguage;)Lnet/minecraft/client/resources/LanguageManager;")
                                        .invokeStatic(MINECRAFT, CoreModCore.mayDeobfuscated("func_71410_x", "getMinecraft"), "()Lnet/minecraft/client/Minecraft;")
                                        .invokeVirtual(MINECRAFT, CoreModCore.mayDeobfuscated("func_110442_L", "getResourceManager"), "()Lnet/minecraft/client/resources/IResourceManager;")
                                        .invokeVirtual("net/minecraft/client/resources/LanguageManager", CoreModCore.mayDeobfuscated("func_110549_a", "onResourceManagerReload"), "(Lnet/minecraft/client/resources/IResourceManager;)V");
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
                }, "net.minecraft.client.gui.GuiLanguage$List");
            }
            if (Configuration.WorldTime.worldTimeAdvancementInterval > 1) {
                processors.register(asm -> {
                    final String counter = asm.addField("astatine$worldTimeFactorCounter", "I", 0);
                    final MethodASM method = asm.methodByName("func_72835_b", "tick");
                    final InstructionList instructions = method.instructions();
                    final JumpInsnNode node = (JumpInsnNode) instructions.findLast(INodeMatcher.ldc("doDaylightCycle")).getNext().getNext();
                    final InstructionList list = new InstructionList();
                    list.getFieldThis(asm.name(), counter, "I")
                            .constant(1)
                            .add(Opcodes.IADD)
                            .dupe()
                            .loadThis()
                            .swap()
                            .setField(asm.name(), counter, "I")
                            .configInt("worldTimeFactor")
                            .doJump(Opcodes.IF_ICMPLT, node.label)
                            .loadThis()
                            .constant(0)
                            .setField(asm.name(), counter, "I");
                    instructions.insert(node, list);
                }, "net.minecraft.client.multiplayer.WorldClient", "net.minecraft.world.WorldServer");
            }
            if (Configuration.WorldTime.showGameTime) {
                processors.register(asm -> {
                    final InstructionList instructions = asm.methodByName("func_175180_a", "renderGameOverlay").instructions();
                    instructions.insert(instructions.find(INodeMatcher.invokes("renderFPSGraph")), new InstructionList()
                            .getFieldThis(asm.name(), CoreModCore.mayDeobfuscated("field_73839_d", "mc"), "Lnet/minecraft/client/Minecraft;")
                            .invokeStatic("com/sing/astatine/utils/Utils", "drawGameTime", "(Lnet/minecraft/client/Minecraft;)V"));
                }, "net.minecraftforge.client.GuiIngameForge");
            }
            if (Configuration.disableStats) {
                processors.register(asm -> asm.methodByName("func_150871_b", "increaseStat").breaks(), "net.minecraft.stats.StatisticsManager");
                processors.register(asm -> {
                    final ImmutableMap<String, MethodASM> methods = asm.methods();
                    methods.get("<clinit>").breaks();
                    methods.get("reinit").breaks();
                    methods.get(CoreModCore.mayDeobfuscated("func_151178_a", "init")).breaks();
                    methods.get(CoreModCore.mayDeobfuscated("func_188055_a", "getBlockStats")).breaksNull();
                    methods.get(CoreModCore.mayDeobfuscated("func_151177_a", "getOneShotStat")).breaksNull();
                    methods.get(CoreModCore.mayDeobfuscated("func_188057_b", "getObjectUseStats")).breaksNull();
                    methods.get(CoreModCore.mayDeobfuscated("func_188059_c", "getObjectBreakStats")).breaksNull();
                    methods.get(CoreModCore.mayDeobfuscated("func_188056_d", "getObjectsPickedUpStats")).breaksNull();
                    methods.get(CoreModCore.mayDeobfuscated("func_188058_e", "getDroppedObjectStats")).breaksNull();
                }, "net.minecraft.stats.StatList");
            }
            if (Configuration.Misc.collections) {
                // What a beautiful shit!
                // Till today, I have never seen a hash map that doesn't implement the Map interface.But mojang made it.
                processors.register(asm -> {
                    asm.node.fields.clear();
                    asm.node.innerClasses.clear();
                    String superName = "it/unimi/dsi/fastutil/ints/Int2ObjectOpenHashMap";
                    asm.node.superName = superName;
                    asm.node.methods.clear();
                    asm.addMethod("<init>", "()V")
                            .instructions()
                            .loadThis()
                            .invokeSpecial(superName, "<init>", "()V")
                            .returns();
                    asm.addMethod(CoreModCore.mayDeobfuscated("func_76041_a", "lookup"), "(I)Ljava/lang/Object;")
                            .instructions()
                            .loadThis()
                            .loadI(1)
                            .invokeSpecial(superName, "get", "(I)Ljava/lang/Object;")
                            .returns();
                    asm.addMethod(CoreModCore.mayDeobfuscated("func_76037_b", "containsItem"), "(I)Z")
                            .instructions()
                            .loadThis()
                            .loadI(1)
                            .invokeSpecial(superName, "containsKey", "(I)Z")
                            .returns();
                    asm.addMethod(CoreModCore.mayDeobfuscated("func_76038_a", "addKey"), "(ILjava/lang/Object;)V")
                            .instructions()
                            .loadThis()
                            .loadI(1)
                            .loadA(2)
                            .invokeSpecial(superName, "put", "(ILjava/lang/Object;)Ljava/lang/Object;")
                            .pop()
                            .returns();
                    asm.addMethod(CoreModCore.mayDeobfuscated("func_76049_d", "removeObject"), "(I)Ljava/lang/Object;")
                            .instructions()
                            .loadThis()
                            .loadI(1)
                            .invokeSpecial(superName, "remove", "(I)Ljava/lang/Object;")
                            .returns();
                    asm.addMethod(CoreModCore.mayDeobfuscated("func_76046_c", "clearMap"), "()V")
                            .instructions()
                            .loadThis()
                            .invokeSpecial(superName, "clear", "()V")
                            .returns();
                }, "net.minecraft.util.IntHashMap");
                processors.register(asm -> {
                    final FieldNode field = asm.fieldByName(CoreModCore.mayDeobfuscated("field_82771_a", "rules"));
                    field.desc = "Lit/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap;";
                    field.signature = null;
                    for (MethodASM method : asm.methodList()) {
                        method.instructions().replaceType("java/util/TreeMap", "it/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap");
                    }
                }, "net.minecraft.world.GameRules");
                processors.register(asm -> asm.methodByName("func_148740_a", "createUnderlyingMap").replace(INodeMatcher.invokes("newHashMap"), ()->InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap")), "net.minecraft.client.audio.SoundRegistry", "net.minecraft.util.registry.RegistrySimple");
                processors.register(asm -> asm.constructor(null).replace(INodeMatcher.invokes("newHashSet"), ()->InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/longs/LongOpenHashSet")), "net.minecraft.world.gen.ChunkProviderServer");
                processors.register(asm -> asm.constructor(null).replace(INodeMatcher.invokes("newHashSet"), ()->InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/objects/ReferenceOpenHashSet")), "net.minecraft.entity.ai.attributes.AttributeMap");
                processors.register(asm -> asm.constructor(null).replace(INodeMatcher.invokes("newHashMap"), ()->InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/objects/Reference2ReferenceOpenHashMap")), "net.minecraft.util.ClassInheritanceMultiMap");
                processors.register(asm -> asm.constructor(null).replace(INodeMatcher.invokes("newHashSet"), ()->InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/objects/ObjectOpenHashSet")),
                        "net.minecraft.entity.Entity",
                        "net.minecraft.client.renderer.RenderGlobal",
                        "net.minecraft.client.multiplayer.WorldClient",
                        "net.minecraft.inventory.Container");
                processors.register(asm -> asm.constructor(null).replace(INodeMatcher.invokes("newHashMap"), ()->InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap")),
                        "net.minecraft.world.chunk.Chunk",
                        "net.minecraft.nbt.NBTTagCompound",
                        "net.minecraft.util.CooldownTracker",
                        "net.minecraft.world.biome.Biome",
                        "net.minecraft.world.WorldServer",
                        "net.minecraft.world.Explosion",
                        "net.minecraft.util.text.translation.LanguageMap",
                        "net.minecraft.scoreboard.Scoreboard",
                        "net.minecraft.block.state.pattern.BlockStateMatcher",
                        "net.minecraft.block.properties.PropertyEnum"
                );
                processors.register(asm -> asm.constructor(null).replace(INodeMatcher.invokes("newHashMap"), ()->InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/ints/Int2ObjectOpenHashMap")), "net.minecraft.village.Village");
                processors.register(asm -> {
                            final InstructionList instructions = asm.constructor(null).instructions();
                            instructions
                                    .replaceOnce(INodeMatcher.invokes("newHashMap"), InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap"));
                            instructions
                                    .replaceOnce(INodeMatcher.invokes("newHashMap"), InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/objects/Object2IntOpenHashMap"));
                            String mapTextureCounters = CoreModCore.mayDeobfuscated("field_110584_c", "mapTextureCounters");
                            String desc = asm.fieldByName(mapTextureCounters).desc = "Lit/unimi/dsi/fastutil/objects/Object2IntOpenHashMap;";
                            instructions.find(INodeMatcher.fields(mapTextureCounters)).desc = desc;
                            String o2iMap = "it/unimi/dsi/fastutil/objects/Object2IntOpenHashMap";
                            final MethodASM method = asm.methodByName("func_110578_a", "getDynamicTextureLocation");
                            method.node.localVariables.clear();
                            method.overwrite(list -> list
                                            .loadThis()
                                            .allocNewAndDupe("net/minecraft/util/ResourceLocation")
                                            .allocNewAndDupe("java/lang/StringBuilder")
                                            .constant("dynamic/")
                                            .construct("java/lang/StringBuilder", "(Ljava/lang/String;)V")
                                            .loadA(1)
                                            .invokeVirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;")
                                            .constant('_')
                                            .invokeVirtual("java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;")
                                            .getFieldThis(asm.name(), mapTextureCounters, desc)
                                            .loadA(1)
                                            .getFieldThis(asm.name(), mapTextureCounters, desc)
                                            .loadA(1)
                                            .invokeVirtual(o2iMap, "getInt", "(Ljava/lang/Object;)I")
                                            .constant(1)
                                            .add(Opcodes.IADD)
                                            .dupe()
                                            .storeI(3)
                                            .invokeVirtual(o2iMap, "put", "(Ljava/lang/Object;I)I")
                                            .pop()
                                            .loadI(3)
                                            .invokeVirtual("java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;")
                                            .invokeVirtual("java/lang/StringBuilder", "toString", "()Ljava/lang/String;")
                                            .construct("net/minecraft/util/ResourceLocation","(Ljava/lang/String;)V")
                                            .dupe()
                                            .storeA(4)
                                            .loadA(2)
                                            .invokeVirtual(asm.name(),CoreModCore.mayDeobfuscated("func_110579_a","loadTexture"),"(Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/texture/ITextureObject;)Z")
                                            .loadA(4)
                                            .returns()
                                    , new AbstractInt2ObjectMap.BasicEntry<>(3, "I")
                            ,new AbstractInt2ObjectMap.BasicEntry<>(4,"Lnet/minecraft/util/ResourceLocation;"));
                        }
                        , "net.minecraft.client.renderer.texture.TextureManager"
                );
                // This optimization are from Lithium mod by JellySquid.
                processors.register(asm -> {
                            String VALUES = CoreModCore.mayDeobfuscated("field_82609_l", "VALUES");
                            //Avoid the modulo/abs operations
                            asm.methodByName("func_176734_d", "getOpposite").overwrite(
                                    list -> list
                                            .staticVar(asm.name(), VALUES, "[Lnet/minecraft/util/EnumFacing;")
                                            .getFieldThis(asm.name(), CoreModCore.mayDeobfuscated("field_176759_h", "opposite"), "I")
                                            .add(Opcodes.AALOAD)
                                            .returns());
                            //Do not allocate an excessive number of Direction arrays
                            asm.methodByName("func_176741_a", "random").overwrite(
                                    list -> list
                                            .staticVar(asm.name(), VALUES, "[Lnet/minecraft/util/EnumFacing;")
                                            .dupe()
                                            .add(Opcodes.ARRAYLENGTH)
                                            .load(0)
                                            .swap()
                                            .invokeVirtual("java/util/Random", "nextInt", "(I)I")
                                            .add(Opcodes.AALOAD)
                                            .returns()
                            );
                            // Lithium feature #2: math/fast_blockpos/DirectionMixin.java
                            asm.addField("offsetX", "I", 0);
                            asm.addField("offsetY", "I", 0);
                            asm.addField("offsetZ", "I", 0);
                            asm.constructor(null).instructions().insertBeforeReturnUnique(new InstructionList()
                                    .loadThis()
                                    .loadA(9)
                                    .invokeVirtual("net/minecraft/util/math/Vec3i", CoreModCore.mayDeobfuscated("func_177958_n", "getX"), "()I")
                                    .setField("net/minecraft/util/EnumFacing", "offsetX", "I")
                                    .loadThis()
                                    .loadA(9)
                                    .invokeVirtual("net/minecraft/util/math/Vec3i", CoreModCore.mayDeobfuscated("func_177956_o", "getY"), "()I")
                                    .setField("net/minecraft/util/EnumFacing", "offsetY", "I")
                                    .loadThis()
                                    .loadA(9)
                                    .invokeVirtual("net/minecraft/util/math/Vec3i", CoreModCore.mayDeobfuscated("func_177952_p", "getZ"), "()I")
                                    .setField("net/minecraft/util/EnumFacing", "offsetZ", "I"));
                            asm.methodByName("func_82601_c", "getXOffset").overwrite(i -> i.getFieldThis("net/minecraft/util/EnumFacing", "offsetX", "I").returns());
                            asm.methodByName("func_96559_d", "getYOffset").overwrite(i -> i.getFieldThis("net/minecraft/util/EnumFacing", "offsetY", "I").returns());
                            asm.methodByName("func_82599_e", "getZOffset").overwrite(i -> i.getFieldThis("net/minecraft/util/EnumFacing", "offsetZ", "I").returns());
                        }
                        , "net.minecraft.util.EnumFacing");

                //Avoid expensive hashmap lookup
                processors.register(asm -> {
                    final InstructionList instructions = asm.staticBlock().instructions();
                    int count=0;
                    for (AbstractInsnNode instruction : instructions) {
                        if(INodeMatcher.invokes("newHashMap").match(instruction))instructions.replace(instruction, new InsnNode(Opcodes.ACONST_NULL));
                        else if(INodeMatcher.fields(CoreModCore.mayDeobfuscated("field_176761_p","NAME_LOOKUP")).match(instruction) && count++==1){
                            instructions.removeWithNext(instruction,8);
                        }
                    }
                    instructions.replace(INodeMatcher.invokes("newHashMap"), new InsnNode(Opcodes.ACONST_NULL));
                    asm.methodByName("func_176717_a", "byName").overwrite(
                            list -> list.load(0)
                                    .doJump(Opcodes.IFNONNULL, InstructionList::returnNull)
                                    .load(0)
                                    .invokeVirtual("java/lang/String", "length", "()I")
                                    .constant(1)
                                    .doJump(Opcodes.IF_ICMPEQ, InstructionList::returnNull)
                                    .load(0)
                                    .constant(0)
                                    .invokeVirtual("java/lang/String", "charAt", "(I)C")
                                    .dupe()
                                    .store(1)
                                    .constant('x')
                                    .doJump(Opcodes.IF_ICMPNE, x -> x.staticVar(asm.name(), "X", "Lnet/minecraft/util/EnumFacing$Axis;").returns())
                                    .load(1)
                                    .constant('y')
                                    .doJump(Opcodes.IF_ICMPNE, x -> x.staticVar(asm.name(), "Y", "Lnet/minecraft/util/EnumFacing$Axis;").returns())
                                    .load(1)
                                    .constant('z')
                                    .doJump(Opcodes.IF_ICMPNE, x -> x.staticVar(asm.name(), "Z", "Lnet/minecraft/util/EnumFacing$Axis;").returns())
                                    .returnNull(),
                            new AbstractInt2ObjectMap.BasicEntry<>(1, "I")
                    );
                }, "net.minecraft.util.EnumFacing$Axis");
                // From Lithium mod: fast_hand_swing/LivingEntityMixin.java
                processors.register(asm -> asm.methodByName("func_82168_bl", "updateArmSwingProgress").insertAtHead(list -> list
                        .getFieldThis(asm.name(), CoreModCore.mayDeobfuscated("field_82175_bq", "isSwingInProgress"), "Z")
                        .doJump(Opcodes.IFNE, l -> l
                                .getFieldThis(asm.name(), CoreModCore.mayDeobfuscated("field_110158_av", "swingProgressInt"), "I")
                                .doJump(Opcodes.IFNE, InstructionList::returns)
                        )
                ), "net.minecraft.entity.EntityLivingBase");
                processors.register(asm->{
                    final MethodASM method = asm.methodByName("func_178604_a", "floodFill");
                    final String type = "it/unimi/dsi/fastutil/ints/IntArrayFIFOQueue";
                    CoreModCore.findLocal(method.node.localVariables,3).desc= "L"+type+";";
                    final InstructionList instructions = method.instructions();
                    instructions.replaceOnce(INodeMatcher.invokes("newArrayDeque"),InstructionList.constructWithNoArgs(type));
                    CoreModCore.setVirtualCall(instructions.find(INodeMatcher.invokes("isEmpty")),type);
                    final MethodInsnNode poll=instructions.find(INodeMatcher.invokes("poll"));
                    CoreModCore.setVirtualCall(poll,type);
                    poll.desc="()I";
                    poll.name="dequeueInt";
                    instructions.removeWithNext(poll.getNext(),2);
                    for (MethodInsnNode node : instructions.findAll(INodeMatcher.invokes("add"))) {
                        CoreModCore.setVirtualCall(node,type);
                        node.desc="(I)V";
                        node.name="enqueue";
                        instructions.remove(node.getPrevious());
                        instructions.remove(node.getNext());
                    }
                },"net.minecraft.client.renderer.chunk.VisGraph");
            }
            if (Configuration.disableStats) {
                processors.register(asm -> asm.methodByName("func_75971_g", "registerStat").breaksThis(), "net.minecraft.stats.StatBasic", "net.minecraft.stats.StatBase");
            }
            if (Configuration.forceItemEntityMerge) {
                processors.register(asm -> {
                    final MethodASM method = asm.methodByName("func_70100_b_", "onCollideWithPlayer");
                    asm.methodByName("func_70289_a", "combineItems").replace(
                            INodeMatcher.invokes(
                                    CoreModCore.mayDeobfuscated("func_77976_d", "getMaxStackSize"))
                            , ()->new InstructionList()
                                    .pop()
                                    .constant(Integer.MAX_VALUE));
                    method.instructions()
                            .redirect(null,
                                    CoreModCore.mayDeobfuscated("func_70441_a", "addItemStackToInventory"), null,
                                    "com/sing/astatine/utils/Utils", "addItemToInventory", "(Lnet/minecraft/entity/player/InventoryPlayer;Lnet/minecraft/item/ItemStack;)Z", Opcodes.INVOKESTATIC);
                }, "net.minecraft.entity.item.EntityItem");
            }
            if (Configuration.enableExtendedStackStorage) {
                processors.register(asm -> {
                    asm.constructor("(Lnet/minecraft/nbt/NBTTagCompound;)V")
                            .instructions()
                            .redirect(null, CoreModCore.mayDeobfuscated("func_74771_c", "getByte"), null, null, CoreModCore.mayDeobfuscated("func_74762_e", "getInteger"), "(Ljava/lang/String;)I", -1);
                    final InstructionList instructions = asm.methodByName("func_77955_b", "writeToNBT")
                            .instructions();
                    instructions.redirect(null, CoreModCore.mayDeobfuscated("func_74774_a", "setByte"), null, null, CoreModCore.mayDeobfuscated("func_74768_a", "setInteger"), "(Ljava/lang/String;I)V", -1);
                    instructions.remove(instructions.find(INodeMatcher.opcode(Opcodes.I2B)));
                }, "net.minecraft.item.ItemStack");
            }
            if (Configuration.projectileRenderDelay > 0) {
                processors.register(asm -> {
                    String name = CoreModCore.mayDeobfuscated("func_145770_h", "isInRangeToRender3d");
                    final MethodASM method = asm.addMethod(name, "(DDD)Z");
                    method.instructions().getFieldThis("net/minecraft/entity/Entity", CoreModCore.mayDeobfuscated("field_70173_aa", "ticksExisted"), "I")
                            .configInt("projectileRenderDelay")
                            .doJump(Opcodes.IF_ICMPGE, InstructionList::return0)
                            .loadThis()
                            .loadVars(Opcodes.DLOAD, 1, 3, 5)
                            .invokeSpecial("net/minecraft/entity/Entity", name, "(DDD)Z")
                            .returns();
                }, "net.minecraft.entity.projectile.EntityThrowable");
            }
            if (Configuration.LazyChunkSaving.enabled) {
                processors.register(asm -> asm.addField("playerStayed", "Z", 0), "net.minecraft.world.chunk.Chunk");
                processors.register(asm -> {
                    final MethodASM method = asm.methodByName("func_70071_h_", "onUpdate");
                    method.insertAtHead(method.createList()
                            .getFieldThis(asm.name(), CoreModCore.mayDeobfuscated("field_70170_p", "world"), "Lnet/minecraft/world/World;")
                            .getFieldThis(asm.name(), CoreModCore.mayDeobfuscated("field_70176_ah", "chunkCoordX"), "I")
                            .getFieldThis(asm.name(), CoreModCore.mayDeobfuscated("field_70164_aj", "chunkCoordZ"), "I")
                            .invokeVirtual("net/minecraft/world/World", CoreModCore.mayDeobfuscated("func_72964_e", "getChunk"), "(II)Lnet/minecraft/world/chunk/Chunk;")
                            .constant(true)
                            .setField("net/minecraft/world/chunk/Chunk", "playerStayed", "Z")
                    );
                }, "net.minecraft.entity.player.EntityPlayerMP");
                processors.register(asm -> {
                    final MethodASM method = asm.methodByName("func_75816_a", "saveChunk");
                    method.insertAtHead(list -> list
                            .load(2)
                            .field("net/minecraft/world/chunk/Chunk", "playerStayed", "Z")
                            .doJump(Opcodes.IFNE, InstructionList::returns)
                    );
                }, "net.minecraft.world.chunk.storage.AnvilChunkLoader");
                // TODO: This feature works terribly with multi-chunk structures(e.g. village),fix this.
                processors.register(asm -> {
//                    final MethodASM method = asm.methodByName("func_180701_a", "recursiveGenerate");
//                    final InstructionList instructions = method.instructions();
//                    final MethodInsnNode node = instructions.find(INodeMatcher.invokes("put"));
//                    instructions.insert(node,method.createList().load(2,3));
                }, "net.minecraft.world.gen.structure.MapGenStructure");

            }
            if (Configuration.Random.fastRandom) {
                processors.register(asm -> processFastRandom(Collections.singletonList(asm.staticBlock())),
                        "net.minecraft.item.Item",
                        "net.minecraft.server.network.NetHandlerLoginServer",
                        "net.minecraft.tileentity.TileEntityDispenser",
                        "net.minecraft.tileentity.TileEntityEnchantmentTable",
                        "net.minecraft.util.math.MathHelper",
                        "net.minecraft.util.datafix.fixes.ZombieProfToType",
                        "net.minecraft.client.particle.ParticleSpell");
                processors.register(asm -> processFastRandom(asm.allConstructors()),
                        "net.minecraft.client.gui.FontRenderer",
                        "net.minecraft.client.audio.MusicTicker",
                        "net.minecraft.client.renderer.entity.RenderEnderman",
                        "net.minecraft.client.gui.GuiEnchantment",
                        "net.minecraft.client.audio.SoundEventAccessor",
                        "net.minecraft.client.network.NetHandlerPlayClient",
                        "net.minecraft.client.particle.Particle",
                        "net.minecraft.client.particle.ParticleManager",
                        "net.minecraft.entity.Entity",
                        "net.minecraft.server.MinecraftServer",
                        "net.minecraft.entity.passive.EntitySquid",
                        "net.minecraft.world.Explosion");
            }
            if (Configuration.Lang.languageSelector) {
                processors.register(
                        asm -> asm.methodByName("func_146284_a", "actionPerformed").instructions()
                                .replaceType("net/minecraft/client/gui/GuiLanguage", "com/sing/astatine/client/gui/multilang/GuiScreenLanguages"),
                        "net.minecraft.client.gui.GuiMainMenu",
                        "net.minecraft.client.gui.GuiOptions"
                );
            }
        }

        @Override
        public byte[] transform(String obfuscatedName, String transformedName, byte[] basicClass) {
            final Collection<Processor> processor = processors.get(transformedName);
            if (processor != null) {
                try {
                    ClassASM asm = ClassASM.get(basicClass);
                    for (Processor consumer : processor) {
                        consumer.process(asm);
                    }
                    return asm.toBytes();
                } catch (Exception e) {
                    LOGGER.error("[Astatine] Unable to load coremod when transforming class '{}'!", transformedName, e);
                    if (Configuration.debug) throw new RuntimeException();
                    else return basicClass;
                }
            }
            return basicClass;
        }
    }
}
