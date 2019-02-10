package minijava.mips;

import minijava.TranslationTestHelper;
import minillvm.ast.*;
import org.junit.Test;

import static minillvm.ast.Ast.*;

/**
 * Simple LLVM programs without null checks, array bounds checks and so on
 */
public class SimpleMipsPrograms {

    @Test
    public void progPrintConstant() throws Exception {
        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                Proc("main", TypeInt(), ParameterList(),
                        BasicBlockList(
                                BasicBlock(
                                        Print(ConstInt(42)),
                                        ReturnExpr(ConstInt(0))
                                )
                        ))
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progAdd() throws Exception {
        TemporaryVar sum = TemporaryVar("sum");
        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                Proc("main", TypeInt(), ParameterList(),
                        BasicBlockList(
                                BasicBlock(
                                        BinaryOperation(sum, ConstInt(3), Add(), ConstInt(4)),
                                        Print(VarRef(sum)),
                                        ReturnExpr(ConstInt(0))
                                )
                        ))
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progAddVar() throws Exception {
        TemporaryVar a = TemporaryVar("a");
        TemporaryVar b = TemporaryVar("b");
        TemporaryVar c = TemporaryVar("c");
        TemporaryVar d = TemporaryVar("d");
        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                Proc("main", TypeInt(), ParameterList(),
                        BasicBlockList(
                                BasicBlock(
                                        BinaryOperation(a, ConstInt(3), Add(), ConstInt(4)),
                                        BinaryOperation(b, VarRef(a), Add(), ConstInt(1)),
                                        BinaryOperation(c, ConstInt(1), Add(), VarRef(b)),
                                        BinaryOperation(d, VarRef(a), Add(), VarRef(c)),
                                        Print(VarRef(d)),
                                        ReturnExpr(ConstInt(0))
                                )
                        ))
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progAddBig() throws Exception {
        TemporaryVar a = TemporaryVar("a");
        TemporaryVar b = TemporaryVar("b");
        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                Proc("main", TypeInt(), ParameterList(),
                        BasicBlockList(
                                BasicBlock(
                                        BinaryOperation(a, ConstInt(3), Add(), ConstInt(4)),
                                        BinaryOperation(b, VarRef(a), Add(), ConstInt(32768)),
                                        Print(VarRef(b)),
                                        ReturnExpr(ConstInt(0))
                                )
                        ))
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progJump() throws Exception {
        BasicBlock entryBlock = BasicBlock(
                Print(ConstInt(41))
        );
        entryBlock.setName("entry");
        BasicBlock otherBlock = BasicBlock(
                Print(ConstInt(42))
        );
        otherBlock.setName("otherBlock");
        entryBlock.add(Jump(otherBlock));
        otherBlock.add(ReturnExpr(ConstInt(0)));

        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                Proc("main", TypeInt(), ParameterList(),
                        BasicBlockList(
                                entryBlock,
                                otherBlock
                        )
                )
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progBranch() throws Exception {
        BasicBlock entry = BasicBlock();
        entry.setName("entry");
        BasicBlock ifTrue = BasicBlock(
                Print(ConstInt(41))
        );
        ifTrue.setName("ifTrue");
        BasicBlock ifFalse = BasicBlock(
                Print(ConstInt(42))
        );
        ifFalse.setName("ifFalse");
        BasicBlock endif = BasicBlock(
                Print(ConstInt(43))
        );
        endif.setName("endif");
        entry.add(Branch(ConstBool(true), ifTrue, ifFalse));
        ifTrue.add(Jump(endif));
        ifFalse.add(Jump(endif));
        endif.add(ReturnExpr(ConstInt(0)));

        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                Proc("main", TypeInt(), ParameterList(),
                        BasicBlockList(
                                entry,
                                ifTrue,
                                ifFalse,
                                endif
                        )
                )
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progSimpleCall() throws Exception {
        TemporaryVar i = TemporaryVar("i");

        Proc procFoo = Proc("foo", TypeInt(), ParameterList(),
                BasicBlockList(
                        BasicBlock(
                                ReturnExpr(ConstInt(1234))
                        )
                ));

        Proc mainProc = Proc("main", TypeInt(), ParameterList(),
                BasicBlockList(
                        BasicBlock(
                                Call(i, ProcedureRef(procFoo), OperandList()),
                                Print(VarRef(i)),
                                ReturnExpr(ConstInt(0))
                        )
                ));

        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                mainProc,
                procFoo
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progCall1Arg() throws Exception {
        TemporaryVar i = TemporaryVar("i");
        TemporaryVar x = TemporaryVar("x");

        Parameter aPar = Parameter(TypeInt(), "a");
        Proc procFoo = Proc("foo", TypeInt(), ParameterList(aPar),
                BasicBlockList(
                        BasicBlock(
                                BinaryOperation(x, VarRef(aPar), Add(), VarRef(aPar)),
                                ReturnExpr(VarRef(x))
                        )
                ));

        Proc mainProc = Proc("main", TypeInt(), ParameterList(),
                BasicBlockList(
                        BasicBlock(
                                Call(i, ProcedureRef(procFoo), OperandList(ConstInt(1))),
                                Print(VarRef(i)),
                                ReturnExpr(ConstInt(0))
                        )
                ));

        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                mainProc,
                procFoo
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progCall4Args() throws Exception {
        // for main
        TemporaryVar i = TemporaryVar("i");

        // for foo
        TemporaryVar x = TemporaryVar("x");
        TemporaryVar y = TemporaryVar("y");
        TemporaryVar z = TemporaryVar("z");

        Parameter aPar = Parameter(TypeInt(), "a");
        Parameter bPar = Parameter(TypeInt(), "b");
        Parameter cPar = Parameter(TypeInt(), "c");
        Parameter dPar = Parameter(TypeInt(), "d");

        Proc procFoo = Proc("foo", TypeInt(), ParameterList(aPar, bPar, cPar, dPar),
                BasicBlockList(
                        BasicBlock(
                                BinaryOperation(x, VarRef(aPar), Add(), VarRef(bPar)),
                                BinaryOperation(y, VarRef(cPar), Add(), VarRef(dPar)),
                                BinaryOperation(z, VarRef(x), Add(), VarRef(y)),
                                ReturnExpr(VarRef(z))
                        )
                ));

        Proc mainProc = Proc("main", TypeInt(), ParameterList(),
                BasicBlockList(
                        BasicBlock(
                                Call(i, ProcedureRef(procFoo), OperandList(ConstInt(1), ConstInt(2), ConstInt(3), ConstInt(4))),
                                Print(VarRef(i)),
                                ReturnExpr(ConstInt(0))
                        )
                ));

        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                mainProc,
                procFoo
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progCall5Args() throws Exception {
        // for main
        TemporaryVar i = TemporaryVar("i");

        // for foo
        TemporaryVar x = TemporaryVar("x");
        TemporaryVar y = TemporaryVar("y");
        TemporaryVar z = TemporaryVar("z");
        TemporaryVar r = TemporaryVar("r");

        Parameter aPar = Parameter(TypeInt(), "a");
        Parameter bPar = Parameter(TypeInt(), "b");
        Parameter cPar = Parameter(TypeInt(), "c");
        Parameter dPar = Parameter(TypeInt(), "d");
        Parameter ePar = Parameter(TypeInt(), "e");

        Proc procFoo = Proc("foo", TypeInt(), ParameterList(aPar, bPar, cPar, dPar, ePar),
                BasicBlockList(
                        BasicBlock(
                                BinaryOperation(x, VarRef(aPar), Add(), VarRef(bPar)),
                                BinaryOperation(y, VarRef(cPar), Add(), VarRef(dPar)),
                                BinaryOperation(z, VarRef(x), Add(), VarRef(y)),
                                BinaryOperation(r, VarRef(z), Add(), VarRef(ePar)),
                                ReturnExpr(VarRef(r))
                        )
                ));

        Proc mainProc = Proc("main", TypeInt(), ParameterList(),
                BasicBlockList(
                        BasicBlock(
                                Call(i, ProcedureRef(procFoo), OperandList(ConstInt(1), ConstInt(2), ConstInt(3), ConstInt(4), ConstInt(5))),
                                Print(VarRef(i)),
                                ReturnExpr(ConstInt(0))
                        )
                ));

        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                mainProc,
                procFoo
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progTwoReturns() throws Exception {
        BasicBlock ifTrue = BasicBlock(
                Print(ConstInt(41)),
                ReturnExpr(ConstInt(0))
        );
        ifTrue.setName("ifTrue");

        BasicBlock ifFalse = BasicBlock(
                Print(ConstInt(42)),
                ReturnExpr(ConstInt(0))
        );
        ifFalse.setName("ifFalse");

        BasicBlock entry = BasicBlock(
                Branch(ConstBool(true), ifTrue, ifFalse)
        );
        entry.setName("entry");

        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                Proc("main", TypeInt(), ParameterList(), BasicBlockList(
                        entry,
                        ifTrue,
                        ifFalse))
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progAlloca() throws Exception {
        TemporaryVar x = TemporaryVar("x");
        TemporaryVar y = TemporaryVar("y");

        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                Proc("main", TypeInt(), ParameterList(), BasicBlockList(
                        BasicBlock(
                                Alloca(x, TypeInt()),
                                Store(VarRef(x), ConstInt(12)),
                                Load(y, VarRef(x)),
                                Print(VarRef(y)),
                                ReturnExpr(ConstInt(0))
                        )))
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progAllocaBranch() throws Exception {
        TemporaryVar x = TemporaryVar("x");
        TemporaryVar y = TemporaryVar("y");
        BasicBlock entry = BasicBlock(
                Alloca(x, TypeInt())
        );
        entry.setName("entry");
        BasicBlock ifTrue = BasicBlock(
                Store(VarRef(x), ConstInt(41))
        );
        ifTrue.setName("ifTrue");
        BasicBlock ifFalse = BasicBlock(
                Store(VarRef(x), ConstInt(42))
        );
        ifFalse.setName("ifFalse");
        BasicBlock endif = BasicBlock(
                Load(y, VarRef(x)),
                Print(VarRef(y))
        );
        endif.setName("endif");
        entry.add(Branch(ConstBool(true), ifTrue, ifFalse));
        ifTrue.add(Jump(endif));
        ifFalse.add(Jump(endif));
        endif.add(ReturnExpr(ConstInt(0)));

        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                Proc("main", TypeInt(), ParameterList(),
                        BasicBlockList(
                                entry,
                                ifTrue,
                                ifFalse,
                                endif
                        )
                )
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progAllocaWhile() throws Exception {
        TemporaryVar i = TemporaryVar("i");
        TemporaryVar j = TemporaryVar("j");

        BasicBlock l1 = BasicBlock();
        l1.setName("l1");
        BasicBlock l2 = BasicBlock();
        l2.setName("l2");
        BasicBlock l3 = BasicBlock();
        l3.setName("l3");
        BasicBlock l4 = BasicBlock();
        l4.setName("l4");

        l1.add(Alloca(i, TypeInt()));
        l1.add(Alloca(j, TypeInt()));
        l1.add(Store(VarRef(i), ConstInt(1)));
        l1.add(Store(VarRef(j), ConstInt(1)));
        l1.add(Jump(l2));

        TemporaryVar t1 = TemporaryVar("t1");
        l2.add(Load(t1, VarRef(i)));
        TemporaryVar test = TemporaryVar("test");
        l2.add(BinaryOperation(test, ConstInt(100), Slt(), VarRef(t1)));
        TemporaryVar t2 = TemporaryVar("t2");
        l2.add(Load(t2, VarRef(j)));
        l2.add(Print(VarRef(t2)));
        l2.add(Branch(VarRef(test), l3, l4));

        TemporaryVar t3 = TemporaryVar("t3");
        l4.add(Load(t3, VarRef(j)));
        TemporaryVar t4 = TemporaryVar("t4");
        l4.add(Load(t4, VarRef(i)));
        TemporaryVar t5 = TemporaryVar("t5");
        l4.add(BinaryOperation(t5, VarRef(t3), Add(), VarRef(t4)));
        l4.add(Store(VarRef(j), VarRef(t5)));
        TemporaryVar t6 = TemporaryVar("t6");
        l4.add(BinaryOperation(t6, VarRef(t3), Add(), ConstInt(1)));
        l4.add(Store(VarRef(i), VarRef(t6)));
        l4.add(Jump(l2));

        l3.add(ReturnExpr(ConstInt(0)));

        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                Proc("main", TypeInt(), ParameterList(), BasicBlockList(
                        l1,
                        l2,
                        l4,
                        l3
                ))
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progAllocStoreLoad() throws Exception {
        TemporaryVar addr = TemporaryVar("addr");
        TemporaryVar arr = TemporaryVar("arr");
        TemporaryVar addr0 = TemporaryVar("addr0");
        TemporaryVar addr1 = TemporaryVar("addr1");
        TemporaryVar addr2 = TemporaryVar("addr2");

        TemporaryVar x = TemporaryVar("x");
        TemporaryVar y = TemporaryVar("y");
        TemporaryVar z = TemporaryVar("z");

        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                Proc("main", TypeInt(), ParameterList(), BasicBlockList(
                        BasicBlock(
                                Alloc(addr, ConstInt(12)),
                                Bitcast(arr, TypePointer(TypeArray(TypeInt(), 3)), VarRef(addr)),

                                GetElementPtr(addr0, VarRef(arr), OperandList(ConstInt(0), ConstInt(0))),
                                Store(VarRef(addr0), ConstInt(40)),

                                GetElementPtr(addr1, VarRef(arr), OperandList(ConstInt(0), ConstInt(1))),
                                Store(VarRef(addr1), ConstInt(41)),

                                GetElementPtr(addr2, VarRef(arr), OperandList(ConstInt(0), ConstInt(2))),
                                Store(VarRef(addr2), ConstInt(42)),

                                Load(x, VarRef(addr0)),
                                Load(y, VarRef(addr1)),
                                Load(z, VarRef(addr2)),

                                Print(VarRef(x)),
                                Print(VarRef(y)),
                                Print(VarRef(z)),

                                ReturnExpr(ConstInt(0))
                        )
                ))
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progStruct() throws Exception {
        TypeStruct mystruct = TypeStruct("mystruct", StructFieldList());
        mystruct.getFields().add(StructField(TypePointer(mystruct), "msp"));
        mystruct.getFields().add(StructField(TypeBool(), "b"));
        mystruct.getFields().add(StructField(TypeInt(), "i"));

        TemporaryVar addr = TemporaryVar("addr");
        TemporaryVar s = TemporaryVar("s");
        TemporaryVar addr0 = TemporaryVar("addr0");
        TemporaryVar addr1 = TemporaryVar("addr1");
        TemporaryVar addr2 = TemporaryVar("addr2");
        TemporaryVar z = TemporaryVar("z");

        Prog prog = Prog(TypeStructList(mystruct), GlobalList(), ProcList(
                Proc("main", TypeInt(), ParameterList(), BasicBlockList(
                        BasicBlock(
                                Alloc(addr, Sizeof(mystruct)),
                                Bitcast(s, TypePointer(mystruct), VarRef(addr)),
                                GetElementPtr(addr0, VarRef(s), OperandList(ConstInt(0), ConstInt(0))),
                                Store(VarRef(addr0), VarRef(s)),

                                GetElementPtr(addr1, VarRef(s), OperandList(ConstInt(0), ConstInt(1))),
                                Store(VarRef(addr1), ConstBool(true)),

                                GetElementPtr(addr2, VarRef(s), OperandList(ConstInt(0), ConstInt(2))),
                                Store(VarRef(addr2), ConstInt(42)),

                                Load(z, VarRef(addr2)),
                                Print(VarRef(z)),

                                ReturnExpr(ConstInt(0))
                        )
                ))
        ));

        System.out.println(prog);

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progError() throws Exception {
        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList(
                Proc("main", TypeInt(), ParameterList(), BasicBlockList(
                        BasicBlock(
                                HaltWithError("This is an error message!")
                        )
                ))
        ));

        TranslationTestHelper.testMIPSTranslation(prog);
    }

    @Test
    public void progGlobals() throws Exception {
        TypeStruct structA = TypeStruct("struct.A", StructFieldList());
        TypeStruct structAVtable = TypeStruct("struct.A_vtable", StructFieldList());

        structA.getFields().add(StructField(TypePointer(structAVtable), "vtable"));
        structAVtable.getFields().add(StructField(TypePointer(TypeProc(TypeRefList(TypePointer(structA), TypeInt()), TypeInt())), "foo"));

        Parameter xPar = Parameter(TypeInt(), "x");
        TemporaryVar temp = TemporaryVar("temp");
        Proc A_foo = Proc("A_foo", TypeInt(), ParameterList(Parameter(TypePointer(structA), "this"), xPar), BasicBlockList(
                BasicBlock(
                        BinaryOperation(temp, VarRef(xPar), Add(), VarRef(xPar)),
                        ReturnExpr(VarRef(temp))
                )
        ));

        Global gAVtable = Global(structAVtable, "g_A_vtable", true, ConstStruct(structAVtable, ConstList(ProcedureRef(A_foo))));

        TemporaryVar allocated = TemporaryVar("allocated");
        TemporaryVar aNew = TemporaryVar("new");
        TemporaryVar addr = TemporaryVar("addr");
        Proc new_A = Proc("_new_A", TypePointer(structA), ParameterList(), BasicBlockList(
                BasicBlock(
                        Alloc(allocated, Sizeof(structA)),
                        Bitcast(aNew, TypePointer(structA), VarRef(allocated)),
                        GetElementPtr(addr, VarRef(aNew), OperandList(ConstInt(0), ConstInt(0))),
                        Store(VarRef(addr), GlobalRef(gAVtable)),
                        ReturnExpr(VarRef(aNew))
                )
        ));

        TemporaryVar a = TemporaryVar("a");
        TemporaryVar vtable_addr = TemporaryVar("vtable_addr");
        TemporaryVar vtable = TemporaryVar("vtable");
        TemporaryVar foo_addr = TemporaryVar("foo_addr");
        TemporaryVar foo = TemporaryVar("foo");
        TemporaryVar res = TemporaryVar("res");
        Proc main = Proc("main", TypeInt(), ParameterList(), BasicBlockList(
                BasicBlock(
                        Call(a, ProcedureRef(new_A), OperandList()),
                        GetElementPtr(vtable_addr, VarRef(a), OperandList(ConstInt(0), ConstInt(0))),
                        Load(vtable, VarRef(vtable_addr)),
                        GetElementPtr(foo_addr, VarRef(vtable), OperandList(ConstInt(0), ConstInt(0))),
                        Load(foo, VarRef(foo_addr)),
                        Call(res, VarRef(foo), OperandList(VarRef(a), ConstInt(7))),
                        Print(VarRef(res)),
                        ReturnExpr(ConstInt(0))
                )
        ));

        Prog prog = Prog(
                TypeStructList(
                        structA,
                        structAVtable
                ),
                GlobalList(
                        gAVtable
                ),
                ProcList(
                        A_foo,
                        new_A,
                        main
                )
        );

        System.out.println(prog);
        System.out.println("--------------------------------------------------");

        TranslationTestHelper.testMIPSTranslation(prog);
    }
}
