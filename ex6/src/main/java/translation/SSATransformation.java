package translation;

import minillvm.ast.*;
import minillvm.printer.DuplicateNames;

import java.util.*;
import java.util.stream.Collectors;

public class SSATransformation {
    /** The current value for each variable */
    private Map<BasicBlock, Map<Variable, Operand>> valueNumbers;

    /** The variables, which store the result of Alloca instructions*/
    private Set<TemporaryVar> allocaVars;

    /** Blocks, which have been completely processed*/
    private Set<BasicBlock> ready;

    /** predecessor blocks for each block */
    private Map<BasicBlock, List<BasicBlock>> predecessors;

    /** map pingfrom old block, to newly created block */
    private Map<BasicBlock, BasicBlock> newBlocks;

    /** actions to run, when block is completed */
    private Map<BasicBlock, List<Runnable>> onComplete;


    /** transforms program to SSA form */
    public void transformProg(Prog prog) {
        for (Proc proc : prog.getProcedures()) {
            processProcedure(proc);
        }
    }

    /** transforms procedure to SSA form, this creates new copies of instructions and basicblocks to do the transformation */
    private void processProcedure(Proc proc) {
        allocaVars = new HashSet<>();
        ready = new HashSet<>();

        predecessors = buildPredecessors(proc.getBasicBlocks());

        initBlocks(proc);

        // collect all Alloca variables into "allocaVars", where the allocation is in the first block of the procedure
        for (Instruction instr : proc.getBasicBlocks().get(0)) {
            if (instr instanceof Alloca) {
                Alloca alloca = (Alloca) instr;
                allocaVars.add(alloca.getVar());
            }
        }

        // keep only the "allocaVars", which are only used inside normal load and store instructions
        proc.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(VarRef varRef) {
                super.visit(varRef);

                Element elem = varRef.getParent();
                while (elem != null) {
                    if (elem instanceof Store && ((Store) elem).getAddress() == varRef
                            || elem instanceof Load) {
                        // used in load or store
                        return;
                    }
                    elem = elem.getParent();
                }
                // used somewhere else
                allocaVars.remove(varRef.getVariable());
            }
        });


        for (BasicBlock block : proc.getBasicBlocks()) {
            processBasicBlock(block);
        }


        // exchange old basic blocks with new basic blocks
        ListIterator<BasicBlock> iter = proc.getBasicBlocks().listIterator();
        while (iter.hasNext()) {
            BasicBlock old = iter.next();
            iter.set(newBlocks.get(old));
        }

        removeUnecessaryPhis(proc);
    }

    /** removes phi nodes, where all choices are equal or only choose between the phi-var itself and one other choice*/
    private void removeUnecessaryPhis(Proc proc) {
        Collection<PhiNode> toRemove = new HashSet<>();
        Map<Variable, Operand> varReplacements = new HashMap<>();
        proc.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(PhiNode phiNode) {
                Operand other = null;
                Operand self = Ast.VarRef(phiNode.getVar());
                boolean allEqual = true;
                for (PhiNodeChoice choice : phiNode.getChoices()) {
                    if (!choice.getValue().structuralEquals(self)) {
                        if (other != null
                                && !choice.getValue().structuralEquals(other)) {
                            allEqual = false;
                            break;
                        }
                        other = choice.getValue();
                    }
                }
                if (allEqual) {
                    toRemove.add(phiNode);
                    if (other == null) {
                        other = dummyValue(phiNode.getType());
                    }
                    varReplacements.put(phiNode.getVar(), other);
                }
            }
        });

        for (BasicBlock block : proc.getBasicBlocks()) {
            block.removeAll(toRemove);
        }

        // replace dummy phi vars
        proc.accept(new Element.DefaultVisitor() {
            @Override
            public void visit(VarRef varRef) {
                Operand newOp = varReplacements.get(varRef.getVariable());
                if (newOp != null) {
                    Operand copy = newOp.copy();
                    varRef.replaceBy(copy);
                    if (copy instanceof VarRef) {
                        VarRef ref = (VarRef) copy;
                        if (ref.structuralEquals(varRef)) {
                            System.out.println("replaced " + varRef + " with " + ref);
                            return;
                        }
                        // recursively replace
                        visit(ref);
                    }
                }
            }
        });
    }

    /** creates new (empty) blocks and initializes the state of the algorithm */
    private void initBlocks(Proc proc) {
        newBlocks = new HashMap<>();
        valueNumbers = new HashMap<>();
        onComplete = new HashMap<>();
        for (BasicBlock basicBlock : proc.getBasicBlocks()) {
            onComplete.put(basicBlock, new ArrayList<>());

            BasicBlock newBlock = Ast.BasicBlock();
            newBlock.setName(basicBlock.getName());
            newBlocks.put(basicBlock, newBlock);
            HashMap<Variable, Operand> initialValues = new HashMap<>();
            valueNumbers.put(basicBlock, initialValues);
            for (Parameter param : proc.getParameters()) {
                initialValues.put(param, Ast.VarRef(param));
            }
        }
    }

    /** gets the current value number for an operand in a block */
    private Operand getVN(BasicBlock currentBlock, Operand op) {
        if (op instanceof VarRef) {
            VarRef varRef = (VarRef) op;
            Variable v = varRef.getVariable();
            return getVN(currentBlock, v);
        } else {
            return op.copy();
        }
    }


    /** gets the current value number of a variable in a block.
     * Recursively searches in predecessor blocks, if a variable is not found in the current block
     * and adds phi-nodes, if there are multiple predecessors. */
    private Operand getVN(BasicBlock currentBlock, Variable v) {
        if (valueNumbers.get(currentBlock).containsKey(v)) {
            return valueNumbers.get(currentBlock).get(v).copy();
        } else {
            List<BasicBlock> preds = predecessors.get(currentBlock);

            if (preds.isEmpty()) {
                // value number not found, this can happen when there is a (possibly infeasible) path, where the variable is not initialized
                // we return some random value instead
                Type t = v.calculateType();
                //noinspection SuspiciousMethodCalls
                if (allocaVars.contains(v)) {
                    t = ((TypePointer) t).getTo();
                }
                return dummyValue(t);
            } else if (preds.size() == 1) {
                return getVN(preds.get(0), v);
            } else {
                // multiple predecessors, so insert phi node (might be removed later, if all values are equal)

                BasicBlock newBlock = newBlocks.get(currentBlock);
                TemporaryVar newVar = Ast.TemporaryVar(v.getName());
                PhiNodeChoiceList choices = Ast.PhiNodeChoiceList();
                PhiNode phi = Ast.PhiNode(newVar, transferType(v), choices);
                newBlock.add(0, phi);

                Operand result = Ast.VarRef(newVar);
                setVN(currentBlock, v, result);

                for (BasicBlock pred : preds) {
                    whenBlockReady(pred, () -> {
                        choices.add(Ast.PhiNodeChoice(newBlocks.get(pred), getVN(pred, v)));
                    });
                }
                return result;
            }
        }
    }

    /** execute given runnable code, when a block is ready */
    private void whenBlockReady(BasicBlock b, Runnable r) {
        if (ready.contains(b)) {
            r.run();
        } else {
            onComplete.get(b).add(r);
        }
    }

    /** calculates the type of a variable and transfers it to the new SSA program.
     * This means that previous alloca vars change from a pointer type to the pointed-to type */
    private Type transferType(Variable v) {
        Type t = v.calculateType();
        //noinspection SuspiciousMethodCalls
        if (allocaVars.contains(v)) {
            return ((TypePointer) t).getTo();
        }
        return t;
    }

    /** set the current value number */
    private void setVN(BasicBlock block, Variable var, Operand value) {
        valueNumbers.get(block).put(var, value);
    }

    /** goes through a basic block and rewrites the instructions into the new block */
    private void processBasicBlock(BasicBlock block) {
        BasicBlock newBlock = newBlocks.get(block);

        for (Instruction instr : block) {
            instr.match(new Instruction.MatcherVoid() {

                @Override
                public void case_Store(Store store) {
                    Operand storedValue = getVN(block, store.getValue());
                    if (store.getAddress() instanceof VarRef) {
                        VarRef varRef = (VarRef) store.getAddress();
                        Variable var = varRef.getVariable();
                        //noinspection SuspiciousMethodCalls
                        if (allocaVars.contains(var)) {
                            setVN(block, var, storedValue);
                            return;
                        }
                    }
                    newBlock.add(Ast.Store(getVN(block, store.getAddress()), storedValue));
                }

                @Override
                public void case_Branch(Branch branch) {
                    newBlock.add(Ast.Branch(
                            getVN(block, branch.getCondition()),
                            newBlocks.get(branch.getIfTrueLabel()),
                            newBlocks.get(branch.getIfFalseLabel())));

                }

                @Override
                public void case_CommentInstr(CommentInstr instr) {
                    newBlock.add(instr.copy());

                }

                @Override
                public void case_ReturnVoid(ReturnVoid instr) {
                    newBlock.add(instr.copy());
                }

                @Override
                public void case_Load(Load load) {
                    if (load.getAddress() instanceof VarRef) {
                        VarRef varRef = (VarRef) load.getAddress();
                        Variable var = varRef.getVariable();
                        //noinspection SuspiciousMethodCalls
                        if (allocaVars.contains(var)) {
                            setVN(block, load.getVar(), getVN(block, var));
                            return;
                        }
                    }
                    TemporaryVar newTemp = copyVar(load);
                    newBlock.add(Ast.Load(newTemp, getVN(block, load.getAddress())));
                    setVN(block, load.getVar(), Ast.VarRef(newTemp));
                }

                @Override
                public void case_Jump(Jump jump) {
                    newBlock.add(Ast.Jump(newBlocks.get(jump.getLabel())));
                }

                @Override
                public void case_Print(Print print) {
                    newBlock.add(Ast.Print(getVN(block, print.getE())));
                }

                @Override
                public void case_PhiNode(PhiNode phiNode) {
                    TemporaryVar newTemp = copyVar(phiNode);
                    PhiNodeChoiceList choices = Ast.PhiNodeChoiceList();
                    for (PhiNodeChoice choice : phiNode.getChoices()) {
                        whenBlockReady(choice.getLabel(), () -> {
                            choices.add(Ast.PhiNodeChoice(
                                    newBlocks.get(choice.getLabel()),
                                    getVN(choice.getLabel(), choice.getValue())
                            ));
                        });
                    }
                    newBlock.add(Ast.PhiNode(newTemp, phiNode.getType(), choices));
                    setVN(block, phiNode.getVar(), Ast.VarRef(newTemp));
                }

                @Override
                public void case_ReturnExpr(ReturnExpr returnExpr) {
                    newBlock.add(Ast.ReturnExpr(getVN(block, returnExpr.getReturnValue())));
                }

                @Override
                public void case_Alloc(Alloc alloc) {
                    TemporaryVar newTemp = copyVar(alloc);
                    newBlock.add(Ast.Alloc(newTemp, getVN(block, alloc.getSizeInBytes())));
                    setVN(block, alloc.getVar(), Ast.VarRef(newTemp));
                }

                @Override
                public void case_Call(Call call) {
                    TemporaryVar newTemp = copyVar(call);
                    Operand function = getVN(block, call.getFunction());
                    OperandList args = call.getArguments().stream()
                            .map(arg -> getVN(block, arg))
                            .collect(Collectors.toCollection(Ast::OperandList));
                    newBlock.add(Ast.Call(newTemp, function, args));
                    setVN(block, call.getVar(), Ast.VarRef(newTemp));
                }

                @Override
                public void case_Bitcast(Bitcast bitcast) {
                    TemporaryVar newTemp = copyVar(bitcast);
                    newBlock.add(Ast.Bitcast(newTemp, bitcast.getType(), getVN(block, bitcast.getExpr())));
                    setVN(block, bitcast.getVar(), Ast.VarRef(newTemp));
                }

                @Override
                public void case_HaltWithError(HaltWithError haltWithError) {
                    newBlock.add(haltWithError.copy());
                }

                @Override
                public void case_GetElementPtr(GetElementPtr gep) {
                    TemporaryVar newTemp = copyVar(gep);
                    Operand baseAddr = getVN(block, gep.getBaseAddress());
                    OperandList indices = gep.getIndices().stream()
                            .map(arg -> getVN(block, arg))
                            .collect(Collectors.toCollection(Ast::OperandList));
                    newBlock.add(Ast.GetElementPtr(newTemp, baseAddr, indices));
                    setVN(block, gep.getVar(), Ast.VarRef(newTemp));
                }

                @Override
                public void case_BinaryOperation(BinaryOperation bop) {
                    TemporaryVar newTemp = copyVar(bop);
                    newBlock.add(Ast.BinaryOperation(newTemp,
                            getVN(block, bop.getLeft()),
                            bop.getOperator().copy(),
                            getVN(block, bop.getRight())));
                    setVN(block, bop.getVar(), Ast.VarRef(newTemp));
                }

                @Override
                public void case_Alloca(Alloca alloca) {
                    if (!allocaVars.contains(alloca.getVar())) {
                        Alloca copy = alloca.copy();
                        newBlock.add(copy);
                        setVN(block, alloca.getVar(), Ast.VarRef(copy.getVar()));
                    }
                }
            });
        }

        // run on complete actions
        onComplete.get(block).forEach(Runnable::run);

        ready.add(block);
    }


    /** build the predecessor map */
    private Map<BasicBlock, List<BasicBlock>> buildPredecessors(BasicBlockList basicBlocks) {
        Map<BasicBlock, List<BasicBlock>> result = new HashMap<>();

        for (BasicBlock block : basicBlocks) {
            result.put(block, new ArrayList<>());
        }

        for (BasicBlock block : basicBlocks) {
            for (BasicBlock suc : getSuccessors(block)) {
                result.get(suc).add(block);
            }
        }
        return result;
    }

    /** get successors of a basic block */
    private List<BasicBlock> getSuccessors(BasicBlock block) {
        Optional<TerminatingInstruction> terminatingInstruction = block.getTerminatingInstruction();
        if (terminatingInstruction.isPresent()) {
            TerminatingInstruction t = terminatingInstruction.get();
            if (t instanceof Jump) {
                Jump jump = (Jump) t;
                return Collections.singletonList(jump.getLabel());
            } else if (t instanceof Branch) {
                Branch branch = ((Branch) t);
                return Arrays.asList(branch.getIfFalseLabel(), branch.getIfTrueLabel());
            }
        }
        return Collections.emptyList();
    }

    private TemporaryVar copyVar(Assign a) {
        return Ast.TemporaryVar(a.getVar().getName());
    }

    /** a dummy value, which can be used to handle some corner cases.
     * The value should never be used at runtime */
    private Operand dummyValue(Type type) {
        if (type instanceof TypeInt) {
            return Ast.ConstInt(0);
        } else if (type instanceof TypeBool) {
            return Ast.ConstBool(false);
        } else {
            return Ast.Nullpointer();
        }
    }

}
