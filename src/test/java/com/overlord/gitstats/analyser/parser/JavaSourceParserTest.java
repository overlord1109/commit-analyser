package com.overlord.gitstats.analyser.parser;

import com.github.javaparser.ast.body.Parameter;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class JavaSourceParserTest {

    @Test
    public void shouldPrintDeclarations() throws FileNotFoundException {
        JavaSourceParser parser = new JavaSourceParser();
        InputStream in = new FileInputStream("src/test/resources/javasrc/GitClient.java");
        Map<String, List<Parameter>> declarations = parser.extractDeclarations(in);
        Assert.assertFalse(declarations.isEmpty());
        for(Map.Entry<String, List<Parameter>> entry : declarations.entrySet()) {
            System.out.println("Method name: " + entry.getKey() + ", Param list: " + entry.getValue());
        }
    }
}
