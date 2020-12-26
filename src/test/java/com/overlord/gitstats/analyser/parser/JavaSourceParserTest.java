package com.overlord.gitstats.analyser.parser;

import com.github.javaparser.ast.body.Parameter;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

public class JavaSourceParserTest {

    @Test
    public void shouldPrintDeclarations() throws FileNotFoundException {
        JavaSourceParser parser = new JavaSourceParser();
        Map<String, List<Parameter>> declarations = parser.extractDeclarations("src/test/resources/javasrc/GitClient.java");
        for(Map.Entry<String, List<Parameter>> entry : declarations.entrySet()) {
            System.out.println("Method name: " + entry.getKey() + ", Param list: " + entry.getValue());
        }
        Assert.assertFalse(declarations.isEmpty());
    }
}
