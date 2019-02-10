package analysis;

import minijava.ast.MJVarDecl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Server on 5/27/2017.
 */
class SymbolTable {

    private List<Map<String, MJVarDecl>> symbolTable;
    private int current_nl;

    /**
     * Initialize the variables
     */
    SymbolTable() {
        this.symbolTable = new ArrayList<>();
        current_nl = 0;
    }

    /**
     * add new payer to the symbol table
     */
    void push() {
        Map<String, MJVarDecl> layer = new HashMap<>();
        this.symbolTable.add(layer);
        this.current_nl++;
    }

    /**
     * remove new payer to the symbol table
     */
    void pop() {
        if (this.current_nl > 0) {
            this.symbolTable.remove(this.current_nl - 1);
            this.current_nl--;
        }
    }

    /**
     * @return the current scope details
     */
    Map<String, MJVarDecl> getCurrentScope() {
        return symbolTable.get(current_nl - 1);
    }

    /**
     * @param id key value for look up
     * @return return the scope details
     */
    Map<String, MJVarDecl> Lookup(String id) {
        Map<String, MJVarDecl> temp = new HashMap<>();

        Map<String, MJVarDecl> currentScope = getCurrentScope();
        if (currentScope.containsKey(id)) {
            temp.put(id, currentScope.get(id));
        }

        return temp;
    }
}
