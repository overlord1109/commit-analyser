package com.overlord.gitstats.analyser.regex;

import org.junit.Assert;
import org.junit.Test;

import static com.overlord.gitstats.analyser.regex.JavaMethodRegex.JAVA_METHOD_PATTERN;

public class RegexTest {

    @Test
    public void testMainMethod() {
        String methodDeclaration = "public static void main(String[] args) {";
        Assert.assertTrue(methodDeclaration.matches(JAVA_METHOD_PATTERN));
    }

    @Test
    public void testMethodWithGenerics() {
        String methodDeclaration = "private List<Integer> getEmployeeIds(List<Employee> employees) {";
        Assert.assertTrue(methodDeclaration.matches(JAVA_METHOD_PATTERN));
    }

    @Test
    public void testPackagePrivateMethod() {
        String methodDeclaration = "MyClass getMyClass() {";
        Assert.assertTrue(methodDeclaration.matches(JAVA_METHOD_PATTERN));
    }

    @Test
    public void testConstructor() {
        String methodDeclaration = "public MyClassConstructor() {";
        Assert.assertTrue(methodDeclaration.matches(JAVA_METHOD_PATTERN));
    }

    @Test
    public void testPackagePrivateConstructor() {
        String methodDeclarationWithoutWhitespace = "MyClassConstructor() {";
        String methodDeclarationWithWhitespace = "\tMyClassConstructor() {";
        Assert.assertFalse(methodDeclarationWithoutWhitespace.matches(JAVA_METHOD_PATTERN));
        Assert.assertTrue(methodDeclarationWithWhitespace.matches(JAVA_METHOD_PATTERN));
    }
}
