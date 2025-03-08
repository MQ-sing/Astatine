package com.sing.astatine.core;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import java.util.*;

@SuppressWarnings("unused")
public class InstructionList implements List<AbstractInsnNode>, Iterable<AbstractInsnNode>, RandomAccess {
    public final InsnList list;
    /**
     * Constructs an InstructionList wrapping an existing {@link InsnList}.
     * @param list The ASM instruction list to wrap.
     */
    public InstructionList(InsnList list) {
        this.list = list;
    }
    /**
     * Constructs an empty InstructionList with a new {@link InsnList}.
     */
    public InstructionList() {
        list = new InsnList();
    }
    /**
     * Appends an ALOAD instruction for the given variable index.
     * @param index The local variable index.
     */
    public void loadA(int index) {
        list.add(new VarInsnNode(Opcodes.ALOAD, index));
    }
    /**
     * Appends an ILOAD instruction for the given variable index.
     * @param index The local variable index.
     */
    public void loadI(int index) {
        list.add(new VarInsnNode(Opcodes.ILOAD, index));
    }
    /**
     * Appends an FLOAD instruction for the given variable index.
     * @param index The local variable index.
     */
    public void loadF(int index) {
        list.add(new VarInsnNode(Opcodes.FLOAD, index));
    }
    /**
     * Appends an DLOAD instruction for the given variable index.
     * @param index The local variable index.
     */
    public void loadD(int index) {
        list.add(new VarInsnNode(Opcodes.DLOAD, index));
    }
    /**
     * Appends an LLOAD instruction for the given variable index.
     * @param index The local variable index.
     */
    public void loadL(int index) {
        list.add(new VarInsnNode(Opcodes.LLOAD, index));
    }
    /**
     * Appends an 'ALOAD 0' instruction(load "this" reference)
     */
    public void loadThis() {
        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
    }
    public void swap(){
        list.add(new InsnNode(Opcodes.SWAP));
    }

