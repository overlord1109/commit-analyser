package com.overlord.gitstats.analyser.core;

import com.github.javaparser.ast.body.Parameter;

import java.util.List;

public class MethodDeclarationComparer {

    /**
     * This method iterates over old parameters while performing element-wise comparison with new parameters.
     * If any parameter from old parameters list is not present at the same position in the new parameter list,
     * it is considered to be removed. Essentially, I check whether old parameter list is a prefix of the new
     * parameter list.
     *
     * Note: Local parameter variable names are not considered, only their types.
     */
    public boolean wasParameterRemoved(List<Parameter> oldParams, List<Parameter> newParams) {
        if(oldParams.size() == 0 && newParams.size() == 0)
            return false;
        for(int i = 0; i < oldParams.size(); i++) {
            if(i >= newParams.size() || !oldParams.get(i).getType().equals(newParams.get(i).getType()))
                return true;
        }
        return false;
    }
}
