package com.overlord.gitstats.analyser.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class UtilsTest {

    @Test
    public void shouldDeleteFilesRecursively() throws IOException {
        File test1 = new File("test-output" + File.separator + "test1");
        test1.mkdirs();
        File test2 = new File("test-output" + File.separator + "test2" + File.separator + "test21");
        test2.mkdirs();

        Assert.assertTrue(test2.exists() && test2.isDirectory());

        File f = new File("test-output");
        Utils.deleteRecursively(f);

        Assert.assertFalse(f.exists());
    }
}
