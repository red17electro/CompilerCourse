package analysis;

import java.util.List;

public interface MethodInfo {

    /** returns the name of the method */
    String getName();

    /** returns the list of parameters */
    List<ParamInfo> getParameters();

    /** returns the return type of the method */
    Type getReturnType();




}
