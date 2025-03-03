package com.sing.astatine.core;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

public class MethodASM {
    public MethodNode node;

    public MethodASM(MethodNode node) {
        this.node = node;
    }
    public InstructionList instructions(){
        return new InstructionList(node.instructions);
    }

    /**
     * Let the method returns directly,void returned.
     */
    public void breaks(){
        node.instructions.insertBefore(node.instructions.getFirst(),new InsnNode(Opcodes.RETURN));
    }
    public void breaks(boolean value){
        final InstructionList list = new InstructionList();
        list.constant(value);
        list.returnI();
        instructions().insertAfterHead(list);
    }
    public int local(String name, String desc, LabelNode start,LabelNode end){
        final int id = node.maxLocals++;
        node.localVariables.add(new LocalVariableNode(name,desc,null,start,end,id));
        return id;
    }
}
