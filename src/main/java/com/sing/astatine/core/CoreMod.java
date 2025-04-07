package com.sing.astatine.core;

import com.google.common.collect.ImmutableMap;
import com.sing.astatine.Configuration;
import com.sing.astatine.utils.Utils;
import com.sing.astatine.utils.config.ConfigurationLoader;
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
        mixinIf(mixins, "mixins.fastrandom.json", Configuration.Random.fastRandom);
        mixinIf(mixins, "mixins.faststar.json", Configuration.StarGen.enabled);
        mixinIf(mixins, "mixins.star_twinkling.json", Configuration.StarTwinkling.enabled);
        mixinIf(mixins, "mixins.weather.json", Configuration.forceWeatherParticleUseConstLight);
        mixinIf(mixins, "mixins.renderskip.json", Configuration.maxEntityRenderDistance != -1 || Configuration.maxTileEntityRenderDistance != -1);
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
                            .jumpIfNot0(instructions.findLastNth(INodeMatcher.labels(), 1));
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
            if (Configuration.miscOptimization) {
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
                processors.register(asm -> asm.methodByName("func_148740_a", "createUnderlyingMap").instructions().replace(INodeMatcher.invokes("newHashMap"), InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap")), "net.minecraft.client.audio.SoundRegistry", "net.minecraft.util.registry.RegistrySimple");
                processors.register(asm -> asm.constructor(null).replace(INodeMatcher.invokes("newHashSet"), InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/longs/LongOpenHashSet")), "net.minecraft.world.gen.ChunkProviderServer");
                processors.register(asm -> asm.constructor(null).replace(INodeMatcher.invokes("newHashSet"), InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/objects/ReferenceOpenHashSet")), "net.minecraft.entity.ai.attributes.AttributeMap");
                processors.register(asm -> asm.constructor(null).replace(INodeMatcher.invokes("newHashSet"), InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/objects/ObjectOpenHashSet")), "net.minecraft.entity.Entity");
                processors.register(asm -> asm.constructor(null).replace(INodeMatcher.invokes("newHashMap"), InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/objects/Reference2ReferenceOpenHashMap")), "net.minecraft.util.ClassInheritanceMultiMap");
                processors.register(asm -> asm.constructor(null).replace(INodeMatcher.invokes("newHashMap"), InstructionList.constructWithNoArgs("it/unimi/dsi/fastutil/objects/Object2ObjectOpenHashMap")), "net.minecraft.world.chunk.Chunk");
            }
            if (Configuration.disableStats) {
                processors.register(asm -> asm.methodByName("func_75971_g", "registerStat").breaksThis(), "net.minecraft.stats.StatBasic", "net.minecraft.stats.StatBase");
            }
            if (Configuration.forceItemEntityMerge) {
                processors.register(asm -> {
                    final MethodASM method = asm.methodByName("func_70100_b_", "onCollideWithPlayer");
                    asm.methodByName("func_70289_a", "combineItems").instructions().replace(
                            INodeMatcher.invokes(
                                    CoreModCore.mayDeobfuscated("func_77976_d", "getMaxStackSize"))
                            , new InstructionList()
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
                processors.register(asm -> {
                    final MethodASM method = asm.methodByName("func_75816_a", "saveChunk");
                    method.insertAtHead(method.createList()
                            .load(2)
                            .invokeVirtual("net/minecraft/world/chunk/Chunk", CoreModCore.mayDeobfuscated("func_177416_w", "getInhabitedTime"), "()J")
                            .staticVar("com/sing/astatine/Configuration$LazyChunkSaving", "minimumInhabitedTime", "J")
                            .add(Opcodes.LCMP)
                            .constant(1)
                            .doJump(Opcodes.IF_ICMPEQ,
                                    InstructionList::returns
                            )
                    );
                }, "net.minecraft.world.chunk.storage.AnvilChunkLoader");
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
                ClassASM asm = ClassASM.get(basicClass);
                for (Processor consumer : processor) {
                    try {
                        consumer.process(asm);
                    } catch (Exception e) {
                        LOGGER.error("[Astatine] Unable to load coremod when transforming class '{}'!", transformedName, e);
                    }
                }
                return asm.toBytes();
            }
            return basicClass;
        }
    }
}
