package com.overlord.gitstats.analyser.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JavaSourceParser {

    /**
     * Extract method declarations as a list of MethodDeclaration.
     * Relevant fields in MethodDecalaration instance are:
     *
     *  String name - this is the name of the method
     *  List<Parameter> parameterList - List of parameters (only Type is considered)
     */
    public List<MethodDeclaration> extractDeclarations(InputStream in) {
        CompilationUnit cu = StaticJavaParser.parse(in);
        MethodVisitor methodVisitor = new MethodVisitor(new ArrayList<>());
        methodVisitor.visit(cu, null);
        return methodVisitor.getDeclarations();
    }

    private static class MethodVisitor extends VoidVisitorAdapter<Void> {

        private final List<MethodDeclaration> declarations;

        public MethodVisitor(List<MethodDeclaration> declarations) {
            this.declarations = declarations;
        }

        @Override
        public void visit(MethodDeclaration declaration, Void arg) {
            super.visit(declaration, arg);
            declarations.add(declaration);
        }

        public List<MethodDeclaration> getDeclarations() {
            return declarations;
        }
    }
}
