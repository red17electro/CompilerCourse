package minillvm.tomips;

import minillvm.ast.Prog;
import mips.ast.MipsProg;
import mips.ast.Mips;


public class MiniLLVMToMips {
	public static MipsProg translateProgram(Prog prog) {
		//TODO this is just an example MIPS program, replace with your translation code
		MipsProg mipsprog = Mips.Prog(
				Mips.StmtList(
						Mips.Label("main"),
						Mips.Move(Mips.Register(30), Mips.Register(29)),
						Mips.Li(Mips.Register(4), 13),
						Mips.Jal(Mips.LabelRef("_print")),
						Mips.Li(Mips.Register(4), 0),
						Mips.Jal(Mips.LabelRef("_exit"))
				)
		);
		System.out.println(mipsprog);
		return mipsprog;
	}
}
