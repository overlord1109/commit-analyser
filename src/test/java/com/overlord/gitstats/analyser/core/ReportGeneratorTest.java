package com.overlord.gitstats.analyser.core;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.overlord.gitstats.analyser.parser.JavaSourceParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class ReportGeneratorTest {

    @Test
    public void shouldFilterOutDeletedAndAddedMethods() throws FileNotFoundException {
        List<MethodDeclaration> oldDecl = new JavaSourceParser().extractDeclarations(new FileInputStream(new File("src/test/resources/javasrc/Before.java")));
        List<MethodDeclaration> newDecl = new JavaSourceParser().extractDeclarations(new FileInputStream(new File("src/test/resources/javasrc/After.java")));
        new ReportGenerator().filterDeletedAndAddedMethods(oldDecl, newDecl);
        Assert.assertEquals(4, oldDecl.size());
        Assert.assertEquals(3, newDecl.size());
        Assert.assertTrue(oldDecl.stream().noneMatch(decl -> decl.getNameAsString().equals("methodD")));
        Assert.assertTrue(newDecl.stream().noneMatch(decl -> decl.getNameAsString().equals("methodE")));
    }

    @Test
    public void shouldFilterOutUnchangedMethods() throws FileNotFoundException {
        List<MethodDeclaration> oldDecl = new JavaSourceParser().extractDeclarations(new FileInputStream(new File("src/test/resources/javasrc/Before.java")));
        List<MethodDeclaration> newDecl = new JavaSourceParser().extractDeclarations(new FileInputStream(new File("src/test/resources/javasrc/After.java")));
        new ReportGenerator().filterOutUnchangedMethods(oldDecl, newDecl);

        Assert.assertEquals(4, oldDecl.size());
        Assert.assertEquals(3, newDecl.size());
    }

    @Test
    public void testSimpleRemoval() {
        MethodDeclaration oldDecl = StaticJavaParser.parseMethodDeclaration("public int sum(int a, int b) { return a + b;}");
        MethodDeclaration newDecl = StaticJavaParser.parseMethodDeclaration("public int sum(int a) { return a;}");
        Assert.assertTrue(new ReportGenerator().wasParameterRemoved(oldDecl, newDecl));
    }

    @Test
    public void testSimpleParamRename() {
        MethodDeclaration oldDecl = StaticJavaParser.parseMethodDeclaration("public int sum(int a, int b) { return a + b;}");
        MethodDeclaration newDecl = StaticJavaParser.parseMethodDeclaration("public int sum(int a, int c) { return a;}");
        Assert.assertFalse(new ReportGenerator().wasParameterRemoved(oldDecl, newDecl));
    }

    @Test
    public void testSimpleParamAdditionAtEnd() {
        MethodDeclaration oldDecl = StaticJavaParser.parseMethodDeclaration("public int sum(int a, int b) { return a + b;}");
        MethodDeclaration newDecl = StaticJavaParser.parseMethodDeclaration("public int sum(int a, int b, int d) { return a + b + d;}");
        Assert.assertFalse(new ReportGenerator().wasParameterRemoved(oldDecl, newDecl));
    }

    @Test
    public void testSimpleParamAdditionInMiddle() {
        MethodDeclaration oldDecl = StaticJavaParser.parseMethodDeclaration("public int sum(int a, int b) { return a + b;}");
        MethodDeclaration newDecl = StaticJavaParser.parseMethodDeclaration("public int sum(int a, long x, int b) { return a + x + d;}");
        Assert.assertFalse(new ReportGenerator().wasParameterRemoved(oldDecl, newDecl));
    }

    @Test
    public void testShuffledAndAddedParams() {
        MethodDeclaration oldDecl = StaticJavaParser.parseMethodDeclaration("public int method(int a, MyClass instance, long b) { return a + b;}");
        MethodDeclaration newDecl = StaticJavaParser.parseMethodDeclaration("public int method(MyClass instance, int a, long x, short b) { return a + x + d;}");
        Assert.assertFalse(new ReportGenerator().wasParameterRemoved(oldDecl, newDecl));
    }

    @Test
    public void testRefactoring() {
        MethodDeclaration oldDecl = StaticJavaParser.parseMethodDeclaration("public int method(List<Integer> first, List<Integer> second) { return a + b;}");
        MethodDeclaration newDecl = StaticJavaParser.parseMethodDeclaration("public int method(Collection<Integer> first, Collection<Integer> second) { return a + x + d;}");
        Assert.assertTrue(new ReportGenerator().wasParameterRemoved(oldDecl, newDecl));
    }
}
