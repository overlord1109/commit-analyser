package com.overlord.gitstats.analyser.parser;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class JavaSourceParserTest {

    @Test
    public void shouldPrintDeclarations() throws FileNotFoundException {
        JavaSourceParser parser = new JavaSourceParser();
        InputStream in = new FileInputStream("src/test/resources/javasrc/GitClient.java");
        List<MethodDeclaration> declarations = parser.extractDeclarations(in);
        Assert.assertFalse(declarations.isEmpty());
        Assert.assertTrue(declarations.stream().anyMatch(decl -> decl.getNameAsString().equals("cloneRepository")));
        for(MethodDeclaration decl : declarations) {
            System.out.println("Method name: " + decl.getNameAsString() + ", Param list: " + decl.getParameters());
        }
    }
}
