package translation;

import minijava.ast.*;
import minillvm.ast.Ast;
import minillvm.ast.BasicBlock;
import minillvm.ast.Operand;
import minillvm.ast.TypePointer;

/**
 * Created by peter on 26.05.16.
 */
public class StmtTranslator implements MJStatement.MatcherVoid {

	private Translator tr;

	public StmtTranslator(Translator translator) {
		this.tr = translator;
	}

	@Override
	public void case_VarDecl(MJVarDecl s) {
		// no code, space is allocated at beginning of method
	}

	@Override
	public void case_StmtWhile(MJStmtWhile s) {
		BasicBlock whileStart = tr.newBasicBlock("whileStart");
		BasicBlock loopBodyStart = tr.newBasicBlock("loopBodyStart");
		BasicBlock endloop = tr.newBasicBlock("endloop");

		// goto loop start
		tr.getCurrentBlock().add(Ast.Jump(whileStart));

		tr.addBasicBlock(whileStart);
		tr.setCurrentBlock(whileStart);
		// evaluate condition
		Operand condition = tr.exprRvalue(s.getCondition());
		// branch based on condition
		tr.getCurrentBlock().add(Ast.Branch(condition, loopBodyStart, endloop));

		// translate loop body
		tr.addBasicBlock(loopBodyStart);
		tr.setCurrentBlock(loopBodyStart);
		tr.translateStmt(s.getLoopBody());
		// at end of loop body go to loop start
		tr.getCurrentBlock().add(Ast.Jump(whileStart));

		// continue after loop:
		tr.addBasicBlock(endloop);
		tr.setCurrentBlock(endloop);

	}

	@Override
	public void case_StmtExpr(MJStmtExpr s) {
		// just translate the expression
		tr.exprRvalue(s.getExpr());
	}

	@Override
	public void case_StmtAssign(MJStmtAssign s) {
		// first translate the left hand side
		Operand lAddr = tr.exprLvalue(s.getLeft());

		// then translate the right hand side
		Operand rValue = tr.exprRvalue(s.getRight());

		Operand rValueCasted = tr.addCastIfNecessary(rValue, ((TypePointer) lAddr.calculateType()).getTo());

		// finally store the result
		tr.addInstruction(Ast.Store(lAddr, rValueCasted));
	}

	@Override
	public void case_StmtPrint(MJStmtPrint s) {
		Operand e = tr.exprRvalue(s.getPrinted());
		tr.addInstruction(Ast.Print(e));
	}

	@Override
	public void case_StmtIf(MJStmtIf s) {
		BasicBlock ifTrue = tr.newBasicBlock("ifTrue");
		BasicBlock ifFalse = tr.newBasicBlock("ifFalse");
		BasicBlock endif = tr.newBasicBlock("endif");

		// translate the condition
		Operand condition = tr.exprRvalue(s.getCondition());
		// jump based on condition
		tr.getCurrentBlock().add(Ast.Branch(condition, ifTrue, ifFalse));

		// translate ifTrue
		tr.addBasicBlock(ifTrue);
		tr.setCurrentBlock(ifTrue);
		tr.translateStmt(s.getIfTrue());
		tr.getCurrentBlock().add(Ast.Jump(endif));

		// translate ifFalse
		tr.addBasicBlock(ifFalse);
		tr.setCurrentBlock(ifFalse);
		tr.translateStmt(s.getIfFalse());
		tr.getCurrentBlock().add(Ast.Jump(endif));

		// continue at endif
		tr.addBasicBlock(endif);
		tr.setCurrentBlock(endif);
	}

	@Override
	public void case_Block(MJBlock block) {
		for (MJStatement s : block) {
			tr.translateStmt(s);
		}
	}

	@Override
	public void case_StmtReturn(MJStmtReturn s) {
		Operand result = tr.exprRvalue(s.getResult());

		Operand castedResult = tr.addCastIfNecessary(result, tr.getCurrentReturnType());

		tr.getCurrentBlock().add(Ast.ReturnExpr(castedResult));

		// set to dummy block, so that nothing is overwritten
		tr.setCurrentBlock(tr.unreachableBlock());
	}
}
