package com.sing.astatine.core;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.transformers.MixinClassWriter;

public class ClassASM {
    public final ClassNode node=new ClassNode();
    private ClassReader reader;
    public static ClassASM get(byte[] bytes){
        ClassReader cr=new ClassReader(bytes);
        final ClassASM asm = new ClassASM();
        cr.accept(asm.node,0);
        asm.reader=cr;
        return asm;
    }
    public MethodASM constructor(String desc){
        for (MethodNode method : node.methods) {
            if(method.name.equals("<init>") && (desc==null || method.desc.equals(desc)))return new MethodASM(method);
        }
        throw new IllegalStateException("Class without constructors");
    }

    public MethodASM staticBlock(){
        for (MethodNode method : node.methods) {
            if( FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc).equals("<clinit>"))return new MethodASM(method);
        }
        throw new IllegalStateException("Class without static blocks");
    }
    public MethodASM methodByName(String srg,String deobfuscated){
        final String name = CoreModCore.isRuntimeDeobfuscated ? deobfuscated : srg;
        for (MethodNode method : node.methods) {
            if( FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name,method.name,method.desc).equals(name))return new MethodASM(method);
        }
        return null;
    }
    public byte[] toBytes(){
        return toBytes(true);
    }
    // Better for find bugs
    public byte[] toBytes(boolean calcFrames){
        // Don't use the ClassWriter!!!!!!!
        // It will cause a stupid bug:"Cannot find class net.minecraft.entity.EntityLivingBase".
        // I found this after 2 days of pain. Don't be me.
        ClassWriter cw = new MixinClassWriter(reader,ClassWriter.COMPUTE_MAXS|(calcFrames?ClassWriter.COMPUTE_FRAMES:0));
        node.accept(cw);
        return cw.toByteArray();
    }
    public String name(){
        return node.name;
    }
    public String field(String name,String desc,Object value){
        node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,name,desc,null,value));
        return name;
    }
}
