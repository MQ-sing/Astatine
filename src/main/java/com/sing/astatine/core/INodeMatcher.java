package com.sing.astatine.core;

import org.objectweb.asm.Opcodes;
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
        return invokes(name, Opcodes.INVOKEVIRTUAL);
    }

    static INodeMatcher<MethodInsnNode> invokes(String name, int opcode) {
        return node -> (node instanceof MethodInsnNode) && node.getOpcode() == opcode && ((MethodInsnNode) node).name.equals(name);
    }

    static INodeMatcher<LdcInsnNode> ldc(Object value) {
        return node -> (node instanceof LdcInsnNode) && ((LdcInsnNode) node).cst.equals(value);
    }

    boolean match(AbstractInsnNode node);
}
