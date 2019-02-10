package frontend;
import minijava.ast.*;
import java.util.ArrayList;
import static minijava.ast.MJ.MethodDeclList;
import static minijava.ast.MJ.VarDeclList;

/**
 * Created by Dilan on 11/05/2017.
 */
public class MemberDecls {
    /**
     * @param name The name either of the Varialbe or of the Method
     * @param extendsClass of the type MJExtended which either passes that class extends another class or not
     * @param list the list which contains Methods and Variables
     * @return MJClassDecl Object
     */
    public static MJClassDecl addToTheList(String name, MJExtended extendsClass, ArrayList list) {
        MJVarDeclList vars = VarDeclList();
        MJMethodDeclList methods = MethodDeclList();
        for (int k = 0; k < list.size(); k++) {
            if (list.get(k) instanceof MJVarDecl) {
                vars.addFront((MJVarDecl) list.get(k));
            } else if (list.get(k) instanceof MJMethodDecl) {
                methods.addFront((MJMethodDecl) list.get(k));
            }
        }

        return MJ.ClassDecl(name, extendsClass, vars, methods);
    }
}