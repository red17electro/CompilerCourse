package analysis;

import minijava.ast.*;

/**
 * Matcher which is used in order to distinguish between specific types of MJType like the ones that are below
 */
public class MatcherTypes implements MJType.Matcher<TypeChecker> {

    @Override
    public TypeChecker case_TypeInt(MJTypeInt typeInt) {
        return TypeChecker.INT;
    }

    @Override
    public TypeChecker case_TypeBool(MJTypeBool typeBool) {
        return TypeChecker.BOOL;
    }

    @Override
    public TypeChecker case_TypeIntArray(MJTypeIntArray typeIntArray) {
        return TypeChecker.INT_ARRAY;
    }

    @Override
    public TypeChecker case_TypeClass(MJTypeClass typeClass) {
        return TypeChecker.CLASS;
    }
}