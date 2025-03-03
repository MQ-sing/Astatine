package com.sing.astatine.core;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

@FunctionalInterface
public interface INodeMatcher<T extends AbstractInsnNode> {
    boolean match(AbstractInsnNode node);
    INodeMatcher<LabelNode> LABELS_MATCHER=node->node instanceof LabelNode;
    static INodeMatcher<LabelNode> labels(){
        return LABELS_MATCHER;
    }
}
