package com.overlord.gitstats.analyser.core;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.Parameter;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ReportGeneratorTest {

    @Test
    public void testSimpleRemoval() {
        List<Parameter> oldParams = StaticJavaParser.parseMethodDeclaration("public int sum(int a, int b) { return a + b;}").getParameters();
        List<Parameter> newParams = StaticJavaParser.parseMethodDeclaration("public int sum(int a) { return a;}").getParameters();
        Assert.assertTrue(new ReportGenerator().wasParameterRemoved(oldParams, newParams));
    }

    @Test
    public void testSimpleParamRename() {
        List<Parameter> oldParams = StaticJavaParser.parseMethodDeclaration("public int sum(int a, int b) { return a + b;}").getParameters();
        List<Parameter> newParams = StaticJavaParser.parseMethodDeclaration("public int sum(int a, int c) { return a;}").getParameters();
        Assert.assertFalse(new ReportGenerator().wasParameterRemoved(oldParams, newParams));
    }

    @Test
    public void testSimpleParamAdditionAtEnd() {
        List<Parameter> oldParams = StaticJavaParser.parseMethodDeclaration("public int sum(int a, int b) { return a + b;}").getParameters();
        List<Parameter> newParams = StaticJavaParser.parseMethodDeclaration("public int sum(int a, int b, int d) { return a + b + d;}").getParameters();
        Assert.assertFalse(new ReportGenerator().wasParameterRemoved(oldParams, newParams));
    }

    @Test
    public void testSimpleParamAdditionInMiddle() {
        List<Parameter> oldParams = StaticJavaParser.parseMethodDeclaration("public int sum(int a, int b) { return a + b;}").getParameters();
        List<Parameter> newParams = StaticJavaParser.parseMethodDeclaration("public int sum(int a, long x, int b) { return a + x + d;}").getParameters();
        Assert.assertEquals("long", newParams.get(1).getType().asString());
        Assert.assertTrue(new ReportGenerator().wasParameterRemoved(oldParams, newParams));
    }
}
