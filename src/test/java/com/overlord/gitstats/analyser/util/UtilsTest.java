package com.overlord.gitstats.analyser.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class UtilsTest {

    @Test
    public void shouldDeleteFilesRecursively() throws IOException {
        new File("test-output" + File.separator + "test1").mkdirs();
        new File("test-output" + File.separator + "test2" + File.separator + "test21").mkdirs();

        File f = new File("test-output");
        Utils.deleteRecursively(f);

        Assert.assertFalse(f.exists());
    }
}
