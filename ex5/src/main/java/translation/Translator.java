package translation;

import minijava.ast.*;
import minillvm.ast.*;
import static minillvm.ast.Ast.*;


public class Translator extends MJElement.DefaultVisitor {

	private final MJProgram javaProg;

	public Translator(MJProgram javaProg) {
		this.javaProg = javaProg;
	}

	public Prog translate() {
		TranslateVisitor tVisitor = new TranslateVisitor();
		javaProg.accept(tVisitor);
		return tVisitor.getProg();
	}
}
