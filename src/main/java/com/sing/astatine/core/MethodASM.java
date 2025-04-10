package com.sing.astatine.core;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class MethodASM {
    public MethodNode node;

    public MethodASM(MethodNode node) {
        this.node = node;
    }
    public InstructionList instructions(){
        return InstructionList.fromMethod(this);
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
        final InstructionList list = new InstructionList();
        list.constant(value);
        list.returns();
        instructions().insertHead(list);
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
    public void replace(INodeMatcher<?> matcher, InstructionList newNodes) {
        instructions().replace(matcher,newNodes);
    }
    public void insertAtHead(InstructionList list){
        node.instructions.insert(node.instructions.getFirst(),list.list);
    }
}
