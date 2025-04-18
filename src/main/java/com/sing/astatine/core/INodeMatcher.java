package com.sing.astatine.core;

import org.objectweb.asm.tree.*;

@FunctionalInterface
public interface INodeMatcher<T extends AbstractInsnNode> {
    INodeMatcher<LabelNode> LABELS_MATCHER = node -> node instanceof LabelNode;
    INodeMatcher<InsnNode> RETURN_MATCHER = node ->
            node.getOpcode() >= 172 && node.getOpcode() <= 177;

    static INodeMatcher<LabelNode> labels() {
        return LABELS_MATCHER;
    }
    static INodeMatcher<InsnNode> returns(){
        return RETURN_MATCHER;
    }
    static INodeMatcher<MethodInsnNode> invokes(String name) {
        return invokes(name, -1);
    }
    static INodeMatcher<MethodInsnNode> opcode(int opcode) {
        return node -> node.getOpcode() == opcode;
    }
    static INodeMatcher<MethodInsnNode> invokes(String name, int opcode) {
        if(opcode==-1)return node->(node instanceof MethodInsnNode) && ((MethodInsnNode) node).name.equals(name);
        return node -> (node instanceof MethodInsnNode) && node.getOpcode() == opcode && ((MethodInsnNode) node).name.equals(name);
    }
    static INodeMatcher<FieldInsnNode> fields(String name){
        return node->(node instanceof FieldInsnNode)&&((FieldInsnNode) node).name.equals(name);
    }

    static INodeMatcher<LdcInsnNode> ldc(Object value) {
        return node -> (node instanceof LdcInsnNode) && ((LdcInsnNode) node).cst.equals(value);
    }

    boolean match(AbstractInsnNode node);
}
