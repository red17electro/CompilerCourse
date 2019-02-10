package frontend;

import minijava.ast.*;

import java.util.List;

public class AstHelper {
	public static MJClassDecl ClassDecl(String name, String ext, List<MJMemberDecl> members) {
		MJMethodDeclList methods = MJ.MethodDeclList();
		MJVarDeclList fields = MJ.VarDeclList();
		MJExtended extended;
		if (ext == null) {
			extended = MJ.ExtendsNothing();
		} else {
			extended = MJ.ExtendsClass(ext);
		}

		for (MJMemberDecl member : members) {
			member.match(new MJMemberDecl.MatcherVoid() {

				@Override
				public void case_MethodDecl(MJMethodDecl methodDecl) {
					methods.add(methodDecl);
				}

				@Override
				public void case_VarDecl(MJVarDecl varDecl) {
					fields.add(varDecl);
				}
			});
		}

		return MJ.ClassDecl(name, extended, fields, methods);
	}
}
