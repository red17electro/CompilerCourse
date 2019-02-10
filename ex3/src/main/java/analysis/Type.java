package analysis;

import minijava.ast.MJType;
import minijava.ast.MJTypeClass;
import minijava.ast.MJVarDecl;

/**
 * Created by Server on 5/29/2017.
 */
public class Type {
    private TypeChecker type;
    private String name;

    /**
     * @param _type receive MJType
     * This method matches the types of MJType with our type implementation
     */
    public Type(MJType _type) {
        if (_type == null) {
            this.type = TypeChecker.NULL;
        } else {
            this.type = _type.match(new MatcherTypes());
        }

        if (type == TypeChecker.CLASS) {
            this.name = ((MJTypeClass) _type).getName();
        } else {
            setNameString();
        }
    }

    /**
     * Setter of the filed name depending on the type
     */
    private void setNameString() {
        switch (type) {
            case BOOL:
                this.name = "BOOL";
                break;
            case INT:
                this.name = "INT";
                break;
            case INT_ARRAY:
                this.name = "INT_ARRAY";
                break;
            case NULL:
                this.name = "NULL";
                break;
            default:
                this.name = "UNDEFINED";
                break;
        }
    }

    /**
     * @param type TypeChecker object
     */
    public Type(TypeChecker type) {
        this.type = type;
        setNameString();
    }

    /**
     * @param type TypeChecker object
     * @param name field name object
     * Initialize the variables
     */
    public Type(TypeChecker type, String name) {
        this.type = type;
        this.name = name;
    }


    /**
     * @return getter for type
     */
    public TypeChecker getType() {
        return type;
    }

    /**
     * @return setter for type
     */
    public void setType(TypeChecker type) {
        this.type = type;
    }

    /**
     * @return getter for name
     */
    public String getName() {
        return name;
    }

    /**
     * @return setter for name
     */
    public void setName(String name) {
        this.name = name;
    }
}
