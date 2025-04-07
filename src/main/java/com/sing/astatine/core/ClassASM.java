package com.sing.astatine.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.transformers.MixinClassWriter;

import java.util.ArrayList;
import java.util.List;

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
    public List<MethodASM> allConstructors(){
        List<MethodASM> list=new ArrayList<>();
        for (MethodNode method : node.methods) {
            if(method.name.equals("<init>"))list.add(new MethodASM(method));
        }
        return list;
    }
    public MethodASM constructor(String desc){
        for (MethodNode method : node.methods) {
            if(method.name.equals("<init>") && (desc==null || method.desc.equals(desc)))return new MethodASM(method);
        }
        throw new IllegalStateException("Class without constructors");
    }

    public MethodASM staticBlock(){
        for (MethodNode method : node.methods) {
            if(method.name.equals("<clinit>"))return new MethodASM(method);
        }
        throw new IllegalStateException("Class without static blocks");
    }
    public MethodASM methodByName(String name){
        for (MethodNode method : node.methods) {
            if(method.name.equals(name))return new MethodASM(method);
        }
        return null;
    }
    @NotNull
    public ImmutableMap<String,MethodASM> methods(){
        final ImmutableMap.Builder<String, MethodASM> builder = ImmutableMap.builder();
        for (MethodNode method : node.methods) {
            builder.put(method.name,new MethodASM(method));
        }
        return builder.build();
    }
    public ImmutableList<MethodASM> methodList(){
        final ImmutableList.Builder<MethodASM> builder = ImmutableList.builder();
        for (MethodNode method : node.methods) {
            builder.add(new MethodASM(method));
        }
        return builder.build();
    }
    public MethodASM methodByName(String srg,String deobfuscated){
        return methodByName(CoreModCore.isRuntimeDeobfuscated ? deobfuscated : srg);
    }
    public MethodASM addMethod(String name,String desc,int access){
        final MethodNode node = new MethodNode(access, name, desc, null, null);
        this.node.methods.add(node);
        return new MethodASM(node);
    }
    public MethodASM addMethod(String name,String desc){
        return addMethod(name,desc,Opcodes.ACC_PUBLIC);
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
    public String addField(String name, String desc, Object value){
        node.fields.add(new FieldNode(Opcodes.ACC_PUBLIC,name,desc,null,value));
        return name;
    }
    public FieldNode fieldByName(String name){
        for (FieldNode field : node.fields) {
            if(field.name.equals(name))return field;
        }
        return null;
    }
}
