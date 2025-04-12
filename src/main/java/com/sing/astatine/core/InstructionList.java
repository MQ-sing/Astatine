package com.sing.astatine.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class InstructionList implements List<AbstractInsnNode>, Iterable<AbstractInsnNode>, RandomAccess {
    @Nullable
    private MethodASM asm;
    public final InsnList list;
    /**
     * Constructs an InstructionList wrapping an existing {@link InsnList}.
     * @param list The ASM instruction list to wrap.
     */
    public InstructionList(InsnList list) {
        this.list = list;
    }
    public static InstructionList fromMethod(MethodASM asm){
        return new InstructionList(asm.node.instructions).bind(asm);
    }
    public InstructionList bind(MethodASM asm){
        this.asm=asm;
        return this;
    }
    /**
     * Constructs an empty InstructionList with a new {@link InsnList}.
     */
    public InstructionList() {
        list = new InsnList();
    }
    public static InstructionList constructWithNoArgs(String name){
        return new InstructionList().allocNewAndDupe(name).construct(name);
    }
    public InstructionList load(int index){
        list.add(new VarInsnNode(CoreModCore.doTypeOffset(Opcodes.ILOAD,Objects.requireNonNull(asm).node.localVariables.get(index).desc),index));
        return this;
    }
    public InstructionList load(int... indexes){
        for (int index : indexes) {
            list.add(new VarInsnNode(CoreModCore.doTypeOffset(Opcodes.ILOAD,Objects.requireNonNull(asm).node.localVariables.get(index).desc),index));
        }
        return this;
    }
    /**
     * Appends an ALOAD instruction for the given variable index.
     * @param index The local variable index.
     */
    public InstructionList loadA(int index) {
        list.add(new VarInsnNode(Opcodes.ALOAD, index));
        return this;
    }
    /**
     * Appends an ILOAD instruction for the given variable index.
     * @param index The local variable index.
     */
    public InstructionList loadI(int index) {
        list.add(new VarInsnNode(Opcodes.ILOAD, index));
        return this;
    }
    /**
     * Appends an FLOAD instruction for the given variable index.
     * @param index The local variable index.
     */
    public InstructionList loadF(int index) {
        list.add(new VarInsnNode(Opcodes.FLOAD, index));
        return this;
    }
    /**
     * Appends an DLOAD instruction for the given variable index.
     * @param index The local variable index.
     */
    public InstructionList loadD(int index) {
        list.add(new VarInsnNode(Opcodes.DLOAD, index));
        return this;
    }

    /**
     * Appends an LLOAD instruction for the given variable index.
     * @param index The local variable index.
     */
    public InstructionList loadL(int index) {
        list.add(new VarInsnNode(Opcodes.LLOAD, index));
        return this;
    }
    public InstructionList loadVars(int opcode,int... indexes){
        for (int index : indexes) {
            list.add(new VarInsnNode(opcode,index));
        }
        return this;
    }
    public InstructionList loadVarsInRange(int opcode,int begin,int end){
        for (int index =begin;index<end;++index) {
            list.add(new VarInsnNode(opcode,index));
        }
        return this;
    }
    /**
     * Appends an 'ALOAD 0' instruction(load "this" reference)
     */
    public InstructionList loadThis() {
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        return this;
    }
    public InstructionList swap(){
        list.add(new InsnNode(Opcodes.SWAP));
        return this;
    }
    public InstructionList store(int index){
        list.add(new VarInsnNode(CoreModCore.doTypeOffset(Opcodes.ISTORE,Objects.requireNonNull(asm).node.localVariables.get(index).desc),index));
        return this;
    }
    public InstructionList storeA(int index){
        list.add(new VarInsnNode(Opcodes.ASTORE, index));
        return this;
    }
    public InstructionList storeI(int index){
        list.add(new VarInsnNode(Opcodes.ISTORE, index));
        return this;
    }
    /**
     * Appends an ACONST_NULL instruction.
     */
    public InstructionList constantNull() {
        list.add(new InsnNode(Opcodes.ACONST_NULL));
        return this;
    }
    /**
     * Appends an ICONST_0 or ICONST_1 instruction based on the boolean value.
     * @param flag The boolean value to push (true -> ICONST_1, false -> ICONST_0).
     */
    public InstructionList constant(boolean flag) {
        list.add(new InsnNode(flag?Opcodes.ICONST_1:Opcodes.ICONST_0));
        return this;
    }
    /**
     * Appends a constant instruction for the given integer value, optimizing for
     * dconst_N, or ldc as appropriate.
     * @param value The double value to push.
     */
    public InstructionList constant(double value) {
        if (value == 0)
            list.add(new InsnNode(Opcodes.DCONST_0));
        else if (value == 1)
            list.add(new InsnNode(Opcodes.DCONST_1));
        else list.add(new LdcInsnNode(value));
        return this;
    }
    /**
     * Appends a constant instruction for the given integer value, optimizing for
     * fconst_N, or ldc as appropriate.
     * @param value The float value to push.
     */
    public InstructionList constant(float value) {
        if (value == 0)
            list.add(new InsnNode(Opcodes.FCONST_0));
        else if (value == 1)
            list.add(new InsnNode(Opcodes.FCONST_1));
        else list.add(new LdcInsnNode(value));
        return this;
    }
    /**
     * Appends a constant instruction for the given integer value, optimizing for
     * lconst_N, or ldc as appropriate.
     * @param value The long value to push.
     */
    public InstructionList constant(long value) {
        if (value == 0)
            list.add(new InsnNode(Opcodes.LCONST_0));
        else if (value == 1)
            list.add(new InsnNode(Opcodes.LCONST_1));
        else list.add(new LdcInsnNode(value));
        return this;
    }
    /**
     * Appends a constant instruction for the given integer value, optimizing for
     * iconst, bipush, sipush, or ldc as appropriate.
     * @param value The integer value to push.
     */
    public InstructionList constant(int value) {
        if (value >= -1 && value <= 5)
            list.add(new InsnNode(Opcodes.ICONST_0 + value));
        else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) list.add(new IntInsnNode(Opcodes.BIPUSH, value));
        else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) list.add(new IntInsnNode(Opcodes.SIPUSH, value));
        else list.add(new LdcInsnNode(value));
        return this;
    }
    /**
     * Appends a ldc instruction for the given String value.
     * @param str The string value to push.
     */
    public InstructionList constant(String str) {
        list.add(new LdcInsnNode(str));
        return this;
    }
    public InstructionList doJump(int opcode,LabelNode label){
        list.add(new JumpInsnNode(opcode,label));
        return this;
    }
    public InstructionList doJump(int opcode,Runnable whenNotJumped){
        LabelNode label = new LabelNode();
        this.add(new JumpInsnNode(opcode, label));
        whenNotJumped.run();
        this.add(label);
        return this;
    }
    public InstructionList doJump(int opcode, Consumer<InstructionList> whenNotJumped){
        LabelNode label = new LabelNode();
        this.add(new JumpInsnNode(opcode, label));
        whenNotJumped.accept(this);
        this.add(label);
        return this;
    }
    public LabelNode jumpIf0(){
        LabelNode label=new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ,label));
        return label;
    }
    public LabelNode jumpIfNot0(){
        LabelNode label=new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFNE,label));
        return label;
    }
    public LabelNode jumpIfNull(){
        LabelNode label=new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFNULL,label));
        return label;
    }
    public LabelNode jumpIfNonNull(){
        LabelNode label=new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFNONNULL,label));
        return label;
    }
    public void cast(String name){
        list.add(new TypeInsnNode(Opcodes.CHECKCAST,name));
    }
    public void isInstanceof(String name){
        list.add(new TypeInsnNode(Opcodes.INSTANCEOF,name));
    }
    public void label(LabelNode label){
        list.add(label);
    }
    public LabelNode label(){
        final LabelNode label = new LabelNode();
        list.add(label);
        return label;
    }
    public InstructionList returns(){
        list.add(new InsnNode(CoreModCore.doTypeOffset(Opcodes.IRETURN,Objects.requireNonNull(asm).returnType())));
        return this;
    }
    /**
     * Add instructions let it return 0,also known as false.
     */
    public void return0(){
        list.add(new InsnNode(Opcodes.ICONST_0));
        list.add(new InsnNode(Opcodes.IRETURN));
    }
    /**
     * Add instructions let it return 1,also known as true.
     */
    public void return1(){
        list.add(new InsnNode(Opcodes.ICONST_1));
        list.add(new InsnNode(Opcodes.IRETURN));
    }
    public void returnNull(){
        list.add(new InsnNode(Opcodes.ACONST_NULL));
        list.add(new InsnNode(Opcodes.ARETURN));
    }

    /**
     * Load a static variable.
     * @param className the class name this static variable in
     * @param name the name of this static variable
     * @param descriptor field descriptioin
     * @return this reference
     */
    public InstructionList staticVar(String className,String name,String descriptor){
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, className, name, descriptor));
        return this;
    }
    public InstructionList setStaticVar(String className,String name,String descriptor){
        list.add(new FieldInsnNode(Opcodes.PUTSTATIC, className, name, descriptor));
        return this;
    }
    public InstructionList configFlag(String name){
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sing/astatine/Configuration", name, "Z"));
        return this;
    }
    public InstructionList configInt(String name){
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sing/astatine/Configuration", name, "I"));
        return this;
    }
    public InstructionList configValue(String name,String desc){
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sing/astatine/Configuration", name, desc));
        return this;
    }
    public InstructionList field(String owner,String name,String desc){
        list.add(new FieldInsnNode(Opcodes.GETFIELD,owner,name,desc));
        return this;
    }
    public InstructionList setField(String owner,String name,String desc){
        list.add(new FieldInsnNode(Opcodes.PUTFIELD,owner,name,desc));
        return this;
    }
    public InstructionList pop(){
        list.add(new InsnNode(Opcodes.POP));
        return this;
    }
    public InstructionList pop2(){
        list.add(new InsnNode(Opcodes.POP2));
        return this;
    }
    public InstructionList invokeStatic(String owner,String name,String desc){
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, desc, false));
        return this;
    }
    public InstructionList getFieldThis(String owner, String name, String desc){
        list.add(new VarInsnNode(Opcodes.ALOAD,0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD,owner,name,desc));
        return this;
    }
    public InstructionList invokeVirtual(String owner,String name,String desc){
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,owner,name,desc,false));
        return this;
    }
    public InstructionList invokeInterface(String owner,String name,String desc){
        list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE,owner,name,desc,true));
        return this;
    }
    public InstructionList invokeVirtualThis(String owner,String name,String desc){
        list.add(new VarInsnNode(Opcodes.ALOAD,0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,owner,name,desc,false));
        return this;
    }
    public InstructionList dupe(){
        list.add(new InsnNode(Opcodes.DUP));
        return this;
    }
    public InstructionList invokeSpecial(String owner,String name,String desc){
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,owner,name,desc,false));
        return this;
    }
    public InstructionList construct(String owner){
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,owner,"<init>","()V",false));
        return this;
    }
    public InstructionList construct(String owner,String desc){
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,owner,"<init>",desc,false));
        return this;
    }
    public InstructionList allocNew(String type){
        list.add(new TypeInsnNode(Opcodes.NEW,type));
        return this;
    }
    public InstructionList allocNewAndDupe(String type){
        list.add(new TypeInsnNode(Opcodes.NEW,type));
        list.add(new InsnNode(Opcodes.DUP));
        return this;
    }

    public InstructionList add(int opcode){
        list.add(new InsnNode(opcode));
        return this;
    }
    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof AbstractInsnNode && list.contains((AbstractInsnNode) o);
    }

    @Override
    public @NotNull Iterator<AbstractInsnNode> iterator() {
        return list.iterator();
    }

    @Override
    public AbstractInsnNode @NotNull [] toArray() {
        return list.toArray();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T @NotNull [] toArray(T @NotNull [] a) {
        return (T[]) list.toArray();
    }

    @Override
    public boolean add(AbstractInsnNode abstractInsnNode) {
        list.add(abstractInsnNode);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof AbstractInsnNode) {
            list.remove((AbstractInsnNode) o);
            return true;
        } else return false;
    }
    public void remove(AbstractInsnNode o) {
        list.remove(o);
    }
    public void removeWithNext(AbstractInsnNode o,int count) {
        while(count-->0) {
            final AbstractInsnNode n=o.getNext();
            list.remove(o);
            o=n;
        }
    }
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (o instanceof AbstractInsnNode) {
                if (!list.contains((AbstractInsnNode) o)) return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends AbstractInsnNode> c) {
        for (AbstractInsnNode abstractInsnNode : c) {
            list.add(abstractInsnNode);
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends AbstractInsnNode> c) {
        AbstractInsnNode lastNode=list.get(index);
        for (AbstractInsnNode abstractInsnNode : c) {
            list.insert(lastNode,abstractInsnNode);
            lastNode = abstractInsnNode;
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object o : c) {
            if (o instanceof AbstractInsnNode) {
                list.remove((AbstractInsnNode) o);
            }
        }
        return true;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public AbstractInsnNode get(int index) {
        return list.get(index);
    }

    @Override
    public AbstractInsnNode set(int index, AbstractInsnNode element) {
        final AbstractInsnNode node = list.get(index);
        list.set(node, element);
        return node;
    }

    @Override
    public void add(int index, AbstractInsnNode element) {
        list.insert(list.get(index), element);
    }

    @Override
    public AbstractInsnNode remove(int index) {
        list.remove(list.get(index));
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return o instanceof AbstractInsnNode ? list.indexOf((AbstractInsnNode) o) : -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return o instanceof AbstractInsnNode ? list.indexOf((AbstractInsnNode) o) : -1;
    }

    @Override
    @Nonnull
    public ListIterator<AbstractInsnNode> listIterator() {
        return list.iterator();
    }

    @Override
    @Nonnull
    public ListIterator<AbstractInsnNode> listIterator(int index) {
        return list.iterator(index);
    }

    @Override
    public @NotNull List<AbstractInsnNode> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    public AbstractInsnNode first() {
        return list.getFirst();
    }

    public AbstractInsnNode last() {
        return list.getLast();
    }
    public void replaceType(String from,String to){
        for (AbstractInsnNode node : this) {
            if (node instanceof TypeInsnNode) {
                final TypeInsnNode insn = (TypeInsnNode) node;
                if (insn.desc.equals(from))
                    insn.desc = to;
            } else if (node instanceof MethodInsnNode) {
                final MethodInsnNode insn = (MethodInsnNode) node;
                if (insn.owner.equals(from))
                    insn.owner = to;
            }else if(node instanceof FieldInsnNode){
                final FieldInsnNode insn = (FieldInsnNode) node;
                if (insn.owner.equals(from))
                    insn.owner = to;
                if(insn.desc.equals("L"+from+";"))
                    insn.desc="L"+to+";";
            }
        }
    }
    public void replace(INodeMatcher<?> matcher, AbstractInsnNode newNode) {
        for (AbstractInsnNode node : this) {
            if (matcher.match(node)) list.set(node, newNode);
        }
    }
    public void replace(AbstractInsnNode node, InstructionList newNodes) {
        list.insert(node,newNodes.list);
        list.remove(node);
    }
    public void replace(AbstractInsnNode node, AbstractInsnNode newNode) {
        list.set(node,newNode);
    }
    public void replace(INodeMatcher<?> matcher, Supplier<InstructionList> newNodes) {
        boolean affected=false;
        for (AbstractInsnNode node : this) {
            if (matcher.match(node)) {
                list.insert(node,newNodes.get().list);
                list.remove(node);
                affected=true;
            }
        }
        if(!affected)
            throw new IllegalStateException("Unable to replace");
    }
    public void replaceOnce(INodeMatcher<?> matcher, InstructionList newNodes) {
        for (AbstractInsnNode node : this) {
            if (matcher.match(node)) {
                list.insert(node,newNodes.list);
                list.remove(node);
                return;
            }
        }
        throw new IllegalStateException("Unable to replace");
    }
    public void redirect(String targetOwner,String targetName,String targetDesc,String newOwner,String newName,String newDesc,int code){
        for (AbstractInsnNode node : this) {
            if (node instanceof MethodInsnNode){
                MethodInsnNode methodInsnNode=(MethodInsnNode) node;
                if((targetOwner==null||methodInsnNode.owner.equals(targetOwner)) &&
                        (targetName==null||methodInsnNode.name.equals(targetName)) &&
                        (targetDesc==null||methodInsnNode.name.equals(targetDesc))){
                    if(newOwner!=null)methodInsnNode.owner=newOwner;
                    if(newDesc!=null)methodInsnNode.desc=newDesc;
                    if(newName!=null)methodInsnNode.name=newName;
                    if(code!=-1)methodInsnNode.setOpcode(code);
                }
            }
        }
    }
    public void insert(AbstractInsnNode location,InstructionList instructionList){
        list.insert(location,instructionList.list);
    }
    public void insertBefore(AbstractInsnNode location,InstructionList instructionList){
        list.insertBefore(location,instructionList.list);
    }
    public void insert(int location,InstructionList instructionList){
        list.insert(list.get(location),instructionList.list);
    }
    public void insertHead(InstructionList instructionList){
        list.insert(list.getFirst(),instructionList.list);
    }
    public void insertBeforeReturnUnique(InstructionList instructionList){
        list.insertBefore(find(node->node.getOpcode() >= Opcodes.IRETURN && node.getOpcode() <= Opcodes.RETURN),instructionList.list);
    }
    public <T extends AbstractInsnNode> T find(INodeMatcher<T> matcher){
        return find(matcher,0);
    }
    @SuppressWarnings("unchecked")
    public <T extends AbstractInsnNode> T find(INodeMatcher<T> matcher,int start){
        for (int i = start; i < this.size(); i++) {
            AbstractInsnNode node = this.get(i);
            if (matcher.match(node)) return (T) node;
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public <T extends AbstractInsnNode>T findLast(INodeMatcher<T> matcher){
        for (int i = this.size() - 1; i >= 0; i--) {
            final AbstractInsnNode value = this.get(i);
            if(matcher.match(value))return (T)value;
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public <T extends AbstractInsnNode> List<T> findAll(INodeMatcher<T> matcher){
        List<T> result = new ArrayList<>();
        for (AbstractInsnNode node : this) {
            if(matcher.match(node))result.add((T)node);
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    public <T extends AbstractInsnNode> T findNth(INodeMatcher<T> matcher,int index){
        for (AbstractInsnNode node : this) {
            if(matcher.match(node)){
                if(index--==0)return (T) node;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractInsnNode> T findLastNth(INodeMatcher<T> matcher,int index){
        for (int i = this.size() - 1; i >= 0; i--) {
            AbstractInsnNode node = this.get(i);
            if (matcher.match(node)) {
                if (index-- == 0) return (T) node;
            }
        }
        return null;
    }
    public static InstructionList of(AbstractInsnNode node){
        final InstructionList list = new InstructionList();
        list.add(node);
        return list;
    }

    public int length() {
        return list.size();
    }
}
