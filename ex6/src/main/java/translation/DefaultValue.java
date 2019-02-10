package translation;

import minijava.ast.*;
import minillvm.ast.Ast;
import minillvm.ast.Operand;

public class DefaultValue {
    public static Operand get(MJType type) {
        return type.match(new MJType.Matcher<Operand>() {
            @Override
            public Operand case_TypeBool(MJTypeBool typeBool) {
                return Ast.ConstBool(false);
            }

            @Override
            public Operand case_TypeClass(MJTypeClass typeClass) {
                return Ast.Nullpointer();
            }

            @Override
            public Operand case_TypeInt(MJTypeInt typeInt) {
                return Ast.ConstInt(0);
            }

            @Override
            public Operand case_TypeIntArray(MJTypeIntArray typeIntArray) {
                return Ast.Nullpointer();
            }
        });
    }
}