    public void storeA(int index){
        list.add(new VarInsnNode(Opcodes.ASTORE, index));
    }
    public void storeI(int index){
        list.add(new VarInsnNode(Opcodes.ISTORE, index));
    }
    /**
     * Appends an ACONST_NULL instruction.
     */
    public void constantNull() {
        list.add(new InsnNode(Opcodes.ACONST_NULL));
    }
    /**
     * Appends an ICONST_0 or ICONST_1 instruction based on the boolean value.
     * @param flag The boolean value to push (true -> ICONST_1, false -> ICONST_0).
     */
    public void constant(boolean flag) {
        list.add(new InsnNode(flag?Opcodes.ICONST_1:Opcodes.ICONST_0));
    }
    /**
     * Appends a constant instruction for the given integer value, optimizing for
     * dconst_N, or ldc as appropriate.
     * @param value The double value to push.
     */
    public void constant(double value) {
        if (value == 0)
            list.add(new InsnNode(Opcodes.DCONST_0));
        else if (value == 1)
            list.add(new InsnNode(Opcodes.DCONST_1));
        else list.add(new LdcInsnNode(value));
    }
    /**
     * Appends a constant instruction for the given integer value, optimizing for
     * fconst_N, or ldc as appropriate.
     * @param value The float value to push.
     */
    public void constant(float value) {
        if (value == 0)
            list.add(new InsnNode(Opcodes.FCONST_0));
        else if (value == 1)
            list.add(new InsnNode(Opcodes.FCONST_1));
        else list.add(new LdcInsnNode(value));
    }
    /**
     * Appends a constant instruction for the given integer value, optimizing for
     * lconst_N, or ldc as appropriate.
     * @param value The long value to push.
     */
    public void constant(long value) {
        if (value == 0)
            list.add(new InsnNode(Opcodes.LCONST_0));
        else if (value == 1)
            list.add(new InsnNode(Opcodes.LCONST_1));
        else list.add(new LdcInsnNode(value));
    }
    /**
     * Appends a constant instruction for the given integer value, optimizing for
     * iconst, bipush, sipush, or ldc as appropriate.
     * @param value The integer value to push.
     */
    public void constant(int value) {
        if (value >= -1 && value <= 5)
            list.add(new InsnNode(Opcodes.ICONST_0 + value));
        else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) list.add(new IntInsnNode(Opcodes.BIPUSH, value));
        else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) list.add(new IntInsnNode(Opcodes.SIPUSH, value));
        else list.add(new LdcInsnNode(value));
    }
    /**
     * Appends a ldc instruction for the given String value.
     * @param str The string value to push.
     */
    public void constant(String str) {
        list.add(new LdcInsnNode(str));
    }
    public void jumpTo(LabelNode label){
        list.add(new JumpInsnNode(Opcodes.GOTO,label));
    }
    public void jumpIf0(LabelNode label){
        list.add(new JumpInsnNode(Opcodes.IFEQ,label));
    }
    public void jumpIfNot0(LabelNode label){
        list.add(new JumpInsnNode(Opcodes.IFNE,label));
    }
    public void jumpIfNull(LabelNode label){
        list.add(new JumpInsnNode(Opcodes.IFNULL,label));
    }
    public void jumpIfNonNull(LabelNode label){
        list.add(new JumpInsnNode(Opcodes.IFNONNULL,label));
    }
    public void doJump(int opcode,LabelNode label){
        list.add(new JumpInsnNode(opcode,label));
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
    public void returnV(){
        list.add(new InsnNode(Opcodes.RETURN));
    }
    public void returnI(){
        list.add(new InsnNode(Opcodes.IRETURN));
    }
    public void returnD(){
        list.add(new InsnNode(Opcodes.DRETURN));
    }
    public void returnL(){
        list.add(new InsnNode(Opcodes.LRETURN));
    }
    public void returnA(){
        list.add(new InsnNode(Opcodes.ARETURN));
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
    public void staticVar(String className,String name,String description){
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, className, name, description));
    }
    public void setStaticVar(String className,String name,String description){
        list.add(new FieldInsnNode(Opcodes.PUTSTATIC, className, name, description));
    }
    public void configFlag(String name){
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sing/astatine/Configuration", name, "Z"));
    }
    public void field(String owner,String name,String desc){
        list.add(new FieldInsnNode(Opcodes.GETFIELD,owner,name,desc));
    }
    public void setField(String owner,String name,String desc){
        list.add(new FieldInsnNode(Opcodes.PUTFIELD,owner,name,desc));
    }
    public void pop(){
        list.add(new InsnNode(Opcodes.POP));
    }
    public void invokeStatic(String owner,String name,String desc){
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, name, desc, false));
    }
    public void getFieldThis(String owner, String name, String desc){
        list.add(new VarInsnNode(Opcodes.ALOAD,0));
        list.add(new FieldInsnNode(Opcodes.GETFIELD,owner,name,desc));
    }
    public void invokeVirtual(String owner,String name,String desc){
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,owner,name,desc,false));
    }
    public void invokeInterface(String owner,String name,String desc){
        list.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE,owner,name,desc,true));
    }
    public void invokeVirtualThis(String owner,String name,String desc){
        list.add(new VarInsnNode(Opcodes.ALOAD,0));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,owner,name,desc,false));
    }
    public void dupe(){
        list.add(new InsnNode(Opcodes.DUP));
    }
    public void invokeSpecial(String owner,String name,String desc){
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,owner,name,desc,false));
    }
    public void construct(String owner){
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL,owner,"<init>","()V",false));
    }
    public void allocNew(String type){
        list.add(new TypeInsnNode(Opcodes.NEW,type));
    }
    public void allocNewAndDupe(String type){
        list.add(new TypeInsnNode(Opcodes.NEW,type));
        list.add(new InsnNode(Opcodes.DUP));
    }

    public void add(int opcode){
        list.add(new InsnNode(opcode));
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
    public void replace(INodeMatcher<?> matcher, AbstractInsnNode newNode) {
        for (AbstractInsnNode node : this) {
            if (matcher.match(node)) list.set(node, newNode);
        }
    }
    public void replace(INodeMatcher<?> matcher, InsnList newNodes) {
        for (AbstractInsnNode node : this) {
            if (matcher.match(node)) {
                list.insert(node,newNodes);
                list.remove(node);
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
    public void insertAfterHead(InstructionList instructionList){
        list.insert(find(INodeMatcher.labels()),instructionList.list);
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
