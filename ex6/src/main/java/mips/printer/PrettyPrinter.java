package mips.printer;


import mips.ast.*;

public class PrettyPrinter implements MipsElement.MatcherVoid{

	private StringBuilder sb;


	public PrettyPrinter(StringBuilder sb){
		this.sb = sb;
	}

	public static String elementToString(MipsElement e) {
		StringBuilder sb = new StringBuilder();
		try {
			new PrettyPrinter(sb).print(e);
			return sb.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
			return "[Error in printing " + e.getClass().getSimpleName() + " ... "  + sb + "]";
		}
	}

	private void print(MipsElement e){
		String comment = e.sourceInfo().getComment();
		if (comment != null) {
			for (String line : comment.split("[\r\n]+")) {
				sb.append("#");
				sb.append(line);
				sb.append("\n");
			}
		}
		e.match(this);
	}

	/**
	 * Write the predefined methods
	 */
	private void appendPredefinedMethods() {
		String halloc =
			"\n         .text\n"
			+ "_halloc:\n"
			+ "         li $v0, 9\n"
			+ "         syscall\n"
			+ "         jr $ra\n";
		String print =
			"\n         .text\n"
			+ "_print:\n"
			+ "         li $v0, 1\n"
			+ "         syscall\n"
			+ "         la $a0, newl\n"
            + "         li $v0, 4\n"
			+ "         syscall\n"
			+ "         jr $ra\n";
		String error =
			"\n         .text\n" +
			"_error:    \n" +
			"           li $v0, 4          \n" +   // print-string
			"           la $a0, _error_msg \n" +
			"           syscall           \n" +
			"           li $a0, 222         \n" +   // exit
			"           li $v0, 17         \n" +   // exit with error
			"           syscall           \n";
		String exit =
				"_exit:     li $v0, 10         \n" +   // exit
				"           syscall           \n";
		String newl =
			"\n         .data\n"
			+ "         .align   0\n"
			+ "newl:    .asciiz \"\\n\"\n";
		String str_er =
			"\n         .data\n"
			+ "         .align   0\n"
			+ "_error_msg:  .asciiz \"ERROR\\0\"";

		sb.append(halloc + print + error + exit+  newl + str_er);
	}


	private void beginNewLine(){
		sb.append("\n");
	}

	private void leaveSpace(){
		sb.append("\t");
	}

	@Override
	public void case_Prog(MipsProg prog) {
		print(prog.getStmts());
		appendPredefinedMethods();
	}


	@Override
	public void case_StmtList(MipsStmtList stmtList) {
		MipsStmtList dataDeclss = Mips.StmtList();
		sb.append(".text\n");
		for(MipsStmt stmt: stmtList){
			if(stmt instanceof MipsDataDecl){
				stmt.setParent(null);
				dataDeclss.add(stmt);
			}else
				print(stmt);
		}
		if(dataDeclss.size() > 0){
			beginNewLine();
			sb.append(".data\n");
			sb.append(".align 2\n");
			for(MipsStmt stmt: dataDeclss){
				print(stmt);
			}
		}
	}

	@Override
	public void case_Syscall(MipsSyscall syscall) {
		leaveSpace();
		sb.append("syscall");
		beginNewLine();
	}

	@Override
	public void case_DataDecl(MipsDataDecl dataItem) {
		sb.append(dataItem.getName()+": " + ".word");
		print(dataItem.getLabels());
		beginNewLine();
	}


	@Override
	public void case_LabelRefList(MipsLabelRefList labelRefList) {
		for(int i=0; i<labelRefList.size();i++){
			beginNewLine(); leaveSpace();
			if(i > 0)
				sb.append(", ");
			print(labelRefList.get(i));
		}
	}

	@Override
	public void case_Jal(MipsJal jal) {
		leaveSpace();
		if(jal.getTarget() instanceof MipsRegister)
			sb.append("jalr ");
		else
			sb.append("jal ");
		print(jal.getTarget());
		beginNewLine();
	}


	@Override
	public void case_J(MipsJ j) {
		leaveSpace();
		if(j.getTarget() instanceof MipsRegister)
			sb.append("jr ");
		else
			sb.append("j ");
		j.getTarget().match(this);
		beginNewLine();

	}




	@Override
	public void case_Li(MipsLi li) {
		leaveSpace();
		sb.append("li ");
		print(li.getDestination());
		sb.append(", " + li.getValue());
		beginNewLine();
	}



	@Override
	public void case_Move(MipsMove move) {
		leaveSpace();
		sb.append("move ");
		print(move.getDestination());
		sb.append(", ");
		print(move.getSource());
		beginNewLine();
	}



	@Override
	public void case_Lw(MipsLw lw) {
		leaveSpace();
		sb.append("lw ");
		print(lw.getDestination());
		sb.append(", ");
		print(lw.getAddess());
		beginNewLine();
	}

	@Override
	public void case_Beqz(MipsBeqz beqz) {
		leaveSpace();
		sb.append("beqz ");
		print(beqz.getConditin());
		sb.append(", ");
		print(beqz.getLabel());
		beginNewLine();
	}

	@Override
	public void case_Sw(MipsSw sw) {
		leaveSpace();
		sb.append("sw ");
		print(sw.getContent());
		sb.append(", ");
		print(sw.getAddess());
		beginNewLine();
	}


