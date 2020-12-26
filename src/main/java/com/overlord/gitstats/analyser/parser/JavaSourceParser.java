package com.overlord.gitstats.analyser.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaSourceParser {

    //Extract method declarations as a map of the method name to a list of parameters
    public Map<String, List<Parameter>> extractDeclarations(InputStream in) {
        CompilationUnit cu = StaticJavaParser.parse(in);
        MethodVisitor methodVisitor = new MethodVisitor(new HashMap<>());
        methodVisitor.visit(cu, null);
        return methodVisitor.getDeclarations();
    }

    private static class MethodVisitor extends VoidVisitorAdapter<Void> {

        private final Map<String, List<Parameter>> declarations;

        public MethodVisitor(Map<String, List<Parameter>> declarations) {
            this.declarations = declarations;
        }

        @Override
        public void visit(MethodDeclaration declaration, Void arg) {
            super.visit(declaration, arg);
            declarations.put(declaration.getName().asString(), declaration.getParameters());
        }

        public Map<String, List<Parameter>> getDeclarations() {
            return declarations;
        }
    }
}
