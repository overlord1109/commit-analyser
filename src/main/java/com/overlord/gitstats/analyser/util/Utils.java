package com.overlord.gitstats.analyser.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Utils {

    public static void deleteRecursively(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                deleteRecursively(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }
}