	@Override
	public void case_Label(MipsLabel label) {
		sb.append(label.getName()+":");
		beginNewLine();
	}


	@Override
	public void case_LabelRef(MipsLabelRef labelRef) {
		sb.append(labelRef.getName());
	}

	@Override
	public void case_Bne(MipsBne bne) {
		leaveSpace();
		sb.append("bne ");
		print(bne.getLeft());
		sb.append(", ");
		print(bne.getRight());
		sb.append(", ");
		print(bne.getLabel());
		beginNewLine();
	}

	@Override
	public void case_BneI(MipsBneI bneI) {
		leaveSpace();
		sb.append("bnei ");
		print(bneI.getLeft());
		sb.append(", " + bneI.getRightI());
		sb.append(", ");
		print(bneI.getLabel());
		beginNewLine();
	}

	@Override
	public void case_Register(MipsRegister register) {
		switch (register.getNumber()){
			case 2: sb.append("$v0"); break;
			case 3: sb.append("$v1"); break;
			case 4: sb.append("$a0"); break;
			case 5: sb.append("$a1"); break;
			case 6: sb.append("$a2"); break;
			case 7: sb.append("$a3"); break;
			case 8: sb.append("$t0"); break;
			case 9: sb.append("$t1"); break;
			case 10: sb.append("$t2"); break;
			case 11: sb.append("$t3"); break;
			case 12: sb.append("$t4"); break;
			case 13: sb.append("$t5"); break;
			case 14: sb.append("$t6"); break;
			case 15: sb.append("$t7"); break;
			case 16: sb.append("$s0"); break;
			case 17: sb.append("$s1"); break;
			case 18: sb.append("$s2"); break;
			case 19: sb.append("$s3"); break;
			case 20: sb.append("$s4"); break;
			case 21: sb.append("$s5"); break;
			case 22: sb.append("$s6"); break;
			case 23: sb.append("$s7"); break;
			case 24: sb.append("$t8"); break;
			case 25: sb.append("$t9"); break;
			case 28: sb.append("$gp"); break;
			case 29: sb.append("$sp"); break;
			case 30: sb.append("$fp"); break;
			case 31: sb.append("$ra"); break;
			default: throw new Error("This register is reserved and should not be used");
		}
	}

	@Override
	public void case_BinaryOpI(MipsBinaryOpI binaryOpI) {
		leaveSpace();
		print(binaryOpI.getOp());
		String addI = binaryOpI.getOp().match(new MipsOperator.Matcher<String>() {

			@Override
			public String case_Div(MipsDiv div) {
				return " ";
			}

			@Override
			public String case_Rem(MipsRem rem) {
				return " ";
			}

			@Override
			public String case_Slt(MipsSlt slt) {
				return "i ";
			}

			@Override
			public String case_Sub(MipsSub sub) {
				return "i ";
			}

			@Override
			public String case_Xor(MipsXor xor) {
				return "i ";
			}

			@Override
			public String case_Or(MipsOr or) {
				return "i ";
			}

			@Override
			public String case_Mul(MipsMul mul) {
				return " ";
			}

			@Override
			public String case_Add(MipsAdd add) {
				return "i ";
			}

			@Override
			public String case_And(MipsAnd and) {
				return "i ";
			}

			@Override
			public String case_Seq(MipsSeq seq) {
				return " ";
			}
		});
		sb.append(addI);
		print(binaryOpI.getResult());
		sb.append(", ");
		print(binaryOpI.getLeft());
		sb.append(", "+ binaryOpI.getRightI());
		beginNewLine();
	}

	@Override
	public void case_BaseAddress(MipsBaseAddress address) {
		sb.append(address.getOffset()+ "(");
		print(address.getReg());
		sb.append(")");
	}


	@Override
	public void case_BinaryOp(MipsBinaryOp binaryOp) {
		leaveSpace();
		print(binaryOp.getOp());
		sb.append(" ");
		print(binaryOp.getResult());
		sb.append(", ");
		print(binaryOp.getLeft());
		sb.append(", ");
		print(binaryOp.getRight());
		beginNewLine();
	}

	@Override
	public void case_Nop(MipsNop nop) {
		beginNewLine();
	}

	@Override
	public void case_Xor(MipsXor xor) {
		sb.append("xor");
	}

	@Override
	public void case_Seq(MipsSeq seq) {
		sb.append("seq");
	}

	@Override
	public void case_And(MipsAnd and) {
		sb.append("and");
	}

	@Override
	public void case_Div(MipsDiv div) {
		sb.append("div");
	}

	@Override
	public void case_Add(MipsAdd add) {
		sb.append("add");
	}

	@Override
	public void case_Slt(MipsSlt slt) {
		sb.append("slt");
	}

	@Override
	public void case_Sub(MipsSub sub) {
		sb.append("sub");
	}

	@Override
	public void case_Rem(MipsRem rem) {
		sb.append("rem");
	}

	@Override
	public void case_Or(MipsOr or) {
		sb.append("or");
	}

	@Override
	public void case_Mul(MipsMul mul) {
		sb.append("mul");
	}

	@Override
	public void case_La(MipsLa la) {
		leaveSpace();
		sb.append("la ");
		print(la.getDestination());
		sb.append(", " );
		print(la.getLabel());
		beginNewLine();
	}

}
