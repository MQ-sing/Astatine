package com.sing.astatine.core;

import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class MethodASM {
    public MethodNode node;

    public MethodASM(MethodNode node) {
        this.node = node;
    }
    public InstructionList instructions(){
        return InstructionList.fromMethod(this);
    }
    @SafeVarargs
    public final InstructionList overwrite(Consumer<InstructionList> newContent, AbstractInt2ObjectMap.BasicEntry<String>... entries){
        final LabelNode firstLabel=new LabelNode();
        final LabelNode lastLabel = new LabelNode();
        for (AbstractInt2ObjectMap.BasicEntry<String> desc : entries) {
            node.localVariables.add(new LocalVariableNode("_"+desc.getIntKey(), desc.getValue(), null,firstLabel,lastLabel,desc.getIntKey()));
        }
        final InstructionList list = createList();
        newContent.accept(list);
        list.add(0,firstLabel);
        list.add(lastLabel);
        node.instructions=list.list;
        return list;
    }
    public InstructionList createList(){
        return new InstructionList().bind(this);
    }

    /**
     * Let the method returns directly,void returned.
     */
    public void breaks(){
        node.instructions.clear();
        node.instructions.add(new InsnNode(Opcodes.RETURN));
    }
    public void breaksNull(){
        node.instructions.clear();
        node.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
        node.instructions.add(new InsnNode(Opcodes.ARETURN));
    }
    public void breaksThis(){
        node.instructions.clear();
        node.instructions.add(new VarInsnNode(Opcodes.ALOAD,0));
        node.instructions.add(new InsnNode(Opcodes.ARETURN));
    }
    public void breaks(boolean value){
        node.instructions=createList().constant(value).returns().list;
    }
    public int local(String name, String desc, LabelNode start,LabelNode end){
        final int index = node.localVariables.size();
        node.localVariables.add(new LocalVariableNode(name,desc,null,start,end,index));
        return index;
    }
    private String returnType;
    public String returnType(){
        if(returnType==null) {
            returnType = CoreModCore.getDescReturnType(node.desc);
        }
        return returnType;
    }
    public void replace(INodeMatcher<?> matcher, Supplier<InstructionList> newNodes) {
        instructions().replace(matcher,newNodes);
    }
    public void insertAtHead(InstructionList list){
        node.instructions.insert(node.instructions.getFirst(),list.list);
    }
    public void insertAtHead(Consumer<InstructionList> f){
        final InstructionList list = createList();
        f.accept(list);
        node.instructions.insert(node.instructions.getFirst(),list.list);
    }
}
