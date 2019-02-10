package analysis;

import minijava.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Server on 5/26/2017.
 */
public enum TypeChecker {
    INT,
    BOOL,
    INT_ARRAY,
    NULL,
    CLASS;

    /**
     * @param left Left type
     * @param right Right type
     * @return true is the left and right are the same types or the subtypes of itself
     */
    public static boolean isSubType(Type left, Type right) {
        if ((left.getType() == NULL &&
                ((right.getType() == INT_ARRAY) || (right.getType() == CLASS)))) {
            return true;
        }

        if ((left.getType() == CLASS) && (right.getType() == CLASS)) {
            return left.getName().equals(right.getName());
        }

        if (right.getType().equals(left.getType())) {
            return true;
        }

        return false;
    }

    /**
     * @param subClass    contain the name of the class
     * @param SupClass    contain the name of the super class
     * @param classesInfo contain all the information about the classes
     * @return if subClass is a child of supClass return true. Otherwise return false
     * if the subClass and the supClass is the same return true (every class is a subType of itself).
     */
    public static boolean isSubType(String subClass, String SupClass, Map<MJClassDecl, MJExtended> classesInfo) {
        boolean isClassSubType = false;

        if (subClass.equals(SupClass))
            isClassSubType = true;

        MJExtended extendsClass = null;
        for (Map.Entry<MJClassDecl, MJExtended> entry : classesInfo.entrySet()) {
            if (entry.getKey().getName().equals(subClass)) {
                extendsClass = entry.getValue();
            }
        }

        if (!(extendsClass instanceof MJExtendsNothing) && extendsClass != null) {
            String extendsName = ((MJExtendsClass) extendsClass).getName();
            if (SupClass.equals(extendsName))
                isClassSubType = true;
            else
                return isSubType(extendsName, SupClass, classesInfo);
        }
        return isClassSubType;
    }
}
