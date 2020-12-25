package com.overlord.gitstats.analyser.regex;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RegexTest {

    @Test
    public void testMainMethod() {
        String methodDeclaration = "- public static void main(String[] args) {";
        Assert.assertTrue(JavaMethodPatternMatcher.instance().matches(methodDeclaration));
    }

    @Test
    public void testMethodWithGenerics() {
        String methodDeclaration = "+ private List<Integer> getEmployeeIds(List<Employee> employees) {";
        Assert.assertTrue(JavaMethodPatternMatcher.instance().matches(methodDeclaration));
    }

    @Test
    public void testPackagePrivateMethod() {
        String methodDeclaration = "- MyClass getMyClass() {";
        Assert.assertTrue(JavaMethodPatternMatcher.instance().matches(methodDeclaration));
    }

    @Test
    public void testConstructor() {
        String methodDeclaration = "+ public MyClassConstructor() {";
        Assert.assertTrue(JavaMethodPatternMatcher.instance().matches(methodDeclaration));
    }

    @Test
    public void testPackagePrivateConstructor() {
        String methodDeclarationWithoutWhitespace = "- MyClassConstructor() {";
        String methodDeclarationWithWhitespace = "+\tMyClassConstructor() {";
        Assert.assertTrue(JavaMethodPatternMatcher.instance().matches(methodDeclarationWithoutWhitespace));
        Assert.assertTrue(JavaMethodPatternMatcher.instance().matches(methodDeclarationWithWhitespace));
    }

    @Test
    public void testMethodCall() {
        String methodCall = "-\t\tint sum = add(1, 2);";
        Assert.assertFalse(JavaMethodPatternMatcher.instance().matches(methodCall));
    }

    @Test
    public void testNewCall() {
        String methodCall = "-\t\tFile myFile = new File(\"path to file\");";
        Assert.assertFalse(JavaMethodPatternMatcher.instance().matches(methodCall));
    }

    @Test
    public void testDiff() throws URISyntaxException, IOException {
        String diff = new String(Files.readAllBytes(
                Paths.get(this.getClass()
                        .getResource("/diff/diff-output-1.txt")
                        .toURI())));
        Assert.assertTrue(JavaMethodPatternMatcher.instance().matches(diff));
    }

    @Test
    public void testDiffWithoutMethodDeclarationChange() throws URISyntaxException, IOException {
        String diff = new String(Files.readAllBytes(
                Paths.get(this.getClass()
                        .getResource("/diff/diff-output-2.txt")
                        .toURI())));
        Assert.assertFalse(JavaMethodPatternMatcher.instance().matches(diff));
    }
}
